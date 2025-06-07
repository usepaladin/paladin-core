package paladin.core.controller.organisation

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import paladin.core.enums.organisation.OrganisationRoles
import paladin.core.models.organisation.Organisation
import paladin.core.models.organisation.OrganisationMember
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
        val organisation: Organisation = this.organisationService.getOrganisation(
            organisationId = organisationId,
            includeMembers = includeMembers
        )

        return ResponseEntity.ok(organisation)
    }

    @PostMapping("/")
    fun createOrganisation(@RequestBody organisation: Organisation): ResponseEntity<Organisation> {
        val createdOrganisation: Organisation = this.organisationService.createOrganisation(
            name = organisation.name,
            plan = organisation.plan
        )

        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrganisation)
    }


    @PutMapping("/")
    fun updateOrganisation(
        @RequestBody organisation: Organisation
    ): ResponseEntity<Organisation> {
        val updatedOrganisation: Organisation = this.organisationService.updateOrganisation(
            organisation = organisation
        )

        return ResponseEntity.ok(updatedOrganisation)
    }

    @DeleteMapping("/{organisationId}")
    fun deleteOrganisation(
        @PathVariable organisationId: UUID
    ): ResponseEntity<Void> {
        this.organisationService.deleteOrganisation(organisationId)
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/{organisationId}/member")
    fun removeMemberFromOrganisation(
        @PathVariable organisationId: UUID,
        @RequestBody member: OrganisationMember
    ): ResponseEntity<Void> {
        this.organisationService.removeMemberFromOrganisation(organisationId, member)
        return ResponseEntity.ok().build()
    }

    @PutMapping("/{organisationId}/member/role/{role}")
    fun updateMemberRole(
        @PathVariable organisationId: UUID,
        @PathVariable role: OrganisationRoles,
        @RequestBody member: OrganisationMember
    ): ResponseEntity<OrganisationMember> {
        val updatedMember: OrganisationMember = this.organisationService.updateMemberRole(
            organisationId = organisationId,
            member = member,
            role = role
        )
        return ResponseEntity.ok(updatedMember)
    }
}