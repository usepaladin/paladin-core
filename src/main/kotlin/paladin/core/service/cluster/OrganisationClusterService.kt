package paladin.core.service.cluster

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import paladin.core.models.cluster.OrganisationCluster
import paladin.core.models.cluster.request.ClusterCreationRequest
import paladin.core.service.encryption.EncryptionService
import paladin.core.service.encryption.OrganisationKeyService
import paladin.core.service.organisation.OrganisationService
import java.util.*

@Service
class OrganisationClusterService(
    private val organisationService: OrganisationService,
    private val keyService: OrganisationKeyService,
    private val encryptionService: EncryptionService
) {

    @PreAuthorize("@organisationSecurity.hasOrgRoleOrHigher(#request.organisationId, 'ADMIN')")
    fun createCluster(request: ClusterCreationRequest) {
    }

    @PreAuthorize("@organisationSecurity.hasOrg(#organisationId)")
    fun getCluster(organisationId: UUID, id: UUID): OrganisationCluster {
        TODO()
    }

    @PreAuthorize("@organisationSecurity.hasOrg(#organisationId)")
    fun getOrganisationClusters(organisationId: UUID): List<OrganisationCluster> {
        TODO()
    }

    @PreAuthorize("@organisationSecurity.hasOrgRoleOrHigher(#request.organisationId, 'ADMIN')")
    fun updateCluster(): OrganisationCluster {
        TODO()
    }

    @PreAuthorize("@organisationSecurity.hasOrgRoleOrHigher(#request.organisationId, 'ADMIN')")
    fun deleteCluster() {
        TODO()
    }

}