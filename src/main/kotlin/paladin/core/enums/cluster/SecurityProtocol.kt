package paladin.core.enums.cluster

enum class SecurityProtocol {
    PLAINTEXT,
    SSL,
    SASL_PLAINTEXT,
    SASL_SSL,
    SASL_OAUTH;

    companion object {
        fun fromString(value: String): SecurityProtocol? {
            return entries.find { it.name.equals(value, ignoreCase = true) }
        }
    }
}
