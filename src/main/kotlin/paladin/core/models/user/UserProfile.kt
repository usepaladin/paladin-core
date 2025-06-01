package paladin.core.models.user

import paladin.core.entities.user.UserEntity
import java.time.ZonedDateTime
import java.util.*


open class UserProfile(
    open val id: UUID,
    open val name: String,
    open val email: String,
    open val avatarUrl: String? = null,
    open val createdAt: ZonedDateTime
) {
    companion object Factory {
        fun fromEntity(entity: UserEntity): UserProfile {
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