package paladin.core.service.auth

import io.github.oshai.kotlinlogging.KLogger
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
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
}