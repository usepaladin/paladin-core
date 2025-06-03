package paladin.core.service.user

import io.github.oshai.kotlinlogging.KLogger
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import paladin.core.exceptions.NotFoundException
import paladin.core.models.user.User
import paladin.core.models.user.UserProfile
import paladin.core.repository.user.UserProfileRepository
import paladin.core.service.auth.AuthTokenService
import paladin.core.util.ServiceUtil.findOrThrow
import java.util.*

@Service
class UserProfileService(
    private val repository: UserProfileRepository,
    private val authTokenService: AuthTokenService,
    private val logger: KLogger
) {

    @Throws(NotFoundException::class)
    fun getUserProfileByEmail(email: String): UserProfile {
        return findOrThrow(email, repository::findByEmail).let {
            UserProfile.fromEntity(it)
        }
    }

    @Throws(NotFoundException::class, IllegalArgumentException::class)
    fun getUserFromSession(): User {
        return authTokenService.getUserId().let {
            findOrThrow(it, repository::findById).let { entity ->
                User.fromEntity(entity)
            }
        }
    }

    @Throws(NotFoundException::class)
    fun getUserProfileById(id: UUID): UserProfile {
        return findOrThrow(id, repository::findById).let {
            UserProfile.fromEntity(it)
        }
    }

    @Throws(NotFoundException::class, IllegalArgumentException::class)
    fun updateUserDetails(user: User): User {
        // Validate Session id matches target user
        authTokenService.getUserId().run {
            if (this != user.id) {
                throw AccessDeniedException("Session user ID does not match provided user ID")
            }
        }

        findOrThrow(user.id, repository::findById).apply {
            displayName = user.name
            email = user.email
            avatarUrl = user.avatarUrl
            phone = user.phone
        }.run {
            repository.save(this)
            logger.info { "Updated user profile with ID: ${this.id}" }
            return User.fromEntity(this)
        }
    }

    fun getBatchUserProfilesByIds(userIds: List<UUID>): List<UserProfile> {
        return repository.findAllById(userIds).map { UserProfile.fromEntity(it) }
    }

    @Throws(NotFoundException::class)
    fun deleteUserProfile(userId: UUID) {
        findOrThrow(userId, repository::findById) // Ensure the user exists before deletion
        repository.deleteById(userId)
        logger.info { "Deleted user profile with ID: $userId" }
    }
}