package paladin.core.configuration.auth

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import paladin.core.enums.organisation.OrganisationRoles
import java.util.*

@Component
class OrganisationSecurity {
    fun hasOrgRole(organisationId: UUID, role: OrganisationRoles): Boolean {
        val authority: String = "ROLE_${organisationId}_$role"

        SecurityContextHolder.getContext().authentication.let {
            if (it == null || !it.isAuthenticated) {
                return false
            }
            if (it.authorities.isEmpty()) {
                return false
            }

            return it.authorities.any { claim -> claim.authority == authority }
        }


    }

    fun hasOrg(organisationId: UUID): Boolean {
        SecurityContextHolder.getContext().authentication.let {
            if (it == null || !it.isAuthenticated) {
                return false
            }
            if (it.authorities.isEmpty()) {
                return false
            }

            return it.authorities.any { claim -> claim.authority.startsWith("ROLE_$organisationId") }
        }
    }

    fun hasOrgRoleOrHigher(
        organisationId: UUID,
        targetRole: OrganisationRoles
    ): Boolean {
        val claim: String = SecurityContextHolder.getContext().authentication.let {
            if (it == null || !it.isAuthenticated) {
                return false
            }
            if (it.authorities.isEmpty()) {
                return false
            }

            it.authorities.firstOrNull { claim -> claim.authority.startsWith("ROLE_$organisationId") } ?: return false
        }.toString()

        return OrganisationRoles.fromString(claim.removePrefix("ROLE_${organisationId}_")).let { role ->
            role != null && role.authority >= targetRole.authority
        }
    }

    fun hasHigherOrgRole(
        organisationId: UUID,
        targetRole: OrganisationRoles
    ): Boolean {
        val claim: String = SecurityContextHolder.getContext().authentication.let {
            if (it == null || !it.isAuthenticated) {
                return false
            }
            if (it.authorities.isEmpty()) {
                return false
            }

            it.authorities.firstOrNull { claim -> claim.authority.startsWith("ROLE_$organisationId") } ?: return false
        }.toString()

        return OrganisationRoles.fromString(claim.removePrefix("ROLE_${organisationId}_")).let { role ->
            role != null && role.authority > targetRole.authority
        }
    }
}