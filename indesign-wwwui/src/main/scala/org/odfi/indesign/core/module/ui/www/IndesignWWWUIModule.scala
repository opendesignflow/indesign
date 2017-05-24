package org.odfi.indesign.core.module.ui.www

import java.awt.event.WindowAdapter

import com.idyria.osi.wsb.webapp.localweb.LocalWebEngine

import org.odfi.indesign.core.harvest.Harvest
import org.odfi.indesign.core.module.IndesignModule
import org.odfi.indesign.core.module.ui.www.views.LocalWebView
import java.awt.event.WindowEvent
import org.odfi.indesign.core.module.ui.www.views.IndesignWWWView
import org.odfi.indesign.core.module.ui.www.fs.FileSystemHarvesterView

object IndesignWWWUIModule extends IndesignModule {

  var mainHarvesters = new WWWViewHarvester
  
  
  def addView[T <:IndesignUIView](v:T) = {
    this.mainHarvesters.gatherPermanent(v)
  }
  
  this.onLoad {

    Harvest.addHarvester(mainHarvesters)
  
     println("Load on WWW UI Module: "+Harvest.getHarvesters[WWWViewHarvester])

  }
  this.onInit {

    LocalWebEngine.addViewHandler("/", classOf[IndesignWWWView])
    mainHarvesters.deliverDirect(new LocalWebView)
    mainHarvesters.deliverDirect(new FileSystemHarvesterView)
    
    
    
  }
  this.onStart {
    
    println("Starting---------: "+mainHarvesters.hashCode())
    LocalWebEngine.lInit
    LocalWebEngine.lStart
    
    LocalWebEngine.uiFrame match {
      case Some(f) =>
        f.addWindowListener(new WindowAdapter {
          override def windowClosing(e:WindowEvent) = {
            println(s"Closed Window going to shutdown state");
            IndesignWWWUIModule.moveToShutdown
          }
        })
      case None => 
    }
  }

  this.onStop {
    println("Stopping WWW UI")
    LocalWebEngine.lStop
    LocalWebEngine.uiFrame match {
      case Some(f) =>
        f.dispose()
        f.setVisible(false)
      case None => 
    }
  }

}