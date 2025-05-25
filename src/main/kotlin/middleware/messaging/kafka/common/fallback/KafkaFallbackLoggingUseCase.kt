package middleware.messaging.kafka.common.fallback

interface KafkaFallbackLoggingUseCase {
    fun save(command: KafkaFallbackLoggingCommand)
}