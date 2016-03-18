package edu.kit.ipe.adl.indesign.module.tcl

import edu.kit.ipe.adl.indesign.core.module.IndesignModule
import edu.kit.ipe.adl.indesign.core.harvest.fs.FileSystemHarvester
import edu.kit.ipe.adl.indesign.module.maven.POMFileHarvester

object TCLModule extends IndesignModule {
  
  def load = {
    //FileSystemHarvester.addChildHarvester(new TCLFileHarvester)
  }
  
}