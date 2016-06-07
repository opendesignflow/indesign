package edu.kit.ipe.adl.indesign.core.module.eclipse

import java.nio.file.Path

import scala.sys.process._

import com.idyria.osi.tea.os.OSDetector

import edu.kit.ipe.adl.indesign.core.harvest.Harvest
import edu.kit.ipe.adl.indesign.core.harvest.Harvester
import edu.kit.ipe.adl.indesign.core.harvest.fs.FileSystemHarvester
import edu.kit.ipe.adl.indesign.core.harvest.fs.HarvestedFile
import edu.kit.ipe.adl.indesign.core.module.IndesignModule


object EclipseModule extends IndesignModule {

  def load = {

    Harvest.registerAutoHarvesterClass(classOf[FileSystemHarvester], classOf[EclipseWorkspaceHarvester])

  }

}

class EclipseWorkspaceHarvester extends Harvester {

  this.onDeliverFor[HarvestedFile] {
    case folder if (folder.isDirectory && folder.hasSubFile(".metadata", "version.ini").isDefined) =>

      // Got Workspace
      var ws = new EclipseWorkspaceFolder(folder.path)
      ws.deriveFrom(folder)
      this.gather(ws)

      println(s"****(Eclipse) Eclipse workspace " + folder)

      ws.onGathered {
        case h if (h==this) => 
        // Add to aether resolver if opened
        // The lock file must be found by lsof
        ws.hasSubFile(".metadata", ".lock") match {

          case Some(lockFile) =>

            println(s"****(Eclipse) Eclipse workspace is open, check lock: " + lockFile)
            OSDetector.getOS match {
              case OSDetector.OS.LINUX =>
                var p = Process(Seq("lsof", "-l", "+D", lockFile.getParentFile.getAbsolutePath))
                p.lineStream_!.find(l => l.contains(lockFile.getAbsolutePath)) match {
                  case Some(line) =>
                    println(s"OK; using")
                    //AetherResolver.session.setWorkspaceReader(new EclipseWorkspaceReader(ws.path.toFile()))
                  case None =>
                }
              case _ =>
            }
          case None =>
        }
      }

      true
  }

}

class EclipseWorkspaceFolder(p: Path) extends HarvestedFile(p) {

}