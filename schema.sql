BEGIN;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
DROP TABLE IF EXISTS "user_profiles";
DROP TABLE IF EXISTS "organisation_invites";
DROP TABLE IF EXISTS "organisation_members";
DROP TABLE IF EXISTS public.user_profiles;
DROP TYPE IF EXISTS ORGANISATION_INVITE_STATUS;
DROP TYPE IF EXISTS ORGANISATION_ROLE;
DROP TABLE IF EXISTS organisations;
DROP TABLE IF EXISTS user_profiles;

COMMIT;

CREATE TABLE IF NOT EXISTS "organisations"
(
    "id"           UUID PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
    "name"         VARCHAR(100)     NOT NULL UNIQUE,
    "avatar_url"   TEXT,
    "member_count" INTEGER          NOT NULL DEFAULT 0,
    "created_at"   TIMESTAMP WITH TIME ZONE  DEFAULT CURRENT_TIMESTAMP,
    "updated_at"   TIMESTAMP WITH TIME ZONE  DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS "user_profiles"
(
    "id"                      UUID PRIMARY KEY NOT NULL REFERENCES auth.users (id) ON DELETE CASCADE,
    "email"                   VARCHAR(100)     NOT NULL UNIQUE,
    "phone"                   VARCHAR(15) UNIQUE,
    "display_name"            VARCHAR(50)      NOT NULL,
    "avatar_url"              TEXT,
    "default_organisation_id" UUID             references public.organisations (id) ON DELETE SET NULL,
    "created_at"              TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    "updated_at"              TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

alter table public.user_profiles
    enable row level security;

create index if not exists idx_user_profiles_email
    on public.user_profiles (email);

create or replace function public.handle_new_user()
    returns trigger
    language plpgsql
    security definer set search_path = ''
as
$$
begin
    insert into public.user_profiles (id, email, display_name, avatar_url)
    values (new.id,
            new.email,
            coalesce(new.raw_user_meta_data ->> 'name', ''),
            coalesce(new.raw_user_meta_data ->> 'avatar_url', null));
    return new;
end;
$$;

-- trigger the function every time a user is created
create or replace trigger on_auth_user_created
    after insert
    on auth.users
    for each row
execute procedure public.handle_new_user();

create or replace function public.handle_phone_confirmation()
    returns trigger
    language plpgsql
    security definer set search_path = ''
as
$$
begin
    if new.phone is not null then
        update public.user_profiles
        set phone = new.phone
        where id = new.id;
    end if;
    return new;
end;
$$;


CREATE TYPE ORGANISATION_ROLE AS ENUM ('OWNER', 'ADMIN', 'DEVELOPER', 'READONLY');

CREATE TABLE IF NOT EXISTS "organisation_members"
(
    "id"              UUID PRIMARY KEY  NOT NULL DEFAULT uuid_generate_v4(),
    "organisation_id" UUID              NOT NULL REFERENCES organisations (id) ON DELETE CASCADE,
    "user_id"         UUID              NOT NULL REFERENCES public.user_profiles (id) ON DELETE CASCADE,
    "role"            ORGANISATION_ROLE NOT NULL DEFAULT 'developer',
    "member_since"    TIMESTAMP WITH TIME ZONE   DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE public.organisation_members
    ADD CONSTRAINT ux_organisation_user UNIQUE (organisation_id, user_id);

CREATE INDEX idx_organisation_members_user_id
    ON public.organisation_members (user_id);


CREATE TYPE ORGANISATION_INVITE_STATUS AS ENUM ('PENDING', 'ACCEPTED', 'DECLINED', 'EXPIRED');

CREATE TABLE IF NOT EXISTS "organisation_invites"
(
    "id"              UUID PRIMARY KEY  NOT NULL DEFAULT uuid_generate_v4(),
    "organisation_id" UUID              NOT NULL REFERENCES organisations (id) ON DELETE CASCADE,
    "email"           VARCHAR(100)      NOT NULL,
    "invite_code"     VARCHAR(12)       NOT NULL CHECK (LENGTH(invite_code) = 12),
    "role"            ORGANISATION_ROLE NOT NULL DEFAULT 'developer',
    "invited_by"      UUID              NOT NULL REFERENCES public.user_profiles (id) ON DELETE CASCADE,
    "created_at"      TIMESTAMP WITH TIME ZONE   DEFAULT CURRENT_TIMESTAMP,
    "expires_at"      TIMESTAMP WITH TIME ZONE   DEFAULT CURRENT_TIMESTAMP + INTERVAL '1 days'
);

CREATE INDEX idx_invite_organisation_id ON public.organisation_invites (organisation_id);
CREATE INDEX idx_invite_email ON public.organisation_invites (email);
CREATE INDEX idx_invite_token ON public.organisation_invites (invite_code);

alter table organisation_invites
    add constraint uq_invite_code unique (invite_code);

CREATE TABLE IF NOT EXISTS "organisation_cluster"
(
    id                UUID PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
    organisation_id   UUID             NOT NULL REFERENCES organisations (id) ON DELETE CASCADE,
    name              VARCHAR(255)     NOT NULL,
    bootstrap_servers TEXT,         -- Comma-separated bootstrap URLs
    auth_mechanism    VARCHAR(50),  -- e.g., SASL/PLAIN, SASL/OAUTHBEARER
    username          VARCHAR(255), -- SASL username or API key
    password          TEXT,         -- Encrypted password or API secret
    created_at        TIMESTAMP WITH TIME ZONE  DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP WITH TIME ZONE  DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE organisation_cluster
    ADD CONSTRAINT uq_organisation_cluster UNIQUE (organisation_id, name);

CREATE INDEX idx_organisation_cluster_org_id ON public.organisation_cluster (organisation_id);

alter table organisation_cluster
    enable row level security;

-- Ensure only organisation members can view their clusters
CREATE POLICY "Users can view their own organisation clusters" on organisation_cluster
    FOR SELECT
    TO authenticated
    USING (
    organisation_id IN (SELECT organisation_id
                        FROM organisation_members
                        WHERE user_id = auth.uid())
    );

-- Ensure only organisation members who are [OWNER, ADMIN, or DEVELOPER] can insert, update, or delete clusters
CREATE POLICY "Users can manage their own organisation clusters" on organisation_cluster
    FOR ALL
    TO authenticated
    USING (
    organisation_id IN (SELECT organisation_id
                        FROM organisation_members
                        WHERE user_id = auth.uid()
                          AND role IN ('OWNER', 'ADMIN', 'DEVELOPER'))
    );


-- Function to update member count
CREATE OR REPLACE FUNCTION public.update_org_member_count()
    RETURNS TRIGGER AS
$$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        -- Increment member count on INSERT
        UPDATE public.organisations
        SET member_count = member_count + 1,
            updated_at   = now()
        WHERE id = NEW.organisation_id;
    ELSIF (TG_OP = 'DELETE') THEN
        -- Decrement member count on DELETE
        UPDATE public.organisations
        SET member_count = member_count - 1,
            updated_at   = now()
        WHERE id = OLD.organisation_id;
    END IF;

    RETURN NULL; -- Triggers on INSERT/DELETE do not modify the rows
END;
$$ LANGUAGE plpgsql;

-- Trigger for INSERT and DELETE on org_member
CREATE or replace TRIGGER trg_update_org_member_count
    AFTER INSERT OR DELETE
    ON public.organisation_members
    FOR EACH ROW
EXECUTE FUNCTION public.update_org_member_count();

ALTER TABLE ORGANISATIONS
    ENABLE ROW LEVEL SECURITY;

/* Add restrictions to ensure that only organisation members can view their organisation*/
CREATE POLICY "Users can view their own organisations" on organisations
    FOR SELECT
    TO authenticated
    USING (
    id IN (SELECT organisation_id
           FROM organisation_members
           WHERE user_id = auth.uid())
    );

/* Add Organisation Roles to Supabase JWT */
CREATE or replace FUNCTION public.custom_access_token_hook(event jsonb)
    RETURNS jsonb
    LANGUAGE plpgsql
    stable
AS
$$
DECLARE
    claims jsonb;
    _roles jsonb;
BEGIN
    SELECT coalesce(
                   jsonb_agg(jsonb_build_object('organisation_id', organisation_id, 'role', role)),
                   '[]'::jsonb)
    INTO _roles
    FROM public.organisation_members
    WHERE user_id = (event ->> 'user_id')::uuid;
    claims := event -> 'claims';
    claims := jsonb_set(claims, '{roles}', _roles, true);
    event := jsonb_set(event, '{claims}', claims, true);
    RETURN event;
END;
$$;

grant usage on schema public to supabase_auth_admin;

grant execute
    on function public.custom_access_token_hook
    to supabase_auth_admin;
revoke execute
    on function public.custom_access_token_hook
    from authenticated, anon, public;

grant all on table public.organisations to supabase_auth_admin;
grant all on table public.user_profiles to supabase_auth_admin;
grant all on table public.organisation_members to supabase_auth_admin;

create policy "Allow auth admin to read organisation member roles" ON public.organisation_members
    as permissive for select
    to supabase_auth_admin
    using (true);
