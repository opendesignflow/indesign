package org.odfi.indesign.core.config

import com.idyria.osi.ooxoo.db.store.DocumentContainer

trait ConfigImplementation {

  def addRealm(name: String) : Unit
  def listAllRealms: List[String]
  def detectLatestRealm: Option[String]

  def cleanRealm : Unit
  def openConfigRealm(str: String) : Unit
  
  def swithToCleanRealm(name: String) = {
    openConfigRealm(name)
    cleanRealm
  }

  def getContainer(str: String): DocumentContainer

}