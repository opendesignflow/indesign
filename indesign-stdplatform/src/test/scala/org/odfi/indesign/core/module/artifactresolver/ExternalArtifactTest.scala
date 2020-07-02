package org.odfi.indesign.core.module.artifactresolver.artifactresolver

import org.odfi.indesign.core.module.artifactresolver.ArtifactExternalRegion
import org.scalatest.FunSuite

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