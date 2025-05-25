package middleware.messaging.kafka.sync.domain.vo

import java.util.*

@JvmInline
value class UserId(val value: UUID) {
    override fun toString(): String = value.toString()

    companion object {
        fun generate(): UserId {
            return UserId(UUID.randomUUID())
        }
    }
}