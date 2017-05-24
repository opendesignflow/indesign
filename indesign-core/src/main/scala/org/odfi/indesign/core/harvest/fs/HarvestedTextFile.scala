package org.odfi.indesign.core.harvest.fs

import java.nio.file.Path
import java.nio.file.Files
import java.io.File

class HarvestedTextFile(path: Path) extends HarvestedFile(path) {

  // Content Cache
  //-----------------

  //- Content Cache
  var contentCache: java.lang.ref.WeakReference[String] = null

  def getTextContent: String = {
    path.toFile().isDirectory() match {
      case true =>
        ""
      case false =>
        contentCache match {
          case lc if (lc != null && lc.get != null) =>
            contentCache.get
          case lc =>

            // Read
            contentCache = new java.lang.ref.WeakReference[String](new String(Files.readAllBytes(path)))

            // Wqtch for changes
            FSGlobalWatch.watcher.onFileChange(this, path.toFile) {
              f =>
                println("File Changed, resetting")
                contentCache = null
            }

            contentCache.get
        }
    }

  }

  // Regexp Extraction
  //----------------

  /**
   * Returns the list containing matches. First item is the whole match
   */
  def regexpExtract(str: String): Option[List[String]] = {

    var r = str.r
    r.findFirstMatchIn(getTextContent) match {
      case Some(res) =>
        Some((0 to res.groupCount).map {
          i =>
            res.group(i)
        }.toList)
      case None => None
    }

  }

  /**
   * Returns the list containing the List of matches. First item is each list the whole match
   */
  def regexpExtractAll(str: String): Option[List[List[String]]] = {

    var r = str.r
    var allmatches = r.findAllMatchIn(getTextContent)
    allmatches.size match {
      case 0 => None
      case other =>

        var mappedRes = allmatches.map {
          res =>
            (0 to res.groupCount).map {
              i =>
                res.group(i)
            }.toList
        }.toList
        Some(mappedRes)

    }

  }

}

object HarvestedTextFile {
  
  def apply (f:File) = new HarvestedTextFile(f.toPath)
  def apply (f:Path) = new HarvestedTextFile(f)
}