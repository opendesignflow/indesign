package edu.kit.ipe.adl.indesign.core.module.ui.www.fs

import edu.kit.ipe.adl.indesign.core.module.ui.www.IndesignUIView
import edu.kit.ipe.adl.indesign.core.module.ui.www.external.MarkdownBuilder
import edu.kit.ipe.adl.indesign.core.harvest.Harvest
import edu.kit.ipe.adl.indesign.core.harvest.fs.FileSystemHarvester
import edu.kit.ipe.adl.indesign.core.harvest.fs.HarvestedFile

class FileSystemHarvesterView extends IndesignUIView with MarkdownBuilder {

  this.viewContent {
    div {

      markdown("""

# File System Harvester Configuration
        
This page will help you configure the File System Harvester.

## What is the FSH

The FSH will scan all the files from a set of base files and deliver them to its child harvesters.
Typical Child Harvesters of FSH will  gather repositories like GIT, or projects like Maven. 

       
         
           
        """)

      div {

      }
      var allHarvesters = Harvest.getHarvesters[FileSystemHarvester]
      allHarvesters match {
        case Some(harvesters) =>

          // Get All Paths
          //--------------------
          "ui raised segment" :: div {
            $(<a class="ui blue ribbon label">Runtime Paths</a>) {
              
            }

            p(textContent("""
              The runtime paths are the paths used by FSH at the moment.
              The paths may have been set by some modules, so removing them wil only affect this running instance of Indesign"""))

            var allPaths = harvesters.map {
              h => h.getResourcesOfType[HarvestedFile]
            }.flatten

            allPaths.size match {
              case 0 =>

              case other =>

                "ui celled table" :: table {
                  thead {

                    tr {
                      th("Path") {

                      }
                      th("") {

                      }
                    }

                  }
                  tbody {

                    allPaths.foreach {
                      basePath =>
                        tr {
                          td(basePath.path.toFile.getAbsolutePath) {

                          }
                          td("") {
                            "ui red button" :: button("Remove") {
                              onClickReload {

                              }
                            }
                          }
                        }
                    }

                  }
                }

            }
          }
          // EOF Runtime

          // Get Config Paths
          //--------------------
          "ui raised segment" :: div {
            importHTML(<a class="ui blue ribbon label">Configuration Paths</a>) 

            p("""
              The configuration paths are set in the FSH config.
              When added or removed, it will affect the FSH runtime paths accross restarts of Indesign
              """)

            "ui celled table" :: table {
              thead {

                tr {
                  th("Path") {

                  }
                  th("") {

                  }
                }

              }
              tbody {

                harvesters.foreach {
                  h =>
                    h.config match {
                      case Some(config) =>
                        config.getKeys("path", "file").foreach {
 
                          key =>
                            // Only edit keys with a value
                            key.values.headOption match {
                              case Some(path) =>
                                tr {
                                  td(path.toString) {

                                  }
                                  td("") {
                                    "ui red button" :: button("Remove from Config") {
                                      onClickReload {
                                        h.config.get.values.keys -= key
                                        h.config.get.resyncToFile
                                      }
                                    }
                                  }
                                }
                              case None =>
                            }
                        }
                      case None =>

                    }

                }

              }
            }
            // EOF Table config

            div {
              /*+@("ondragover", "fileDragOver(event)")
              +@("ondrop", "fileDrop(event)")*/
              textContent("drag file here to add")
              onFileDrop {
                f =>

                  println("Droped filed: " + f)

              }

            }

            /*script("""
              function fileDragOver(event) {
                event.preventDefault();
                console.log("Type: "+event.dataTransfer.types);
                if (event.dataTransfer) {
                  if ($.inArray("Files",event.dataTransfer.types) || event.dataTransfer.types=="Files") {
                    console.log("Contains Files");
                    return true;
                  }
                }
                return false;
               
              }
              function fileDrop(event) {
                event.preventDefault();
                console.log("Dropped");
                
                $(event.dataTransfer.files).each(function(i,e) {
                  console.log("Dropped: "+e);
                });
              }
              """)
            */
          }

        // EOF COnfig

        case None =>

          "ui warning message" :: div {
            textContent("""
              No FileSystem Harvester has been added to Indesign.
             
              """)
            "ui blue button" :: button("Add a File System Harvester") {
              onClickReload {

                var key = Harvest.config.get.addKey("harvester", "class")
                var value = key.values.add
                value.data = classOf[FileSystemHarvester].getCanonicalName
                Harvest.config.get.resyncToFile
                println("Added Config to Harvest")

              }
            }

          }
      }

    }
  }

}