package traza.service

import traza.model.{Statistics, _}
import cats.implicits._
import monix.reactive.Observable
import traza.util.{JsonProducer, LogLineParser}
import scala.concurrent.{Await, Future}
import monix.execution.Scheduler.Implicits.global
import scala.concurrent.duration.Duration

object TraceGeneration {

  type LogLines = Map[String, Map[String,List[LogLine]]]

  private var ourState: LogLines = Map.empty
  private var ourTimeoutCheck: Map[String, Long]= Map.empty
  private var statistics=Statistics()
  private var ourTraces: Map[String,Trace] = Map.empty


  /*
  !! Change content ourTrace - side effect - temp. solution to fix issue
   */
  private def doWeHaveACompleteTrace(log:LogLine, data: Map[String,List[LogLine]]):Option[Trace] =synchronized {

    if( log.callerSpan.equals("null")) {
      val newtrace= Trace(service=log.serviceName,log.startTimestamp, log.endTimestamp,log.traceId, log.span, List.empty)
      ourTraces = ourTraces + (log.traceId ->  newtrace)
    }

    if( ourTraces.contains(log.traceId)){

      val thetrace = ourTraces.get(log.traceId).get

      val allthecaller = (data.values.flatten.map( _.callerSpan)).toSet
      val allthecalled = (data.values.flatten.map( _.span) ++ List(thetrace.span)).toSet

      if( allthecaller.subsetOf(allthecalled)) thetrace.some
        else None

    }else None
  }

  private def doWeHaveASimpleLog(log:LogLine):Boolean = !log.callerSpan.equals("null")

  private def getLineAsLog( aline: String): Either[String,LogLine] = LogLineParser.readLine(aline)


  private def reconstructTheTrace( thetrace: Trace, data: Map[String,List[LogLine]] ): Either[String, FinalTrace] = {
    if( data.isEmpty  ){
      Right( FinalTrace(thetrace.traceId, thetrace))
    }

    def consumeMap( map: Map[String, List[LogLine]], startingSpan: String ): List[Service] = {
      if( map.isEmpty) List.empty[Service]
      else{
        val ourService=map.get(startingSpan).getOrElse(List.empty)
        ourService.map{ logline =>
          Service(  logline.serviceName, logline.startTimestamp, logline.endTimestamp,
            consumeMap( map.filter(_._1!=startingSpan), logline.span))

        }
      }
    }

    val allthecaller = (data.values.flatten.map( _.callerSpan)).toSet
    val allthecalled = (data.values.flatten.map( _.span) ++ List(thetrace.span)).toSet

    if( !allthecaller.subsetOf(allthecalled))
      Left("missing connection in the trace")
    else Right(  FinalTrace(thetrace.traceId,
      thetrace.copy( calls = consumeMap(data, thetrace.span)  )))
  }



  private def keepServiceInMemory( logLine: LogLine, map: Map[String, Map[String,List[LogLine]]]):LogLines = {
    map |+| Map( logLine.traceId -> Map(logLine.callerSpan -> List(logLine)))
  }


  private def removeServiceInMemory( traceid: String, data: LogLines):LogLines= {
    data - traceid
  }


  private def cleaningMemory( data: LogLines, timeoutCheck: Map[String,Long]) = {
    val keysToRemove:List[String]= timeoutCheck.filter( System.currentTimeMillis() - _._2  > 30000).map(_._1).toList

    (data.filterNot{x=>keysToRemove.contains(x._1)}, timeoutCheck.filterNot{x=>keysToRemove.contains(x._1)})
  }


  /*
  experiment based on Monix - Observable pattern
  use inner state => synchronized
   */
  def readFile()= synchronized{
    val source=Observable.fromIterator( scala.io.Source.fromFile("log.txt").getLines()  ).flatMap(observerTranformer(_)).map{
      result =>
        result match {
          case Right(trace) => println(JsonProducer.toJson(trace))
          case Left(error) => println("missing trace:"+error)
        }
    }
   Await.result(source.runAsyncGetLast, Duration.Inf)
  }

