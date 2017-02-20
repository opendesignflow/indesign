package org.odfi.indesign.core.config

import com.idyria.osi.ooxoo.db.store.mem.MemContainer

class DefaultMemoryConfigImplementation extends ConfigImplementation {
  
  
  var realmString = ""
  
  def openConfigRealm(str:String) = {
    realmString = str
  }
  
  
  def getContainer(str: String)  = realmString match {
    case "" => new MemContainer(str)
    case other => new MemContainer(other+"."+str)
  }

}