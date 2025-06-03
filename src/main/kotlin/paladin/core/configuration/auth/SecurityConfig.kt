package paladin.core.configuration.auth


import com.nimbusds.jose.jwk.source.ImmutableSecret
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.web.SecurityFilterChain
import paladin.core.configuration.properties.SecurityConfigurationProperties
import javax.crypto.spec.SecretKeySpec

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig(
    private val securityConfig: SecurityConfigurationProperties
) {

    private val secretKey = SecretKeySpec(securityConfig.jwtSecretKey.toByteArray(Charsets.UTF_8), "HmacSHA256")

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() } // Disable CSRF for stateless APIs
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) } // Stateless session
            .authorizeHttpRequests { auth ->
                auth
                    .anyRequest().authenticated() // Require authentication for all other endpoints
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { } // Enable JWT validation
            }
        return http.build()
    }

    @Bean
    fun jwtDecoder(): JwtDecoder {
        // Use HS256 with the SUPABASE_JWT_SECRET
        val secret = ImmutableSecret<Nothing>(secretKey)
        return NimbusJwtDecoder.withSecretKey(secret.secretKey)
            .macAlgorithm(MacAlgorithm.HS256)
            .build()
    }

    @Bean
    fun methodSecurityExpressionHandler(): MethodSecurityExpressionHandler {
        return CustomMethodSecurityExpressionHandler()
    }
}