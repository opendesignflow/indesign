package edu.kit.ipe.adl.indesign.core.config

import com.idyria.osi.ooxoo.db.store.mem.MemContainer

class DefaultMemoryConfigImplementation extends ConfigImplementation{
  
  def getContainer(str:String) = new MemContainer(str)
  
}