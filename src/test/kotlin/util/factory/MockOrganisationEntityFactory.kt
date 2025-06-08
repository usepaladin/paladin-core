package util.factory

import paladin.core.entities.organisation.OrganisationEntity
import paladin.core.entities.organisation.OrganisationInviteEntity
import paladin.core.entities.organisation.OrganisationMemberEntity
import paladin.core.entities.user.UserEntity
import paladin.core.enums.organisation.OrganisationInviteStatus
import paladin.core.enums.organisation.OrganisationPlan
import paladin.core.enums.organisation.OrganisationRoles
import java.util.*

object MockOrganisationEntityFactory {

    fun createOrganisation(
        id: UUID = UUID.randomUUID(),
        name: String = "Test Organisation",
        plan: OrganisationPlan = OrganisationPlan.PRO,
        members: MutableSet<OrganisationMemberEntity> = mutableSetOf()
    ) = OrganisationEntity(
        id = id,
        name = name,
        plan = plan,
    ).apply {
        this.members = members
    }

    fun createOrganisationMember(
        user: UserEntity,
        organisationId: UUID,
        role: OrganisationRoles = OrganisationRoles.DEVELOPER,
    ): OrganisationMemberEntity {
        user.id.let {
            if (it == null) {
                throw IllegalArgumentException("User ID must not be null")
            }

            return OrganisationMemberEntity(
                id = OrganisationMemberEntity.OrganisationMemberKey(
                    organisationId = organisationId,
                    userId = it
                ),
                role = role,
            ).apply {
                this.user = user
            }
        }
    }

    fun createOrganisationInvite(
        email: String,
        organisationId: UUID,
        role: OrganisationRoles = OrganisationRoles.DEVELOPER,
        token: String = OrganisationInviteEntity.generateSecureToken(),
        invitedBy: UUID = UUID.randomUUID(),
        status: OrganisationInviteStatus = OrganisationInviteStatus.PENDING
    ) = OrganisationInviteEntity(
        id = UUID.randomUUID(),
        email = email,
        organisationId = organisationId,
        role = role,
        token = token,
        inviteStatus = status,
        invitedBy = invitedBy
    )


}