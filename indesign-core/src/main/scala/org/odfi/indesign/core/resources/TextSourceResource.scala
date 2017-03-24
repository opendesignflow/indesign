package org.odfi.indesign.core.resources

import org.odfi.indesign.core.harvest.fs.HarvestedFile
import java.nio.file.Path
import org.odfi.indesign.core.harvest.HarvestedResource
import com.idyria.osi.tea.io.TeaIOUtils
import java.nio.file.Files

trait TextSourceResource extends HarvestedResource {

  def getTextContent: String

}

class FileTextSourceResource(p: Path) extends HarvestedFile(p) with TextSourceResource {

  // Get And Cache Content
  //--------

  //- Content Cache
  var textContentCache: java.lang.ref.WeakReference[String] = null

  def getTextContent: String = {
    path.toFile().isDirectory() match {
      case true =>
        ""
      case false =>
        textContentCache match {
          case tc if (tc != null && tc.get != null) =>
            tc.get
          case lc =>

            textContentCache = new java.lang.ref.WeakReference[String](new String(Files.readAllBytes(path)))
            textContentCache.get
        }
    }

  }
  /*var linesCache: java.lang.ref.WeakReference[List[String]] = null

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

  }*/

}

class StringTextSourceResource(val content: String) extends TextSourceResource {

  def getId = getClass.getSimpleName + ":" + hashCode()

  def getTextContent: String = content
}