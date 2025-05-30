package paladin.core.repository.user

import org.springframework.data.jpa.repository.JpaRepository
import paladin.core.entities.user.UserProfileEntity
import java.util.*

interface UserProfileRepository : JpaRepository<UserProfileEntity, UUID> {
    fun findByEmail(email: String): Optional<UserProfileEntity>
    fun findByDisplayName(name: String): Optional<UserProfileEntity>
}