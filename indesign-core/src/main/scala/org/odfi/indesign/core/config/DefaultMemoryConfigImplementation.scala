package org.odfi.indesign.core.config

import com.idyria.osi.ooxoo.db.store.mem.MemContainer

class DefaultMemoryConfigImplementation extends ConfigImplementation{
  
  def getContainer(str:String) = new MemContainer(str)
  
}