package edu.kit.ipe.adl.indesign.module.scala

import edu.kit.ipe.adl.indesign.core.harvest.fs.HarvestedFile
import edu.kit.ipe.adl.indesign.core.harvest.Harvester

class ScalaAppHarvester extends Harvester[ScalaSourceFile,ScalaSourceFile] {
  
  def doHarvest = {
    
    //println(s"Inside DO harvest for scala")
    
  }
  
  override def deliver(r:ScalaSourceFile) = {
    
    //println(s"Delivering to scala harvester -> "+r.path+" -> "+r.path.toString.endsWith(".scala") )
    //println(s"Lines: "+r.getLines)
    
    
     r.getLines.find { line => line.contains("extends App") }.isDefined match {
       case true =>
        println(s"Found App")
        gather(new ScalaSourceFile(r.path))
        true
      case _ => 
        false
    }
    
  }
  
}