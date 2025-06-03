package paladin.core.entities.organisation

import jakarta.persistence.*
import paladin.core.entities.user.UserEntity
import paladin.core.enums.organisation.OrganisationInviteStatus
import paladin.core.enums.organisation.OrganisationRoles
import java.time.ZonedDateTime
import java.util.*

@Entity
@Table(
    name = "organisation_invites",
    schema = "public",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uc_organisation_invites_email",
            columnNames = ["organisation_id", "email", "invite_code"]
        )
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

    @Id
    @GeneratedValue
    @Column(name = "organisation_id", columnDefinition = "UUID", nullable = false)
    val organisationId: UUID,

    @Column(name = "email")
    val email: String,

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    val role: OrganisationRoles = OrganisationRoles.DEVELOPER,

    @Column(name = "invite_code", length = 12, nullable = false)
    val token: String,

    @Column(name = "invited_by", nullable = false, columnDefinition = "UUID")
    val invitedBy: UUID,

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    var inviteStatus: OrganisationInviteStatus = OrganisationInviteStatus.PENDING,

    @Column(name = "expires_at")
    val expiresAt: ZonedDateTime = ZonedDateTime.now().plusDays(1),

    @Column(name = "created_at", updatable = false)
    val createdAt: ZonedDateTime = ZonedDateTime.now()
) {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id", referencedColumnName = "id", insertable = false, updatable = false)
    var organisation: OrganisationEntity? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by", referencedColumnName = "id", insertable = false, updatable = false)
    var invitedByUser: UserEntity? = null
}