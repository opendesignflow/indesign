package edu.kit.ipe.adl.indesign.core.module.webdraw.superchart

import edu.kit.ipe.adl.indesign.core.module.IndesignModule
import com.idyria.osi.wsb.webapp.localweb.LocalWebHTMLVIewCompiler

object SuperchartModule extends IndesignModule {
  
  def load = {
    
    LocalWebHTMLVIewCompiler.addCompileTrait(classOf[SuperChartBuilder])
    
  }
  
}