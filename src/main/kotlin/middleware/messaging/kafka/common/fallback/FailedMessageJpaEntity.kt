package middleware.messaging.kafka.common.fallback

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "failed_message")
class FailedMessageJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val topic: String,

    @Column(nullable = true)
    val messageKey: String?,

    @Column(nullable = true)
    val messageValue: String?,

    @Column(nullable = false)
    val partition: Int,

    @Column(nullable = false)
    val messageOffset: Long,

    @Column(nullable = true)
    val errorMessage: String?,

    @Column(nullable = false)
    val errorStackTrace: String,

    @Column(nullable = false, updatable = false)
    var loggedAt: LocalDateTime? = null,
) {
    @PrePersist
    fun setCreatedAtIfNull() {
        if (loggedAt == null) {
            loggedAt = LocalDateTime.now()
        }
    }
}