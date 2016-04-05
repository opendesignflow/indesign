package edu.kit.ipe.adl.indesign.core.module.artifactresolver

import java.io.File
import java.net.URL
import org.apache.maven.repository.internal.MavenRepositorySystemUtils
import org.eclipse.aether.DefaultRepositorySystemSession
import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.RepositorySystemSession
import org.eclipse.aether.artifact.Artifact
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.collection.CollectRequest
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory
import org.eclipse.aether.repository.LocalRepository
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.resolution.ArtifactDescriptorRequest
import org.eclipse.aether.resolution.ArtifactResult
import org.eclipse.aether.resolution.DependencyRequest
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory
import org.eclipse.aether.spi.connector.transport.TransporterFactory
import org.eclipse.aether.transport.file.FileTransporterFactory
import org.eclipse.aether.transport.http.HttpTransporterFactory
import org.eclipse.aether.util.artifact.JavaScopes
import org.eclipse.aether.util.filter.DependencyFilterUtils
import org.eclipse.aether.graph.Dependency
import com.idyria.osi.tea.logging.TLogSource
import scala.collection.JavaConversions._
import com.idyria.osi.tea.listeners.ListeningSupport
import sun.reflect.ReflectionFactory.GetReflectionFactoryAction
import org.eclipse.aether.resolution.ArtifactRequest
import org.eclipse.aether.resolution.ArtifactResolutionException

class AetherConfiguration extends ListeningSupport {

  // Init Locator for Various Transport Factories
  //----------------------
  var locator = MavenRepositorySystemUtils.newServiceLocator();

  locator.addService(classOf[RepositoryConnectorFactory], classOf[BasicRepositoryConnectorFactory])
  locator.addService(classOf[TransporterFactory], classOf[FileTransporterFactory]);
  locator.addService(classOf[TransporterFactory], classOf[HttpTransporterFactory]);

  // Config
  //---------------------

  var localRepositoryPath = new File(sys.props("user.home") + File.separator + ".m2" + File.separator + "repository").getAbsoluteFile

  def setLocalRepositoryPath(f: File) = f.isDirectory() match {
    case false =>
      sys.error("Cannot set local repository to non directory: " + f.getAbsolutePath)
    case true =>
      this.localRepositoryPath = f
      this.@->("changed")
  }

  var repositories = List[RemoteRepository]()
  this.addDefaultRemoteRepository("central", new URL("http://central.maven.org/maven2/"))
  // addDefaultRemoteRepository("idyria.central", new URL("http://www.idyria.com/access/osi/artifactory/libs-release"))

  /**
   * Add Remote repository with default layout
   * Makes sure the Transport is supported
   */
  def addDefaultRemoteRepository(id: String, url: URL) = {

    var rb = new RemoteRepository.Builder(id, "default", url.toExternalForm())
    this.repositories = this.repositories :+ rb.build()

  }

  // System Creators
  //-----------------------
  def newRepositorySystem: RepositorySystem = {

    /*
         * Aether's components implement org.eclipse.aether.spi.locator.Service to ease manual wiring and using the
         * prepopulated DefaultServiceLocator, we only need to register the repository connector and transporter
         * factories.
         */
    //var locator = MavenRepositorySystemUtils.newServiceLocator();

    //locator.addService(classOf[RepositoryConnectorFactory], classOf[BasicRepositoryConnectorFactory]);
    //locator.addService(classOf[TransporterFactory], classOf[FileTransporterFactory]);
    //locator.addService(classOf[TransporterFactory], classOf[HttpTransporterFactory]);

    /*locator.setErrorHandler( new DefaultServiceLocator.ErrorHandler()
        {
            @Override
            public void serviceCreationFailed( Class<?> type, Class<?> impl, Throwable exception )
            {
                exception.printStackTrace();
            }
        } );*/

    return locator.getService(classOf[RepositorySystem]);

    //return org.eclipse.aether.examples.manual.ManualRepositorySystemFactory.newRepositorySystem();
    // return org.eclipse.aether.examples.guice.GuiceRepositorySystemFactory.newRepositorySystem();
    // return org.eclipse.aether.examples.sisu.SisuRepositorySystemFactory.newRepositorySystem();
    // return org.eclipse.aether.examples.plexus.PlexusRepositorySystemFactory.newRepositorySystem();
  }

