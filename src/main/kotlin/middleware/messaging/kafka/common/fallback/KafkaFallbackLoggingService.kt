package middleware.messaging.kafka.common.fallback

import org.springframework.stereotype.Service

@Service
class KafkaFallbackLoggingService(
    private val fallbackJpaRepository: FallbackJpaRepository,
) : KafkaFallbackLoggingUseCase {

    override fun save(command: KafkaFallbackLoggingCommand) {
        fallbackJpaRepository.save(
            FailedMessageJpaEntity(
                topic = command.record.topic(),
                messageKey = command.record.key()?.toString(),
                messageValue = command.record.value()?.toString(),
                partition = command.record.partition(),
                messageOffset = command.record.offset(),
                errorMessage = command.exception.message,
                errorStackTrace = command.exception.stackTraceToString(),
            )
        )
    }
}