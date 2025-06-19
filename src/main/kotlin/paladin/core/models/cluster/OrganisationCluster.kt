package paladin.core.models.cluster

import paladin.core.enums.cluster.SecurityProtocol
import java.util.*

data class OrganisationCluster(
    val id: UUID,
    val organisationId: UUID,
    val name: String,
    val bootstrapServers: String,
    val securityProtocol: SecurityProtocol,
    val username: String? = null,
    val password: String? = null,
    val createdAt: String,
    val updatedAt: String
)