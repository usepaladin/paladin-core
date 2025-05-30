package paladin.core.entities.user

import jakarta.persistence.*
import java.time.ZonedDateTime
import java.util.*

@Entity
@Table(
    name = "user_profiles",
    schema = "public",
    indexes = [
        Index(name = "idx_profiles_email", columnList = "email"),
        Index(name = "idx_profiles_name", columnList = "display_name")
    ]
)
data class UserProfileEntity(
    @Id
    @GeneratedValue
    @Column(name = "id", columnDefinition = "UUID DEFAULT uuid_generate_v4()", nullable = false)
    val id: UUID? = null,
    
    @Column(name = "email", nullable = false)
    var email: String,

    @Column(name = "display_name", nullable = false)
    var displayName: String,

    @Column(name = "avatar_url")
    var avatarUrl: String? = null,

    @Column(
        name = "created_at",
        nullable = false,
        updatable = false
    ) var createdAt: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "updated_at", nullable = false) var updatedAt: ZonedDateTime = ZonedDateTime.now()

) {
    @PrePersist
    fun onPrePersist() {
        createdAt = ZonedDateTime.now()
        updatedAt = ZonedDateTime.now()
    }

    @PreUpdate
    fun onPreUpdate() {
        updatedAt = ZonedDateTime.now()
    }
}