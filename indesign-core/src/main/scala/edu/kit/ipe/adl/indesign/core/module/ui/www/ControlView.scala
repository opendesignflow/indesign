package edu.kit.ipe.adl.indesign.core.module.ui.www

import edu.kit.ipe.adl.indesign.core.brain.Brain
import edu.kit.ipe.adl.indesign.core.harvest.Harvest
import edu.kit.ipe.adl.indesign.core.harvest.Harvester

class ControlView extends IndesignUIView {

  this.viewContent {

    div {

      h1("InDesign UI ") {

      }

      /*input {
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
*/
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

          def harvesterLine(hv: Harvester): Unit = {
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

              td("") {

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
              child: Harvester => harvesterLine(child)
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
  }

}