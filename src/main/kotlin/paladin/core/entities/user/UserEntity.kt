package paladin.core.entities.user

import jakarta.persistence.*
import paladin.core.entities.organisation.OrganisationEntity
import paladin.core.entities.organisation.OrganisationMemberEntity
import java.time.ZonedDateTime
import java.util.*

@Entity
@Table(
    name = "user_profiles",
    schema = "public",
    uniqueConstraints = [
        UniqueConstraint(name = "uc_profiles_email", columnNames = ["email"]),
        UniqueConstraint(name = "uc_profiles_phone", columnNames = ["phone"])
    ],
    indexes = [
        Index(name = "idx_profiles_email", columnList = "email")
    ]
)
data class UserEntity(
    @Id
    @GeneratedValue
    @Column(name = "id")
    val id: UUID? = null,

    @Column(name = "email", nullable = false)
    var email: String,

    @Column(name = "phone")
    var phone: String? = null,

    @Column(name = "display_name", nullable = false)
    var displayName: String,

    @Column(name = "avatar_url")
    var avatarUrl: String? = null,

    @Column(name = "default_organisation_id", nullable = true, columnDefinition = "UUID")
    var defaultOrganisationId: UUID? = null,

    @Column(
        name = "created_at",
        nullable = false,
        updatable = false
    ) var createdAt: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "updated_at", nullable = false) var updatedAt: ZonedDateTime = ZonedDateTime.now()
) {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_organisation_id", referencedColumnName = "id", insertable = false, updatable = true)
    var defaultOrganisation: OrganisationEntity? = null

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "organisation_members",
        schema = "public",
        joinColumns = [JoinColumn(name = "user_id", referencedColumnName = "id")],
    )
    var organisations: MutableSet<OrganisationMemberEntity> = mutableSetOf()

    @PrePersist
    fun onPrePersist() {
        createdAt = ZonedDateTime.now()
        updatedAt = ZonedDateTime.now()
    }

    @PreUpdate
    fun onPreUpdate() {
        updatedAt = ZonedDateTime.now()
    }
}