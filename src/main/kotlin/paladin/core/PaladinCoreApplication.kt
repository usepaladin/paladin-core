package paladin.core

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PaladinCoreApplication

fun main(args: Array<String>) {
    runApplication<PaladinCoreApplication>(*args)
}
