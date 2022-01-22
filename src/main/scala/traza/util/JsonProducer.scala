package traza.util

import java.time.LocalDateTime
import traza.model.{FinalTrace, Service, Trace}
import org.json4s._
import org.json4s.native.Serialization.write
import FieldSerializer._
import org.json4s._


object JsonProducer {


  val traceSerializer = FieldSerializer[Trace](
    renameTo("startTimestamp", "start") orElse ignore("span") orElse ignore("traceId") orElse
      renameTo("endTimestamp", "end")  )

  val serviceSerializer = FieldSerializer[Service](
    renameTo("startTimestamp", "start") orElse ignore("span") orElse ignore("traceId") orElse
      renameTo("endTimestamp", "end")  )

  val finaltraceSerializer = FieldSerializer[FinalTrace](
    renameTo("traceName", "trace")  )



  case object JLocalDateTimeSerializer extends CustomSerializer[LocalDateTime](format => (
    {
      case JString(s) =>
        null
      case JNull => null
    },
    {
      case d: LocalDateTime =>
        JString( d.format(DateFormatter.writingFormatter) )
    }
  ))


  implicit val formats = DefaultFormats + traceSerializer + serviceSerializer + finaltraceSerializer + JLocalDateTimeSerializer

  def toJson( trace: Trace  ): String = {
    write((trace))
  }

  def toJson( trace: FinalTrace  ): String = {
    write((trace))
  }
}
