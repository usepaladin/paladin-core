package paladin.core.configuration.auth

import org.aopalliance.intercept.MethodInvocation
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations
import org.springframework.security.core.Authentication

class CustomMethodSecurityExpressionHandler : DefaultMethodSecurityExpressionHandler() {
    override fun createSecurityExpressionRoot(
        authentication: Authentication,
        invocation: MethodInvocation
    ): MethodSecurityExpressionOperations {
        return SecurityExpressionRootConfiguration(authentication).apply {
            setThis(invocation.`this`)
            setPermissionEvaluator(permissionEvaluator)
            setTrustResolver(trustResolver)
            setRoleHierarchy(roleHierarchy)
        }
    }
}