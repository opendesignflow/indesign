package org.odfi.indesign.core.module.stream
import com.idyria.osi.ooxoo.model.ModelBuilder
import com.idyria.osi.ooxoo.model.producer
import com.idyria.osi.ooxoo.model.producers
import com.idyria.osi.ooxoo.model.out.markdown.MDProducer
import com.idyria.osi.ooxoo.model.out.scala.ScalaProducer
import com.idyria.osi.ooxoo.core.buffers.structural.io.sax.STAXSyncTrait

@producers(Array(
  new producer(value = classOf[ScalaProducer]),
  new producer(value = classOf[MDProducer])))
class StreamModel extends ModelBuilder {
  
  "StreamUpdate" is {
     attribute("streamID")
     
     "Content" ofType("cdata")
  }
  
  "StreamOpen" is {
    attribute("streamID")
  }
  "StreamClose" is {
    attribute("streamID")
  }
  
  
}