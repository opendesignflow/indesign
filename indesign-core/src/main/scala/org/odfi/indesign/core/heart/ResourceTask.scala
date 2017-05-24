package org.odfi.indesign.core.heart

import org.odfi.indesign.core.harvest.HarvestedResource

abstract class ResourceTask(val tid : String,val resource : HarvestedResource) extends HeartTask[Any] {
  
  
  def getId = s"${resource.getId}:$tid"
}