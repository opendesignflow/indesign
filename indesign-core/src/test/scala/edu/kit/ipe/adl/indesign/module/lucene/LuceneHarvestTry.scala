package edu.kit.ipe.adl.indesign.module.lucene

import edu.kit.ipe.adl.indesign.core.brain.Brain
import edu.kit.ipe.adl.indesign.module.scala.ScalaModule
import edu.kit.ipe.adl.indesign.core.module.ui.www.IndesignWWWUIModule
import edu.kit.ipe.adl.indesign.core.module.eclipse.EclipseModule

import edu.kit.ipe.adl.indesign.module.maven.MavenModule
import edu.kit.ipe.adl.indesign.core.harvest.Harvest
import edu.kit.ipe.adl.indesign.core.module.lucene.LuceneModule
import edu.kit.ipe.adl.indesign.core.harvest.fs.FileSystemHarvester
import java.io.File
import edu.kit.ipe.adl.indesign.module.maven.MavenProjectHarvester
import edu.kit.ipe.adl.indesign.core.module.lucene.LuceneIndexableHarvester
import com.idyria.osi.tea.logging.TLog
import edu.kit.ipe.adl.indesign.core.harvest.Harvester
import edu.kit.ipe.adl.indesign.core.module.git.GitModule

object LuceneHarvestTry extends App {

  TLog.setLevel(classOf[Harvester], TLog.Level.FULL)
  
  
  Brain += (
    Harvest, MavenModule, ScalaModule, EclipseModule, LuceneModule,GitModule)

  Brain.init

  var fsh = new FileSystemHarvester

  fsh.addPath(new File("src/test/testFS").toPath())

  
  Harvest.addHarvester(fsh)
  fsh.addChildHarvester(new MavenProjectHarvester) 
  Harvest.run
  Harvest.printHarvesters
  
  Harvest.onHarvesters[LuceneIndexableHarvester] {
    case luceneHarvester =>  
      
      println(s"On Indexable Harvester: "+luceneHarvester.getResources.size)
      
  }
  

}