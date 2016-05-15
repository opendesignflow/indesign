package edu.kit.ipe.adl.indesign.core.module.git

import edu.kit.ipe.adl.indesign.core.module.IndesignModule
import edu.kit.ipe.adl.indesign.core.harvest.Harvest
import edu.kit.ipe.adl.indesign.core.harvest.fs.FileSystemHarvester
import edu.kit.ipe.adl.indesign.core.module.ui.www.WWWViewHarvester

object GitModule extends IndesignModule {
  
  def load = {
    Harvest.registerAutoHarvesterObject(classOf[FileSystemHarvester], GitHarvester)
    
    WWWViewHarvester.deliverDirect(new GitWWWView)
  }
  
}