package paladin.core.service.encryption

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.slf4j.LoggerFactory
import util.TestLogAppender
import util.factory.MockObjectMapperFactory
import util.mock.User
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class EncryptionServiceTest {
    private lateinit var encryptionService: EncryptionService
    private lateinit var testAppender: TestLogAppender
    private var logger: KLogger = KotlinLogging.logger {}
    private lateinit var logbackLogger: Logger
    private val encryptionBase64Key = Base64.getEncoder().encodeToString(ByteArray(16) { 1 })

    @BeforeEach
    fun setUp() {
        logbackLogger = LoggerFactory.getLogger(logger.name) as Logger
        testAppender = TestLogAppender.factory(logbackLogger, Level.DEBUG)
        encryptionService = EncryptionService(MockObjectMapperFactory.objectMapper, logger)

    }

    @Test
    fun `should encrypt and decrypt string correctly`() {
        val original = "Hello, secure world!"
        val encrypted = encryptionService.encrypt(original, encryptionBase64Key)
        assertNotNull(encrypted)

        val decrypted = encryptionService.decrypt(encrypted, encryptionBase64Key)
        assertEquals(original, decrypted)
    }

    @Test
    fun `should encrypt and decrypt object as map`() {
        val obj = mapOf("username" to "admin", "active" to true)
        val encrypted = encryptionService.encryptObject(obj, encryptionBase64Key)
        assertNotNull(encrypted)

        val decrypted = encryptionService.decryptObject(encrypted, encryptionBase64Key)
        assertEquals(obj["username"], decrypted?.get("username"))
        assertEquals(obj["active"], decrypted?.get("active"))
    }

    @Test
    fun `should decrypt to typed object`() {
        val user = User("Alice", 30)

        val encrypted = encryptionService.encryptObject(user, encryptionBase64Key)
        val decrypted = encryptionService.decryptObject(encrypted!!, User::class.java, encryptionBase64Key)

        assertNotNull(decrypted)
        assertEquals(user.name, decrypted.name)
        assertEquals(user.age, decrypted.age)
    }

    @Test
    fun `should decrypt using TypeReference`() {
        val original = listOf("one", "two", "three")
        val json = ObjectMapper().writeValueAsString(original)
        val encrypted = encryptionService.encrypt(json, encryptionBase64Key)

        val decrypted =
            encryptionService.decryptObject(encrypted!!, object : TypeReference<List<String>>() {}, encryptionBase64Key)
        assertEquals(original, decrypted)
    }

    @Test
    fun `should return null on decryption failure with invalid Base64`() {
        val invalidBase64 = "!!not_base64!!"
        val result = encryptionService.decrypt(invalidBase64, encryptionBase64Key)
        assertTrue {
            testAppender.logs.any {
                it.level == Level.ERROR
            }
        }
        assertNull(result)
    }

    @Test
    fun `should return null when ciphertext is too short`() {
        val shortCiphertext = Base64.getEncoder().encodeToString(ByteArray(5))
        val result = encryptionService.decrypt(shortCiphertext, encryptionBase64Key)
        assertTrue {
            testAppender.logs.any {
                it.level == Level.ERROR && it.message.contains("Invalid ciphertext format")
            }
        }
        assertNull(result)
    }
}