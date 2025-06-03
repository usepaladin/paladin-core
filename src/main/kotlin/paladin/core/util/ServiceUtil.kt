package paladin.core.util

import paladin.core.exceptions.NotFoundException
import java.util.*

object ServiceUtil {

    /**
     * Finds a user profile by the given query and returns it as its mapped DTO.
     */
    @Throws(NotFoundException::class)
    fun <T, V> findOrThrow(data: T, query: (T) -> Optional<V>): V {
        return query.invoke(data)
            .orElseThrow { NotFoundException("Entity not found for query: $data") }
    }
}