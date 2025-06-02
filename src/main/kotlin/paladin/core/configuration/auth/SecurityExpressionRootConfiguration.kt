package paladin.core.configuration.auth

import org.springframework.security.access.AccessDeniedException
import org.springframework.security.access.expression.SecurityExpressionRoot
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations
import org.springframework.security.core.Authentication
import paladin.core.enums.organisation.OrganisationRoles
import java.util.*

data class SecurityExpressionRootConfiguration(
    val authentication: Authentication
) : SecurityExpressionRoot(authentication), MethodSecurityExpressionOperations {

    private var filterObject: Any? = null
    private var returnObject: Any? = null
    private var target: Any? = null

    fun hasOrgRole(organisationId: UUID, role: OrganisationRoles) {
        val authority: String = "ROLE_${organisationId}_$role"
        if (!authentication.authorities.any { it.authority == authority }) {
            throw AccessDeniedException("User does not have the required role: $role for organisation: $organisationId")
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