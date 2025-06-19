package paladin.core.configuration.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "paladin")
data class ApplicationConfigurationProperties(
    val includeStackTrace: Boolean = true,
    val standalone: Boolean = false,
    val requireDataEncryption: Boolean = true,
    val encryptionKey: String? = null,
)
