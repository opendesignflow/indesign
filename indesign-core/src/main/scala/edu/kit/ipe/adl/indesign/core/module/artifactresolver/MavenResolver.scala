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
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.repository.LocalRepository
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.resolution.ArtifactDescriptorRequest
import org.eclipse.aether.resolution.ArtifactRequest
import org.eclipse.aether.resolution.ArtifactResult
import org.eclipse.aether.resolution.DependencyRequest
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory
import org.eclipse.aether.spi.connector.transport.TransporterFactory
import org.eclipse.aether.spi.connector.transport.TransporterFactory
import org.eclipse.aether.transport.file.FileTransporterFactory
import org.eclipse.aether.transport.file.FileTransporterFactory
import org.eclipse.aether.transport.http.HttpTransporterFactory
import org.eclipse.aether.transport.http.HttpTransporterFactory
import org.eclipse.aether.util.artifact.JavaScopes
import org.eclipse.aether.util.filter.DependencyFilterUtils

import scala.collection.JavaConversions._

import com.idyria.osi.tea.logging.TLogSource


/*
object MavenResolver {

  // Create Settings
  //------------------

  //var settings = cli = new MavenCli();

  // Initialise Aether
  //--------------------
  var config = new AetherConfiguration
  var system = config.newRepositorySystem;

  var session = config.newRepositorySystemSession(system);

  def resolveDependencies(groupId: String, artifactId: String, version: String, scope: String): List[Dependency] = {

    this.resolveDependencies(groupId, artifactId, version).filter(d => d.getScope == scope)
  }

  def resolveDependencies(groupId: String, artifactId: String, version: String): List[Dependency] = {

    this.resolveDependencies(new DefaultArtifact(s"$groupId:$artifactId:$version"))

  }

  def getArtifactPath(groupId: String, artifactId: String, version: String) : File = {

    getArtifactPath(new DefaultArtifact(s"$groupId:$artifactId:$version"))
  }

  def getArtifactPath(artifact: Artifact) : File = {

    var descriptorRequest = new ArtifactRequest();
    descriptorRequest.setArtifact(artifact);
    descriptorRequest.setRepositories(config.newRepositories(system, session));

    //var descriptorResult = system.readArtifactDescriptor(session, descriptorRequest);
    var descriptorResult = system.resolveArtifact(session, descriptorRequest)
    descriptorResult.getArtifact.getFile
  }

  /**
   * Get Dependencies of Artifact
   */
  def resolveDependencies(artifact: Artifact): List[Dependency] = {

    var descriptorRequest = new ArtifactDescriptorRequest();
    descriptorRequest.setArtifact(artifact);
    descriptorRequest.setRepositories(config.newRepositories(system, session));

    var descriptorResult = system.readArtifactDescriptor(session, descriptorRequest);

    descriptorResult.getDependencies().toList
  }

}*/