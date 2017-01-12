package org.odfi.indesign.core.module.webdraw.superchart

import org.odfi.indesign.core.module.IndesignModule
import com.idyria.osi.wsb.webapp.localweb.LocalWebHTMLVIewCompiler

object SuperchartModule extends IndesignModule {
  
  def load = {
    
    LocalWebHTMLVIewCompiler.addCompileTrait(classOf[SuperChartBuilder])
    
  }
  
}