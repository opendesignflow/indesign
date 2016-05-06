package edu.kit.ipe.adl.indesign.module.scala

import edu.kit.ipe.adl.indesign.core.harvest.Harvester
import edu.kit.ipe.adl.indesign.core.harvest.fs.HarvestedFile
import edu.kit.ipe.adl.indesign.module.maven.MavenProjectHarvester
import edu.kit.ipe.adl.indesign.core.module.lucene.LuceneResourceProvider

class ScalaProjectHarvester extends Harvester with LuceneResourceProvider {

  this.addChildHarvester(new ScalaAppHarvester)
  
 

  this.onDeliverFor[HarvestedFile] {

    case r =>

    r.path.toString.endsWith(".scala") match {
      case true =>

        gather(new ScalaSourceFile(r.path).deriveFrom(r))
        true
      case _ =>
        false
    }
  }

}