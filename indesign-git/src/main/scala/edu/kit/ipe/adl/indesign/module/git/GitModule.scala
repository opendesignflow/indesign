package edu.kit.ipe.adl.indesign.module.git

import edu.kit.ipe.adl.indesign.core.module.IndesignModule
import edu.kit.ipe.adl.indesign.core.harvest.Harvest
import edu.kit.ipe.adl.indesign.core.harvest.fs.FileSystemHarvester
import edu.kit.ipe.adl.indesign.core.module.ui.www.WWWViewHarvester
import edu.kit.ipe.adl.indesign.module.maven.MavenProjectHarvester

object GitModule extends IndesignModule {
  
  def load = {
   // println(s"GIT LOAD")
    Harvest.registerAutoHarvesterClass(classOf[FileSystemHarvester], classOf[GitHarvester])
    Harvest.registerAutoHarvesterClass(classOf[GitHarvester], classOf[MavenProjectHarvester])
    WWWViewHarvester.deliverDirect(new GitWWWView)
  }
  
}