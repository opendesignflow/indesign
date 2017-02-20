package org.odfi.indesign.core.config

import org.odfi.indesign.core.harvest.fs.HarvestedFile
import java.nio.file.Path
import org.odfi.indesign.core.config.ooxoo.OOXOOFSConfigImplementation
import java.io.File
import org.odfi.indesign.core.config.model.CommonConfig

class FolderWithConfig(p:Path) extends HarvestedFile(p) with ConfigHolder {
  
  // Prepare folder where config can be stored
  var configurationSource = new OOXOOFSConfigImplementation(new File(p.toFile(),".indesign"))
  configurationSource.openConfigRealm(Config.currentRealm)
  
  def isConfigLoaded = {
    configurationSource.getContainer("config").parsedDocumentCache.contains(Config.documentName(this))
  }
  
  def config = {
    configurationSource.getContainer("config").documentFromClass[CommonConfig](Config.documentName(this),autocreate=true)
    //None
  }
  
}