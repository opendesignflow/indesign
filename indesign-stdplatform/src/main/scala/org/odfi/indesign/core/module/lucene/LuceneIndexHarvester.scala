package org.odfi.indesign.core.module.lucene

import org.odfi.indesign.core.harvest.Harvester
import org.odfi.indesign.core.harvest.HarvestedResource
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.index.IndexWriter
import java.io.File
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.store.Directory

class LuceneIndexHarvester extends Harvester {

  this.onDeliverFor[LuceneIndexResource] {
    case r => gather(r); true
  }

}

trait LuceneIndexProvider extends Harvester {

}

trait LuceneIndexResource extends HarvestedResource {

  // Prepare Lucene
  //-----------
  var luceneAnalyser: Option[StandardAnalyzer] = None

  // Store the index in memory:
  //var directory = new RAMDirectory();
  // To store an index on disk, use this instead:
  //new File("testindex").mkdirs
  //DirectoryUtilities.deleteDirectoryContent(new File("testindex"))
  //var directory = FSDirectory.open(new File("testindex").toPath);

  def getLuceneDirectory: File

  var luceneConfig: Option[IndexWriterConfig] = None
  var luceneWriter: Option[IndexWriter] = None
  var luceneDirectoy: Option[Directory] = None
  this.onAdded {
    
    case h if (h.isInstanceOf[LuceneIndexProvider]) =>

      logInfo[LuceneIndexResource]("Lucene Index Gathered in harvester, creating repository")
      
      getLuceneDirectory.mkdirs
      /*luceneAnalyser = Some(new StandardAnalyzer())
      luceneDirectoy = Some(FSDirectory.open(getLuceneDirectory.toPath))
      luceneConfig = Some(new IndexWriterConfig(luceneAnalyser.get))
      luceneWriter = Some(new IndexWriter(luceneDirectoy.get, luceneConfig.get))*/

  }

}