package org.odfi.indesign.core.module.process

import java.io.File

import org.odfi.indesign.core.harvest.HarvestedResource
import scala.jdk.CollectionConverters._

class IDCommand (val startPath: File) extends HarvestedResource {

  def getId = startPath.getAbsolutePath

  def isValid = this.startPath.exists()

  // Start/Stop, Process output
  //------------

  def createToolProcess(args: Array[String],runFolder : File = new File("")) : IDProcess= {
    var cmd = List(startPath.getAbsolutePath) ++(args.toIterable)
    var pb = new ProcessBuilder(cmd.asJava)
    pb.directory(runFolder.getCanonicalFile)
    var tp = new IDProcess(pb)
    
    tp
  }
  
  def createToolProcess(args: String*): IDProcess = {
    
    var cmd = List(startPath.getCanonicalPath) ++ args.toList
    var pb = new ProcessBuilder(cmd.asJava)
    var tp = new IDProcess(pb)
    
    tp
  }

  /*var process: Option[Process] = None
  def runTool(args: String*) = process match {
    case None =>
      var cmd = List(startPath.getAbsolutePath) ++ args
      var pb = new ProcessBuilder(cmd)
      pb.inheritIO()
      try {
        process = Some(pb.start())

      } catch {
        case e: Throwable => e.printStackTrace()
      }

    case Some(process) => throw new RuntimeException("Tool Already Started")
  }

  def killTool = process match {
    case None =>
      throw new RuntimeException("Cannot kill non started tool")

    case Some(p) =>
      p.destroyForcibly()
      process = None
  }*/

}