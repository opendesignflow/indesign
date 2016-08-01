package edu.kit.ipe.adl.indesign.module.scala

import edu.kit.ipe.adl.indesign.core.harvest.HarvestedResource
import edu.kit.ipe.adl.indesign.core.harvest.fs.HarvestedFile
import java.nio.file.Path
import edu.kit.ipe.adl.indesign.module.maven.MavenProjectResource
import java.net.URLClassLoader
import edu.kit.ipe.adl.indesign.core.module.lucene.LuceneIndexable
import org.apache.lucene.document.Document
import org.apache.lucene.document.TextField
import org.apache.lucene.document.Field
import edu.kit.ipe.adl.indesign.core.module.buildsystem.JavaSourceFile
import edu.kit.ipe.adl.indesign.core.harvest.fs.HarvestedTextFile

class ScalaSourceFile(r: Path) extends HarvestedTextFile(r) with LuceneIndexable with JavaSourceFile {

  
  /*
   * Returns the first class or first object
   */
  def getMainType = {
    this.getDefinedClasses match {
      case cl if (cl.size > 0) =>
        cl.headOption
      case _ =>
        this.getDefinedObjects match {
          case ol if (ol.size > 0) =>
            ol.headOption
          case _ => None
        }

    }
  }

  def getDefinedPackage: String = {

    this.getLines.collectFirst {

      case l if (l.trim.startsWith("package")) =>

        """\s*package ([\w\.]+)""".r.findFirstMatchIn(l) match {
          case Some(m) =>
            m.group(1)
          case None => ""
        }

    } match {
      case Some(l) => l
      case None => ""
    }

  }

  def getDefinedObjects: List[String] = {
    this.getLines.collect {

      case l if (l.trim.startsWith("object")) =>

        """\s*object ([\w\.]+)\s*.*""".r.findFirstMatchIn(l) match {
          case Some(m) =>
            m.group(1)
          case None => ""
        }

    }.filter(obj => obj != null && obj != "").map {obj => this.getDefinedPackage+"."+obj+"$"}
  }

  def getDefinedClasses: List[String] = {
    this.getLines.collect {

      case l if (l.trim.startsWith("class")) =>

        """\s*class ([\w\.]+)\s*.*""".r.findFirstMatchIn(l) match {
          case Some(m) =>
            m.group(1)
          case None => ""
        }

    }.filter(cl => cl != null && cl != "").map {cl => this.getDefinedPackage+"."+cl}
  }

  /**
   * Try to find a compiling project in the related sources and compile
   */
  def ensureCompiled = {

    getUpchainCompilingProject match {
      case Some(project) => 
        project.compile(this)
      case None => 
        sys.error("Cannot ensure Scala Source File is compiled, no compiling project found in parent resources")
    }

  }

  def loadClass: Class[_] = {
    val loadClass = this.getDefinedPackage + "." + this.getDefinedClasses(0)

    //println(s"loading class: " + loadClass)

  getUpchainCompilingProject match {
      case Some(project) => project.classdomain.get.loadClass(loadClass)
      case None => 
        sys.error(s"Cannot load class $loadClass for $this, no compiling project found in parent resources")
    }

    

  }

  def getUpchainCompilingProject = this.findUpchainResource[MavenProjectResource]

  // Events
  //-----------------
  override def onChange(cl: => Unit): Unit = {

    //getUpchainCompilingProject.watcher.onFileChange(r.toFile())(cl)

  }

  // Indexsing
  //---------------
  def toLuceneDocuments = {

    // Basic Document
    var doc = new Document
    doc.add(new Field("realm", "scala", TextField.TYPE_STORED))
    doc.add(new Field("type", "file", TextField.TYPE_STORED))

    var res = List(doc)

    try {
      var loadedClass = this.loadClass
      // Create A document for each method
      loadedClass.getDeclaredMethods.foreach {
        m =>

          println(s"-> Method: " + m.getName)

          var doc = new Document
          doc.add(new Field("type", "method", TextField.TYPE_STORED))
          doc.add(new Field("scala.method.name", m.getName, TextField.TYPE_STORED))
          doc.add(new Field("scala.method.cname", loadedClass.getCanonicalName + "." + m.getName, TextField.TYPE_STORED))
          res = res :+ doc
        //iwriter.addDocument(doc);
      }
    } catch {
      case e: Throwable =>
    }
    /* doc.add(new Field("path", this.path.toFile.getAbsolutePath, TextField.TYPE_STORED))
    doc.add(new Field("java.method.name", m.getName, TextField.TYPE_STORED))
    doc.add(new Field("java.method.cname", loadedClass.getCanonicalName + "." + m.getName, TextField.TYPE_STORED))*/

    res
  }

}

class ScalaAppSourceFile(r: Path) extends ScalaSourceFile(r) {

  def run = {

    val runClass = this.getDefinedPackage + "." + this.getDefinedObjects(0)

    println(s"Running class: " + runClass)

    var p = getUpchainCompilingProject.get
    p.compile(this)
    p.withClassLoader(p.classdomain.get) {

      var i = p.classdomain.get.loadClass(runClass)
      println(s"Found class: " + i)
      println(s"Current cd" + p.classdomain.get)
      println(s"Current Thread: " + Thread.currentThread().getContextClassLoader)
      println(s"Class CL: " + i.getClassLoader)
      i.getClassLoader match {
        case u: URLClassLoader =>
          u.getURLs.foreach {
            u =>
              println(s"---> CL " + u.toExternalForm())
          }

      }

      var main = i.getMethod("main", classOf[Array[String]])

      main.invoke(null, Array[String]())

      ""
    }

  }

}