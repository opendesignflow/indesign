package edu.kit.ipe.adl.indesign.module.lucene

import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.store.RAMDirectory
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.document.Document
import org.apache.lucene.document.TextField
import org.apache.lucene.document.Field
import org.apache.lucene.queryparser.classic.QueryParser
import java.util.jar.JarFile
import java.util.jar.JarInputStream
import com.idyria.osi.tea.compile.ClassDomain
import com.idyria.osi.tea.timing.TimingSupport
import org.apache.lucene.store.FSDirectory
import java.io.File
import com.idyria.osi.tea.file.DirectoryUtilities
import edu.kit.ipe.adl.indesign.core.harvest.fs.FileSystemHarvester
import edu.kit.ipe.adl.indesign.core.harvest.Harvester
import edu.kit.ipe.adl.indesign.core.harvest.fs.HarvestedFile

object LuceneTry extends App with TimingSupport {

  val searchRepeat = 2

  // Prepare Lucene
  //-----------
  var analyzer = new StandardAnalyzer();

  // Store the index in memory:
  //var directory = new RAMDirectory();
  // To store an index on disk, use this instead:
  new File("testindex").mkdirs
  DirectoryUtilities.deleteDirectoryContent(new File("testindex"))
  var directory = FSDirectory.open(new File("testindex").toPath);
  var config = new IndexWriterConfig(analyzer);
  var iwriter = new IndexWriter(directory, config);

  // Get Jar File 
  //------------------

  // Prepare loading domain
  //---------------
 

  var jars = List("tea-3.1.2-SNAPSHOT.jar", "rt.jar")

  val keywords = List("javadoc","sources")
  
  var fsh = new FileSystemHarvester
  fsh.addPath(new File("""C:\Users\leysr_000\.m2\repository""").toPath)
  var h = fsh.addChildHarvester(new Harvester {
    
    this.onDeliverFor[HarvestedFile] {
      case f if(f.path.toFile.isFile() &&  f.path.toString.endsWith(".jar") && keywords.find(kw => f.path.toString.contains(kw)).isEmpty) => 
       
       // println(s"Found jar: $f")
        gather(f)
        true
        
    }
  })
  fsh.harvest
  h.finishGather
  
  println(s"Total of "+h.getResources.size)
  

  h.onResources[HarvestedFile] {
    jarResource =>
      println(s"Doing jar: $jarResource")
      
      /*var jar = getClass.getClassLoader.getResource(jarName)
      var jarFile = new JarInputStream(jar.openStream())*/
      
      var jar = jarResource.path.toFile.toURI.toURL
      var jarFile = new JarInputStream(jar.openStream())
      
       var classDomain = new ClassDomain
      classDomain.addURL(jar)
      
      var stop = false 
      while (!stop) {
        jarFile.getNextEntry match {
          case null => stop = true
          case entry if (!entry.getName.contains("$") && entry.getName.matches(".+[a-zA-Z]+\\.class")) =>

            var className = entry.getName.replace(".class", "").replace("/", ".")
            
            println(s"-> ${entry.getName} -> Class: " + className)

            var loadedClass = classDomain.loadClass(className)

            // Create A document for each method
            loadedClass.getDeclaredMethods.foreach {
              m =>

                println(s"-> Method: " + m.getName)
                
                var doc = new Document
                doc.add(new Field("type", "method", TextField.TYPE_STORED))
                doc.add(new Field("java.method.name", m.getName, TextField.TYPE_STORED))
                doc.add(new Field("java.method.cname", loadedClass.getCanonicalName + "." + m.getName, TextField.TYPE_STORED))
                iwriter.addDocument(doc);
            }

          case entry =>
          // println(s"-> Entry: " + entry.getName)
        }
      }
  }
  iwriter.close

  // Find
  var methodSearch = "/.*fork.*/"
  var ireader = DirectoryReader.open(directory);
  var isearcher = new IndexSearcher(ireader);
  // Parse a simple query that searches for "text":
  var parser = new QueryParser("java.method.name", analyzer);
  var query = parser.parse(methodSearch);

  (0 until searchRepeat).foreach {
    i =>
      var t = time {

        var hits = isearcher.search(query, 1000)

        hits.scoreDocs.foreach {
          hitDoc =>

            var doc = ireader.document(hitDoc.doc)

            println(s"Found method: " + doc.get("java.method.cname"))

        }
      }
      println(s"Search and report took: " + t + ", size of index was " + ireader.numDocs())
  }
  sys.exit

  /* var doc = new Document();
  var text = "This is the text to be indexed.";
  var text2 = "This is the text 2 to be indexed.";
  var text3 = "This is the nt 2 to be indexed.";
  doc.add(new Field("fieldname", text, TextField.TYPE_STORED));
  doc.add(new Field("fieldname", text2, TextField.TYPE_STORED));
  doc.add(new Field("fieldname", text3, TextField.TYPE_STORED));
  iwriter.addDocument(doc);
  iwriter.close();

  // Now search the index:
  var ireader = DirectoryReader.open(directory);
  var isearcher = new IndexSearcher(ireader);
  // Parse a simple query that searches for "text":
  var parser = new QueryParser("fieldname", analyzer);
  var query = parser.parse("text");

  var hits = isearcher.search(query, 1000, org.apache.lucene.search.Sort.RELEVANCE);

  //assertEquals(1, hits.length);

  //

  // Iterate through the results:

  hits.fields.toList.foreach {
    sortField =>
      println(" Got field: " + sortField.toString())

    //var doc = isearcher.doc(hitdoc.doc) 
    // println(s"Found: "+doc.getFields("fieldname").toList)
  }

  ireader.close();
  directory.close();*/

}