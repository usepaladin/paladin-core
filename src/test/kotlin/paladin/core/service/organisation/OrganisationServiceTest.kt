package paladin.core.service.organisation

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import paladin.core.enums.organisation.OrganisationRoles
import paladin.core.repository.organisation.OrganisationMemberRepository
import paladin.core.repository.organisation.OrganisationRepository
import paladin.core.service.auth.AuthTokenService
import util.OrganisationRole
import util.TestLogAppender
import util.WithUserPersona
import java.util.*

@SpringBootTest
@ExtendWith(MockKExtension::class)
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

    private lateinit var organisationService: OrganisationService
    private lateinit var testAppender: TestLogAppender
    private var logger: KLogger = KotlinLogging.logger {}
    private lateinit var logbackLogger: Logger

    @Autowired
    private lateinit var authTokenService: AuthTokenService

    @MockK
    private lateinit var organisationRepository: OrganisationRepository

    @MockK
    private lateinit var organisationMemberRepository: OrganisationMemberRepository

    @BeforeEach
    fun setUp() {
        logbackLogger = LoggerFactory.getLogger(logger.name) as Logger
        testAppender = TestLogAppender.factory(logbackLogger, Level.DEBUG)
        organisationService = OrganisationService(
            organisationRepository = organisationRepository,
            organisationMemberRepository = organisationMemberRepository,
            logger = logger,
            authTokenService = authTokenService,
        )
    }

    @AfterEach
    fun tearDown() {
        logbackLogger.detachAppender(testAppender)
        testAppender.stop()
    }


    @Test
    fun `handle organisation fetch with appropriate permissions`() {
    }

    @Test
    fun `handle organisation fetch without required organisation`() {
    }

    @Test
    fun `handle organisation invocation without required permission`() {
    }


}