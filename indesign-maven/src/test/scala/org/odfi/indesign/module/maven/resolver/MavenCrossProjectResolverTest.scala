package org.odfi.indesign.module.maven.resolver

import org.scalatest.FunSuite
import org.odfi.indesign.core.artifactresolver.ArtifactResolverModule
import org.odfi.indesign.core.artifactresolver.AetherResolver
import com.idyria.osi.tea.logging.TLog
import org.odfi.indesign.core.harvest.fs.FileSystemHarvester
import java.io.File
import org.odfi.indesign.core.harvest.Harvest
import org.odfi.indesign.module.maven.MavenModule
import org.odfi.indesign.core.brain.Brain


/**
 * FIXME
 */
class MavenCrossProjectResolverTest extends FunSuite {
  
  test("Cross Project Resolver") {
    
    // Set Resolver Workspace reader to MavenProjectReader
    //--------------
    TLog.setLevel(classOf[MavenProjectIndesignWorkspaceReader], TLog.Level.FULL)
    AetherResolver.session.setWorkspaceReader( MavenProjectIndesignWorkspaceReader)
    
    // Harvest Projects
    //------------
    var fsh = new FileSystemHarvester
    fsh.addPath(new File("src/test/testFS").toPath())
    Harvest.addHarvester(fsh)
    Brain.deliverDirect(MavenModule)
    Brain.moveToStart
    
    Harvest.run
    Harvest.printHarvesters
    
    // Resolve
    //----------------
    val groupId="org.odfi.indesign.module.maven.test"
    val artifactId = "maven-app-internaldep"
    val version ="0.0.1-SNAPSHOT"
    
   
    //-- Get Dependencies
    var res = AetherResolver.resolveArtifactAndDependencies(groupId, artifactId, version)
   // assertResult(5)(res.size)
    
    //-- Get CLasspath
    var cp = AetherResolver.resolveArtifactAndDependenciesClasspath(groupId, artifactId, version)
   // assertResult(5)(cp.size)
    
    
  }
  
}