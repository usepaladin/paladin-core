package paladin.core.controller.organisation

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/organisation")
class OrganisationController {

    @PreAuthorize("hasOrgRole(#organisationId, 'ADMIN')")
    @PutMapping("/")
}