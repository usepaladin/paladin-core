package paladin.core.models.cluster.request

import paladin.core.enums.cluster.SecurityProtocol
import java.util.*

data class ClusterCreationRequest(
    val organisationId: UUID,
    val name: String,
    val bootstrapServers: String,
    val securityProtocol: SecurityProtocol,
    val username: String? = null,
    val password: String? = null
)