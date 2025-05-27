package middleware.messaging.kafka.sync.config

import middleware.messaging.kafka.common.config.KafkaErrorHandlerProvider
import middleware.messaging.kafka.sync.adapter.out.kafka.event.OrderCreatedEvent
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.KafkaTemplate

@Configuration
class SyncKafkaConsumerConfig(
    private val errorHandlerProvider: KafkaErrorHandlerProvider
) {

    @Bean
    fun kafkaListenerContainerFactoryForOrderCreatedEvent(
        consumerFactory: ConsumerFactory<String, OrderCreatedEvent>,
        kafkaTemplate: KafkaTemplate<Any, Any>,
    ): ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent>()
        factory.consumerFactory = consumerFactory
        factory.setCommonErrorHandler(errorHandlerProvider.createDefaultErrorHandler(kafkaTemplate))
        return factory
    }
}