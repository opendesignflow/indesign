package org.odfi.indesign.module.inative.cmake

import org.odfi.indesign.core.harvest.Harvester
import org.odfi.indesign.core.harvest.fs.HarvestedFile
import java.io.File

class CMakeProjectHarvester extends Harvester {
  
  this.onDeliverFor[HarvestedFile] {
    case folder if (folder.isDirectory && new File(folder.path.toFile,"CMakeLists.txt").exists) =>
      gather(new CMakeProject(folder))
      true
  }
}

class CMakeProject(base:HarvestedFile) extends HarvestedFile(base.path) {
  deriveFrom(base)
}