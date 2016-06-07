package edu.kit.ipe.adl.indesign.core.artifactresolver

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
import com.idyria.osi.tea.logging.TLog
import org.eclipse.aether.impl.MetadataGeneratorFactory
import org.apache.maven.repository.internal.DefaultVersionRangeResolver
import org.apache.maven.repository.internal.DefaultVersionResolver
import org.apache.maven.repository.internal.VersionsMetadataGeneratorFactory
import org.apache.maven.repository.internal.DefaultArtifactDescriptorReader
import org.eclipse.aether.impl.DefaultServiceLocator
import org.eclipse.aether.impl.VersionRangeResolver
import org.eclipse.aether.impl.ArtifactDescriptorReader
import org.eclipse.aether.impl.VersionResolver
import org.apache.maven.repository.internal.SnapshotMetadataGeneratorFactory

class AetherConfiguration extends ListeningSupport {

  // Init Locator for Various Transport Factories
  //----------------------
  // var locator = MavenRepositorySystemUtils.newServiceLocator();

  var locator = new DefaultServiceLocator();
  locator.addService(classOf[ArtifactDescriptorReader], classOf[DefaultArtifactDescriptorReader]);
  locator.addService(classOf[VersionResolver], classOf[DefaultVersionResolver]);
  locator.addService(classOf[VersionRangeResolver], classOf[DefaultVersionRangeResolver]);
  locator.addService(classOf[MetadataGeneratorFactory], classOf[SnapshotMetadataGeneratorFactory]);
  locator.addService(classOf[MetadataGeneratorFactory], classOf[VersionsMetadataGeneratorFactory]);

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

  //this.session.setW

  //session.setWorkspaceReader(new EclipseWorkspaceReader(new File("/home/rleys/eclipse-workspaces/mars/")))
  //TLog.setLevel(classOf[EclipseWorkspaceReader], TLog.Level.FULL)

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
  def resolveArtifact(groupId: String, artifactId: String, version: String, classifier: String = "jar"): Option[Artifact] = {
    resolveArtifact(new DefaultArtifact(s"$groupId:$artifactId:$classifier:$version"))
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

    // println(s"Resolving with : $session + $dependencyRequest")

    try {
      var artifactResults =
        system.resolveDependencies(session, dependencyRequest).getArtifactResults();
      artifactResults.toList
    } catch {
      case e: NullPointerException =>
        logWarn("Nullpointer exception while resolving dependencies")
        List()
    }

    //   println(s"Deps size: "+artifactResults.size())

  }

  // Classpath building helpers
  //----------------------------

  def resolveArtifactAndDependencies(groupId: String, artifactId: String, version: String, classifier: String = "jar", scope: String = "compile"): List[Artifact] = {

    resolveArtifactAndDependencies(new DefaultArtifact(s"$groupId:$artifactId:$classifier:$version"), scope)
  }

  /**
   * Artifact and Dependencies are resolved
   */
  def resolveArtifactAndDependencies(artifact: Artifact, scope: String): List[Artifact] = {

    var realArt = artifact.getFile match {
      case null => this.resolveArtifact(artifact)

      case _ => Some(artifact)
    }
    realArt match {
      case Some(art) =>

        var res = this.resolveDependencies(artifact, scope)
        res.find { p => p.isMissing() } match {
          case Some(missing) =>

            throw new RuntimeException(s"Cannot Resolve Artifact and Dependencies of $artifact , artifact ${missing.getArtifact} is missing")
          case None =>

            // Return the list of artifacts, but resolve actual file paths if there is a workspace reader
            res.map {
              a =>
                if (session.getWorkspaceReader != null) {

                  a.getArtifact.setFile(resolveArtifactsFile(a.getArtifact).get)
                  //println(s"Resolving file for ${a.getArtifact} because of ws reader "+a.getArtifact.getExtension+" -> "+resolveArtifactsFile(a.getArtifact))
                }
                a.getArtifact

            }
        }

      case None =>
        throw new RuntimeException(s"Cannot Resolve Artifact and Dependencies of $artifact , artifact not found")
    }

  }

  /**
   * Artifact and Dependencies are resolved
   */
  def resolveArtifactAndDependenciesClasspath(artifact: Artifact, scope: String): List[URL] = {

    this.resolveArtifactAndDependencies(artifact, scope).map {
      a =>
        session.getWorkspaceReader match {
          case null =>
            a.getFile.toURI().toURL()
          case p =>
            resolveArtifactsFile(a).get.toURI().toURL()
        }
    }

  }

  def resolveArtifactAndDependenciesClasspath(groupId: String, artifactId: String, version: String, classifier: String = "jar", scope: String = "compile"): List[URL] = {
    this.resolveArtifactAndDependenciesClasspath(new DefaultArtifact(s"$groupId:$artifactId:$classifier:$version"), scope)
  }

  /**
   * If the artifact is a jar and the file is a pom.xml, return the local compiling output as dependebcy file
   */
  def resolveArtifactsFile(art: Artifact): Option[File] = {

    art.getFile match {
      case null => None
      case f if (f.getAbsolutePath.endsWith("pom.xml") && art.getExtension == "jar") =>
        Some(new File(f.getAbsoluteFile.getParentFile, "target/classes"))
      case f => Some(f)

    }

  }

}

object AetherResolver extends AetherResolver {

}