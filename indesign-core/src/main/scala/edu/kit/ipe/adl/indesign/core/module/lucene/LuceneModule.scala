package edu.kit.ipe.adl.indesign.core.module.lucene

import edu.kit.ipe.adl.indesign.core.module.IndesignModule
import edu.kit.ipe.adl.indesign.core.harvest.Harvest
import edu.kit.ipe.adl.indesign.module.scala.ScalaProjectHarvester

object LuceneModule extends IndesignModule {
  
  
  def load = {
    
    // Add Resource Harvester to all indexed re
    Harvest.registerAutoHarvesterClass(classOf[LuceneResourceProvider], classOf[LuceneIndexableHarvester])
    Harvest.registerAutoHarvesterClass(classOf[LuceneIndexProvider], classOf[LuceneIndexHarvester])
    //Harvest.registerAutoHarvesterObject(classOf[ScalaProjectHarvester], LuceneIndexableHarvester)
  }
  
  
}