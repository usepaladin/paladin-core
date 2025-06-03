package paladin.core.repository.organisation

import org.springframework.data.jpa.repository.JpaRepository
import paladin.core.entities.organisation.OrganisationInviteEntity
import java.util.*


interface OrganisationInviteRepository : JpaRepository<OrganisationInviteEntity, UUID>