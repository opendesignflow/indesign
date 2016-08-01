package edu.kit.ipe.adl.indesign.fastbuild.ui

import edu.kit.ipe.adl.indesign.core.module.ui.www._
import edu.kit.ipe.adl.indesign.core.harvest.Harvest
import edu.kit.ipe.adl.indesign.fastbuild.FBProjectHarvester
import edu.kit.ipe.adl.indesign.fastbuild.FBProject
import edu.kit.ipe.adl.indesign.module.scala.ScalaSourceFile
import com.idyria.osi.tea.compile.FileCompileError
import edu.kit.ipe.adl.indesign.core.module.ui.www.edit.ace.ACEEditorBuilder

class FastBuildOverview extends IndesignUIView with ACEEditorBuilder {

  this.viewContent {

    div {
      h1("Fast Build Projects") {

      }

      var projects = Harvest.collectResourcesOnHarvesters[FBProjectHarvester, FBProject, FBProject] {
        case p => p
      }

      projects.size match {
        case 0 =>
          "ui info message" :: "No Projects to display"

        case other =>

          var selectedProject = this.getTempBufferValue[FBProject]("focus-project")

          // Selection Table
          //----------------
          "ui celled table" :: table {
            thead("", "Name", "Path", "Errors", "Actions")
            tbody {
              projects.foreach {
                fbProject =>
                  tr {

                    td("") {
                      tempBufferRadio("")("focus-project" -> fbProject) {
                        reload
                      }

                    }
                    td(fbProject.getProjectName) {

                    }
                    td(fbProject.path.toFile().getAbsolutePath) {

                    }

                    // errors
                    td("") {

                    }

                    // Actions
                    td("") {
                      "ui blue button" :: button("Build") {
                        onClickReload {
                          fbProject.buildFull
                        }
                      }
                    }
                  }
              }
            }
          }
          // EOF Selection Table

          // Summary and editing
          //--------------------------
          selectedProject match {
            case Some(selectedProject) =>

              "ui two column grid" :: div {

                "column" :: div {

                  "ui raised segment " :: div {

                    // Global Info
                    //----------------
                    $(<a class="ui green ribbon label">Overview</a>)
                    div {
                      +@("style" -> "margin:10px")

                      "" :: s"Name: ${selectedProject.getProjectName}"
                      "" :: s"Location: ${selectedProject.path.toString}"

                    }

                    // Dependencies Info
                    //----------------
                    $(<a class="ui blue ribbon label">Dependencies</a>)
                    div {
                      +@("style" -> "margin:10px")

                      "ui info message" :: "No depedencies so far"

                    }
                  }

                }
                // EOF Coluln one

                "column" :: div {

                  "ui raised segment " :: div {
                    // Source Errors
                    //----------------------
                    div {
                      $(<a class="ui red ribbon label">Source Errors</a>)

                      selectedProject.getSubDerivedResources[ScalaSourceFile].filter {
                        src =>
                          println(s"Testing File for error presence ${src.hashCode()}: " + src.path.toFile.getCanonicalPath)
                          src.hasErrors
                      } match {

                        case sources if (sources.size == 0) =>
                          "ui info message" :: "No sources with errors found"
                        case sources =>

                          "ui celled table " :: table {
                            thead("File/Line", "Column", "Error")
                            tbody {
                              sources.foreach {
                                src =>

                                  // File Name
                                  tr {
                                    td(src.path.toFile.getName) {
                                      +@("colspan" -> "3")

                                      "ui icon button" :: button("") {
                                        "ui edit icon " :: i {}
                                        onClickReload {
                                          putToTempBuffer("focus-file", src)
                                        }
                                      }

                                    }
                                  }
                                  // Errors
                                  src.errors.collect { case err if (classOf[FileCompileError].isInstance(err)) => err.asInstanceOf[FileCompileError] }.foreach {
                                    error =>
                                      tr {

                                        td(error.line.toString) {

                                        }
                                        td(error.column.toString) {

                                        }
                                        td(error.errorMessage) {

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
                // EOF column 2

              }
              // EOF Summary Grid

              // Editor
              //---------------
              getTempBufferValue[ScalaSourceFile]("focus-file") match {
                case None =>
                case Some(scalaSource) =>

                  // Find first error
                  //scalaSource.getE

                  div {
                    var allErrors = scalaSource.getErrorsOfType[FileCompileError]
                    "" :: s"Errors: ${allErrors.size}"

                    aceEditor("scala", "100%", "400px", scalaSource.getTextContent) {

                      allErrors.headOption match {
                        case Some(err) =>
                          //, message: \"${err.errorMessage}\"
                          +@("data-error", s"""{"line": ${err.line},"column": ${err.column}}""")
                          
                        case None =>
                      }

                    }
                    /*script("""
                            var currentEditor = this;
                            $(function() {
                              
                              console.log("Init local editor: "+this)
                              
                            });
                            """)*/
                  }

              }

            case None =>
              "ui info message " :: "Please select a project from the table to see details"

              // Test Highligher
              //-----------------------
              "" :: div {
                id("editor")
                +@("style" -> "width:100%;height:350px;position:relative")

                textContent("""
package simplefb

object TestMain extends App {
  
}""")

              }

          }
      }

      /*script("""
        $(function() {
            
            $()
            var editor = ace.edit("editor");
            editor.setTheme("ace/theme/monokai");
            editor.getSession().setMode("ace/mode/scala");
    
    
        });
      
        
        """)*/
    }
    // EOF Main div

  }

}