  def newRepositorySystemSession(system: RepositorySystem): DefaultRepositorySystemSession = {

    // Create Session
    //--------------------
    var session = MavenRepositorySystemUtils.newSession();

    // Set Local Repository
    //--------------------------------
    session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, new LocalRepository(localRepositoryPath.getAbsolutePath)));

    //var localRepo = new LocalRepository("target/local-repo");
    //var localRepo = new LocalRepository(sys.props("user.home") + File.separator + ".m2" + File.separator + "repository");

    // session.setTransferListener( new ConsoleTransferListener() );
    // session.setRepositoryListener( new ConsoleRepositoryListener() );

    // uncomment to generate dirty trees
    // session.setDependencyGraphTransformer( null );

    return session;
  }

  def getRepositories = this.repositories

}

/**
 * Main Resolver class
 */
class AetherResolver extends TLogSource {

  val config: AetherConfiguration = new AetherConfiguration

  var system: RepositorySystem = null

  var session: DefaultRepositorySystemSession = null

  def reinit = {
    system = config.newRepositorySystem;
    session = config.newRepositorySystemSession(system);
  }
  reinit

  // Get Artifact
  //------------------------
  def getArtifactPath(groupId: String, artifactId: String, version: String): Option[File] = {

    getArtifactPath(new DefaultArtifact(s"$groupId:$artifactId:$version"))
  }

  def getArtifactPath(groupId: String, artifactId: String, version: String, classifier: String): Option[File] = {

    var a = new DefaultArtifact(s"$groupId:$artifactId:$classifier:$version")
    println(s"Resolving: " + a.getClassifier)
    getArtifactPath(a)
  }

  /**
   * Runs a resolution on the artifact to make sure it is populated
   */
  def resolveArtifact(artifact: Artifact): Option[Artifact] = {

    // Request Artifact
    //----------------------
    var descriptorRequest = new ArtifactRequest();
    descriptorRequest.setArtifact(artifact);
    descriptorRequest.setRepositories(config.getRepositories);

    // Process Result
    //--------------------------

    //var descriptorResult = system.readArtifactDescriptor(session, descriptorRequest);

    try {
      var descriptorResult = system.resolveArtifact(session, descriptorRequest)
      Some(descriptorResult.getArtifact)
    } catch {
      case e: ArtifactResolutionException =>
        //e.printStackTrace()
        None

    }

  }

  /**
   * Resolves the artifact and tries to return the file
   */
  def getArtifactPath(artifact: Artifact): Option[File] = {

    this.resolveArtifact(artifact) match {
      case Some(artifact) => Some(artifact.getFile)
      case None => None
    }

  }

  // Get Deps
  //------------------------
  def getDependencies(groupId: String, artifactId: String, version: String, scope: String): List[Dependency] = {

    this.getDependencies(groupId, artifactId, version).filter(d => d.getScope == scope)
  }

  def getDependencies(groupId: String, artifactId: String, version: String): List[Dependency] = {

    this.getDependencies(new DefaultArtifact(s"$groupId:$artifactId:$version"))

  }

  def getDependencies(artifact: Artifact): List[Dependency] = {

    var descriptorRequest = new ArtifactDescriptorRequest();
    descriptorRequest.setArtifact(artifact);
    descriptorRequest.setRepositories(config.getRepositories);

    var descriptorResult = system.readArtifactDescriptor(session, descriptorRequest);

    descriptorResult.getDependencies().toList
  }

  // Transistive Resolution
  //-------------------------------

  def resolveDependencies(groupId: String, artifactId: String, version: String, classifier: String = "jar", scope: String = "compile"): List[ArtifactResult] = {

    this.resolveDependencies(new DefaultArtifact(s"$groupId:$artifactId:$classifier:$version"), scope)
  }

  /**
   * Resolve dependencies
   *
   *
   */
  def resolveDependencies(artifact: Artifact, scope: String): List[ArtifactResult] = {

    logFine(s"Resolving dependencies for $artifact")

    //-- Prepare Scope filter
    var classpathFilter = DependencyFilterUtils.classpathFilter(scope);

    //-- Use Collect Request to collect everything about this artifact
    var collectRequest = new CollectRequest();
    collectRequest.setRoot(new Dependency(artifact, scope));
    collectRequest.setRepositories(config.getRepositories);

    //-- Dependency Request will do the actual collection and filter the scope
    var dependencyRequest = new DependencyRequest(collectRequest, classpathFilter);

    var artifactResults =
      system.resolveDependencies(session, dependencyRequest).getArtifactResults();

 //   println(s"Deps size: "+artifactResults.size())
 
    artifactResults.toList

  }

  // Classpath building helpers
  //----------------------------

  

}

object AetherResolver extends AetherResolver {

}