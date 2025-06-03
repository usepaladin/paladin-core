package paladin.core.models.organisation

import paladin.core.entities.organisation.OrganisationEntity
import paladin.core.entities.organisation.OrganisationMemberEntity
import paladin.core.enums.organisation.OrganisationRoles
import paladin.core.models.user.UserProfile
import java.time.ZonedDateTime
import java.util.*

data class OrganisationMember(
    val user: UserProfile,
    val organisationId: UUID,
    val role: OrganisationRoles,
    val memberSince: ZonedDateTime,
    val organisation: Organisation? = null
) {
    companion object Factory {
        fun fromEntity(entity: OrganisationMemberEntity, organisation: OrganisationEntity? = null): OrganisationMember {
            entity.id.let {
                return OrganisationMember(
                    user = UserProfile.fromEntity(entity.user),
                    organisationId = it.organisationId,
                    role = entity.role,
                    memberSince = entity.memberSince,
                    organisation = organisation?.let { entity -> Organisation.fromEntity(entity) }
                )
            }
        }
    }
}