package paladin.core.controller.organisation

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import paladin.core.models.organisation.Organisation
import paladin.core.service.organisation.OrganisationService
import java.util.*

@RestController
@RequestMapping("/api/v1/organisation")
class OrganisationController(
    private val organisationService: OrganisationService
) {


    @GetMapping("/{organisationId}")
    fun getOrganisation(
        @PathVariable organisationId: UUID,
        @RequestParam includeMembers: Boolean = false
    ): ResponseEntity<Organisation> {
        TODO()
    }

    @PostMapping("/")
    fun createOrganisation(@RequestBody organisation: Organisation): ResponseEntity<Organisation> {
        TODO()
    }


    @PutMapping("/")
    fun updateOrganisation(
        @RequestBody organisation: Organisation
    ): ResponseEntity<Organisation> {
        TODO()
    }


    @DeleteMapping("/{organisationId}")
    fun deleteOrganisation(
        @PathVariable organisationId: UUID
    ): ResponseEntity<Void> {
        TODO()
    }
}