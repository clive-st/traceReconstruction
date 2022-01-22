package example


import org.scalatest._
import traza.util.{JsonProducer, LogLineParser}

class LogLineParserSpec extends FlatSpec with Matchers {


  "The parser" should "parse the examples lines" in {
    val trace1ACtoAD= LogLineParser.readLine("2016-10-20 12:43:34.000 2016-10-20 12:43:35.000 trace1 back-end-3 ac->ad")


    assert( trace1ACtoAD.right.get.callerSpan == "ac" )
    assert( trace1ACtoAD.right.get.span == "ad" )
    assert( trace1ACtoAD.right.get.traceId == "trace1" )


    val traceEndACtoAD= LogLineParser.readLine("2016-10-20 12:43:32.000 2016-10-20 12:43:42.000 trace1 front-end null->aa")

    assert( traceEndACtoAD.right.get.callerSpan == "null" )
  }


  "The parser" should "return an error msg if the format is invalid" in {
    val trace1ACtoAD= LogLineParser.readLine("2016-10-xxx20 12:43:34.000 2016-10-20 12:43:35.000 trace1 back-end-3 ac->ad")

    assert( trace1ACtoAD.isLeft)
  }
}
