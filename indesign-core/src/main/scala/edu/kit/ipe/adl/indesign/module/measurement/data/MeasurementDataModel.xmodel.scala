package edu.kit.ipe.adl.indesign.module.measurement.session


import com.idyria.osi.ooxoo.model.ModelBuilder
import com.idyria.osi.ooxoo.model.producer
import com.idyria.osi.ooxoo.model.producers
import com.idyria.osi.ooxoo.model.out.markdown.MDProducer
import com.idyria.osi.ooxoo.model.out.scala.ScalaProducer

@producers(Array(
  new producer(value = classOf[ScalaProducer]),
  new producer(value = classOf[MDProducer])))
object MeasurementDataModel extends ModelBuilder {
 
 
  
  val graph = "Graph" is {
    isTrait(true)
    attribute("name")
    attribute("creationDate") ofType "datetime"
    
  }
  
  //-- XY
  val xyGraph = "XYGraph" is {
    withTrait(graph)
    
    "Point" multiple {
      attribute("x") ofType "double"
      ofType("double")
    }
  }
  
  
}