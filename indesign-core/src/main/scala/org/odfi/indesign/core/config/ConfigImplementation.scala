package org.odfi.indesign.core.config

import com.idyria.osi.ooxoo.db.store.DocumentContainer

trait ConfigImplementation {
  
  
  def getContainer(str:String) : DocumentContainer
  
}