package edu.kit.ipe.adl.indesign.fastbuild

import edu.kit.ipe.adl.indesign.core.harvest.HarvestedResource
import edu.kit.ipe.adl.indesign.core.harvest.fs.HarvestedFile
import java.io.File
import edu.kit.ipe.adl.indesign.core.harvest.fs.HarvestedTextFile
import edu.kit.ipe.adl.indesign.module.scala.ScalaSourceFile
import com.idyria.osi.tea.compile.IDCompiler
import com.idyria.osi.tea.compile.FileCompileError

class FBProject(bf: HarvestedFile) extends HarvestedFile(bf.path) {
  deriveFrom(bf)

  // Config File
  //-----------------
  var configFile = new HarvestedTextFile(new File(bf.path.toFile, "fbuild.txt").toPath)

  // Parameters
  //--------------
  def getProjectName = {
    configFile.regexpExtract("""(?m)\s*:name\s+set\s+\"?([^"]+)\"?\s*$""") match {
      case Some(res) => res(1)
      case None => path.toFile().getName
    }
  }

  def isAutoBuild = {
    configFile.regexpExtract("""(?m)\s*:autobuild\s+set\s+true\s*$""") match {
      case Some(res) => true
      case None => false
    }
  }

  // Sanity Check
  //-------------------

  // Build Path
  //-----------------

  // Compilation
  //-------------------
  var compiler: Option[IDCompiler] = None
  def getCompilationOutput = {
    configFile.regexpExtract("""(?m)\s*:compileOut\s+set\s+\"?([^"]+)\"?\s*$""") match {
      case Some(res) =>
        new File(res(1)).isAbsolute() match {
          case true => new File(res(1)).getCanonicalFile
          case false =>

            new File(path.toFile, res(1)).getCanonicalFile

        }
      case None =>

        new File(path.toFile(), "target/classes")

    }
  }

  def getCompiler = compiler.getOrElse {

    var c = new IDCompiler
    var of = getCompilationOutput
    of.mkdirs()
    c.addSourceOutputFolders(path.toFile.getCanonicalFile -> of)

    compiler = Some(c)

    c
  }

  def buildFull = {

    var allSources = this.getSubDerivedResources[ScalaSourceFile]
    var allSourcesFiles = allSources.map {
      res => res.path.toFile.getCanonicalFile
    }.distinct

    //println("Compiling files: "+allSources)

    // Clean All Errors
    allSources.foreach(sf => sf.resetErrorsOfType[FileCompileError])

    // Compile and reset errors
    getCompiler.compileFiles(allSourcesFiles.toSeq) match {
      case None =>
      case Some(err) =>

        // println("Error in file: "+err.file)
        // println(err.getLocalizedMessage)

        // Set Error on remaining
        allSources.find { f =>
          //println(s"Testing file for error add  ${f.hashCode()}: "+f.path.toFile.getCanonicalPath)
          f.path.toFile.getCanonicalPath == err.file
        } match {
          case Some(sourceFile) =>

            sourceFile.addError(err)
          case None =>
        }
    }

  }
}