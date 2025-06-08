package paladin.core.enums.organisation

enum class OrganisationRoles(val authority: Int) {
    OWNER(4),
    ADMIN(3),
    DEVELOPER(2),
    READONLY(1);

    companion object {
        fun fromString(role: String): OrganisationRoles {
            return entries.find { it.name.equals(role, ignoreCase = true) }
                ?: throw IllegalArgumentException("Invalid role: $role")
        }

        fun fromAuthority(authority: Int): OrganisationRoles? {
            return entries.find { it.authority == authority }
        }
    }
}

fun OrganisationRoles.hasHigherAuthorityThan(other: OrganisationRoles, inclusive: Boolean = false): Boolean {
    inclusive.let {
        if (it) {
            return this.authority >= other.authority
        }

        return this.authority > other.authority
    }
}