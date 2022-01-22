package example

import org.scalatest._
import traza.model.FinalTrace
import traza.service.TraceGeneration

class TraceGenerationSpec extends FlatSpec with Matchers with Assertions {


  def validateInnerService(trace:FinalTrace, innerService: String, finalService: String ) =
    assert(trace.root.calls.filter(_.service===innerService).map(_.calls).flatten.map(_.service).contains(finalService))


  "The tracegeneration" should "generate a valid trace for the example" in {

    val textLines = List(
      "2016-10-20 12:43:34.000 2016-10-20 12:43:35.000 trace1 back-end-3 ac->ad",
      "2016-10-20 12:43:33.000 2016-10-20 12:43:36.000 trace1 back-end-1 aa->ac",
      "2016-10-20 12:43:38.000 2016-10-20 12:43:40.000 trace1 back-end-2 aa->ab",
      "2016-10-20 12:43:32.000 2016-10-20 12:43:42.000 trace1 front-end null->aa"
    )


    val testSuccess = (trace:FinalTrace) => {
        assert( trace.traceName === "trace1")
        assert( trace.root.calls.size === 2)
        assert( trace.root.calls.map(_.service).contains("back-end-1"))
        assert( trace.root.calls.map(_.service).contains("back-end-2"))
        validateInnerService(trace, "back-end-1", "back-end-3")
    }:Unit


    textLines.foreach( TraceGeneration.readLine(_,testSuccess,   _ => Unit) )

  }


  "The tracegeneration" should "detect incomplete trace" in {

    val textLines = List(
      "2016-10-20 12:43:34.000 2016-10-20 12:43:35.000 trace1 back-end-3 ac->ad",
      "2016-10-20 12:43:38.000 2016-10-20 12:43:40.000 trace1 back-end-2 aa->ab",
      "2016-10-20 12:43:32.000 2016-10-20 12:43:42.000 trace1 front-end null->aa"
    )

    textLines.foreach(  TraceGeneration.readLine(_,_ => fail("incomplete trace - should fail"),  _ => Unit))

  }


}
