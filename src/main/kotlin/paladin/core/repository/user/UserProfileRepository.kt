package paladin.core.repository.user

import org.springframework.data.jpa.repository.JpaRepository
import paladin.core.entities.user.UserEntity
import java.util.*

interface UserProfileRepository : JpaRepository<UserEntity, UUID> {
    fun findByEmail(email: String): Optional<UserEntity>
    fun findByDisplayName(name: String): Optional<UserEntity>
}