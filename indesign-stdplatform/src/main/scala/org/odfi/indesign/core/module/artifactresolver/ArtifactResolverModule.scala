package org.odfi.indesign.core.module.artifactresolver

import java.net.URL

import org.odfi.indesign.core.module.IndesignModule

object ArtifactResolverModule extends IndesignModule {

  this.onLoad {

    //-- Add Open design flow reps
    AetherResolver.config.addDefaultRemoteRepository("odfi.central", new URL("http://www.opendesignflow.org/maven/internal/"))
    AetherResolver.config.addDefaultRemoteRepository("odfi.snapshots", new URL("http://www.opendesignflow.org/maven/snapshots/"))
  }

}