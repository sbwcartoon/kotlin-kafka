package middleware.messaging.kafka.sync.domain.vo

@JvmInline
value class Quantity(val value: Int) {
    override fun toString(): String = value.toString()
}