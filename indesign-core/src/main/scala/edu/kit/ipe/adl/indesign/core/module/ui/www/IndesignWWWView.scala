package edu.kit.ipe.adl.indesign.core.module.ui.www

import com.idyria.osi.wsb.webapp.localweb.LocalWebHTMLVIew
import com.idyria.osi.wsb.webapp.localweb.DefaultLocalWebHTMLBuilder
import java.net.URL
import java.net.URI
import edu.kit.ipe.adl.indesign.core.brain.Brain
import com.idyria.osi.wsb.webapp.localweb.LocalWebEngine
import edu.kit.ipe.adl.indesign.core.harvest.Harvest
import edu.kit.ipe.adl.indesign.core.harvest.Harvester

class IndesignWWWView extends LocalWebHTMLVIew with DefaultLocalWebHTMLBuilder {

  var valueSee = "Test"

  this.viewContent {
    html {
      head {
        /*script(new URI(s"//localhost:${LocalWebEngine.httpConnector.port}/resources/localweb/jquery.min.js")) {

        }*/
        stylesheet(new URI(s"/resources/semantic/semantic.min.css")) {

        }
        stylesheet(new URI(s"/resources/indesign.css")) {

        }
        script(new URI(s"/resources/semantic/semantic.min.js")) {

        }

        stylesheet(new URI(s"/resources/jquery.treetable.css")) {

        }
        script(new URI(s"/resources/jquery.treetable.js")) {

        }

      }
      body {

        "ui grid container" :: div {

          "left floated two wide column" :: div {
            // Menu
            //-----------------------
            <div class="ui pointing vertical menu">
              <a class="item">
                Site Title
              </a>
              <div class="item">
                <b>Grouped Section</b>
                <div class="menu">
                  <a class="item">Subsection 1</a>
                  <a class="active item">Subsection 1</a>
                  <a class="item">Subsection 1</a>
                </div>
              </div>
              <div class="ui dropdown item">
                Dropdown<i class="dropdown icon"></i>
                <div class="menu">
                  <div class="item">Choice 1</div>
                  <div class="item">Choice 2</div>
                  <div class="item">Choice 3</div>
                </div>
              </div>
            </div>
            "ui pointing vertical menu" :: div {
              id("main-menu")
              "item" :: a("#") {
                textContent("InDesign")
              }
              "active item" :: a("#") {
                textContent("Control")
                +@("reRender" -> "true")
                onClick {
                  detachView("page")
                }
              }
              "item" :: div {
                span {
                  textContent("Modules")
                  "menu" :: div {

                    WWWViewHarvester.getResources.foreach {
                      moduleView =>
                        "item" :: a("#") {
                          
                          textContent(moduleView.name)
                          +@("reRender" -> "true")
                          
                          onClick {
                            //placeView(moduleView.getClass, "page")
                            placeView(moduleView,"page")
                          }
                        }
                    }

                  }
                }
              }

            }
          }

          viewPlaceHolder("page", "thirteen wide column") {

            h1("InDesign UI ") {

            }

            input {
              +@("type" -> "button")
              +@("value" -> s"Click me 2 -> $valueSee")
              textContent("Click me")
              val b = currentNode
              println(s"Building node: " + b.hashCode())
              onClick {
                println(s"Hello World 2 with value Seee: " + valueSee + ", changing node: " + b.hashCode())
                b.apply("value" -> "I was Clicked")
                valueSee = " last click on: " + System.currentTimeMillis()
              }
            }

            input {
              +@("type" -> "button")
              +@("value" -> s"Click me with automatic Rerender -> $valueSee")
              +@("reRender" -> "true")
              textContent("Click me")
              onClick {
                println(s"Hello World 2 with value Seee: " + valueSee)
                currentNode.apply("value" -> "I was Clicked")
                valueSee = " last click on: " + System.currentTimeMillis()
              }
            }

            // Brain
            //-------------------
            $(<h2 class="ui header">
                <i class="settings icon"></i>
                <div class="content">
                  Brain
                  <div class="sub header">Brain Region/Modules</div>
                </div>
              </h2>)

            //-- Regions Table 
            "ui celled sortable table" :: table {
              thead {
                tr {
                  "sorted descending" :: th("Region") {

                  }

                  th("State") {

                  }
                  th("Error") {

                  }
                }
              }
              tbody {

                Brain.regions.foreach {
                  r =>
                    tr {
                      td(r.name) {

                      }
                      r.currentState match {
                        case None =>
                          "negative" :: td("Not in Lifecylce") {

                          }
                        case Some(s) =>
                          td("") {
                            "icon checkmark" :: i {

                            }
                            span(textContent(s))

                          }
                      }
                      "positive" :: td("") {
                        "icon checkmark" :: i {

                        }
                        span(textContent("None"))
                      }
                    }
                }

              }
            }

            // Harvest
            //------------------
            $(<h2 class="ui header">
                <i class="settings icon"></i>
                <div class="content">
                  Harvest
                  <div class="sub header">Harvesters and Found Resources</div>
                </div>
              </h2>)

            //-- harvest run
            div {
              "ui icon button" :: button("Run Harvest") {
                "icon settings" :: i {

                }
                reRender
                onClick {
                  Harvest.run
                }
              }

            }

            //-- Harvest Table 
            "ui celled sortable table" :: table {
              id("harvest-table")
              thead {
                tr {
                  "sorted descending" :: th("Harvester") {

                  }

                  th("Resources Count") {

                  }
                  th("Info") {

                  }
                  th("Last Run") {

                  }
                  th("Error") {

                  }
                }
              }
              tbody {

                def harvesterLine(hv: Harvester[_, _]): Unit = {
                  //-- Current
                  tr {

                    // Id
                    +@("data-tt-id" -> hv.hierarchyName())

                    // Parent
                    +@("data-tt-parent-id" -> hv.hierarchyName(withoutSelf = true))

                    td(hv.getClass.getSimpleName) {

                    }

                    td(hv.getResources.size.toString()) {

                    }

                    td(hv.lastRun.toString) {

                    }

                    //-- Error 
                    hv.getLastError match {
                      case Some(e) =>
                        "negative" :: td("") {
                          "icon close" :: i {

                          }
                          span(textContent(e.getLocalizedMessage))
                        }
                      case None =>
                        "positive" :: td("") {
                          "icon checkmark" :: i {

                          }
                          span(textContent("None"))
                        }
                    }

                  }

                  //-- Children
                  hv.childHarvesters.foreach {
                    child: Harvester[_, _] => harvesterLine(child)
                  }
                }
                Harvest.harvesters.foreach {
                  r =>
                    harvesterLine(r)

                }

              }
            } // EOF harvest table

            script("""

$(function() {
	$("#harvest-table").treetable();
});
//$("#harvest-table").treetable();
""")

          }
          // EOF Page Column
        }

      }
    }
  }
}