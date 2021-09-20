package org.odfi.indesign.core.module.stats

import com.idyria.osi.ooxoo.model.ModelBuilder
import com.idyria.osi.ooxoo.model.producer
import com.idyria.osi.ooxoo.model.producers
import com.idyria.osi.ooxoo.model.out.markdown.MDProducer
import com.idyria.osi.ooxoo.model.out.scala.ScalaProducer
import com.idyria.osi.ooxoo.core.buffers.structural.io.sax.STAXSyncTrait
import com.idyria.osi.ooxoo.core.buffers.id.IdAndRefIdModelBuilder

@producers(Array(
  new producer(value = classOf[ScalaProducer]),
  new producer(value = classOf[MDProducer])))
class StatsModel extends ModelBuilder with IdAndRefIdModelBuilder {

    /**
     * Add standard elements for statistics gathering
     */
  "StatsSupport" is {
      makeTraitAndUseCustomImplementation
      
      "Stats" is {
          
          "Stat" multiple {
              withElementID
              attribute("lastUpdated") ofType("datetime")
              attribute("dataType")
              attribute("value")
              attribute("history") ofType("boolean") default("false")
              "History" multiple {
                   attribute("value")
                   attribute("savedDate") ofType("datetime")
              }
          }
      }
  }
}