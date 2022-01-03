package org.odfi.indesign.module.git

import org.odfi.indesign.core.harvest.HarvestedResource

import java.net.URI
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

import java.io.File
import scala.jdk.CollectionConverters.CollectionHasAsScala

class GitRemoteRepository(val uri:URI) extends HarvestedResource {
  
  def getId = "GIT:"+uri.toString()
  
  var gitRepository: Option[Repository] = None
  var git: Option[Git] = None;
  
  def cloneTo(f:File,branch:String) = {
    
    Git.cloneRepository()
      .setDirectory(f)
      .setBranch(branch)
      .setURI(uri.toString())
      .call()
    
  }
  
 def listBranchesNames = {
   
    Git.lsRemoteRepository()
    .setRemote(uri.toString())
    .setHeads(true)
    .setTags(false)
    .call().asScala.filter {
      r => r.getName.contains("/")
    }.map {
      r => r.getName.stripPrefix("refs/heads/")
    }.toList
   
 }
  
  def getJGitRepository = gitRepository match {
    case Some(r) => r 
    case None => 
      
     /* var builder = new FileRepositoryBuilder();
      builder.setGitDir(new File(path.toFile().getCanonicalFile, ".git"))
      builder.readEnvironment()

      //builder.s
      
      gitRepository = Some(builder.build())
      git = Some(new Git(gitRepository.get))*/
      
  }
  
  
}