package org.odfi.indesign.module.git

import org.odfi.indesign.core.harvest.Harvester
import org.odfi.indesign.core.harvest.fs.HarvestedFile
import java.io.File
import org.odfi.indesign.core.harvest.fs.FileSystemHarvester

class GitHarvester extends FileSystemHarvester {
  
  
  this.onDeliverFor[HarvestedFile] {
    case f if (f.path.toFile.isDirectory && new File(f.path.toFile,".git/config").exists()) =>
      var gitResource = new GitRepository(f)
     
      gather(gitResource)
      true
  }
  
}