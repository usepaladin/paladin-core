package paladin.core.service.user

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.access.AccessDeniedException
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import paladin.core.entities.user.UserEntity
import paladin.core.models.user.User
import paladin.core.repository.user.UserProfileRepository
import paladin.core.service.auth.AuthTokenService
import util.TestLogAppender
import util.WithUserPersona
import util.factory.MockUserEntityFactory
import java.util.*

@SpringBootTest
@ExtendWith(MockitoExtension::class)
@ActiveProfiles("test")
@WithUserPersona(
    userId = "f8b1c2d3-4e5f-6789-abcd-ef0123456789",
    email = "email@email.com",
    displayName = "Jared Tucker"
)
class UserProfileServiceTest {

    private val userId: UUID = UUID.fromString("f8b1c2d3-4e5f-6789-abcd-ef0123456789")
    private val secondaryUserId: UUID = UUID.fromString("a1b2c3d4-5e6f-7890-abcd-ef0123456789")

    private lateinit var testAppender: TestLogAppender
    private var logger: KLogger = KotlinLogging.logger {}
    private lateinit var logbackLogger: Logger

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

    @Autowired
    private lateinit var authTokenService: AuthTokenService

    @MockitoBean
    private lateinit var userProfileRepository: UserProfileRepository

    @Autowired
    private lateinit var userProfileService: UserProfileService

    @Test
    fun `handle user update with correct permissions`() {
        val entity: UserEntity = MockUserEntityFactory.createUser(
            id = userId,
            name = "Jared Tucker",
            email = "email@email.com"

        )

        Mockito.`when`(userProfileRepository.findById(userId)).thenReturn(Optional.of(entity))

        val updatedEmail: String = "email2@email.com"
        val updatedEntity = entity.apply {
            email = updatedEmail
        }

        val userRepresentation = User.fromEntity(updatedEntity)

        Mockito.`when`(userProfileRepository.save(any())).thenReturn(updatedEntity)
        userProfileService.updateUserDetails(userRepresentation).let {
            assert(it.email == updatedEmail) { "User email was not updated correctly" }
            assert(it.id == userId) { "User ID does not match expected ID" }
            assert(it.name == entity.displayName) { "User name does not match expected name" }
        }
    }


    @WithUserPersona(
        userId = "a1b2c3d4-5e6f-7890-abcd-ef0123456789",
        email = "test.email@email.com",
        displayName = "Not Jared Tucker"
    )
    @Test
    fun `handle updating another users profile`() {
        // Mock a scenario where a user is updating the profile of another user
        val updatedUser: User = MockUserEntityFactory.createUser(
            id = userId
        ).let {
            User.fromEntity(it)
        }

        assertThrows<AccessDeniedException> {
            userProfileService.updateUserDetails(updatedUser)
        }
    }

    @Test
    fun `handle fetching current session users account`() {
        // Get details of current mock user from session
        val sessionClaims: Map<String, Any> = authTokenService.getAllClaims()
        MockUserEntityFactory.createUser(
            id = userId,
            name = "Jared Tucker",
            email = "email@email.com"

        ).let {
            it.run {
                Mockito.`when`(userProfileRepository.findById(userId)).thenReturn(Optional.of(it))

            }
            User.fromEntity(it)
        }

        userProfileService.getUserFromSession().let {
            assert(it.id == userId) { "User ID does not match expected ID from session" }
            assert(it.email == sessionClaims["email"]) { "User email does not match expected email from session" }
            sessionClaims["user_metadata"]?.let { metadata ->
                (metadata as Map<*, *>)["displayName"]
            }.run {
                assertNotNull(this) { "Display name should not be null" }
                assert(this == it.name) { "User display name does not match expected display name from session" }
            }
        }
    }


}