package org.odfi.indesign.module.scala

import org.odfi.indesign.core.harvest.fs.HarvestedFile
import org.odfi.indesign.core.harvest.Harvester

class ScalaAppHarvester extends Harvester {
  
 
  
  this.onDeliverFor[ScalaSourceFile] {

    case r =>
    
    //println(s"Delivering to scala harvester -> "+r.path+" -> "+r.path.toString.endsWith(".scala") )
    //println(s"Lines: "+r.getLines)
    
    
     r.getLines.find { line => line.contains("extends App") }.isDefined match {
       case true =>
        logFine(s"Found App")
        gather(new ScalaAppSourceFile(r.path).deriveFrom(r))
        true
      case _ => 
        false
    }
    
  }
  
}