package edu.kit.ipe.adl.indesign.core.module.ui.www

import edu.kit.ipe.adl.indesign.core.harvest.Harvester
import edu.kit.ipe.adl.indesign.core.harvest.Harvest
import edu.kit.ipe.adl.indesign.module.scala.ScalaProjectHarvester
import edu.kit.ipe.adl.indesign.module.scala.ScalaSourceFile

object WWWViewHarvester extends Harvester {

  //this.autoCleanResources = false

  this.onDeliver {
    case r: IndesignUIView =>
      gather(r)
      println(s"Got a view delivered, size now: " + this.getResources.size)
      true
    case r: ScalaSourceFile =>

      r.getLines.find { line => line.contains(s"extends ${classOf[IndesignUIView].getSimpleName.replace("$","")}") }.isDefined match {
        case true =>
          
          var proxyView = new IndesignUIView().deriveFrom(r)
          println(s"Found Ui View: "+proxyView.getId)
          gather(proxyView)
          true
        case _ =>
          false
      }

      
  }

  
  // Auto Reg
  //---------------------
  Harvest.registerAutoHarvesterObject(classOf[ScalaProjectHarvester], this)
}