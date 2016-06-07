package edu.kit.ipe.adl.indesign.core.config.model


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
    
    // Values
    //------------
    "Values" is {
      "Key" multiple {
        attribute("name")
        attribute("keyType") default "string"
        "Value" multiple {
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