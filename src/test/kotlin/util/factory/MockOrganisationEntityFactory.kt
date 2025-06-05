package util.factory

import paladin.core.entities.organisation.OrganisationEntity
import paladin.core.entities.organisation.OrganisationInviteEntity
import paladin.core.entities.organisation.OrganisationMemberEntity
import paladin.core.enums.organisation.OrganisationPlan
import paladin.core.enums.organisation.OrganisationRoles
import java.util.*

object MockOrganisationEntityFactory {

    fun createOrganisation(
        id: UUID = UUID.randomUUID(),
        name: String = "Test Organisation",
        plan: OrganisationPlan = OrganisationPlan.PRO,
    ) = OrganisationEntity(
        id = id,
        name = name,
        plan = plan,
    )

    fun createOrganisationMember(
        userId: UUID,
        organisationId: UUID,
        role: OrganisationRoles = OrganisationRoles.DEVELOPER,
    ) = OrganisationMemberEntity(
        id = OrganisationMemberEntity.OrganisationMemberKey(
            organisationId = organisationId,
            userId = userId
        ),
        role = role
    )

    fun createOrganisationInvite(
        email: String,
        organisationId: UUID,
        role: OrganisationRoles = OrganisationRoles.DEVELOPER,
        token: String = UUID.randomUUID().toString().substring(0, 12),
    ) = OrganisationInviteEntity(
        id = UUID.randomUUID(),
        email = email,
        organisationId = organisationId,
        role = role,
        token = token,
        invitedBy = UUID.randomUUID()
    )


}