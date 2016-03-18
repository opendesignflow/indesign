package edu.kit.ipe.adl.indesign.module.maven

import edu.kit.ipe.adl.indesign.core.module.IndesignModule
import edu.kit.ipe.adl.indesign.core.harvest.fs.FileSystemHarvester

object MavenModule extends IndesignModule {
  
  def load = {
    //FileSystemHarvester.addChildHarvester(new POMFileHarvester)
  }
}