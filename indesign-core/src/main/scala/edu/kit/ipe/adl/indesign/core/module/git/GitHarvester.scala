package edu.kit.ipe.adl.indesign.core.module.git

import edu.kit.ipe.adl.indesign.core.harvest.Harvester
import edu.kit.ipe.adl.indesign.core.harvest.fs.HarvestedFile
import java.io.File

object GitHarvester extends Harvester {
  
  
  this.onDeliverFor[HarvestedFile] {
    case f if (f.path.toFile.isDirectory && new File(f.path.toFile,".git/config").exists()) =>
      var gitResource = new GitRepository(f.path)
      gather(gitResource)
      true
  }
  
}