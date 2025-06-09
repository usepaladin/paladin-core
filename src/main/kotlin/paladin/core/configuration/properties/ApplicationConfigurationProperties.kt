package paladin.core.configuration.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "paladin")
data class ApplicationConfigurationProperties(
    val includeStackTrace: Boolean = true,
)
