package paladin.core.service.auth

import io.github.oshai.kotlinlogging.KLogger
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service
import paladin.core.configuration.auth.OrganisationRole
import java.util.*

@Service
class AuthTokenService(private val logger: KLogger) {

    /**
     * Retrieves the JWT from the security context.
     */
    private fun getJwt(): Jwt {
        val authentication = SecurityContextHolder.getContext().authentication
        authentication.let {
            if (it == null || it.principal !is Jwt) {
                logger.warn { "No JWT found in the security context" }
                throw AccessDeniedException("No JWT found in the security context")
            }

            return it.principal as Jwt
        }
    }

    /**
     * Retrieves the user ID from the JWT claims.
     */
    @Throws(AccessDeniedException::class, IllegalArgumentException::class)
    fun getUserId(): UUID {
        return getJwt().claims["user_id"].let {
            if (it == null) {
                logger.warn { "User ID not found in JWT claims" }
                throw AccessDeniedException("User ID not found in JWT claims")
            } else {
                UUID.fromString(it.toString())
            }
        }

    }

    /**
     * Retrieves all associated user metadata from the JWT Claim
     */
    fun getAllClaims(): Map<String, Any> {
        return getJwt().claims
            .also { logger.info { "Retrieved claims: $it" } }
    }

    fun getCurrentUserAuthorities(): Collection<String> {
        return SecurityContextHolder.getContext().authentication?.authorities
            ?.map { it.authority } ?: emptyList()
    }

    fun getUserOrganisationRoles(): List<OrganisationRole> {
        val authentication = SecurityContextHolder.getContext().authentication
        return if (authentication is JwtAuthenticationToken) {
            extractOrganisationRoles(authentication.token)
        } else emptyList()
    }

    private fun extractOrganisationRoles(jwt: Jwt): List<OrganisationRole> {
        return try {
            val rolesRaw = jwt.getClaim<List<Map<String, Any>>>("roles")
            rolesRaw?.mapNotNull { role ->
                val orgIdStr = role["organisation_id"]?.toString()
                val roleStr = role["role"]?.toString()
                if (orgIdStr != null && roleStr != null) {
                    try {
                        OrganisationRole(UUID.fromString(orgIdStr), roleStr)
                    } catch (e: Exception) {
                        logger.warn { "Failed to parse organisation role: orgId=$orgIdStr, role=$roleStr, error=${e.message}" }
                        null
                    }
                } else null
            } ?: emptyList()
        } catch (e: Exception) {
            logger.warn { "Failed to extract organisation roles from JWT: ${e.message}" }
            emptyList()
        }
    }
}