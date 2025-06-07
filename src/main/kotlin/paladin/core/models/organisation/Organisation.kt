package paladin.core.models.organisation

import paladin.core.entities.organisation.OrganisationEntity
import paladin.core.enums.organisation.OrganisationPlan
import java.time.ZonedDateTime
import java.util.*

data class Organisation(
    val id: UUID,
    val name: String,
    val plan: OrganisationPlan,
    val memberCount: Int,
    val createdAt: ZonedDateTime,
    val members: List<OrganisationMember> = listOf(),
) {
    companion object Factory {
        fun fromEntity(entity: OrganisationEntity, includeMembers: Boolean = false): Organisation {
            return Organisation(
                id = entity.id ?: throw IllegalArgumentException("OrganisationEntity must have a non-null id"),
                name = entity.name,
                plan = entity.plan,
                members = if (includeMembers) {
                    entity.members.map { OrganisationMember.fromEntity(it) }
                } else {
                    listOf()
                },
                memberCount = entity.memberCount,
                createdAt = entity.createdAt,
            )
        }

    }
}