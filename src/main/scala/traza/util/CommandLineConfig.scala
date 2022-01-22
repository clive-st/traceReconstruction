package traza.util

import cats.implicits._

case class CommandLineConfig( inputFile: Option[String]=None, experiment: Boolean = false)

object readCommandLine {
  val parser = new scopt.OptionParser[CommandLineConfig]("Traza") {
    head("Traza", "snapshot")


    opt[String]('i', "inputFile").action((x, c) =>
      c.copy(inputFile = x.some)).text("inputFile")


    opt[String]('x', "experiment").action((x, c) =>
      c.copy(experiment = x.equalsIgnoreCase("y"))).text("experiment y or no")

  }
}






