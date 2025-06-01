package paladin.core.entities.organisation

import jakarta.persistence.*
import java.time.ZonedDateTime
import java.util.*

@Entity
@Table(
    name = "organisations",
    schema = "public"
)
data class OrganisationEntity(
    @Id
    @GeneratedValue
    @Column(name = "id", columnDefinition = "UUID DEFAULT uuid_generate_v4()", nullable = false)
    val id: UUID? = null,

    @Column(name = "name", nullable = false, unique = true)
    var name: String,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "updated_at", nullable = false, updatable = false)
    val updatedAt: ZonedDateTime = ZonedDateTime.now(),
)