package org.odfi.indesign.core.module.lucene

import org.odfi.indesign.core.harvest.HarvestedResource
import org.apache.lucene.document.Document

trait LuceneIndexable extends HarvestedResource {
  
  def toLuceneDocuments : List[Document]
  
}

class LuceneIndexed(val indexable : LuceneIndexable) extends HarvestedResource {
  
  def  getId = indexable.getId
  this.deriveFrom(indexable)
  
  
}