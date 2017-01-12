package org.odfi.indesign.core.module.lucene

import org.odfi.indesign.core.harvest.Harvester

class LuceneIndexableHarvester extends Harvester {
  
  
  this.onDeliverFor[LuceneIndexable] {
    case indexable => 
      
      gather(new LuceneIndexed(indexable))
      true
  }
  
}



trait LuceneResourceProvider extends Harvester {
  
}