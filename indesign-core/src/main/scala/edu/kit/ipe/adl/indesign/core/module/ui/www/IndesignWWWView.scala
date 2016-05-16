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
        script(new URI(s"/resources/indesign.js")) {

        }

        // Modules
        //----------------
        script(new URI(s"/resources/modules/heart/heart.js")) {

        }

      }
      body {

        "ui grid container" :: div {

          "left floated three wide column" :: div {
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
              "item" :: a("#") {
                textContent("Control")
                reRender
                onClick {

                  placeView(new ControlView , "page", viewready = false)

                }
              }
              "active item" :: a("#") {
                textContent("Home")
                +@("reRender" -> "true")
                onClick {

                  IndesignWWWView.defaultView match {
                    case Some(v) =>
                      placeView(v, "page", viewready = false)

                    case None =>
                      detachView("page")
                  }

                }
              }
              "item" :: div {
                span {
                  textContent("Modules")
                  "menu" :: div {

                    WWWViewHarvester.onResources[IndesignUIView] {
                      moduleView =>
                        "item" :: a("#") {

                          //var ready = (moduleView.contentClosure==null || moduleView.proxy.isDefined)
                          var ready = moduleView.isProxy
                          //textContent(moduleView.name + s"(${ready})")
                          textContent(moduleView.name)
                          +@("reRender" -> "true")

                          onClick {
                            //placeView(moduleView.getClass, "page")
                            placeView(moduleView, "page", viewready = ready)
                          }
                        }
                    }

                  }
                }
              }

            }
          }

          // Main Page
          //--------------
          viewPlaceHolder("page", "twelve wide column") {

            p {
              textContent("No Placed View 2")
            }

          }

          IndesignWWWView.defaultView match {
            case Some(v) =>
              // println(s"********** Placed ***********")
              viewPlaces.get("page") match {
                case Some((container, null)) =>
                  placeView(v, "page", viewready = false)
                case _ =>
              }

            case None =>
          }

          // EOF Main Page
        }

      }
    }
  }
}

object IndesignWWWView {

  var defaultView: Option[IndesignUIView] = None

}