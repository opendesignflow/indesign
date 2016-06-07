package edu.kit.ipe.adl.indesign.module.scala

import edu.kit.ipe.adl.indesign.core.harvest.fs.HarvestedFile
import edu.kit.ipe.adl.indesign.core.harvest.Harvester

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