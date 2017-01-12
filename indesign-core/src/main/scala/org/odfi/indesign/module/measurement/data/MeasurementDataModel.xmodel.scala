package org.odfi.indesign.module.measurement.data


import com.idyria.osi.ooxoo.model.ModelBuilder
import com.idyria.osi.ooxoo.model.producer
import com.idyria.osi.ooxoo.model.producers
import com.idyria.osi.ooxoo.model.out.markdown.MDProducer
import com.idyria.osi.ooxoo.model.out.scala.ScalaProducer
import com.idyria.osi.ooxoo.core.buffers.structural.io.sax.STAXSyncTrait

@producers(Array(
  new producer(value = classOf[ScalaProducer]),
  new producer(value = classOf[MDProducer])))
object MeasurementDataModel extends ModelBuilder {
 
 
  // Data definition
  //----------------------------
  
  val graph = "Graph" is {
    isTrait
    withTrait(classOf[STAXSyncTrait])
    attribute("name")
    attribute("creationDate") ofType "datetime"
    attribute("display") ofType "string" default "line"
    
  }
  
  //-- XY
  val xyGraph = "XYGraph" is {
    withTrait(graph)
    
    "Point" multiple {
      "X" ofType "float"
      "Y" multiple {
        ofType("float")
      }
    }
  }
  
  
  // Protocols
  //------------------------
  
  "UpdateGraphRequest" is {
    
    //-- Target Id
    attribute("targetId")
    
    //-- Graph
    importElement(graph)
  }
  
  
  
}