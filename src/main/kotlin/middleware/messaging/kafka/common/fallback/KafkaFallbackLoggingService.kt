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
                key = command.record.key()?.toString(),
                value = command.record.value()?.toString(),
                partition = command.record.partition(),
                offset = command.record.offset(),
                consumerErrorMessage = command.consumerException.message,
                consumerErrorStackTrace = command.consumerException.stackTraceToString(),
                dltErrorMessage = command.dltException.message,
                dltErrorStackTrace = command.dltException.stackTraceToString(),
            )
        )
    }
}