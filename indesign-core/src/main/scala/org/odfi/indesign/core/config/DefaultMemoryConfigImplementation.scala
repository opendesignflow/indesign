package org.odfi.indesign.core.config

import com.idyria.osi.ooxoo.db.store.mem.MemContainer

class DefaultMemoryConfigImplementation extends ConfigImplementation {
  
  
  var realmString = ""
  
  def addRealm(name:String) = {
    
  }
  
  def detectLatestRealm: Option[String] = {
    None
  }
  
  def listAllRealms = {
    List()
  }
  
  def openConfigRealm(str:String) = {
    realmString = str
  }
  
  def cleanRealm = {
    
  }
  
  
  def getContainer(str: String)  = realmString match {
    case "" => new MemContainer(str)
    case other => new MemContainer(other+"."+str)
  }

}