package edu.kit.ipe.adl.indesign.module.scala.ui

import edu.kit.ipe.adl.indesign.core.module.ui.www.IndesignUIView
import edu.kit.ipe.adl.indesign.core.harvest.Harvest
import edu.kit.ipe.adl.indesign.module.scala.ScalaAppHarvester
import edu.kit.ipe.adl.indesign.core.module.ui.www.external.HighlightJSBuilder
import edu.kit.ipe.adl.indesign.module.scala.ScalaAppSourceFile

class ScalaOverview extends IndesignUIView with HighlightJSBuilder {

  this.root

  this.viewContent {

    div {
      h2("Scala Overview") {

      }

      "ui celled table" :: table {

        thead {
          th("App File") {

          }
          th("App Name") {

          }
          th("Action") {

          }
        }

        tbody {

          Harvest.onHarvesters[ScalaAppHarvester] {
            case h =>
              h.onResources[ScalaAppSourceFile] {
                sf => 
                  tr {
                    td(sf.path.toFile().getCanonicalPath) {
                      
                    }
                    td(sf.getDefinedObjects.head) {
                      
                    }
                    td("") {
                      
                      "ui button" :: button("Run") {
                        onClick {
                          sf.run
                        }
                      }
                    }
                  }
              }
              
          }
        }
      }

    }

  }
}