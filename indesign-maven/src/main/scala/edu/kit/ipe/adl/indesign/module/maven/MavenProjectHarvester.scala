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
   
    //println(s"Running Do Harvest on Maven havester, resources: " + this.getResources)
    //println("Testing for double resources")
    /*this.getResources.groupBy { r => r.getId }.foreach {
      case (id,all) if (all.size>1) => 
        println("Found double: "+id)
      case _ => 
    }*/
    super.doHarvest
  }

  /**
   * If the Fiel is a folder, and has a POM,xml in it -> go
   */
  this.onDeliverFor[HarvestedFile] {
    case r if (r.path.toFile().getName == "pom.xml") =>
    

      logFine(s"Delivered Maven Project ")
      var mp = new MavenProjectResource(r.path.toFile.getParentFile.getCanonicalFile.toPath)
      gather(mp.deriveFrom(r))
      mp.onGathered {
        case h if (h == this) =>
         // WWWViewHarvester.deliverDirect(mp.view)
      }
      true
    case r if (new File(r.path.toFile(), "pom.xml").exists()) =>

      var pomFile = new File(r.path.toFile(), "pom.xml")

      logFine(s"Delivered Maven Project ")
      var mp = new MavenProjectResource(r.path)
      gather(mp.deriveFrom(r))
      mp.onGathered {
        case h if (h == this) =>
         /// WWWViewHarvester.deliverDirect(mp.view)
      }

      true
    case _ => false

  }

  //Harvest.updateAutoHarvesterOn(this)

}

object MavenProjectHarvester {

}