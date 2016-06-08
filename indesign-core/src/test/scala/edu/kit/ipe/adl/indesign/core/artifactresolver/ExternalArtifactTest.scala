package edu.kit.ipe.adl.indesign.core.artifactresolver

import org.scalatest.FunSuite
import edu.kit.ipe.adl.indesign.core.brain.artifact.ArtifactExternalRegion

class ExternalArtifactTest extends FunSuite {
  
  
  test("Discover Test") {
    
    //edu.kit.ipe.adl.indesign:indesign-maven:0.0.1-SNAPSHOT
    var external = new ArtifactExternalRegion("edu.kit.ipe.adl.indesign","indesign-maven","0.0.1-SNAPSHOT")
    external.updateClassDomain
    external.discoverRegions.foreach {
      r => 
        println("Found: "+r)
    }
    
  }
}