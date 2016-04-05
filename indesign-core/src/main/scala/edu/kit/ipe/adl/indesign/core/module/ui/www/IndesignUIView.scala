package edu.kit.ipe.adl.indesign.core.module.ui.www

import edu.kit.ipe.adl.indesign.core.harvest.HarvestedResource
import com.idyria.osi.wsb.webapp.localweb.LocalWebHTMLVIew

trait IndesignUIView extends LocalWebHTMLVIew with HarvestedResource {
  
  def name = getClass.getSimpleName.replace("$","")
  
  var reloadEnable = true
  
  def getId = getClass.getCanonicalName
  
}