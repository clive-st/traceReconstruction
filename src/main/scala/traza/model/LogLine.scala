package traza.model

import java.time.LocalDateTime

case class LogLine( startTimestamp: LocalDateTime, endTimestamp: LocalDateTime,
                    traceId: String, serviceName: String, callerSpan: String, span: String )





