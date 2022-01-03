package org.odfi.indesign.core.config.model

import org.odfi.ooxoo.model.ModelBuilder
import org.odfi.ooxoo.model.producer
import org.odfi.ooxoo.model.producers
import org.odfi.ooxoo.model.out.markdown.MDProducer
import org.odfi.ooxoo.model.out.scala.ScalaProducer
import org.odfi.ooxoo.core.buffers.structural.io.sax.STAXSyncTrait

@producers(Array(
  new producer(value = classOf[ScalaProducer]),
  new producer(value = classOf[MDProducer])))
class ConfigModel extends ModelBuilder {

  // Common COnfig
  //----------------------
  val commonConfig = "CommonConfig" is {
    elementsStack.head.makeTraitAndUseCustomImplementation
    withTrait(classOf[STAXSyncTrait])

    // Configuration Support
    //--------------
    "SupportedConfig" is {

      "ConfigValue" multiple {
        makeTraitAndUseCustomImplementation
        attribute("name")
        attribute("keyType")
        attribute("hint")
        attribute("default")
        attribute("softwareSet") ofType("boolean") default("false")
        "Description" ofType "string"

        "Parameter" multiple {
          attribute("name")
          ofType("string")
        }
      }

    }

    // Values
    //------------
    "Values" is {
      "Key" multiple {
        attribute("name")
        attribute("keyType") default "string"
        "Value" multiple {
          attribute("hint")
          ofType("string")
        }
      }
    }

    // Other Content
    //-----------
    "Custom" is {
      any
    }

  }
  // Module Config
  //-------------------
  "RegionConfig" is {
    withTrait(commonConfig)
  }

  // Harvester Config
  //-------------------
  "HarvesterConfig" is {
    withTrait(commonConfig)
  }

  // Default
  //--------------
  "DefaultConfig" is {
    withTrait(commonConfig)
  }

}