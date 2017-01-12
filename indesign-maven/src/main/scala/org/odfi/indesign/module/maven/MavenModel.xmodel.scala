package org.odfi.indesign.module.maven

import com.idyria.osi.ooxoo.model.ModelBuilder
import com.idyria.osi.ooxoo.model.producer
import com.idyria.osi.ooxoo.model.producers
import com.idyria.osi.ooxoo.model.out.markdown.MDProducer
import com.idyria.osi.ooxoo.model.out.scala.ScalaProducer

@producers(Array(
  new producer(value = classOf[ScalaProducer]),
  new producer(value = classOf[MDProducer])))
object MavenModels extends ModelBuilder {

  "project" is {
    elementsStack.head.makeTraitAndUseCustomImplementation
    
    "groupId" ofType ("string")
    "artifactId" ofType ("string")
    "version" ofType ("string")
    "package" ofType ("string")

    // Parents 
    //-------------------
    "parent" is {
      "groupId" ofType ("string")
      "artifactId" ofType ("string")
      "version" ofType ("string")
      "relativePath" ofType "string"
    }
    
    // Repositories
    //------------------
    "pluginRepositories" is {
      
      "pluginRepository" multiple {
        "id" ofType "string"
        "name" ofType "string"
        "url" ofType "url"
        
        "snapshots" is {
          "enabled" ofType "boolean" default "true"
        }
        
      }
    }
    "repositories" is {
      
      "repository" multiple {
        "id" ofType "string"
        "name" ofType "string"
        "url" ofType "url"
        
        "snapshots" is {
          "enabled" ofType "boolean" default "true"
        }
        
      }
    }

    // Dependencies
    //-------------
    "dependencies" is {
      "dependency" multiple {
        "groupId" ofType ("string")
      "artifactId" ofType ("string")
      "version" ofType ("string")
      "scope" ofType("string")
      
      }
    }
    
    
    
    
  }

}