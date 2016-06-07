package edu.kit.ipe.adl.indesign.module.git

import edu.kit.ipe.adl.indesign.core.module.ui.www.IndesignUIView
 
class GitWWWView extends IndesignUIView {

  this.root

  this.viewContent {

    div {
      h1("Git Repositories View 2") {

      }

      "ui table" :: table {
        thead {
          th("Path") {

          }
          th("State") {

          }
        }

        tbody {
          /*GitHarvester.onResources[GitRepository] {
            rep =>
              tr {
                td(rep.path.toFile().getAbsolutePath) {
                  
                }
                td("Unknown") {
                  
                }
              }

          }*/
        }
      }

    }

  }
}