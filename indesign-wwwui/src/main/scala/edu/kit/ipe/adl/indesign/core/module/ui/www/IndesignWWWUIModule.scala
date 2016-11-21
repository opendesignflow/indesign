package edu.kit.ipe.adl.indesign.core.module.ui.www

import java.awt.event.WindowAdapter

import com.idyria.osi.wsb.webapp.localweb.LocalWebEngine

import edu.kit.ipe.adl.indesign.core.harvest.Harvest
import edu.kit.ipe.adl.indesign.core.module.IndesignModule
import edu.kit.ipe.adl.indesign.core.module.ui.www.views.LocalWebView
import java.awt.event.WindowEvent
import edu.kit.ipe.adl.indesign.core.module.ui.www.views.IndesignWWWView
import edu.kit.ipe.adl.indesign.core.module.ui.www.fs.FileSystemHarvesterView

object IndesignWWWUIModule extends IndesignModule {

  var mainHarvesters = new WWWViewHarvester
  
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