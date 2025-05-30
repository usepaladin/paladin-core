package paladin.core.service.user

import io.github.oshai.kotlinlogging.KLogger
import org.springframework.stereotype.Service
import paladin.core.entities.user.UserProfileEntity
import paladin.core.models.user.UserProfile
import paladin.core.repository.user.UserProfileRepository
import java.util.*

@Service
class UserProfileService(
    private val repository: UserProfileRepository,
    private val logger: KLogger
) {

    /**
     * Finds a user profile by the given query and returns it as its mapped DTO.
     */
    @Throws(IllegalArgumentException::class)
    private fun <T> findOrThrow(data: T, query: (T) -> Optional<UserProfileEntity>): UserProfileEntity {
        return query.invoke(data)
            .orElseThrow { IllegalArgumentException("User not found for query: $data") }
    }

    @Throws(IllegalArgumentException::class)
    fun getUserProfileByEmail(email: String): UserProfile {
        return findOrThrow(email, repository::findByEmail).let {
            UserProfile.fromEntity(it)
        }
    }

    @Throws(IllegalArgumentException::class)
    fun getUserProfileById(id: UUID): UserProfile {
        return findOrThrow(id, repository::findById).let {
            UserProfile.fromEntity(it)
        }
    }

    @Throws(IllegalArgumentException::class)
    fun getUserProfileByDisplayName(name: String): UserProfile {
        return findOrThrow(name, repository::findByDisplayName)
            .let {
                UserProfile.fromEntity(it)
            }
    }

    fun updateUserProfile(userProfile: UserProfile): UserProfile {
        findOrThrow(userProfile.id, repository::findById).apply {
            displayName = userProfile.name
            email = userProfile.email
            avatarUrl = userProfile.avatarUrl
        }.run {
            repository.save(this)
            logger.info { "Updated user profile with ID: ${this.id}" }
            return UserProfile.fromEntity(this)
        }
    }

    fun deleteUserProfile(userId: UUID) {
        if (!repository.existsById(userId)) {
            throw IllegalArgumentException("User not found for ID: $userId")
        }
        repository.deleteById(userId)
        logger.info { "Deleted user profile with ID: $userId" }
    }
}