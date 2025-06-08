package paladin.core.configuration.properties

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("paladin.security")
data class SecurityConfigurationProperties(
    @field:NotBlank(message = "JWT secret key must not be blank")
    @field:Size(min = 32, message = "JWT secret key must be at least 32 characters long")
    val jwtSecretKey: String,

    @field:NotBlank(message = "JWT issuer must not be blank")
    val jwtIssuer: String,
    val allowedOrigins: List<String> = listOf("*"),
)