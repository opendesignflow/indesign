package org.odfi.indesign.core.config.model

import com.idyria.osi.ooxoo.model.ModelBuilder
import com.idyria.osi.ooxoo.model.producer
import com.idyria.osi.ooxoo.model.producers
import com.idyria.osi.ooxoo.model.out.markdown.MDProducer
import com.idyria.osi.ooxoo.model.out.scala.ScalaProducer
import com.idyria.osi.ooxoo.core.buffers.structural.io.sax.STAXSyncTrait

@producers(Array(
  new producer(value = classOf[ScalaProducer]),
  new producer(value = classOf[MDProducer])))
object ConfigModel extends ModelBuilder {

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