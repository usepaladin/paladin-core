package paladin.core.models.user

import paladin.core.entities.user.UserProfileEntity
import java.time.ZonedDateTime
import java.util.*


data class UserProfile(
    val id: UUID,
    val name: String,
    val email: String,
    val avatarUrl: String? = null,
    val createdAt: ZonedDateTime
) {
    companion object Factory {
        fun fromEntity(entity: UserProfileEntity): UserProfile {
            entity.id.let {
                if (it == null) {
                    throw IllegalArgumentException("UserProfileEntity must have a non-null id")
                }

                return UserProfile(
                    id = it,
                    name = entity.displayName,
                    email = entity.email,
                    avatarUrl = entity.avatarUrl,
                    createdAt = entity.createdAt
                )
            }

        }
    }
}