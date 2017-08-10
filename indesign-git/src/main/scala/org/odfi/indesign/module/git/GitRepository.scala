package org.odfi.indesign.module.git

import org.odfi.indesign.core.harvest.HarvestedResource
import org.odfi.indesign.core.harvest.fs.HarvestedFile
import java.nio.file.Path
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import java.io.File
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.api.Git

class GitRepository(f: HarvestedFile) extends HarvestedFile(f.path) {
  deriveFrom(f)
  
  // JGit Interface
  var gitRepository: Option[Repository] = None
  var git: Option[Git] = None;
  
  
  this.onGathered {
    case h =>
      println("Gathering")
      var builder = new FileRepositoryBuilder();
      builder.setGitDir(new File(path.toFile().getCanonicalFile, ".git"))
      builder.readEnvironment()

      gitRepository = Some(builder.build())
      git = Some(new Git(gitRepository.get))
  }

  def getGitOrNone[T](cl: Git => T) = {
    git match {
      case None => None
      case Some(g) => Some(cl(g))
    }
  }
  
  def getCurrentBranch = getGitOrNone(_.getRepository.getBranch)
}