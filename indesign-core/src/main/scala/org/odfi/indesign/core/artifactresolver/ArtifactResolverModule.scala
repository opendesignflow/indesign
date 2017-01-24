package org.odfi.indesign.core.artifactresolver

import org.odfi.indesign.core.brain.BrainRegion
import org.odfi.indesign.core.module.IndesignModule
import java.net.URL

object ArtifactResolverModule extends IndesignModule {

  this.onLoad {

    //-- Add Open design flow reps
    AetherResolver.config.addDefaultRemoteRepository("odfi.central", new URL("https://www.opendesignflow.org/maven/internal/"))
    AetherResolver.config.addDefaultRemoteRepository("odfi.snapshots", new URL("https://www.opendesignflow.org/maven/snapshots/"))
  }

}