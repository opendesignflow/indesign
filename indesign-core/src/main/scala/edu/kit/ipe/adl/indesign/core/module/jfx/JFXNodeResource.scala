package edu.kit.ipe.adl.indesign.core.module.jfx

import javafx.scene.Node
import edu.kit.ipe.adl.indesign.core.harvest.HarvestedResource

class JFXNodeResource(val node:Node) extends HarvestedResource {
  def getId = node.getId
}