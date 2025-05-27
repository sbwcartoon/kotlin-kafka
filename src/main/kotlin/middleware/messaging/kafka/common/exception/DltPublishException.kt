package middleware.messaging.kafka.common.exception

class DltPublishException(
    e: Exception,
) : RuntimeException(e.message)