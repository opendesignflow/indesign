package org.odfi.indesign.core.module.filestash

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
object FileStashModel extends ModelBuilder with IdAndRefIdModelBuilder {

  "FileStash" is {
    makeTraitAndUseCustomImplementation
    
    "BasePath" ofType "string"
    
    "Owner" multiple {
      
      requestContainerReference
      withElementID
      
      makeTraitAndUseCustomImplementation

      "Stash" multiple {
        
        requestContainerReference
        withElementID
        
        makeTraitAndUseCustomImplementation
        
        
        "CreationDate" ofType("datetime")
        "Validity" ofType("datetime")
        "FilesCount" ofType("integer") default("0")
        
        "AccessTicket" multiple {
          withElementID
          
          attribute("canWrite") ofType("boolean")
          attribute("validity") ofType("datetime")
          
        }
        
      }

    }

  }
}