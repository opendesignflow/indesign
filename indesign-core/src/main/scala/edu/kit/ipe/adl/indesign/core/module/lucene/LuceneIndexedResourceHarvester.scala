package edu.kit.ipe.adl.indesign.core.module.lucene

import edu.kit.ipe.adl.indesign.core.harvest.Harvester

class LuceneIndexableHarvester extends Harvester {
  
  
  this.onDeliverFor[LuceneIndexable] {
    case indexable => 
      
      gather(new LuceneIndexed(indexable))
      true
  }
  
}



trait LuceneResourceProvider extends Harvester {
  
}