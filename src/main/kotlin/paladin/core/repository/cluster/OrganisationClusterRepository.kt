package paladin.core.repository.cluster

import org.springframework.data.jpa.repository.JpaRepository
import paladin.core.entities.cluster.OrganisationClusterEntity
import java.util.*


interface OrganisationClusterRepository : JpaRepository<OrganisationClusterEntity, UUID>