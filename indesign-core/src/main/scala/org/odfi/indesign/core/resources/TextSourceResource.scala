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
  def flushTextContent : Unit
  
  // Lines Interface
  //-----------
  
  def getTextLines  =  getTextContent.split('\n')
  

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

  def setTextContent(input:String) = textContentCache match {
    
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
  
  
  def flushTextContent = if(lastModification>0) {
    
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
  
  def setTextContent(input:String) = this.synchronized {
    this.content = input
    input
  }
  
  /**
   * Not Doing anything
   */
  def flushTextContent = {
    
  }
}