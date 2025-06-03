package paladin.core.enums.organisation

enum class OrganisationRoles(val authority: Int) {
    OWNER(4),
    ADMIN(3),
    DEVELOPER(2),
    READONLY(1);

    companion object {
        fun fromString(role: String): OrganisationRoles? {
            return entries.find { it.name.equals(role, ignoreCase = true) }
        }

        fun fromAuthority(authority: Int): OrganisationRoles? {
            return entries.find { it.authority == authority }
        }
    }
}