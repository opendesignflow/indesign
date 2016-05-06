package edu.kit.ipe.adl.indesign.module.maven

import java.io.File
import java.nio.file.Path
import com.idyria.osi.tea.compile.ClassDomain
import com.idyria.osi.tea.compile.FileCompileError
import com.idyria.osi.tea.compile.IDCompiler
import edu.kit.ipe.adl.indesign.core.brain.SingleBrainRegion
import edu.kit.ipe.adl.indesign.core.harvest.fs.HarvestedFile
import edu.kit.ipe.adl.indesign.core.heart.DefaultHeartTask
import edu.kit.ipe.adl.indesign.core.heart.HeartTask
import edu.kit.ipe.adl.indesign.core.module.ui.www.WWWViewHarvester
import com.idyria.osi.tea.compile.ClassDomainSupport
import com.idyria.osi.tea.files.FileWatcher
import edu.kit.ipe.adl.indesign.core.module.artifactresolver.AetherResolver
import org.eclipse.aether.artifact.Artifact
import java.net.URL

class MavenProjectResource(p: Path) extends HarvestedFile(p) with SingleBrainRegion with ClassDomainSupport {

  //-- Get Pom File 
  var pomFile = new File(p.toFile(), "pom.xml")

  //-- File Watcher for this project
  var watcher = new FileWatcher
  watcher.start

  //-- Maven Model
  var projectModel = project(pomFile.toURI().toURL())

  /**
   * On File Change, update model and invalidate dependencies
   */
  watcher.onFileChange(pomFile) {
    this.projectModel = project(pomFile.toURI().toURL())
    this.dependencies = None
  }

  override def name = projectModel.artifactId

  //-- WWW VIew
  var view = new MavenWWWView(this)

  // Dependencies
  //---------------------
  var dependencies: Option[List[Artifact]] = None
  var dependenciesURLS: Option[Array[URL]] = None

  def getDependencies = dependencies.getOrElse {

    // Update Aether Resolver with Resolution URLS
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
        AetherResolver.resolveArtifactAndDependencies(d.groupId, d.artifactId, d.version)
    }.flatten.toList

    dependencies = Some(res)
    dependenciesURLS = None

    res

  }

  def getDependenciesURL = dependenciesURLS.getOrElse {
    
    // Map List of dependencies
    var res = projectModel.dependencies.dependencies.filter(d => d.scope == null || d.scope.toString() == "compile").map {
      d =>
        AetherResolver.resolveArtifactAndDependenciesClasspath(d.groupId, d.artifactId, d.version)
    }.flatten.toArray

    dependenciesURLS = Some(res)

    res
  }

  // Compiler Stuff
  //-----------------

  //-- Classdomain
 // var classDomain = new ClassDomain(Thread.currentThread().getContextClassLoader)
  var classDomain = new ClassDomain(getClass.getClassLoader)

  //-- Compiler
  var compiler: Option[IDCompiler] = None

  this.onAdded {
    case h if (h.isInstanceOf[MavenProjectHarvester]) =>

      WWWViewHarvester.deliverDirect(view)
      // println(s"Maven Project resource added to harvster")
      MavenModule.addSubRegion(this)

    //-- 

    case _ =>
  }

  this.onProcess {
    //println(s"Creating Compiler for MavenProject")

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
    classDomain = null
    this.compiler = null
    System.gc

    // Recreate
    this.compiler = Some(new IDCompiler)
    this.classDomain = new ClassDomain(pCl)
    this.classDomain.addURL(new File(this.path.toFile(), "target/classes").toURI().toURL())
    this.compiler.get.addSourceOutputFolders((new File(this.path.toFile(), "src/main/scala"), new File(this.path.toFile(), "target/classes")))

    // Add dependencies
    //var urlDeps = this.getDependencies.map(_.getFile.toURI().toURL()).toArray
    this.compiler.get.addClasspathURL(getDependenciesURL)
    getDependenciesURL.foreach(this.classDomain.addURL(_))

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