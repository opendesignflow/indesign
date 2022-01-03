package org.odfi.indesign.module.git

import org.odfi.indesign.core.harvest.fs.HarvestedFile

import java.io.File
import org.eclipse.jgit.api.Git
import org.odfi.indesign.module.git.GitRepository

import scala.jdk.CollectionConverters.CollectionHasAsScala;


object GitTest extends App {
  
  var gr = new GitRepository(new HarvestedFile(new File("../").toPath()));
  gr.@->("gathered",null)
  
  //-- Results
  println("Rep: "+gr.gitRepository)
  var rep = gr.gitRepository.get
  println("Bare: "+rep.isBare())
  
  
  var git = new Git(rep);
  var s = git.status().call()
  println(s"CLean: ${s.isClean()}")
  s.getModified.asScala.foreach {
    c => 
      println("M: "+c)
  }
}