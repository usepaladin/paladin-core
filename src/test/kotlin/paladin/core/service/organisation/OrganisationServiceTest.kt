package paladin.core.service.organisation

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.access.AccessDeniedException
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import paladin.core.entities.organisation.OrganisationEntity
import paladin.core.enums.organisation.OrganisationRoles
import paladin.core.repository.organisation.OrganisationMemberRepository
import paladin.core.repository.organisation.OrganisationRepository
import paladin.core.service.auth.AuthTokenService
import util.OrganisationRole
import util.TestLogAppender
import util.WithUserPersona
import util.factory.MockOrganisationEntityFactory
import java.util.*

@SpringBootTest
@ExtendWith(MockitoExtension::class)
@ActiveProfiles("test")
@WithUserPersona(
    userId = "f8b1c2d3-4e5f-6789-abcd-ef0123456789",
    email = "email@email.com",
    displayName = "Jared Tucker",
    roles = [
        OrganisationRole(
            organisationId = "f8b1c2d3-4e5f-6789-abcd-ef9876543210",
            role = OrganisationRoles.OWNER
        ),
        OrganisationRole(
            organisationId = "e9b1c2d3-4e5f-6789-abcd-ef9876543210",
            role = OrganisationRoles.DEVELOPER
        )
    ]
)
class OrganisationServiceTest {

    private val userId: UUID = UUID.fromString("f8b1c2d3-4e5f-6789-abcd-ef0123456789")

    // Two Organisation Ids that belong to the user
    private val organisationId1: UUID = UUID.fromString("f8b1c2d3-4e5f-6789-abcd-ef9876543210")
    private val organisationId2: UUID = UUID.fromString("e9b1c2d3-4e5f-6789-abcd-ef9876543210")

    // Organisation Id to test access control with an org a user is not apart of
    private val organisationId3 = UUID.fromString("d8b1c2d3-4e5f-6789-abcd-ef9876543210")

    private lateinit var testAppender: TestLogAppender
    private var logger: KLogger = KotlinLogging.logger {}
    private lateinit var logbackLogger: Logger

    @Autowired
    private lateinit var authTokenService: AuthTokenService

    @MockitoBean
    private lateinit var organisationRepository: OrganisationRepository

    @MockitoBean
    private lateinit var organisationMemberRepository: OrganisationMemberRepository

    @Autowired
    private lateinit var organisationService: OrganisationService


    @BeforeEach
    fun setUp() {
        logbackLogger = LoggerFactory.getLogger(logger.name) as Logger
        testAppender = TestLogAppender.factory(logbackLogger, Level.DEBUG)

    }

    @AfterEach
    fun tearDown() {
        logbackLogger.detachAppender(testAppender)
        testAppender.stop()
    }


    @Test
    fun `handle organisation fetch with appropriate permissions`() {
        val entity: OrganisationEntity = MockOrganisationEntityFactory.createOrganisation(
            id = organisationId1,
            name = "Test Organisation",
        )

        Mockito.`when`(organisationRepository.findById(organisationId1)).thenReturn(Optional.of(entity))
        val organisation = organisationService.getOrganisation(organisationId1)
        assert(organisation.id == organisationId1)

    }

    @Test
    fun `handle organisation fetch without required organisation`() {
        val entity: OrganisationEntity = MockOrganisationEntityFactory.createOrganisation(
            // This is the organisation the user does not have access to
            id = organisationId3,
            name = "Test Organisation 3",
        )

        Mockito.`when`(organisationRepository.findById(organisationId3)).thenReturn(Optional.of(entity))

        assertThrows<AccessDeniedException> {
            organisationService.getOrganisation(organisationId3)
        }
    }

    @Test
    fun `handle organisation invocation without required permission`() {
        val entity: OrganisationEntity = MockOrganisationEntityFactory.createOrganisation(
            // This is the organisation the user is not the owner of
            id = organisationId2,
            name = "Test Organisation 2",
        )

        Mockito.`when`(organisationRepository.findById(organisationId2)).thenReturn(Optional.of(entity))
        // Assert user can fetch the organisation given org roles
        organisationService.getOrganisation(organisationId2).run {
            assert(id == organisationId2) { "Organisation ID does not match expected ID" }
            assert(name == "Test Organisation 2") { "Organisation name does not match expected name" }
        }
        // Assert user cannot delete organisation given lack of `Owner` privileges
        assertThrows<AccessDeniedException> {
            organisationService.deleteOrganisation(organisationId2)
        }
    }


}