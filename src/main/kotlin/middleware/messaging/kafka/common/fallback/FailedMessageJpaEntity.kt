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
    val key: String?,

    @Column(nullable = true)
    val value: String?,

    @Column(nullable = false)
    val partition: Int,

    @Column(nullable = false)
    val offset: Long,

    @Column(nullable = false, updatable = false)
    var loggedAt: LocalDateTime? = null,

    @Column(nullable = true)
    val consumerErrorMessage: String?,

    @Column(nullable = false)
    val consumerErrorStackTrace: String,

    @Column(nullable = true)
    val dltErrorMessage: String?,

    @Column(nullable = false)
    val dltErrorStackTrace: String,
) {
    @PrePersist
    fun setCreatedAtIfNull() {
        if (loggedAt == null) {
            loggedAt = LocalDateTime.now()
        }
    }
}