package traza


import traza.model.{FinalTrace, Statistics}
import traza.service.TraceGeneration
import traza.util.{CommandLineConfig, JsonProducer, readCommandLine}

import scala.io.{Source, StdIn}


object Main extends Main with App {
  readCommandLine.parser.parse(args, CommandLineConfig()) match {
    case Some(config) =>

      if( config.experiment ) TraceGeneration.readFile();
      else
        config.inputFile match {
          case Some(filename) =>
            for (line <- Source.fromFile(filename).getLines) {
              TraceGeneration.readLine(line, (ft:FinalTrace) => println(JsonProducer.toJson(ft)),

                (ft:Statistics) => System.err.println("statistics:"+ft))
            }

          case None =>
            for (ln <- io.Source.stdin.getLines)
              TraceGeneration.readLine(ln, (ft:FinalTrace) => println(JsonProducer.toJson(ft)),
                (ft:Statistics) => System.err.println("statistics:"+ft))
        }


    case None =>
    // arguments are bad, error message will have been displayed
  }
}

trait Main {

}
