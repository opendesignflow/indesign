package org.odfi.indesign.module.maven.region

import java.io.File

import org.odfi.indesign.module.maven.region.MavenExternalBrainRegionBuilder;
import org.scalatest.FunSuite
import org.scalatest.GivenWhenThen

import com.idyria.osi.tea.logging.TLog

import org.odfi.indesign.core.artifactresolver.AetherResolver
import org.odfi.indesign.core.brain.Brain
import org.odfi.indesign.core.harvest.fs.HarvestedFile
import org.odfi.indesign.module.maven.resolver.MavenProjectIndesignWorkspaceReader
import org.odfi.indesign.core.brain.artifact.ArtifactRegion

class MavenRegionRuntimeClassdomainTest extends FunSuite with GivenWhenThen {

  test("Dependent Region have parent ClassDomain correctly set") {

    TLog.setLevel(classOf[MavenProjectIndesignWorkspaceReader], TLog.Level.FULL)
    TLog.setLevel(classOf[MavenExternalBrainRegion], TLog.Level.FULL)

    //---------------------------
    // Create External Regions
    //---------------------------
    var builder = new MavenExternalBrainRegionBuilder
    AetherResolver.session.setWorkspaceReader(MavenProjectIndesignWorkspaceReader)
    var regionA = builder.build(new File("src/test/testFS/maven-app-dep").toURI().toURL).asInstanceOf[MavenExternalBrainRegion]
    var regionB = builder.build(new File("src/test/testFS/maven-app-internaldep").toURI().toURL).asInstanceOf[MavenExternalBrainRegion]

    //---------------------------
    // Add to Brain
    //---------------------------
    Brain.deliverDirect(regionB)
    Brain.deliverDirect(regionA)

    //---------------------------
    // Add Maven resolver
    //---------------------------
    When("The Workspace reader is set")

    //---------------

    // Check Dependencies
    //-------------------------
    regionA.forceUpdateDependencies
    //regionB.forceUpdateDependencies
    var depsA = regionA.getDependencies
    var depsB = regionB.getDependencies
    println(depsA)
    println(depsB)
    regionB.getDependenciesURL.foreach {
      d =>
        println("D: " + d.getFile)
    }

    // Move to init; after that, classdomains should be parents of each other
    //-------------------
    Brain.moveToInit

  }

  test("Two Dependent Regions runtime Module have correct dependend class loading") {

    TLog.setLevel(classOf[MavenProjectIndesignWorkspaceReader], TLog.Level.FULL)
    TLog.setLevel(classOf[MavenExternalBrainRegion], TLog.Level.FULL)

    //---------------------------
    // Create External Regions
    //---------------------------
    var builder = new MavenExternalBrainRegionBuilder
    AetherResolver.session.setWorkspaceReader(MavenProjectIndesignWorkspaceReader)

    var regionA = builder.build(new File("src/test/testFS/maven-app-dep").toURI().toURL).asInstanceOf[MavenExternalBrainRegion]
    var regionB = builder.build(new File("src/test/testFS/maven-app-internaldep").toURI().toURL).asInstanceOf[MavenExternalBrainRegion]

    //---------------------------
    // Add to Brain
    //---------------------------
    Brain.deliverDirect(regionB)
    Brain.deliverDirect(regionA)

    //-----------------------------
    // Compile
    //-----------------------------
    regionA.compile(new HarvestedFile(new File("src/test/testFS/maven-app-dep/src/main/scala/test/AppDepModule.scala").toPath()))
    regionB.compile(new HarvestedFile(new File("src/test/testFS/maven-app-internaldep/src/main/scala/test/InternalAppDepModule.scala").toPath()))

    // Move to init; after that, classdomains should be parents of each other
    //-------------------
    Brain.moveToInit

    //-----------------------------
    // Load B Actual Module, should call to same instance of A
    //-----------------------------
    regionB.addRegionClass("appInternalDep.test.InternalAppDepModule$")
    regionA.addRegionClass("appDep.test.AppDepModule$")

  }

  test("One parent region, on child, and another needs the two previous") {

    TLog.setLevel(classOf[MavenProjectIndesignWorkspaceReader], TLog.Level.FULL)
    TLog.setLevel(classOf[MavenExternalBrainRegion], TLog.Level.FULL)
    TLog.setLevel(classOf[ArtifactRegion], TLog.Level.FULL)
    
     //------------------------------
    // Create REgions
    //------------------------------
    var builder = new MavenExternalBrainRegionBuilder
    AetherResolver.session.setWorkspaceReader(MavenProjectIndesignWorkspaceReader)
    
    var regions = List("maven-app-needstwo", "maven-app-dep", "maven-app-internaldep").map {
      regionName =>
        builder.build(new File(s"src/test/testFS/$regionName").toURI().toURL).asInstanceOf[MavenExternalBrainRegion]
    }
    
     //------------------------------
     // Add to brain
     //------------------------------
    regions.foreach {
      r => 
        Brain.deliverDirect(r)
    }
    
    // Move to init; after that, classdomains should be parents of each other
    //-------------------
    Brain.moveToInit
    
    
    

  }
}