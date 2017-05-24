package org.odfi.indesign.core.module.jfx

import javafx.scene.Node
import org.odfi.indesign.core.harvest.HarvestedResource

class JFXNodeResource(val node:Node) extends HarvestedResource {
  def getId = node.getId
}