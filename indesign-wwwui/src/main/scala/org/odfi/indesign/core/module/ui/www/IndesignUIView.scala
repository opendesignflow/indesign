package org.odfi.indesign.core.module.ui.www

import org.odfi.indesign.core.harvest.HarvestedResource
import com.idyria.osi.wsb.webapp.localweb.LocalWebHTMLVIew
import com.idyria.osi.wsb.webapp.localweb.LocalWebHTMLVIewCompiler
import org.odfi.indesign.core.module.buildsystem.JavaSourceFile
import org.odfi.indesign.core.module.buildsystem.SourceFile

class IndesignUIView extends LocalWebHTMLVIew with HarvestedResource with IndesignUIHtmlBuilder {

  // Standalone
  //--------------

  /**
   * If set, the main UI module will map this view to a specific path and open a link to a new tab
   */
  var targetViewPath: Option[String] = None

  def changeTargetViewPath(path: String) = {
    this.targetViewPath = Some(path)
    this
  }

  // ! Important, if the View is derived from a Scala Source File, then root it
  //------------------
  var isProxy = false
  var proxiedView: Option[LocalWebHTMLVIew] = None
  /*this.onProcess {
    this.parentResource match {
      case Some(p: ScalaSourceFile) =>
        this.root
      case _ =>

    }
  }*/

  override def getClassLoader = proxiedView match {
    case None => super.getClassLoader
    case Some(v) => v.getClassLoader
  }

  // Actions
  //---------------

  override def getActions = proxiedView match {
    case Some(v) => v.getActions
    case None => super.getActions
  }

  // INfos
  //------------
  
  /**
   * Alias for #getUIViewName
   */
  //var name = getUIViewName
  
  def getUIViewName = {
    this.parentResource match {
      case Some(p: SourceFile) =>
        isProxy = true
        p.path.toFile().getName
      case _ =>

        getClass.getSimpleName.replace("$", "")
    }

  }

  def getId = this.parentResource match {
    case Some(p: SourceFile) =>
      isProxy = true
      p.path.toFile().getName
    case _ =>

      getClass.getCanonicalName
  }

  var reloadEnable = true

  // Rendering can be local or from source
  //----------------------
  override def render = {

    this.isProxy match {
      case true if (proxiedView == None) =>
        this.parentResource match {

          case Some(p: JavaSourceFile) =>

            // Ensure Compilation is done
            p.ensureCompiled
            var cl = p.loadClass

            println(s"View source file: " + cl)
            // Create UI View 
            var view = LocalWebHTMLVIewCompiler.newInstance[LocalWebHTMLVIew](None, cl.asInstanceOf[Class[LocalWebHTMLVIew]])

            // Set proxy on new view 
            view.proxy = Some(this)
            this.proxiedView = Some(view)
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

                // Close old view
                //this.proxiedView.get.closeView 

                // Set proxy on new view 
                view.proxy = Some(this)
                this.proxiedView = Some(view)
                view.viewPath = this.viewPath
              }

              // Refresh
              this.getTopParentView.@->("refresh")
            }

            proxiedView.get.rerender

          case _ =>
            super.render
        }

      case true if (proxiedView.isDefined) =>
        proxiedView.get.rerender
      case _ =>
        super.render
    }

  }

}