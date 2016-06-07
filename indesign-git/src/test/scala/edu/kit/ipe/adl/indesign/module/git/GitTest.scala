package edu.kit.ipe.adl.indesign.module.git

import edu.kit.ipe.adl.indesign.core.harvest.fs.HarvestedFile
import java.io.File
import org.eclipse.jgit.api.Git
import scala.collection.JavaConversions._ 

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
  s.getModified.foreach {
    c => 
      println("M: "+c)
  }
}