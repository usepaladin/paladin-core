package paladin.core.models.organisation.request

import paladin.core.enums.organisation.OrganisationPlan

data class OrganisationCreationRequest(
    val name: String,
    val avatarUrl: String? = null,
    val plan: OrganisationPlan,
    val default: Boolean = false
)