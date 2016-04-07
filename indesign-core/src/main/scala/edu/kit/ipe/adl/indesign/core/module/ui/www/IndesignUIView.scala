package edu.kit.ipe.adl.indesign.core.module.ui.www

import edu.kit.ipe.adl.indesign.core.harvest.HarvestedResource
import com.idyria.osi.wsb.webapp.localweb.LocalWebHTMLVIew
import edu.kit.ipe.adl.indesign.module.scala.ScalaSourceFile
import org.hamcrest.core.IsInstanceOf
import com.idyria.osi.wsb.webapp.localweb.LocalWebHTMLVIewCompiler

class IndesignUIView extends LocalWebHTMLVIew with HarvestedResource {

  // ! Important, if the View is derived from a Scala Source File, then root it
  //------------------
  /*this.onProcess {
    this.parentResource match {
      case Some(p: ScalaSourceFile) =>
        this.root
      case _ =>

    }
  }*/

  // INfos
  //------------
  def name = {

    this.parentResource match {
      case Some(p: ScalaSourceFile) => p.path.toFile().getName
      case _ =>

        getClass.getSimpleName.replace("$", "")
    }

  }

  def getId = this.parentResource match {
    case Some(p: ScalaSourceFile) => p.path.toFile().getName
    case _ =>

      getClass.getCanonicalName
  }

  var reloadEnable = true

  // Rendering can be local or from source
  //----------------------
  override def render = {

    this.contentClosure match {

      // If Content closuren ot defined and there is a parent resource, try to get the content using this resource
      case null if (this.parentResource != None) =>

        this.parentResource match {
          case Some(p: ScalaSourceFile) =>

            // Ensure Compilation is done
            p.ensureCompiled
            var cl = p.loadClass

            println(s"View source file: " + cl)
            // Create UI View 
            var view = LocalWebHTMLVIewCompiler.newInstance[LocalWebHTMLVIew](None, cl.asInstanceOf[Class[LocalWebHTMLVIew]])

            // Set proxy on new view 
            view.proxy = Some(this)
            this.contentClosure = view.contentClosure
            view.viewPath = this.viewPath

            // On reload, replace
            p.onChange {

              println(s"Modified scala source, trying to relaod view")

              // Compile, and keep errors on the main proxying view
              keepErrorsOn(this) {
                p.ensureCompiled

                // If we came up here, no errors
                //p.getUpchainCompilingProject.resetClassDomain
                var cl = p.loadClass
                var view = LocalWebHTMLVIewCompiler.newInstance[LocalWebHTMLVIew](None, cl.asInstanceOf[Class[LocalWebHTMLVIew]])

                // Set proxy on new view 
                view.proxy = Some(this)
                this.contentClosure = view.contentClosure
                view.viewPath = this.viewPath
              }

              // Refresh
              this.getTopParentView.@->("refresh")
            }

          case _ =>
            super.render
        }

        super.render
      case _ =>
        super.render
    }

  }

}