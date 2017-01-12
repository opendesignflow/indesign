package org.odfi.indesign.core.config.ooxoo

import org.odfi.indesign.core.config.ConfigImplementation
import java.io.File
import com.idyria.osi.ooxoo.db.store.fs.FSStore

class OOXOOFSConfigImplementation(var baseFile : File) extends ConfigImplementation {
  
  // Create FSStore
  var fsStore = new FSStore(baseFile)
  
  // Containers
  def getContainer(str:String) = fsStore.container(str)
  
}