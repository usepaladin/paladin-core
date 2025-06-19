package paladin.core.controller.cluster

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/cluster")
@Tag(name = "Cluster Management", description = "Endpoints for managing organisation message clusters")
class ClusterController