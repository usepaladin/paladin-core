package paladin.core.entities.organisation

import jakarta.persistence.*
import paladin.core.entities.cluster.OrganisationClusterEntity
import paladin.core.enums.organisation.OrganisationPlan
import paladin.core.models.organisation.Organisation
import java.time.ZonedDateTime
import java.util.*

@Entity
@Table(
    name = "organisations",
    uniqueConstraints = [
        UniqueConstraint(name = "organisation_name_unique", columnNames = ["name"])
    ]
)
data class OrganisationEntity(
    @Id
    @GeneratedValue
    @Column(name = "id", columnDefinition = "UUID DEFAULT uuid_generate_v4()", nullable = false)
    val id: UUID? = null,

    @Column(name = "name", nullable = false, updatable = true)
    var name: String,

    @Column(name = "avatarUrl", nullable = true, updatable = true)
    var avatarUrl: String? = null,

    @Column(name = "plan", nullable = false, updatable = true)
    @Enumerated(EnumType.STRING)
    var plan: OrganisationPlan,

    @Column(name = "member_count", nullable = false, updatable = false)
    val memberCount: Int = 0,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "updated_at", nullable = false, updatable = true)
    var updatedAt: ZonedDateTime = ZonedDateTime.now(),
) {
    @OneToMany(mappedBy = "organisation", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
    var members: MutableSet<OrganisationMemberEntity> = mutableSetOf()

    @OneToMany(mappedBy = "organisation", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
    var invites: MutableSet<OrganisationInviteEntity> = mutableSetOf()

    @OneToMany(mappedBy = "organisation", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
    var clusters: MutableSet<OrganisationClusterEntity> = mutableSetOf()

    @PrePersist
    fun onPrePersist() {
        createdAt = ZonedDateTime.now()
        updatedAt = ZonedDateTime.now()
    }

    @PreUpdate
    fun onPreUpdate() {
        updatedAt = ZonedDateTime.now()
    }

    companion object Factory {
        fun fromRepresentation(organisation: Organisation): OrganisationEntity {
            return OrganisationEntity(
                id = organisation.id,
                name = organisation.name,
                avatarUrl = organisation.avatarUrl,
                plan = organisation.plan,
                createdAt = organisation.createdAt,
            )
        }
    }

}