package org.odfi.indesign.core.config.ooxoo

import org.odfi.indesign.core.config.ConfigImplementation
import java.io.File
import com.idyria.osi.ooxoo.db.store.fs.FSStore

class OOXOOFSConfigImplementation(var baseFile : File) extends ConfigImplementation {
  
  // Create FSStore
  var fsStore = new FSStore(baseFile)
  
  var realmFSStore : Option[FSStore] = None
  
  def openConfigRealm(str:String) = {
    this.realmFSStore = Some(new FSStore(new File(baseFile,str)))
  }
  
  // Containers
  def getContainer(str:String) = this.realmFSStore match {
    case Some(store) => store.container(str)
    case None => throw new IllegalArgumentException("Cannot open container if no realm has been opened")
  }
  
}