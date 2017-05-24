package org.odfi.indesign.core.harvest.fs

import org.odfi.indesign.core.harvest.HarvestedResource
import java.nio.file.Path
import java.nio.file.Files
import scala.collection.JavaConversions._
import scala.language.implicitConversions
import java.io.File
import java.io.FileFilter
import scala.collection.convert.DecorateAsScala
import java.sql.Timestamp
import java.util.Formatter.DateTime
import java.util.Date
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

class HarvestedFile(val path: Path) extends HarvestedResource with DecorateAsScala {

  def getId = getClass.getCanonicalName + ":" + path.toAbsolutePath().toString()
  def getName = path.getFileName.toString()
  def getNameNoExtension = getName.replaceAll("""\..*$""", "")
  
  def isNamed(str:String) = path.getFileName.toString==str
  
  override def toString = getClass.getSimpleName.replace("$", "") + ": " + getId

  // Conversion
  //-----------------
  
  def newFileWithExtension(ext:String) = {
    
    var newname = getNameNoExtension+ext
    
    HarvestedFile(new File(path.toFile.getParentFile,newname))
    
  }
  
  // Utils
  //---------------

  def sameAs(r: HarvestedFile): Boolean = {
    r.path.toFile().getCanonicalPath == this.path.toFile().getCanonicalPath
  }

  def sameAs(r: Path): Boolean = {
    r.toFile().getCanonicalPath == this.path.toFile().getCanonicalPath
  }

  def isDirectory = path.toFile().isDirectory()

  /**
   * Same as get sub file
   */
  def hasSubFile(filePath: String*): Option[HarvestedFile] = {
    var f = new File(path.toFile, filePath.mkString(File.separator))
    f.exists() match {
      case true  => Some(HarvestedFile(f))
      case false => None
    }
  }

  def getSubFile(filePath: String*): Option[HarvestedFile] = {
    var f = new File(path.toFile, filePath.mkString(File.separator))
    f.exists() match {
      case true  => Some( HarvestedFile(f))
      case false => None
    }
   
  }

  
  
  /**
   * Creates Sub Folder
   */
  def createSubFolder(filePath: String*) = getSubDir(filePath.mkString(File.separator))

  /**
   * Returns sub dir
   * @parameter p path/In/This/Format
   */
  def getSubDir(p: String) = {
    var sub = new File(path.toFile, p).getCanonicalFile
    sub.mkdirs
    HarvestedFile(sub)
  }

  def mkdirs = this.path.toFile.mkdirs()

  def getExtension = path.toFile.getName.split("\\.").last

  // Loops
  //-----------
  def subFiles(filter: File => Boolean) = {
    this.path.toFile().listFiles(new FileFilter {
      def accept(f: File) = filter(f)
    }).map(HarvestedFile(_))
  }

  // Extraction from name
  //----------------
  def extractFromName(p: String) = {
    p.r.findFirstMatchIn(this.getName) match {
      case None                          => None
      case Some(m) if (m.groupCount >= 1) => Some(m.group(1))
      case Some(m)                       => Some(m.group(0))
    }
  }

  /**
   * Finds a numerica value of at least 10 digits and convert it to a DateTime
   */
  def extractTimeStampFromName = {

    extractFromName("[0-9]{10,}") match {
      case None => None
      case Some(tds) =>
    
        var dt = LocalDateTime.from(Instant.ofEpochMilli(tds.toLong).atZone(ZoneId.systemDefault()))

        Some(dt)
    }
  }

  // Check rights
  //---------

  def canWrite = {
    path.toFile().canWrite()
  }

  def canRead = {
    path.toFile().canRead()
  }

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

object HarvestedFile {

  def apply(f: File) = new HarvestedFile(f.toPath)
  def apply(f: Path) = new HarvestedFile(f)

  implicit def harvestedFileToJavaFile(hf: HarvestedFile) = hf.path.toFile().getCanonicalFile
  implicit def fileToHFile(f: File) = new HarvestedFile(f.getCanonicalFile.toPath())

}