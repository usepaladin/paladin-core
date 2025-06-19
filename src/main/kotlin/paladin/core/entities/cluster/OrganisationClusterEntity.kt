package paladin.core.entities.cluster

import jakarta.persistence.*
import paladin.core.enums.cluster.SecurityProtocol
import java.time.ZonedDateTime
import java.util.*


@Entity
@Table(
    name = "organisation_cluster",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_organisation_cluster", columnNames = ["organisation_id", "name"])
    ],
    indexes = [
        Index(name = "idx_organisation_cluster_org_id", columnList = "organisation_id"),
    ]
)
data class OrganisationClusterEntity(
    @Id
    @GeneratedValue
    @Column(name = "id", columnDefinition = "UUID DEFAULT uuid_generate_v4()", nullable = false)
    val id: UUID? = null,

    @Column(name = "organisation_id", columnDefinition = "UUID", nullable = false)
    val organisationId: UUID,

    @Column(name = "name", nullable = false, length = 255)
    var name: String,

    @Column(name = "bootstrap_servers", nullable = false, length = 1024)
    var bootstrapServers: String,

    @Column(name = "security_protocol", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    var securityProtocol: SecurityProtocol = SecurityProtocol.PLAINTEXT,

    // Encrypted
    // Username/API Key
    @Column(name = "username", length = 255)
    var username: String? = null,

    // Encrypted
    // Password/API Secret
    @Column(name = "password", length = 255)
    var password: String? = null,

    @Column(name = "created_at", updatable = false)
    var createdAt: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: ZonedDateTime = ZonedDateTime.now()
) {
    @PrePersist
    fun onPrePersist() {
        createdAt = ZonedDateTime.now()
        updatedAt = ZonedDateTime.now()
    }

    @PreUpdate
    fun onPreUpdate() {
        updatedAt = ZonedDateTime.now()
    }
}