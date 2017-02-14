package org.odfi.indesign.core.module.ui.www.views

import org.odfi.indesign.core.brain.Brain
import org.odfi.indesign.core.harvest.Harvest
import org.odfi.indesign.core.harvest.Harvester
import org.odfi.indesign.core.brain.ExternalBrainRegion
import java.io.File
import org.odfi.indesign.core.brain.BrainRegion
import org.odfi.indesign.core.module.ui.www.external.DataTableBuilder
import com.idyria.osi.ooxoo.core.buffers.structural.xelement
import org.odfi.indesign.core.brain.ExternalBrainRegion
import org.odfi.indesign.core.module.ui.www.IndesignUIView
import com.google.inject.spi.ModuleSource
import org.odfi.indesign.core.module.ui.www.config.ConfigUIBuilder

class ControlView extends IndesignUIView with DataTableBuilder with ConfigUIBuilder {

  this.viewContent {

    div {

      h1("InDesign UI 2") {

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
      importHTML(<h2 class="ui header">
          <i class="settings icon"></i>
          <div class="content">
            Brain
            <div class="sub header">Brain Region/Modules</div>
          </div>
        </h2>)

      div {
        "ui button" :: button("Restart") {

          reload
          onClick {
            Brain.moveToStop
            println("Shutdown now reset")
            Brain.resetState
            println("Now Start")
            Brain.moveToStart
          }

        }
      }

      // Steps 
      //--------------
      div(textContent("Brain state: " + Brain.currentState.get + " - " + Brain.states.indexOf(Brain.currentState.get)))
      "ui ordered steps" :: div {

        Brain.states.zipWithIndex.foreach {

          case (state, i) if (i == Brain.states.indexOf(Brain.currentState.get)) =>
            "active step" :: div {
              "content" :: div {
                "title" :: div {
                  textContent(state)
                }
              }
            }

          case (state, i) if (i < Brain.states.indexOf(Brain.currentState.get)) =>

            "completed step" :: div {
              "content" :: div {
                "title" :: div {
                  textContent(state)
                }
              }
            }

          case (state, i) =>
            "step" :: div {
              "content" :: div {
                "title" :: div {
                  textContent(state)
                }
              }
            }

        }

      }

      //-- Regions Table 
      //-------------------------

      //-- Add Region
      //-----------------
      div {
        "ui  floating message error" :: div {
          +@("style" -> "display:none")
        }
        onNode(tempBufferSelect("region-add-type", ("folder" -> "Folder"))) {
          reRender
        }

        getTempBufferValue[String]("region-add-type") match {
          case Some("folder") =>
            "ui input" :: input {

              bindValue {
                folder: String =>

                  //-- Create file 
                  var f = new File(folder)
                  f.exists() match {
                    case true =>

                      //-- Add to config
                      var key = Brain.config.get.addKey("region", "external-region")
                      key.values.add.data = f.getCanonicalPath
                      Brain.config.get.resyncToFile

                    //-- Update Brain

                    case false => throw new RuntimeException("Cannot add non existing file: " + f)
                  }

                //-- Check 

              }
            }

          case _ => span("Unsupported type")
        }

      }
      //-- Table
      "ui celled sortable table treetable" :: table {
        id("region-table")
        thead {
          tr {
            "sorted descending" :: th("Region") {

            }

            th("Type") {

            }

            th("State") {

            }
            th("Error") {

            }

            th("Action") {

            }
          }
        }
        tbody {

          def brainRegionLine(region: BrainRegion) = {
            "leaf expanded" :: tr {
              // Id
              +@("data-tt-id" -> region.resourceHierarchyName())

              // Parent
              +@("data-tt-parent-id" -> region.resourceHierarchyName(withoutSelf = true))

              // name
              "collapsing" :: td("") {
                importHTML(<i class="block layout icon"></i>)
                importHTML(<i class="settings icon ui popup-activate"></i>)

                "ui flowing popup top left transition hidden" :: div {
                  h4("Config") {

                  }
                  configTable(region.config)
                }

                span {
                  textContent(region.name)
                }

              }

              //-- type
              classOf[ExternalBrainRegion].isInstance(region) match {
                case true =>
                  td("") {
                    span(textContent("External: " + region.getClass.getSimpleName))
                    "text" :: p {
                      textContent(region.getClass.getClassLoader.toString)
                    }
                    "text" :: p {
                      textContent(region.asInstanceOf[ExternalBrainRegion].getId)
                    }
                    "text" :: p {
                      textContent(region.asInstanceOf[ExternalBrainRegion].getRegionPath)
                    }
                    /*"text" :: p {
                      textContent(s"Builder tainted: " + region.asInstanceOf[ExternalBrainRegion].regionBuilder.get.isTainted)
                    }*/

                  }
                case false =>
                  td("Internal") {

                  }
              }

              //-- Lifecycle State
              region.currentState match {
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

              //-- Error State
              td("") {
                region.hasErrors match {
                  case true =>
                    classes("negative")
                    span(textContent(region.errors.size.toString))
                  case false =>
                    classes("positive")
                    importHTML(<i class="icon checkmark"></i>)
                    span(textContent("None"))
                }

              }

              //-- Actions
              td("") {

                // Remove from config if in config
                classOf[ExternalBrainRegion].isInstance(region) match {
                  case true =>

                    "ui button" :: button("Remove from Config") {

                    }
                    "ui button" :: button("Reload") {
                      reload
                      onClick {
                        region.asInstanceOf[ExternalBrainRegion].reload
                      }
                    }
                    "ui button popup-activate" :: button("Loaded Regions") {

                    }
                    "ui flowing popup top left transition hidden" :: div {

                      //-- Make list to add 
                      var available = region.asInstanceOf[ExternalBrainRegion].discoverRegions
                      //var available = region.getDerivedResources[ModuleSourceFile].map { msf => msf.getDiscoveredModules }.flatten.toList.distinct
                      // println(s"Discovered: $available")

                      //var regionsClasses = r.derivedResources.map { rs => rs.getClass.getName.trim }.toList
                      var regionsClasses = region.asInstanceOf[ExternalBrainRegion].configKey.get.values.drop(1).map { v => v.toString }
                      var remaining = available.filter(a => !regionsClasses.contains(a))

                      remaining.size match {
                        case 0 =>
                          "ui info message" :: div {
                            textContent("No Regions available to add")
                          }
                        case other =>
                          tempBufferSelect(s"${region.name}-regionLoad", remaining.map { name => (name, name.split("\\.").last) })

                          "ui info error" :: div {

                          }
                          "ui icon button" :: button("") {
                            +@("data-content" -> "Add Region to be loaded")
                            +@("reload" -> "true")
                            "add icon" :: i()

                            onClick {
                              var name = getTempBufferValue[String](s"${region.name}-regionLoad") match {
                                case None =>
                                  remaining.head
                                case Some(v) => v
                              }
                              region.asInstanceOf[ExternalBrainRegion].addRegionClass(name.toString)
                              Brain.config.get.resyncToFile
                            }
                          }
                      }

                      //-- Regions Tables
                      region.hasDerivedResourceOfType[BrainRegion] match {
                        case false =>
                          "ui info message" :: div {
                            textContent("No Regions were loaded so far")
                          }
                        case true =>
                          "ui table" :: table {
                            thead {
                              th("Class") {

                              }
                              th("Action") {

                              }
                            }
                            tbody {
                              region.onDerivedResources[BrainRegion] {
                                case res =>
                                  tr {
                                    td(res.getClass.getName) {

                                    }
                                    td("") {

                                    }
                                  }

                              }
                            }
                          }
                      }

                    }

                  case false =>
                    Brain.config match {
                      case Some(config) =>
                        config.isInConfig("region", region.getClass.getName) match {

                          case true =>
                            "ui button" :: button(s"Remove from Config") {
                              onClick {

                                ///-- Delete
                                Brain.config.get.removeFromConfig("region", region.getClass.getName)
                                Brain.config.get.resyncToFile

                                //-- Stop
                                //Brain.regions = Brain.regions.filter(_ != r)
                                //r.kill
                              }
                            }
                          case false =>
                            span("Internal Non Configured: " + region.getClass.getName)

                        }
                      case None =>
                    }
                }

              }

            }
          }
          // EOF Brain Region Line

          Brain.onResources[BrainRegion] {
            case r =>
              brainRegionLine(r)
              r.onDerivedResources[BrainRegion] {
                case dr => brainRegionLine(dr)
              }
          }

        }
      }

      // Harvest
      //------------------
      importHTML(<h2 class="ui header">
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

              td("") {

                importHTML(<i class="shipping icon"></i>)
                importHTML(<i class="settings icon ui popup-activate"></i>)

                "ui flowing popup top left transition hidden" :: div {
                  h4("Config") {

                  }
                  configTable(hv.config)
                }
                span(textContent(hv.getClass.getSimpleName))

              }

              // Resources count
              td("") {

                hv.getResources.size match {
                  case 0 =>
                    "ui info message" :: div {
                      textContent("Not Resources")
                    }
                  case _ =>
                    "ui button popup-activate" :: button(s"${hv.getResources.size.toString()} Resources Infos") {

                    }
                    "ui flowing popup top left transition hidden" :: div {
                      "ui celled table vui-datatables" :: table {
                        thead {
                          tr {
                            th("Name") {

                            }
                            th("Error State") {

                            }
                          }
                        }
                        tbody {
                          hv.getResources.foreach {
                            ar =>
                              tr {
                                td(ar.getDisplayName) {

                                }
                                //-- Resource Error 
                                ar.getLastError match {
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
                          }
                        }
                      }
                    }

                }

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
	$("#region-table").treetable();
});
//$("#harvest-table").treetable();
""")

    }
  }

}