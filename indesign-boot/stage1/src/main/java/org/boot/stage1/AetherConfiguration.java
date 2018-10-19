package org.boot.stage1;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.repository.internal.DefaultArtifactDescriptorReader;
import org.apache.maven.repository.internal.DefaultVersionRangeResolver;
import org.apache.maven.repository.internal.DefaultVersionResolver;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.apache.maven.repository.internal.SnapshotMetadataGeneratorFactory;
import org.apache.maven.repository.internal.VersionsMetadataGeneratorFactory;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.ArtifactDescriptorReader;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.impl.MetadataGeneratorFactory;
import org.eclipse.aether.impl.VersionRangeResolver;
import org.eclipse.aether.impl.VersionResolver;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;

public class AetherConfiguration {

	DefaultServiceLocator locator = new DefaultServiceLocator();

	File localRepositoryPath = new File("libs/.m2/repository").getAbsoluteFile();

	LinkedList<RemoteRepository> repositories = new LinkedList<RemoteRepository>();

	public AetherConfiguration() {

		locator.addService(ArtifactDescriptorReader.class, DefaultArtifactDescriptorReader.class);
		locator.addService(VersionResolver.class, DefaultVersionResolver.class);
		locator.addService(VersionRangeResolver.class, DefaultVersionRangeResolver.class);
		locator.addService(MetadataGeneratorFactory.class, SnapshotMetadataGeneratorFactory.class);
		locator.addService(MetadataGeneratorFactory.class, VersionsMetadataGeneratorFactory.class);
		locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
		locator.addService(TransporterFactory.class, FileTransporterFactory.class);
		locator.addService(TransporterFactory.class, HttpTransporterFactory.class);

		localRepositoryPath = new File("libs/.m2/repository").getAbsoluteFile();

	}

	// Config
	// ---------------------

	public void addRemoteRepositories() {
		try {
			this.addDefaultRemoteRepository("central", new URL("http://central.maven.org/maven2/"));
			this.addDefaultRemoteRepository("odfi.releases",
					new URL("http://www.opendesignflow.org/maven/repository/internal/"));
			this.addDefaultRemoteRepository("odfi.snapshots",
					new URL("http://www.opendesignflow.org/maven/repository/snapshots/"));

			this.addDefaultRemoteRepository("sonatype", new URL("http://oss.sonatype.org/content/groups/public"));

		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	// addDefaultRemoteRepository("idyria.central", new
	// URL("http://www.idyria.com/access/osi/artifactory/libs-release"))

	/**
	 * Add Remote repository with default layout Makes sure the Transport is
	 * supported
	 */
	public RemoteRepository addDefaultRemoteRepository(String id, URL url) {

		RemoteRepository.Builder rb = new RemoteRepository.Builder(id, "default", url.toExternalForm());

		if (id.contains("snapshots")) {
			RepositoryPolicy policy = new RepositoryPolicy(true, RepositoryPolicy.UPDATE_POLICY_ALWAYS,
					RepositoryPolicy.CHECKSUM_POLICY_WARN);
			rb.setSnapshotPolicy(policy);
		} else {
			RepositoryPolicy policy = new RepositoryPolicy(false, RepositoryPolicy.UPDATE_POLICY_ALWAYS,
					RepositoryPolicy.CHECKSUM_POLICY_WARN);
			rb.setSnapshotPolicy(policy);
		}

		RemoteRepository rep = rb.build();
		this.repositories.add(rep);

		return rep;

	}

	// System Creators
	// -----------------------
	public RepositorySystem newRepositorySystem() {

		return locator.getService(RepositorySystem.class);

	}

	public DefaultRepositorySystemSession newRepositorySystemSession(RepositorySystem system) {

		// Create Session
		// --------------------
		DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

		// Set Local Repository
		// --------------------------------
		LocalRepository lr = new LocalRepository(localRepositoryPath.getAbsolutePath());
		LocalRepositoryManager manager = system.newLocalRepositoryManager(session, lr);
		
		session.setLocalRepositoryManager(manager);

		return session;
	}

	public List<RemoteRepository> getRepositories() {
		return this.repositories;
	}

}
