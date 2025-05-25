package middleware.messaging.kafka.common.exception

class DltPublishException(
    e: RuntimeException,
) : RuntimeException(e.message)