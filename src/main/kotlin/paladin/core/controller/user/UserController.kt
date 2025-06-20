package paladin.core.controller.user

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import paladin.core.models.user.User
import paladin.core.models.user.UserProfile
import paladin.core.service.user.UserProfileService
import java.util.*

@RestController
@RequestMapping("/api/v1/user")
@Tag(name = "User Management", description = "Endpoints for managing user profiles and details")
class UserController(
    private val profileService: UserProfileService
) {

    @PutMapping("/")
    fun updateUserProfile(@RequestBody user: User): ResponseEntity<User> {
        val updatedUserProfile = profileService.updateUserDetails(user)
        return ResponseEntity.ok(updatedUserProfile)
    }

    @GetMapping("/")
    fun getCurrentUser(): ResponseEntity<User> {
        val user: User = profileService.getUserFromSession()
        return ResponseEntity.ok(user)
    }

    @GetMapping("/id/{userId}")
    fun getUserProfileById(@PathVariable userId: UUID): ResponseEntity<UserProfile> {
        val userProfile = profileService.getUserProfileById(userId)
        return ResponseEntity.ok(userProfile)
    }

    /**
     * Fetches many publicly available User Profiles by their associated IDs
     */
    @GetMapping("/ids")
    fun getBatchUserDisplayByIds(@RequestParam userIds: List<UUID>): ResponseEntity<List<UserProfile>> {
        val userProfiles = profileService.getBatchUserProfilesByIds(userIds)
        return ResponseEntity.ok(userProfiles)
    }

    @DeleteMapping("/id/{userId}")
    fun deleteUserProfileById(@PathVariable userId: UUID): ResponseEntity<Void> {
        profileService.deleteUserProfile(userId)
        return ResponseEntity.noContent().build()
    }
}