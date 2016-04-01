package edu.kit.ipe.adl.indesign.module.measurement

import edu.kit.ipe.adl.indesign.core.harvest.HarvestedResource

trait Device extends HarvestedResource {
  
  
  def open
  def close
}