package edu.kit.ipe.adl.indesign.core.artifactresolver

import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfterAll
import java.io.File
import org.scalatest.BeforeAndAfterAllConfigMap
import org.scalatest.ConfigMap
import com.idyria.osi.tea.file.DirectoryUtilities
import java.net.URL

class ArtifactResolverTest extends FunSuite with BeforeAndAfterAllConfigMap {
  
  val testRepositoryFolder = new File("target/test-repository")
  
 
  
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
    
    assertResult(None)(AetherResolver.getArtifactPath("com.idyria.osi", "superpom-scala", "2.11.7.r1","pom"))
    
    
  }
  
  test("Get Artifact with repository set") {
    
    AetherResolver.config.addDefaultRemoteRepository("idyria.central", new URL("http://www.idyria.com/access/osi/artifactory/libs-release"))
    
    var file = AetherResolver.getArtifactPath("com.idyria.osi", "superpom-scala", "2.11.7.r1","pom")
    assertResult(true)(file.isDefined)
    
    /*var file = AetherResolver.getArtifactPath("com.idyria.osi", "superpom-scala", "2.11.7.r1","pom")
    
    // Resolve Artifact
    var dependencies = AetherResolver.getDependencies("com.idyria.osi", "superpom-scala", "2.11.7.r1","pom")*/
    
    
    
  }
  
  test("Resolve compile dependencies") {
    
    
    var deps = AetherResolver.resolveDependencies("com.idyria.osi", "superpom-scala", "2.11.7.r1",classifier = "pom",scope="compile")
    
    deps.foreach {
      d => 
        
      
        println(s"D: "+d.getArtifact.getFile)
        
    }
    
  }
  
  
  
  
}