package org.odfi.indesign.module.lucene
/*
import java.io.File

import org.odfi.tea.logging.TLog

import org.odfi.indesign.core.brain.Brain
import org.odfi.indesign.core.harvest.Harvest
import org.odfi.indesign.core.harvest.Harvester
import org.odfi.indesign.core.harvest.fs.FileSystemHarvester
import org.odfi.indesign.core.module.eclipse.EclipseModule
import org.odfi.indesign.core.module.lucene.LuceneIndexableHarvester
import org.odfi.indesign.core.module.lucene.LuceneModule
import org.odfi.indesign.module.maven.MavenModule
import org.odfi.indesign.module.maven.MavenProjectHarvester
import org.odfi.indesign.module.scala.ScalaModule

object LuceneHarvestTry extends App {

  TLog.setLevel(classOf[Harvester], TLog.Level.FULL)
  
  
  Brain += (
    Harvest, MavenModule, ScalaModule, EclipseModule, LuceneModule)

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
  

}*/