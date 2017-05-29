package org.odfi.indesign.core.resources

import org.odfi.indesign.core.harvest.fs.HarvestedFile
import java.nio.file.Path
import org.odfi.indesign.core.harvest.HarvestedResource
import com.idyria.osi.tea.io.TeaIOUtils
import java.nio.file.Files

trait TextSourceResource extends HarvestedResource {

  /**
   * Retrieve Text Content
   */
  def getTextContent: String

  /**
   * Set the Text Content
   */
  def setTextContent(input: String)

  /**
   *
   */
  def flushTextContent: Unit

  // Lines Interface
  //-----------

  def getTextLines = getTextContent.split('\n')

}

trait RegexpTextSourceResource extends TextSourceResource {

  // Regexp Extraction
  //----------------
  var expressionsCache = scala.collection.mutable.HashMap[String,String]()
  
  def regexpExtractFirstGroupCached(expr:String) = {
    expressionsCache.get(expr) match {
      case Some(res) => Some(res)
      case None => 
        
        //-- Extract
        regexpExtract(expr) match {
          case None => 
            None
          case Some(matchRes) if(matchRes.size<=1) => 
            None
          case Some(matchRes) => 
            expressionsCache(expr) = matchRes(1)
            Some(matchRes(1))
        }
        
    }
  }
  
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
    var mappedRes = allmatches.map {
      res =>
        (0 to res.groupCount).map {
          i =>
            res.group(i)
        }.toList
    }.toList

    mappedRes.size match {
      case 0 =>
        None
      case other =>

        Some(mappedRes)

    }

  }
  
  /**
   * Calls regexpExtractAllList and returns an empty list if no results were provided
   */
  def regexpExtractAllList(str: String) = regexpExtractAll(str) match {
    case Some(r) => r
    case None => List()
  }
 
}

class FileTextSourceResource(p: Path) extends HarvestedFile(p) with TextSourceResource {

  // Get And Cache Content
  //--------

  //- Content Cache
  var textContentCache: java.lang.ref.WeakReference[String] = null
  var lastModification = 0L

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

  def setTextContent(input: String) = textContentCache match {

    //-- Something, clear and reset
    case tc if (tc != null && tc.get != null) =>
      lastModification = System.currentTimeMillis()
      tc.clear()
      textContentCache = new java.lang.ref.WeakReference[String](input)
      textContentCache.get
    //-- NOthing, then just set
    case lc =>
      lastModification = System.currentTimeMillis()
      textContentCache = new java.lang.ref.WeakReference[String](input)
      textContentCache.get

  }

  def flushTextContent = if (lastModification > 0) {

    try {
      TeaIOUtils.writeToFile(p.toFile(), this.getTextContent)

    } finally {
      lastModification = 0
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

class StringTextSourceResource(var content: String) extends TextSourceResource {

  def getId = getClass.getSimpleName + ":" + hashCode()

  def getTextContent: String = this.synchronized(content)

  def setTextContent(input: String) = this.synchronized {
    this.content = input
    input
  }

  /**
   * Not Doing anything
   */
  def flushTextContent = {

  }
}