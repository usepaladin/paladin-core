package paladin.core.repository.organisation

import org.springframework.data.jpa.repository.JpaRepository
import paladin.core.entities.organisation.OrganisationMemberEntity
import java.util.*


interface OrganisationMemberRepository : JpaRepository<OrganisationMemberEntity, UUID>