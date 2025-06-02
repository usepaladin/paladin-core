package paladin.core.entities.user

import jakarta.persistence.*
import java.util.*

@Entity
@Table(
    name = "user_default_organisation",
    schema = "public"
)
data class UserDefaultOrganisationEntity(
    @EmbeddedId
    val id: DefaultOrganisationEntityKey
) {
    @Embeddable
    data class DefaultOrganisationEntityKey(
        @Column(name = "user_id", nullable = false)
        val userId: UUID,

        @Column(name = "organisation_id", nullable = false)
        val organisationId: UUID
    )
}