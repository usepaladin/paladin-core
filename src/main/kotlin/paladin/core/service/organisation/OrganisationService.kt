package paladin.core.service.organisation

import io.github.oshai.kotlinlogging.KLogger
import jakarta.transaction.Transactional
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.access.prepost.PreAuthorize
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
    private val logger: KLogger,
    private val authTokenService: AuthTokenService
) {


    @Throws(NotFoundException::class)
    @PreAuthorize("@organisationSecurity.hasOrg(#organisationId)")
    fun getOrganisation(organisationId: UUID, includeMembers: Boolean = false): Organisation {
        return findOrThrow(organisationId, organisationRepository::findById).let {
            Organisation.fromEntity(it, includeMembers)
        }
    }

    /**
     * Transactional given our createOrganisation method creates both an Organisation and its first member.
     */
    @Throws(AccessDeniedException::class, IllegalArgumentException::class)
    @Transactional
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

    @PreAuthorize("@organisationSecurity.hasOrgRoleOrHigher(#organisation.id, 'ADMIN')")
    fun updateOrganisation(organisation: Organisation): Organisation {
        findOrThrow(organisation.id, organisationRepository::findById).run {
            val entity = this.apply {
                name = organisation.name
                plan = organisation.plan
            }

            // Save the updated organisation entity
            return Organisation.fromEntity(organisationRepository.save(entity))
        }
    }

    /**
     * Transactional given the need to delete all members associated with the organisation before deleting the organisation itself.
     */
    @PreAuthorize("@organisationSecurity.hasOrgRoleOrHigher(#organisationId, 'OWNER')")
    @Transactional
    fun deleteOrganisation(organisationId: UUID) {
        // Check if the organisation exists
        val organisation: OrganisationEntity = findOrThrow(organisationId, organisationRepository::findById)

        // Delete all members associated with the organisation
        organisationMemberRepository.deleteByIdOrganisationId(organisationId)

        // Delete the organisation itself
        organisationRepository.delete(organisation)

        logger.info { "Organisation with ID $organisationId deleted successfully." }
    }

    /**
     * Invoked from Invitation accept action. Users cannot directly add others to an organisation.
     */
    fun addMemberToOrganisation() {

    }

    /**
     * Allow permission to remove member from organisation under the following conditions:
     *  - The user is the owner of the organisation
     *  - The user is an admin and has a role higher than the member's role (ie. ADMIN can remove DEVELOPER/READONLY, but not OWNER or ADMIN)
     *  - The user is trying to remove themselves from the organisation
     */
    @PreAuthorize(
        """
           @organisationSecurity.isUpdatingOrganisationMember(#organisationId, #member) or @organisationSecurity.isUpdatingSelf(#member)
        """
    )
    fun removeMemberFromOrganisation(organisationId: UUID, member: OrganisationMember) {
        // Assert that the removed member is not currently the owner of the organisation
        if (member.role == OrganisationRoles.OWNER) {
            throw IllegalArgumentException("Cannot remove the owner of the organisation. Please transfer ownership first.")
        }

        OrganisationMemberEntity.OrganisationMemberKey(
            organisationId = organisationId,
            userId = member.user.id
        ).run {
            findOrThrow(this, organisationMemberRepository::findById)
            organisationMemberRepository.deleteById(this)
            logger.info { "Member with ID ${member.user.id} removed from organisation $organisationId successfully." }
        }
    }

    /**
     * Allow permission to update a member's role in the organisation under the following conditions:
     *  - The user is the owner of the organisation
     *  - The user is an admin and has a role higher than the member's role (ie. ADMIN can alter roles of DEVELOPER/READONLY users, but not OWNER or ADMIN)
     */
    @PreAuthorize(
        """
        @organisationSecurity.isUpdatingOrganisationMember(#organisationId, #member)
        """
    )
    fun updateMemberRole(
        organisationId: UUID,
        member: OrganisationMember,
        role: OrganisationRoles
    ): OrganisationMember {

        // Ensure that if the new role is that of OWNER, that only the current owner can assign it
        if (role == OrganisationRoles.OWNER || member.role == OrganisationRoles.OWNER) {
            throw IllegalArgumentException("Transfer of ownership must be done through a dedicated transfer ownership method.")
        }

        OrganisationMemberEntity.OrganisationMemberKey(
            organisationId = organisationId,
            userId = member.user.id
        ).run {
            findOrThrow(this, organisationMemberRepository::findById).run {
                this.apply {
                    this.role = role
                }

                organisationMemberRepository.save(this)
                logger.info { "Member with ID ${member.user.id} role updated to $role in organisation $organisationId successfully." }
                return OrganisationMember.fromEntity(this)
            }
        }
    }
}