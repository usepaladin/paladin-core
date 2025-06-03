package paladin.core.entities.organisation

import jakarta.persistence.*
import paladin.core.entities.user.UserEntity
import paladin.core.enums.organisation.OrganisationRoles
import java.time.ZonedDateTime
import java.util.*

@Entity
@Table(
    name = "organisation_members",
)
data class OrganisationMemberEntity(
    // User ID + Organisation ID as composite key
    @EmbeddedId
    val id: OrganisationMemberKey,

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false, updatable = true)
    val role: OrganisationRoles,

    @Column(name = "member_since", nullable = false, updatable = false)
    val memberSince: ZonedDateTime = ZonedDateTime.now(),
) {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
    val user: UserEntity? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id", referencedColumnName = "id", insertable = false, updatable = false)
    var organisation: OrganisationEntity? = null

    @Embeddable
    data class OrganisationMemberKey(
        @Column(name = "organisation_id", nullable = false)
        val organisationId: UUID,

        @Column(name = "user_id", nullable = false)
        val userId: UUID
    )
}

