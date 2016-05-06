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

class ScalaSourceFile(r: Path) extends HarvestedFile(r) with LuceneIndexable {

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

    }.filter(obj => obj != null && obj != "")
  }

  def getDefinedClasses: List[String] = {
    this.getLines.collect {

      case l if (l.trim.startsWith("class")) =>

        """\s*class ([\w\.]+)\s*.*""".r.findFirstMatchIn(l) match {
          case Some(m) =>
            m.group(1)
          case None => ""
        }

    }.filter(obj => obj != null && obj != "")
  }

  /**
   * Try to find a compiling project in the related sources and compile
   */
  def ensureCompiled = {

    getUpchainCompilingProject.compile(this)

  }

  def loadClass: Class[_] = {
    val loadClass = this.getDefinedPackage + "." + this.getDefinedClasses(0)

    //println(s"loading class: " + loadClass)

    var cl = getUpchainCompilingProject.classDomain.loadClass(loadClass)

    cl

  }

  def getUpchainCompilingProject = this.findUpchainResource[MavenProjectResource] match {
    case Some(mp: MavenProjectResource) =>

      mp

    case _ =>
      sys.error("Cannot ensure Scala Source File is compiled, no compiling project found in parent resources")
  }

  // Events
  //-----------------
  def onChange(cl: => Unit) = {

    getUpchainCompilingProject.watcher.onFileChange(r.toFile())(cl)

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

    var p = getUpchainCompilingProject
    p.withClassLoader(p.classDomain) {

      var i = p.classDomain.loadClass(runClass)
      println(s"Found class: " + i)
      println(s"Current cd" + p.classDomain)
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