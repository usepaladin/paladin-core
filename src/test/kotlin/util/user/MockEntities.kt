package util.user

import paladin.core.entities.user.UserEntity
import java.time.ZonedDateTime
import java.util.*

object MockUserEntityFactory {

    fun createUser(
        id: UUID = UUID.randomUUID(),
        name: String = "Test User",
        email: String = "email@email.com",
        phone: String = "1234567890",
    ): UserEntity {
        return UserEntity(
            id = id,
            displayName = name,
            email = email,
            phone = phone,
            avatarUrl = null,
            createdAt = ZonedDateTime.now()
        )
    }

}