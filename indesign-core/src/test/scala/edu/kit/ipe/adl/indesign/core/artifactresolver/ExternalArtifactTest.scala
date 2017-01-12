package org.odfi.indesign.core.artifactresolver

import org.scalatest.FunSuite
import org.odfi.indesign.core.brain.artifact.ArtifactExternalRegion

class ExternalArtifactTest extends FunSuite {
  
  
  test("Discover Test") {
    
    //org.odfi.indesign:indesign-maven:0.0.1-SNAPSHOT
    var external = new ArtifactExternalRegion("org.odfi.indesign","indesign-maven","0.0.1-SNAPSHOT")
    external.updateClassDomain
    external.discoverRegions.foreach {
      r => 
        println("Found: "+r)
    }
    
  }
}