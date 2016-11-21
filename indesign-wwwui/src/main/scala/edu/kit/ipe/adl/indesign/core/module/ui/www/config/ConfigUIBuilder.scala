package edu.kit.ipe.adl.indesign.core.module.ui.www.config

import edu.kit.ipe.adl.indesign.core.module.ui.www.external.ExternalBuilder
import edu.kit.ipe.adl.indesign.core.config.Config
import com.idyria.osi.vui.html.HTMLNode
import org.w3c.dom.html.HTMLElement
import edu.kit.ipe.adl.indesign.core.config.model.CommonConfig

trait ConfigUIBuilder extends ExternalBuilder {

  def configTable(config: Option[CommonConfig]): HTMLNode[HTMLElement, _] = {

    config match {
      case None =>
        "ui info message" :: "No Config Available to edit"
      case Some(config) =>
        div {
          "ui compact celled table" :: table {
            "full-width" :: thead {
              tr {
                th("") {

                }
                th("Key") {

                }

                th("Value") {

                }
              }

            }
            // EOF head

            //-- Gather stuff

            tbody {

              config.values.keys.foreach {
                key =>

                  // Key Row
                  //------------
                  tr {
                    td("") {

                      "remove circle icon" :: i {
                        onClick {
                          config.values.keys -= key
                          config.resyncToFile
                        }
                      }
                    }
                    td(key.keyType) {
                      +@("colspan" -> "2")
                    }

                  }
                  // Value sfor Key
                  //----------
                  key.values.foreach {
                    value =>
                      tr {
                        td("") {

                        }
                        td("") {
                          $(<i class="remove circle icon"></i>) {

                          }
                          "remove circle icon" :: i {
                            onClick {
                              key.values -= value
                              config.resyncToFile
                            }
                          }
                        }
                        td("") {
                          span(textContent(value))
                        }
                      }
                  }

              }

            }
            // EOF Body

            "full-width" :: tfoot {
              tr {
                th("") {

                }
                th("") {
                  +@("colspan" -> "2")

                  "ui right floated form" :: div {

                    "fields" :: div {
                      "field" :: div {
                        label("Key") {

                        }
                        input {
                          ++@(("type" -> "text"))
                          ++@("name" -> "newKey")
                          ++@("required" -> "true")
                        }
                      }
                      "field" :: div {
                        label("Value") {

                        }
                        input {
                          ++@(("type" -> "text"))
                          ++@("name" -> "newValue")
                          ++@("required" -> "true")
                        }
                      }
                      "error hidden" :: div {

                      }
                      "field" :: div {
                        label("") {

                        }
                        "ui button" :: button("Add") {
                          onClick {
                            println("Adding new Key")
                          }
                        }
                      }

                    }

                  }
                }
              }
            }
          }
          // EOF Table
        }
    }

  }

}