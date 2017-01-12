package org.odfi.indesign.module.maven

import java.io.File
import java.nio.file.Path
import com.idyria.osi.tea.compile.ClassDomain
import com.idyria.osi.tea.compile.FileCompileError
import com.idyria.osi.tea.compile.IDCompiler
import org.odfi.indesign.core.harvest.fs.HarvestedFile
import org.odfi.indesign.core.heart.DefaultHeartTask
import org.odfi.indesign.core.heart.HeartTask
import org.odfi.indesign.core.module.ui.www.WWWViewHarvester
import com.idyria.osi.tea.compile.ClassDomainSupport
import com.idyria.osi.tea.files.FileWatcher
import org.odfi.indesign.core.artifactresolver.AetherResolver
import org.eclipse.aether.artifact.Artifact
import java.net.URL
import org.odfi.indesign.core.module.lucene.LuceneIndexResource
import org.odfi.indesign.core.brain.BrainRegion
import com.idyria.osi.tea.files.FileWatcherAdvanced
import org.odfi.indesign.core.brain.Brain
import com.idyria.osi.tea.compile.ClassDomainContainer

class MavenProjectResource(p: Path) extends HarvestedFile(p) with ClassDomainSupport with LuceneIndexResource with ClassDomainContainer {

  //-- Get Pom File 
  var pomFile = new File(p.toFile(), "pom.xml")

  //-- File Watcher for this project
  //var watcher = new FileWatcher

  //-- Maven Model
  var projectModel = project(pomFile.toURI().toURL())

  //-- ID Stuff
  override def getId = projectModel.artifactId

  /**
   * On File Change, update model and invalidate dependencies
   */
  MavenProjectResource.watcher.onFileChange(this, pomFile) {
    file =>
      this.projectModel = project(pomFile.toURI().toURL())
    //this.dependencies = None
  }

  //-- Indexing
  def getLuceneDirectory = new File(p.toFile, ".indesign-lucene-index")

  //-- WWW VIew
  //var view = new MavenWWWView(this)

  // Dependencies
  //---------------------
  var dependencies = List[Artifact]()
  var dependenciesURLS = List[URL]()

  /**
   * Update Dependencies, meaning resresolve everything and add to list
   */
  def updateDependencies: Unit = {

    // Update Aether Resolver with Resolution URLS
    keepErrorsOn(this) {
      projectModel.repositories.repositories.foreach {
        r =>
          AetherResolver.config.addDefaultRemoteRepository(r.id, r.url.data.toURL)
      }
      projectModel.pluginRepositories.pluginRepositories.foreach {
        r =>
          AetherResolver.config.addDefaultRemoteRepository(r.id, r.url.data.toURL)
      }

      // Map List of dependencies
      var newDeps = projectModel.dependencies.dependencies.filter(d => d.scope == null || d.scope.toString() == "compile").map {
        d =>
          try {
            //println(s"Dep needed ${d.artifactId}:${d.groupId}:${d.version}")
            AetherResolver.resolveArtifactAndDependencies(d.groupId, d.artifactId, d.version)
          } catch {
            case re: Throwable =>
              println(re.getLocalizedMessage)
              List[Artifact]()
          }
      }.flatten.toList.distinct

      // println(s"Dep Res ${res.toList}")
      dependencies = (dependencies ::: newDeps).distinct
      dependenciesURLS = dependencies.map { d => AetherResolver.resolveArtifactsFile(d) }.filter(_.isDefined).map { _.get.toURI().toURL() }
      //dependenciesURLS = None

    }

  }

  def getDependencies = dependencies

  def getDependenciesURL = dependenciesURLS

  // Compiler Stuff
  //-----------------

  //-- Classdomain
  // var classDomain = new ClassDomain(Thread.currentThread().getContextClassLoader)
  //var classDomain = new ClassDomain(classOf[Brain].getClassLoader)
  this.createNewClassDomain(classOf[Brain].getClassLoader)

  //-- Compiler
  var compiler: Option[IDCompiler] = None

  this.onAdded {
    case h if (h.isInstanceOf[MavenProjectHarvester]) =>

      
      updateDependencies
      
    //view.originalHarvester = this.originalHarvester
    //WWWViewHarvester.deliverDirect(view)

    // println(s"Maven Project resource added to harvster")
    //MavenModule.addSubRegion(this)

    //-- 

    case _ =>
  }
  
  this.onProcess {
    //println(s"Creating Compiler for MavenProject")

    // watcher.start

    compiler match {
      case None =>
        resetClassDomain
      /*this.compiler = Some(new IDCompiler)
        this.classDomain.addURL(new File(this.path.toFile(),"target/classes").toURI().toURL() )
        this.compiler.get.addSourceOutputFolders((new File(this.path.toFile(),"src/main/scala"),new File(this.path.toFile(),"target/classes")))*/

      case _ =>
    }

  }
  
  

  /**
   * When CD is rebuild
   */
  this.onRebuildClassDomain {
    
    //-- Add Build output
    this.classdomain.get.addURL(new File(this.path.toFile(), "target/classes").toURI().toURL())
    
    //-- Update Dependencies
    updateDependencies
    var du = getDependenciesURL
    du.foreach(this.classdomain.get.addURL(_))
  }

  def resetClassDomain: Unit = {

    recreateClassDomain

    // Clear
    //var pCl = classDomain.getParent
    //classDomain.tainted = true
    //classDomain = null
    this.compiler = null
    System.gc

    // Recreate compiler
    //this.classDomain = new ClassDomain(pCl)
    this.withClassLoader(this.classdomain.get) {
      this.compiler = Some(new IDCompiler)

     // this.classDomain.addURL(new File(this.path.toFile(), "target/classes").toURI().toURL())
      this.compiler.get.addSourceOutputFolders((new File(this.path.toFile(), "src/main/scala"), new File(this.path.toFile(), "target/classes")))

      // Add dependencies
      //var urlDeps = this.getDependencies.map(_.getFile.toURI().toURL()).toArray
      var du = getDependenciesURL
      this.compiler.get.addClasspathURL(du.toArray)

    }

  }

  def forceUpdateDependencies = {

    /*dependencies = None
    dependenciesURLS = None
    getDependencies*/
    updateDependencies
    this.@->("rebuild")
    
    /*var du = getDependenciesURL

    //-- Update
    this.compiler match {
      case Some(comp) => comp.addClasspathURL(du.toArray)
      case None =>
    }

    du.foreach(this.classdomain.get.addURL(_))*/
  }

  // Compiler Request
  //--------------------

  /**
   * Compile a file
   */
  def compile(h: HarvestedFile) = {

    resetClassDomain
    this.compiler match {
      case Some(compiler) =>

        withClassLoader(classdomain.get) {
          compiler.compileFile(h.path.toFile()) match {
            case Some(errors) =>
              throw errors
            case None =>
          }

        }

      case None =>
        sys.error("Cannot compile file " + h + ", compiler was not set, maybe resource was not processed")
    }

  }

  // Builder Task
  //-----------------------
  /*class BuilderTask extends DefaultHeartTask {

    
    
    def doTask = {

    }

  }*/

}

object MavenProjectResource {
  var watcher = new FileWatcherAdvanced
  watcher.start
}