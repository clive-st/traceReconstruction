package example

import java.time.LocalDateTime

import org.scalatest._
import traza.model.{FinalTrace, Trace}
import traza.service.TraceGeneration
import traza.util.{JsonProducer, LogLineParser}

class JsonProducerSpec extends FlatSpec with Matchers {


  "The JsonProducer" should "be able to generate a simple trace without services" in {
    val simpleTrace = Trace( "front-end", LocalDateTime.now(), LocalDateTime.now(), "trace-1", "aa", List.empty)
    val json= JsonProducer.toJson(simpleTrace)

    assert( json.contains("start"))
    assert( json.contains("end"))
    assert( json.contains("calls"))
  }


  "The JsonProducer" should "be able to reproduce the example" in {
    val textLines = List(
      "2016-10-20 12:43:34.000 2016-10-20 12:43:35.000 trace1 back-end-3 ac->ad",
      "2016-10-20 12:43:33.000 2016-10-20 12:43:36.000 trace1 back-end-1 aa->ac",
      "2016-10-20 12:43:38.000 2016-10-20 12:43:40.000 trace1 back-end-2 aa->ab",
      "2016-10-20 12:43:32.000 2016-10-20 12:43:42.000 trace1 front-end null->aa"
    )


    textLines.foreach( TraceGeneration.readLine(_,(trace:FinalTrace) => println( JsonProducer.toJson(trace)), _ => Unit) )

    """{"trace":"trace1",
       "root":{"service":"front-end","start":"2016-10-20 12:43:32.000","end":"2016-10-20 12:43:42.000",
       "calls":[
         {"service":"back-end-1","start":"2016-10-20 12:43:33.000","end":"2016-10-20 12:43:36.000",
          "calls":[{"service":"back-end-3","start":"2016-10-20 12:43:34.000","end":"2016-10-20 12:43:35.000","calls":[]}]},{"service":"back-end-2","start":"2016-10-20 12:43:38.000","end":"2016-10-20 12:43:40.000","calls":[]}]}}""".stripMargin


    """{“trace: “trace1”,
      “root”: {
      “service”: “front-end”,
      “start”: “2016-10-20 12:43:32.000”, “end”: “2016-10-20 12:43:42.000”, “calls”: [
      {“service”: “back-end-1”,
        “start”: “2016-10-20 12:43:33.000”,
        “end”: “2016-10-20 12:43:36.000”,
        “calls”: [
        {“service”: “back-end-3”,
          “start”: “2016-10-20 12:43:34.000”,
          “end”: “2016-10-20 12:43:35.000”}]}, {“service”, “back-end-2”,
        “start”: “2016-10-20 12:43:38.000”, “end”: “2016-10-20 12:43:40.000”} ]}}"""
  }
}
