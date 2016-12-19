package edu.kit.ipe.adl.indesign.core.module.ui.www

import edu.kit.ipe.adl.indesign.core.harvest.Harvester
import edu.kit.ipe.adl.indesign.core.harvest.Harvest
import edu.kit.ipe.adl.indesign.core.module.buildsystem.JavaSourceFile
import com.idyria.osi.wsb.webapp.localweb.LocalWebEngine
import com.idyria.osi.vui.core.view.AView
import com.idyria.osi.wsb.webapp.localweb.LocalWebHTMLVIew

class WWWViewHarvester extends Harvester {

  //this.autoCleanResources = false

  override def doHarvest = {
    
   // println("Doing harvest on ")
    
  }
  
  def deliverDirectToPath(pair:(String , IndesignUIView)) = {
    var v = pair._2
    v.changeTargetViewPath(pair._1)
    this.deliverDirect(v)
  }
  
  this.onDeliver {
    case r: IndesignUIView =>
      gather(r)
      //println(s"Got a view delivered $r, size now: " + this.getResources.size)
      r.onGathered {
        case h => 
          
         // println(s"View gathered")
          // If View has a specific target path, then register it in Local Web Tree
          r.targetViewPath match {
            case Some(path)  =>
              //println("Adding view as standlone: "+path)
              LocalWebEngine.addViewHandler(path, r.getClass)
            case _ => 
             // println(s"View In other WWW h")
          }
      }
      true
    case r: JavaSourceFile =>

      r.getLines.find { line => line.contains(s"extends ${classOf[IndesignUIView].getSimpleName.replace("$","")}") }.isDefined match {
        case true =>
          
          var proxyView = new IndesignUIView().deriveFrom(r)
          println(s"Found Ui View: "+proxyView.getId)
          gather(proxyView)
          true
        case _ =>
          false
      }
    case r => 
      println(s"Deliber direct of: $r");
      r.asInstanceOf[IndesignUIView]
      false
      
  }

  
  // Auto Reg
  //---------------------
  //Harvest.registerAutoHarvesterObject(classOf[ScalaProjectHarvester], this)
}