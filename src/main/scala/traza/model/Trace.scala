package traza.model

import java.time.LocalDateTime


case class Service( service: String, startTimestamp: LocalDateTime, endTimestamp: LocalDateTime,
                    calls: List[Service])

case class Trace( service: String, startTimestamp: LocalDateTime, endTimestamp: LocalDateTime, traceId: String,
                  span:String, calls: List[Service] )


case class FinalTrace( traceName: String, root: Trace)