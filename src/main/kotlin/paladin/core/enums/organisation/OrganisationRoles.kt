package paladin.core.enums.organisation

enum class OrganisationRoles {
    OWNER,
    ADMIN,
    DEVELOPER,
    READONLY;

    companion object {
        fun fromString(role: String): OrganisationRoles? {
            return entries.find { it.name.equals(role, ignoreCase = true) }
        }
    }
}