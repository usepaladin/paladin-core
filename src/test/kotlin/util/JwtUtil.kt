package util

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSSigner
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import java.util.*

object JwtTestUtil {
    fun createTestJwt(
        secret: String,
        id: String,
        email: String,
        customClaims: Map<String, Any> = emptyMap(),
        expirationSeconds: Long = 3600
    ): String {
        val signer: JWSSigner = MACSigner(secret.toByteArray())

        val now = Date()
        val exp = Date(now.time + expirationSeconds * 1000)

        val claimsBuilder = JWTClaimsSet.Builder()
            .subject(id)
            .issuer("test-issuer")
            .issueTime(now)
            .expirationTime(exp)
            .claim("email", email)

        customClaims.forEach { (k, v) -> claimsBuilder.claim(k, v) }

        val claims = claimsBuilder.build()

        val signedJWT = SignedJWT(
            JWSHeader(JWSAlgorithm.HS256),
            claims
        )

        signedJWT.sign(signer)
        return signedJWT.serialize()
    }
}

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@ExtendWith(WithMockJwtExtension::class)
annotation class WithMockJwt(
    val userId: String,
    val email: String,
    val secret: String = "default-test-secret", // default for convenience
    val expirationSeconds: Long = 3600
)

class WithMockJwtExtension : BeforeEachCallback, AfterEachCallback {

    override fun beforeEach(context: ExtensionContext) {
        val annotation = findEffectiveAnnotation(context)

        if (annotation != null) {
            val jwt = JwtTestUtil.createTestJwt(
                secret = annotation.secret,
                id = annotation.userId,
                email = annotation.email,
                expirationSeconds = annotation.expirationSeconds
            )

            val auth = UsernamePasswordAuthenticationToken(
                annotation.userId,
                jwt,
                listOf(SimpleGrantedAuthority("ROLE_USER")) // Can be made dynamic later
            )

            SecurityContextHolder.getContext().authentication = auth
        }
    }

    override fun afterEach(context: ExtensionContext?) {
        SecurityContextHolder.clearContext()
    }

    private fun findEffectiveAnnotation(context: ExtensionContext): WithMockJwt? {
        val methodAnnotation = context.testMethod
            .flatMap { Optional.ofNullable(it.getAnnotation(WithMockJwt::class.java)) }

        if (methodAnnotation.isPresent) {
            return methodAnnotation.get()
        }

        val classAnnotation = context.testClass
            .flatMap { Optional.ofNullable(it.getAnnotation(WithMockJwt::class.java)) }

        return classAnnotation.orElse(null)
    }
}