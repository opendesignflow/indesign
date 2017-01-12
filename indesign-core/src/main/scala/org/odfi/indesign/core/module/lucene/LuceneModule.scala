package org.odfi.indesign.core.module.lucene

import org.odfi.indesign.core.module.IndesignModule
import org.odfi.indesign.core.harvest.Harvest

object LuceneModule extends IndesignModule {
  
  
  def load = {
    
    // Add Resource Harvester to all indexed re
    Harvest.registerAutoHarvesterClass(classOf[LuceneResourceProvider], classOf[LuceneIndexableHarvester])
    Harvest.registerAutoHarvesterClass(classOf[LuceneIndexProvider], classOf[LuceneIndexHarvester])
    //Harvest.registerAutoHarvesterObject(classOf[ScalaProjectHarvester], LuceneIndexableHarvester)
  }
  
  
}