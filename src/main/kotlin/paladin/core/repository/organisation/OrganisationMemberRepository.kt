package paladin.core.repository.organisation

import org.springframework.data.jpa.repository.JpaRepository
import paladin.core.entities.organisation.OrganisationMemberEntity
import java.util.*


interface OrganisationMemberRepository : JpaRepository<OrganisationMemberEntity, UUID> {
    fun deleteByIdOrganisationId(organisationId: UUID)
    fun findByIdUserId(userId: UUID): List<OrganisationMemberEntity>
    fun findByIdOrganisationId(organisationId: UUID): List<OrganisationMemberEntity>
}