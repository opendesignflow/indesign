package edu.kit.ipe.adl.indesign.core.harvest.fs

import edu.kit.ipe.adl.indesign.core.harvest.HarvestedResource
import java.nio.file.Path
import java.nio.file.Files
import scala.collection.JavaConversions._
import scala.language.implicitConversions

class HarvestedFile(val path: Path) extends HarvestedResource {

  def getId = path.toAbsolutePath().toString()

  // Get And Cache Content
  //--------

  //- Content Cache
  var linesCache: java.lang.ref.WeakReference[List[String]] = null

  def getLines: List[String] = {
    path.toFile().isDirectory() match {
      case true =>
        List[String]()
      case false =>
        linesCache match {
          case lc if (lc != null && lc.get != null) =>
            linesCache.get
          case lc =>
            linesCache = new java.lang.ref.WeakReference[List[String]](Files.readAllLines(path).toList)
            linesCache.get
        }
    }

  }

}