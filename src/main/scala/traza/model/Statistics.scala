package traza.model

case class Statistics( errorReadingLogLine: Int =0, incompleteTrace: Int=0, completeTrace: Int=0,
                       nbrTraceRemovedForTimeout:Int =0){

  override def toString()={
    s"nbr error reading line:$errorReadingLogLine , nbr incomplete trace: $incompleteTrace, nbr complete trace: $completeTrace,nbr timeout: $nbrTraceRemovedForTimeout "
  }

}
