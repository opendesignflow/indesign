package org.boot.stage1;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositoryEvent;
import org.eclipse.aether.RepositoryListener;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RequestTrace;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.transfer.TransferCancelledException;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transfer.TransferListener;
import org.eclipse.aether.util.filter.DependencyFilterUtils;


public class BootResolver {

	// INSTANCE("Singleton");

	private static BootResolver inst;

	// Configuration
	// --------------
	AetherConfiguration config = new AetherConfiguration();

	// System and Session
	// ------------------------
	RepositorySystem system;
	DefaultRepositorySystemSession session;

	public void init() {
		system = config.newRepositorySystem();
		session = config.newRepositorySystemSession(system);
	}

	public static BootResolver getInstance() {
		if (inst == null) {
			inst = new BootResolver();
		}
		return inst;
	}

	public File resolveArtifactFile(Artifact artifact) throws ArtifactResolutionException {

		// Request Artifact
		// ----------------------
		ArtifactRequest descriptorRequest = new ArtifactRequest();
		descriptorRequest.setArtifact(artifact);
		descriptorRequest.setRepositories(config.getRepositories());

		// Process Result
		// --------------------------

		// var descriptorResult = system.readArtifactDescriptor(session,
		// descriptorRequest);

		try {
			ArtifactResult descriptorResult = system.resolveArtifact(session, descriptorRequest);
			return descriptorResult.getArtifact().getFile();
		} catch (ArtifactResolutionException e) {
			// e.printStackTrace()
			System.out.println("Resolving $artifact failed: " + e.getLocalizedMessage());
			throw e;

		}

	}

	public Stream<Artifact> resolveArtifactDependencies(Artifact artifact, String scope) {

		// -- Prepare Scope filter
		DependencyFilter classpathFilter = DependencyFilterUtils.classpathFilter(scope);

		// -- Use Collect Request to collect everything about this artifact
		CollectRequest collectRequest = new CollectRequest();
		collectRequest.setRoot(new Dependency(artifact, scope));
		collectRequest.setRepositories(config.getRepositories());
		
	

		// -- Dependency Request will do the actual collection and filter the scope
		DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, classpathFilter);

	
		
		// println(s"Resolving with : $session + $dependencyRequest")

		try {
			
		/*	session.setTransferListener(new TransferListener() {
				
				@Override
				public void transferSucceeded(TransferEvent event) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void transferStarted(TransferEvent event) throws TransferCancelledException {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void transferProgressed(TransferEvent event) throws TransferCancelledException {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void transferInitiated(TransferEvent event) throws TransferCancelledException {
					System.out.println("Transfering: "+event.getResource().getResourceName()+" In: "+event.getResource().getRepositoryUrl());
					
					
				}
				
				@Override
				public void transferFailed(TransferEvent event) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void transferCorrupted(TransferEvent event) throws TransferCancelledException {
					// TODO Auto-generated method stub
					
				}
			});
			session.setRepositoryListener(new RepositoryListener() {
				
				@Override
				public void metadataResolving(RepositoryEvent event) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void metadataResolved(RepositoryEvent event) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void metadataInvalid(RepositoryEvent event) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void metadataInstalling(RepositoryEvent event) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void metadataInstalled(RepositoryEvent event) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void metadataDownloading(RepositoryEvent event) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void metadataDownloaded(RepositoryEvent event) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void metadataDeploying(RepositoryEvent event) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void metadataDeployed(RepositoryEvent event) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void artifactResolving(RepositoryEvent event) {
					System.out.println("Resolving: "+event.getArtifact().getArtifactId()+" In: "+event.getRepository().getId());
					
				}
				
				@Override
				public void artifactResolved(RepositoryEvent event) {
					System.out.println("Resolving: "+event.getArtifact().getArtifactId()+" In: "+event.getRepository().getId());
					
					
				}
				
				@Override
				public void artifactInstalling(RepositoryEvent event) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void artifactInstalled(RepositoryEvent event) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void artifactDownloading(RepositoryEvent event) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void artifactDownloaded(RepositoryEvent event) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void artifactDescriptorMissing(RepositoryEvent event) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void artifactDescriptorInvalid(RepositoryEvent event) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void artifactDeploying(RepositoryEvent event) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void artifactDeployed(RepositoryEvent event) {
					// TODO Auto-generated method stub
					
				}
			});*/
			
			List<ArtifactResult> artifactResults = system.resolveDependencies(session, dependencyRequest)
					.getArtifactResults();

			return artifactResults.stream().map(res -> res.getArtifact());
			
			
		} catch (Throwable e) {
			e.printStackTrace();
			return Stream.of();
		}
	

	}
	
	public List<URL> resolveArtifactAndDependenciesAsURL(Artifact artifact,String scope) throws ArtifactResolutionException, MalformedURLException {
	 
		// Resolve main artifact file
		 File artifactFile;
		 if (artifact.getFile()!=null) {
			 artifactFile = artifact.getFile();
		 } else {
			 artifactFile = this.resolveArtifactFile(artifact);
		 }
		 
		 // Resolve dependencies
		 Stream<Artifact> deps = resolveArtifactDependencies(artifact, scope);
		
		 List<URL> depsURLS = deps.map (art-> {
			try {
				return art.getFile().toURI().toURL();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}).collect(Collectors.toList());
	
		 
		 return depsURLS;
	 }
	 

}
