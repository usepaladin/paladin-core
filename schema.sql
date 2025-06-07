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


CREATE TYPE ORGANISATION_ROLE AS ENUM ('owner', 'admin', 'developer', 'readonly');

CREATE TABLE IF NOT EXISTS ORGANISATION_MEMBERS
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

CREATE TYPE ORGANISATION_INVITE_STATUS AS ENUM ('pending', 'accepted', 'declined', 'expired');

CREATE TABLE IF NOT EXISTS ORGANISATION_INVITES
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

alter table organisation_invites
    add constraint uq_invite_code unique (invite_code);

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
CREATE FUNCTION public.custom_access_token_hook()
    RETURNS jsonb
    LANGUAGE plpgsql
    SECURITY definer
    SET search_path = public
AS
$$
DECLARE
    _roles jsonb;
BEGIN
    SELECT coalesce(
                   jsonb_agg(jsonb_build_object('organisation_id', organisation_id, 'role', role)),
                   '[]'::jsonb)
    INTO _roles
    FROM organisation_members
    WHERE user_id = (auth.uid());
    RETURN jsonb_build_object('roles', _roles);
END;
$$;


GRANT ALL ON FUNCTION public.custom_access_token_hook TO supabase_auth_admin;

