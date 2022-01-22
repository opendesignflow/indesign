package org.odfi.indesign.core.module.artifactresolver


import org.scalatest.BeforeAndAfterAll

import java.io.File
import org.scalatest.BeforeAndAfterAllConfigMap
import org.scalatest.ConfigMap
import org.odfi.tea.file.DirectoryUtilities

import java.net.URL
import org.odfi.indesign.core.module.artifactresolver.AetherResolver
import org.scalatest.funsuite.AnyFunSuite

class ArtifactResolverTest extends AnyFunSuite with BeforeAndAfterAllConfigMap {
  
  val testRepositoryFolder = new File("target/test-repository")

  val resolveGroupId = "org.odfi"
  val resolveArtifactId = "superpom-scala"
  val resolveVersion = "2.13.1.r1"
 
  
  override def beforeAll(cm : ConfigMap) = {
    
    println(s"Before all")
    testRepositoryFolder.mkdirs()
    DirectoryUtilities.deleteDirectoryContent(testRepositoryFolder)
    
    //configMap : ConfigMap => 
    
    // Configure Resolver
    AetherResolver.config.setLocalRepositoryPath(testRepositoryFolder)
    AetherResolver.reinit
    
  }
  
  
  test("Get Artifact, no remote repository set") {
    
    assertResult(None)(AetherResolver.getArtifactPath(resolveGroupId, resolveArtifactId, resolveVersion,"pom"))
    
    
  }
  
  test("Get Artifact with repository set") {
    
    AetherResolver.config.addDefaultRemoteRepository("idyria.central", new URL("http://repo.opendesignflow.org/maven/repository/internal/"))
    
    var file = AetherResolver.getArtifactPath(resolveGroupId, resolveArtifactId, resolveVersion,"pom")
    assertResult(true)(file.isDefined)
    
  }
  
  test("Resolve compile dependencies") {
    
    
    var deps = AetherResolver.resolveDependencies(resolveGroupId, resolveArtifactId, resolveVersion,classifier = "pom",scope="compile")
    
    deps.foreach {
      d => 
        
      
        println(s"D: "+d.getArtifact.getFile)
        
    }
    
  }
  
  
  
  
  
}