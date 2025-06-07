package paladin.core.entities.organisation

import jakarta.persistence.*
import paladin.core.enums.organisation.OrganisationPlan
import java.time.ZonedDateTime
import java.util.*

@Entity
@Table(
    name = "organisations",
    schema = "public"
)
data class OrganisationEntity(
    @Id
    @GeneratedValue
    @Column(name = "id", columnDefinition = "UUID DEFAULT uuid_generate_v4()", nullable = false)
    val id: UUID? = null,

    @Column(name = "name", nullable = false, updatable = true)
    var name: String,

    @Column(name = "plan", nullable = false, updatable = true)
    @Enumerated(EnumType.STRING)
    var plan: OrganisationPlan,

    @Column(name = "member_count", nullable = false, updatable = false)
    val memberCount: Int = 0,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "updated_at", nullable = false, updatable = true)
    val updatedAt: ZonedDateTime = ZonedDateTime.now(),
) {
    @OneToMany(mappedBy = "organisation", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
    var members: MutableSet<OrganisationMemberEntity> = mutableSetOf()
}