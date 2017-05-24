package org.odfi.indesign.module.git.gitlab

import org.odfi.indesign.core.harvest.Harvester
import org.odfi.indesign.core.harvest.HarvestedResource

class GitlabHarvester extends Harvester {
  
  
}

class GitlabProjectResource(model:GitlabTraitGitlabProject) extends HarvestedResource {
  
  def getId = model.id
  
  
  
}