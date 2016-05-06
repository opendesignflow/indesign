package edu.kit.ipe.adl.indesign.module.maven

import edu.kit.ipe.adl.indesign.core.harvest.fs.FileSystemHarvester
import edu.kit.ipe.adl.indesign.core.harvest.fs.HarvestedFile
import java.io.File
import edu.kit.ipe.adl.indesign.core.module.ui.www.WWWViewHarvester
import edu.kit.ipe.adl.indesign.core.module.lucene.LuceneIndexProvider
import edu.kit.ipe.adl.indesign.core.harvest.Harvest


class MavenProjectHarvester extends FileSystemHarvester with LuceneIndexProvider {

  this.addChildHarvester(new POMFileHarvester)

  override def doHarvest = {
    println(s"Running Do Harvest on Maven havester, resources: " + this.getResources)
    super.doHarvest
  }

  /**
   * If the Fiel is a folder, and has a POM,xml in it -> go
   */
  this.onDeliverFor[HarvestedFile] {
    case r =>
      var pomFile = new File(r.path.toFile(), "pom.xml")

      (r.path.toFile().isDirectory(), pomFile.exists) match {
        case (true, true) =>
          println(s"Delivered Maven Project ")
          var mp = gather(new MavenProjectResource(r.path))
          mp.onGathered {
            case h if(h==this) => 
               WWWViewHarvester.deliverDirect(mp.view)
          }
         

          true
        case _ => false
      }
  }

    Harvest.updateAutoHarvesterOn(this)
  
}

object MavenProjectHarvester {

}