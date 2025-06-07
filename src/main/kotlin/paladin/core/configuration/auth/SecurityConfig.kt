package paladin.core.configuration.auth

import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.web.SecurityFilterChain
import paladin.core.configuration.properties.SecurityConfigurationProperties
import javax.crypto.spec.SecretKeySpec

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig(
    private val securityConfig: SecurityConfigurationProperties,
    private val jwtConverter: CustomJwtAuthenticationConverter
) {

    private val secretKey = SecretKeySpec(securityConfig.jwtSecretKey.toByteArray(Charsets.UTF_8), "HmacSHA256")

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() } // Disable CSRF for stateless APIs
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) } // Stateless session
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/public/**").permitAll() // Allow public endpoints
                    .anyRequest().authenticated() // Require authentication for all other endpoints
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(jwtConverter)
                }
            }
            .exceptionHandling { exceptions ->
                exceptions
                    .authenticationEntryPoint { _, response, authException ->
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.message)
                    }.accessDeniedHandler { _, response, accessDeniedException ->
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, accessDeniedException.message)
                    }

            }

        return http.build()
    }

    @Bean
    fun jwtDecoder(): JwtDecoder {
        return NimbusJwtDecoder.withSecretKey(secretKey).build()
    }

}