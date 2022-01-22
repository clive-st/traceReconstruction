package traza.util

import java.time.LocalDateTime
import traza.model.LogLine
import scala.util.Try
import scala.util.parsing.combinator.RegexParsers


class SimpleLogLineParser extends RegexParsers {

  def logdate: Parser[LocalDateTime]   = """([\d|\-]*[T|\s]{1}\S*)""".r   ^^ {  LocalDateTime.parse(_, DateFormatter.readingFormatter) }

  def callerspan: Parser[String]   = """[^\-]*""".r   ^^ {  _.toString }

  def linkspan: Parser[Unit] = """->""".r ^^ {  _.toString }

  def span: Parser[String]   = """\S*""".r   ^^ {  _.toString }

  def idTrace: Parser[String] = """\S*""".r ^^ { _.toString }

  def serviceName:Parser[String] = """\S*""".r ^^ { _.toString }

  def logline: Parser[LogLine] = logdate ~ logdate ~ idTrace ~ serviceName ~ callerspan ~ linkspan ~ span   ^^
    { case st ~ et ~ idt ~ sn ~ cspan ~ _ ~ span  => LogLine(st,et, idt,sn,cspan,span) }
}

object LogLineParser extends SimpleLogLineParser{


  def readLine( aline: String ): Either[String, LogLine] = {
    Try{
      parse(logline, aline) match {
        case Success(matched,_) => Right(matched)
        case Failure(msg,_) => Left("FAILURE: " + msg)
        case Error(msg,_) => Left("ERROR: " + msg)
      }
    }.toEither match {
      case Left(value:Throwable) => Left( value.getMessage)
      case Right(contains:Either[String,LogLine]) =>contains
    }
  }
}
