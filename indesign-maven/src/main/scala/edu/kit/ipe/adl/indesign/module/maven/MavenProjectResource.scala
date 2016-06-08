package edu.kit.ipe.adl.indesign.module.maven

import java.io.File
import java.nio.file.Path
import com.idyria.osi.tea.compile.ClassDomain
import com.idyria.osi.tea.compile.FileCompileError
import com.idyria.osi.tea.compile.IDCompiler
import edu.kit.ipe.adl.indesign.core.harvest.fs.HarvestedFile
import edu.kit.ipe.adl.indesign.core.heart.DefaultHeartTask
import edu.kit.ipe.adl.indesign.core.heart.HeartTask
import edu.kit.ipe.adl.indesign.core.module.ui.www.WWWViewHarvester
import com.idyria.osi.tea.compile.ClassDomainSupport
import com.idyria.osi.tea.files.FileWatcher
import edu.kit.ipe.adl.indesign.core.artifactresolver.AetherResolver
import org.eclipse.aether.artifact.Artifact
import java.net.URL
import edu.kit.ipe.adl.indesign.core.module.lucene.LuceneIndexResource
import edu.kit.ipe.adl.indesign.core.brain.BrainRegion
import com.idyria.osi.tea.files.FileWatcherAdvanced
import edu.kit.ipe.adl.indesign.core.brain.Brain

class MavenProjectResource(p: Path) extends HarvestedFile(p) with ClassDomainSupport with LuceneIndexResource {

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
      this.dependencies = None
  }

  //-- Indexing
  def getLuceneDirectory = new File(p.toFile, ".indesign-lucene-index")

  //-- WWW VIew
  //var view = new MavenWWWView(this)

  // Dependencies
  //---------------------
  var dependencies: Option[List[Artifact]] = None
  var dependenciesURLS: Option[Array[URL]] = None

  def getDependencies = dependencies.getOrElse {

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
      var res = projectModel.dependencies.dependencies.filter(d => d.scope == null || d.scope.toString() == "compile").map {
        d =>
          try {
            AetherResolver.resolveArtifactAndDependencies(d.groupId, d.artifactId, d.version)
          } catch {
            case re: Throwable =>
              List[Artifact]()
          }
      }.flatten.toList

      dependencies = Some(res)
      //dependenciesURLS = None

      res
    }
    dependencies.get

  }

  def getDependenciesURL = dependenciesURLS.getOrElse {

    // Map List of dependencies
    /* var res = projectModel.dependencies.dependencies.filter(d => d.scope == null || d.scope.toString() == "compile").map {
      d =>
        AetherResolver.resolveArtifactAndDependenciesClasspath(d.groupId, d.artifactId, d.version)
    }.flatten.toArray*/

    keepErrorsOn(this) {
      var res = projectModel.dependencies.dependencies.filter(d => d.scope == null || d.scope.toString() == "compile").map {
        d =>
          try {
            AetherResolver.resolveArtifactAndDependenciesClasspath(d.groupId, d.artifactId, d.version)
          } catch {
            case re: Throwable =>
              List[URL]()
          }
      }.flatten

      /* var res = AetherResolver.
            resolveArtifactAndDependenciesClasspath(projectModel.groupId, projectModel.artifactId, projectModel.version)*/

      dependenciesURLS = Some(res.toArray)

      res.toArray
    }
    dependenciesURLS.get
  }

  // Compiler Stuff
  //-----------------

  //-- Classdomain
  // var classDomain = new ClassDomain(Thread.currentThread().getContextClassLoader)
  var classDomain = new ClassDomain(classOf[Brain].getClassLoader)

  //-- Compiler
  var compiler: Option[IDCompiler] = None

  this.onAdded {
    case h if (h.isInstanceOf[MavenProjectHarvester]) =>

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

  def resetClassDomain: Unit = {

    // Clear
    var pCl = classDomain.getParent
    classDomain.tainted = true
    classDomain = null
    this.compiler = null
    System.gc

    // Recreate
    this.classDomain = new ClassDomain(pCl)
    this.withClassLoader(this.classDomain) {
      this.compiler = Some(new IDCompiler)

      this.classDomain.addURL(new File(this.path.toFile(), "target/classes").toURI().toURL())
      this.compiler.get.addSourceOutputFolders((new File(this.path.toFile(), "src/main/scala"), new File(this.path.toFile(), "target/classes")))

      // Add dependencies
      //var urlDeps = this.getDependencies.map(_.getFile.toURI().toURL()).toArray
      var du = getDependenciesURL
      this.compiler.get.addClasspathURL(du)
      du.foreach(this.classDomain.addURL(_))

    }

  }

  def forceUpdateDependencies = {
    dependencies = None
    dependenciesURLS = None
    getDependencies
    var du = getDependenciesURL
    
    //-- Update
    this.compiler.get.addClasspathURL(du)
    du.foreach(this.classDomain.addURL(_))
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

        withClassLoader(classDomain) {
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