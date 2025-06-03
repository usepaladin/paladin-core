package paladin.core.repository.organisation

import org.springframework.data.jpa.repository.JpaRepository
import paladin.core.entities.organisation.OrganisationEntity
import java.util.*

interface OrganisationRepository : JpaRepository<OrganisationEntity, UUID> {
}