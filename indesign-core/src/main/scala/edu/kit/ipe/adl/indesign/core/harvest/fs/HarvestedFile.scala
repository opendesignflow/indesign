package edu.kit.ipe.adl.indesign.core.harvest.fs

import edu.kit.ipe.adl.indesign.core.harvest.HarvestedResource
import java.nio.file.Path
import java.nio.file.Files
import scala.collection.JavaConversions._
import scala.language.implicitConversions
import java.io.File

class HarvestedFile(val path: Path) extends HarvestedResource {

  def getId = getClass.getCanonicalName+":"+path.toAbsolutePath().toString()
  def getName = path.getFileName.toString()
  
  override def toString = getClass.getSimpleName.replace("$", "") + ": " + getId

  // Utils
  //---------------

  def isDirectory = path.toFile().isDirectory()

  def hasSubFile(filePath: String*): Option[File] = {
    var f = new File(path.toFile, filePath.mkString(File.separator))
    f.exists() match {
      case true => Some(f)
      case false => None
    }
  }
  
  def getExtension = path.toFile.getName.split("\\.").last
  
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
  
  def apply (f:File) = new HarvestedFile(f.toPath)
  def apply (f:Path) = new HarvestedFile(f)
  
  implicit def harvestedFileToJavaFile(hf:HarvestedFile) = hf.path.toFile().getCanonicalFile
  
}