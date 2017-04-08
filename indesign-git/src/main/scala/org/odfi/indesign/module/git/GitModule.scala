package org.odfi.indesign.module.git

import org.odfi.indesign.core.module.IndesignModule
import org.odfi.indesign.core.harvest.Harvest
import org.odfi.indesign.core.harvest.fs.FileSystemHarvester
import org.odfi.indesign.core.module.ui.www.WWWViewHarvester

object GitModule extends IndesignModule {

  this.onInit {
    //Harvest.registerAutoHarvesterClass(classOf[FileSystemHarvester], classOf[GitHarvester])
    //Harvest.registerAutoHarvesterClass(classOf[GitHarvester], classOf[MavenProjectHarvester])
    //WWWViewHarvester.deliverDirect(new GitWWWView)
  }
}