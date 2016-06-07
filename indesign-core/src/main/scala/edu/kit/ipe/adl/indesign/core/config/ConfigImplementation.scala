package edu.kit.ipe.adl.indesign.core.config

import com.idyria.osi.ooxoo.db.store.DocumentContainer

trait ConfigImplementation {
  
  
  def getContainer(str:String) : DocumentContainer
  
}