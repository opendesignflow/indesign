package org.odfi.indesign.core.tests

import scala.sys.process._
import org.odfi.tea.logging.TLog

import java.io.File

object Sandbox extends App {
  
  
  /*var p = Process(Seq("lsof","-l","+D","/home/rleys/eclipse-workspaces/mars/.metadata/"))
  var res = p.lineStream_!
  var foundlock = res.find {
    line => line.contains("/home/rleys/eclipse-workspaces/mars/.metadata/.lock")
  }
  println(foundlock)*/
  
  
  /*
  
  AetherResolver.session.setWorkspaceReader(new EclipseWorkspaceReader(new File("/home/rleys/eclipse-workspaces/mars/")))
  TLog.setLevel(classOf[EclipseWorkspaceReader], TLog.Level.FULL)
  
  AetherResolver.resolveArtifact("kit.edu.ipe.adl", "chiptest-lib", "0.0.1-SNAPSHOT") match {
    case Some(art) => 
      println(s"Found "-> art.getFile)
      AetherResolver.resolveArtifactAndDependenciesClasspath(art,"compile").foreach {
        d => 
          println(s"D: $d -> "+d.getFile)
      }
    case None => 
  }*/
  
  /*var res = Seq("lsof","-l","+D","/home/rleys/eclipse-workspaces/mars/.metadata/").!!
  */
  
  
  
  //"""lsof -c java -C +D /home/rleys/eclipse-workspaces/mars/.metadata/"""
  
}