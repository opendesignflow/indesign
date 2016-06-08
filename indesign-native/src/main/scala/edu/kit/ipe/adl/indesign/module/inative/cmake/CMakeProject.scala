package edu.kit.ipe.adl.indesign.module.inative.cmake

import edu.kit.ipe.adl.indesign.core.harvest.Harvester
import edu.kit.ipe.adl.indesign.core.harvest.fs.HarvestedFile
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