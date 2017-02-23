package org.odfi.indesign.core.config

import com.idyria.osi.ooxoo.db.store.DocumentContainer

trait ConfigImplementation {
  
  
  def addRealm(name:String)
  def listAllRealms : List[String]
  def detectLatestRealm : Option[String]
  def openConfigRealm(str:String)
  def getContainer(str:String) : DocumentContainer
  
}