  /*
  same code as read - but this time the return is an Observable, from future
  if we have a complete trace to reconstruct, otherwise an empty one.
  !! side effect
   */
  def observerTranformer( aline: String): Observable[Either[String, FinalTrace]] =synchronized {
    val eitherErrorOrLog=getLineAsLog(aline)

    eitherErrorOrLog match {

      case Right(ourLog) =>

        val (newState, newOurTimeoutCheck, obs) = {

          val ourStateUpdated = if( !logLineIsATrace(ourLog )){
            keepServiceInMemory(ourLog, ourState)
          }else ourState


          val tracedata = ourStateUpdated.getOrElse(ourLog.traceId, Map.empty)


          doWeHaveACompleteTrace(ourLog, tracedata).map {
            thetrace =>

              val obs=Observable.fromFuture( Future{reconstructTheTrace(thetrace, ourStateUpdated.getOrElse(thetrace.traceId, Map.empty))})

              (ourStateUpdated - thetrace.traceId, ourTimeoutCheck - thetrace.traceId, obs)
          }.getOrElse{
            (ourStateUpdated,
              (ourTimeoutCheck - ourLog.traceId) |+| Map(ourLog.traceId -> System.currentTimeMillis()), Observable.empty)
          }
        }

        val (finalourState,finalourTimeoutCheck) = cleaningMemory( newState,newOurTimeoutCheck )

        ourState=finalourState
        ourTimeoutCheck=finalourTimeoutCheck

        obs

      case Left(errorMsg)  =>
        Observable.empty
    }
  }


  private def logLineIsATrace( logLine: LogLine):Boolean = {
    logLine.callerSpan.equalsIgnoreCase("null")
  }


  /*
  parse the line, check if that's a "trace" line ( null ->), if not add it into memory
  then check if for the current trace.id if we already a "trace", if yes,
  we check if we complete trace, if yes, we build it.
  !! Change content ourTrace - side effect */
  def readLine( aline: String,
                howtotreatSuccess: FinalTrace => Unit,howtotreatStatistics: Statistics => Unit ): Unit =synchronized{
    val eitherErrorOrLog=getLineAsLog(aline)

    eitherErrorOrLog match {
      case Right(ourLog) =>

        val (newState, newOurTimeoutCheck) = {

          val ourStateUpdated = if( !logLineIsATrace(ourLog)){
            keepServiceInMemory(ourLog, ourState)
          }else ourState


          val tracedata = ourStateUpdated.getOrElse(ourLog.traceId, Map.empty)


          doWeHaveACompleteTrace(ourLog, tracedata).map {
            thetrace =>

              reconstructTheTrace(thetrace, ourStateUpdated.getOrElse(thetrace.traceId, Map.empty)) match {
                case Right(finaltrace) =>
                  howtotreatSuccess(finaltrace)
                  statistics = statistics.copy(completeTrace = statistics.completeTrace + 1)
                case Left(errormsg) =>
                  statistics = statistics.copy(incompleteTrace = statistics.incompleteTrace + 1)
              }

              (ourStateUpdated - thetrace.traceId, ourTimeoutCheck - thetrace.traceId)
          }.getOrElse{
            (ourStateUpdated,
            (ourTimeoutCheck - ourLog.traceId) |+| Map(ourLog.traceId -> System.currentTimeMillis()))
          }
        }


        val (finalourState,finalourTimeoutCheck) = cleaningMemory( newState,newOurTimeoutCheck )

        if( newState.size != finalourState.size)
          statistics = statistics.copy( nbrTraceRemovedForTimeout = statistics.nbrTraceRemovedForTimeout+1 )

        ourState=finalourState
        ourTimeoutCheck=finalourTimeoutCheck

      case Left(errorMsg)  =>
        statistics = statistics.copy( errorReadingLogLine = statistics.errorReadingLogLine+1 )
    }
    howtotreatStatistics(statistics )
  }
}
