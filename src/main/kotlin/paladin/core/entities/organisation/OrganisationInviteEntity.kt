package paladin.core.entities.organisation

import jakarta.persistence.*
import paladin.core.enums.organisation.OrganisationInviteStatus
import java.time.ZonedDateTime
import java.util.*

@Entity
@Table(
    name = "organisation_invites",
    schema = "public",
    uniqueConstraints = [
        UniqueConstraint(name = "uc_organisation_invites_email", columnNames = ["organisation_id, email"])
    ],
    indexes = [
        Index(name = "idx_organisation_invites_email", columnList = "email")
    ]
)
data class OrganisationInviteEntity(

    @Id
    @GeneratedValue
    @Column(columnDefinition = "UUID DEFAULT uuid_generate_v4()", updatable = false, nullable = false)
    val id: UUID? = null,

    @Column(name = "email")
    val email: String,

    @Column(name = "invite_code", length = 12, nullable = false)
    val token: String,

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    var inviteStatus: OrganisationInviteStatus = OrganisationInviteStatus.PENDING,

    @Column(name = "expires_at")
    val expiresAt: ZonedDateTime = ZonedDateTime.now().plusDays(1),
    
    @Column(name = "created_at", updatable = false)
    val createdAt: ZonedDateTime = ZonedDateTime.now()
)