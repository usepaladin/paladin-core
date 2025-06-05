package paladin.core.configuration.auth

import org.springframework.security.access.expression.SecurityExpressionRoot
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import paladin.core.enums.organisation.OrganisationRoles
import java.util.*

class SecurityExpressionRootConfiguration(
    authentication: Authentication
) : SecurityExpressionRoot(authentication), MethodSecurityExpressionOperations {

    private var filterObject: Any? = null
    private var returnObject: Any? = null
    private var target: Any? = null


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

    override fun setFilterObject(filterObject: Any?) {
        this.filterObject = filterObject
    }

    override fun getFilterObject(): Any? = filterObject

    override fun setReturnObject(returnObject: Any?) {
        this.returnObject = returnObject
    }

    override fun getReturnObject(): Any? = returnObject

    override fun getThis(): Any? = target

    fun setThis(target: Any?) {
        this.target = target
    }
}