package edu.kit.ipe.adl.indesign.core.module.ui.www

import java.awt.event.WindowAdapter

import com.idyria.osi.wsb.webapp.localweb.LocalWebEngine

import edu.kit.ipe.adl.indesign.core.harvest.Harvest
import edu.kit.ipe.adl.indesign.core.module.IndesignModule
import edu.kit.ipe.adl.indesign.core.module.ui.www.views.LocalWebView
import java.awt.event.WindowEvent
import edu.kit.ipe.adl.indesign.core.module.ui.www.views.IndesignWWWView

object IndesignWWWUIModule extends IndesignModule {

  this.onLoad {

    Harvest.addHarvester(WWWViewHarvester)

  }
  this.onInit {

    LocalWebEngine.addViewHandler("/", classOf[IndesignWWWView])
    WWWViewHarvester.deliverDirect(new LocalWebView)
    
    
    
  }
  this.onStart {
    
    println("RELOADING---------")
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
    LocalWebEngine.lStop
  }

}