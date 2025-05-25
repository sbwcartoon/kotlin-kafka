package middleware.messaging.kafka.sync.domain.vo

import java.util.*

@JvmInline
value class OrderId(val value: UUID) {
    override fun toString(): String = value.toString()

    companion object {
        fun generate(): OrderId {
            return OrderId(UUID.randomUUID())
        }
    }
}