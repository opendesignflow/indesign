package edu.kit.ipe.adl.indesign.fastbuild

import edu.kit.ipe.adl.indesign.core.harvest.Harvester
import edu.kit.ipe.adl.indesign.core.harvest.fs.FileSystemHarvester
import edu.kit.ipe.adl.indesign.core.harvest.fs.HarvestedFile
import java.io.File

class FBProjectHarvester extends FileSystemHarvester {
  
  
  this.onDeliverFor[HarvestedFile] {
    case f if (f.path.toFile.isDirectory) => 
      
      new File(f.path.toFile,"fbuild.txt") match {
        case fbf if (fbf.exists) => 
          gather(new FBProject(f))
          true
        case _ => 
          false
      }
        
      
      
  }
}