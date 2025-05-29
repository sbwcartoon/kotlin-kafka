package middleware.messaging.kafka.sync.config

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@EmbeddedKafka(
    partitions = 1,
    topics = ["test"],
    brokerProperties = ["listeners=PLAINTEXT://localhost:0"]
)
@SpringBootTest(
    properties = [
        "spring.kafka.bootstrap-servers=\${spring.embedded.kafka.brokers}",
        "kafka.topic.order-created=test",
    ],
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
)
annotation class EmbeddedKafkaForTest