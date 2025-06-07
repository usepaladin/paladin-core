package paladin.core.models.user

import paladin.core.entities.user.UserEntity
import paladin.core.models.organisation.Organisation
import paladin.core.models.organisation.OrganisationMember
import java.time.ZonedDateTime
import java.util.*

data class User(
    override val id: UUID,
    override val name: String,
    override val email: String,
    val phone: String?,
    val memberships: List<OrganisationMember> = listOf(),
    val defaultOrganisation: Organisation? = null,
    override val avatarUrl: String? = null,
    override val createdAt: ZonedDateTime
) : UserProfile(id, name, email, avatarUrl, createdAt) {
    companion object Factory {

        fun fromEntity(entity: UserEntity): User {
            entity.id.let {
                if (it == null) {
                    throw IllegalArgumentException("UserEntity ID cannot be null")
                }
                return User(
                    id = it,
                    name = entity.displayName,
                    email = entity.email,
                    phone = entity.phone,
                    avatarUrl = entity.avatarUrl,
                    createdAt = entity.createdAt,
                    memberships = entity.organisations.map { entity ->
                        OrganisationMember.fromEntity(entity, entity.organisation)
                    },
                    defaultOrganisation = entity.defaultOrganisation?.let { entity ->
                        Organisation.fromEntity(entity)
                    }
                )
            }
        }
    }
}