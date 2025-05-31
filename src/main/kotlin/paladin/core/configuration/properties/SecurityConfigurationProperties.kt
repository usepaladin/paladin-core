package paladin.core.configuration.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("paladin.security")
data class SecurityConfigurationProperties(
    val jwtSecretKey: String,
    val jwtIssuer: String
)