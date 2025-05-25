package middleware.messaging.kafka.sync.domain.vo

import java.util.*

@JvmInline
value class ProductId(val value: UUID) {
    override fun toString(): String = value.toString()

    companion object {
        fun generate(): ProductId {
            return ProductId(UUID.randomUUID())
        }
    }
}