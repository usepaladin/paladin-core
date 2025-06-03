package paladin.core.service.organisation

import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import paladin.core.entities.organisation.OrganisationEntity
import paladin.core.entities.organisation.OrganisationMemberEntity
import paladin.core.enums.organisation.OrganisationPlan
import paladin.core.enums.organisation.OrganisationRoles
import paladin.core.exceptions.NotFoundException
import paladin.core.models.organisation.Organisation
import paladin.core.models.organisation.OrganisationMember
import paladin.core.repository.organisation.OrganisationMemberRepository
import paladin.core.repository.organisation.OrganisationRepository
import paladin.core.service.auth.AuthTokenService
import paladin.core.util.ServiceUtil.findOrThrow
import java.util.*

@Service
class OrganisationService(
    private val organisationRepository: OrganisationRepository,
    private val organisationMemberRepository: OrganisationMemberRepository,
    private val authTokenService: AuthTokenService
) {

    @Throws(NotFoundException::class)
    fun getOrganisation(organisationId: UUID, includeMembers: Boolean = false): Organisation {
        return findOrThrow(organisationId, organisationRepository::findById).let {
            Organisation.fromEntity(it, includeMembers)
        }
    }

    @Throws(AccessDeniedException::class, IllegalArgumentException::class)
    fun createOrganisation(name: String, plan: OrganisationPlan): Organisation {
        // Gets the user ID from the auth token to act as the Organisation creator
        val userId: UUID = authTokenService.getUserId()
        if (plan == OrganisationPlan.ENTERPRISE) {
            throw IllegalArgumentException("Enterprise plan is not supported for automated creation. Done via admin panel.")
        }

        // Create and save the organisation entity
        val organisation: Organisation = OrganisationEntity(
            name = name,
            plan = plan,
        ).run {
            organisationRepository.save(this).let { entity ->
                Organisation.fromEntity(entity)
            }
        }

        // Add the creator as the first member/owner of the organisation
        val key = OrganisationMemberEntity.OrganisationMemberKey(
            organisationId = organisation.id,
            userId = userId
        )

        val member: OrganisationMember = OrganisationMemberEntity(key, OrganisationRoles.OWNER).run {
            organisationMemberRepository.save(this).let { entity ->
                OrganisationMember.fromEntity(entity)
            }
        }

        // Update the organisation with the first member
        return organisation.copy(
            members = listOf(member),
            memberCount = 1
        )
    }
}