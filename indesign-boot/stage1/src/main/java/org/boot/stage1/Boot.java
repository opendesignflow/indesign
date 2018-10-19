package org.boot.stage1;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class Boot extends Application {

	public static void main(String[] args) {

		Platform.setImplicitExit(true);
		launch(args);
	}

	Scene currentScene;
	GridPane gp;

	public void onJFXThread(Runnable r) {
		Platform.runLater(r);
	}

	public void setBootStep(String prefix, String str) {

		onJFXThread(() -> {
			Label stepLabel = (Label) gp.lookup("#bootStepLabel");
			stepLabel.setText("[" + prefix + "] " + str);
		});

	}
	
	public void changeScene(Pane root) {
		
		FadeTransition ft = new FadeTransition(Duration.millis(1000), currentScene.getRoot());
		ft.setFromValue(1.0);
		ft.setToValue(0.1);
		ft.setCycleCount(Timeline.INDEFINITE);
		ft.setAutoReverse(true);
		ft.play();
	}

	@Override
	public void start(Stage stage) throws Exception {

		Platform.setImplicitExit(true);

		System.out.println("Heelo From JDK");

		// Initial Parameters
		// -------------------------
		boolean firstStart = false;
		if (BootResolver.getInstance().config.localRepositoryPath.listFiles() == null
				|| BootResolver.getInstance().config.localRepositoryPath.listFiles().length == 0) {
			firstStart = true;
			
			
			
		}

		// Open Splash Screen
		// -------------------
		
		stage.initStyle( StageStyle.TRANSPARENT);
		stage.setWidth(600);
		stage.setHeight(300);

		gp = FXMLLoader.load(getClass().getClassLoader().getResource("stage1-splash.fxml"));

		currentScene = new Scene(gp);
		currentScene.setFill(Color.TRANSPARENT);
		stage.setScene(currentScene);
		
		//-- Init warning
		Label warningLabel = (Label) gp.lookup("#warningLabel");
		if (firstStart) {
			warningLabel.setText("First B00T run, Loading might be slow.");
		} else {
			warningLabel.setVisible(false);
		}
		
		stage.show();
		stage.centerOnScreen();

		// Loading Stage 2 main Jar
		// -----------------------
		Thread mainThread = new Thread(new Runnable() {

			public void run() {

				try {
					URLClassLoader stage2CL = null;
					File stage2POM = new File("stage2/pom.xml");
					LinkedList<URL> stage2Classpath = new LinkedList<URL>();
					if (stage2POM.exists()) {

						setBootStep("Loading App", "Opening Descriptor....");

						// Init Resolver
						// --------------------
						BootResolver.getInstance().config.localRepositoryPath = new File(
								System.getProperty("user.home") + "/.m2/repository").getCanonicalFile();
						System.out.println(
								"M2 Respo: " + BootResolver.getInstance().config.localRepositoryPath.toString());
						BootResolver.getInstance().config.addRemoteRepositories();
						BootResolver.getInstance().init();

						// Load Stage 2 using Dev Folder
						// -------------------

						System.out.println("Load Using Dev version of Stage 2");
						stage2Classpath.add(new File("stage2/target/classes").getCanonicalFile().toURI().toURL());

						// stage2CL = new URLClassLoader(new URL[] {new
						// File("stage2/target/classes").toURI().toURL()});

						// Resolve Dependencies
						// ------------------------------
						Document stage2POMXML = DocumentBuilderFactory.newInstance().newDocumentBuilder()
								.parse(stage2POM);

						setBootStep("Loading App", "Resolving dependencies....");

						NodeList dependencies = stage2POMXML.getDocumentElement().getElementsByTagName("dependencies");
						if (dependencies != null && dependencies.getLength() > 0) {
							NodeList dependency = ((Element) dependencies.item(0)).getElementsByTagName("dependency");

							if (dependency != null && dependency.getLength() > 0) {

								for (int i = 0; i < dependency.getLength(); i++) {

									Element dep = (Element) dependency.item(i);

									// Gather Dependency Data
									NodeList artifactIdL = dep.getElementsByTagName("artifactId");
									String artifactId = artifactIdL == null || artifactIdL.getLength() == 0 ? null
											: artifactIdL.item(0).getTextContent();

									if (artifactId == null || artifactId == "") {
										throw new RuntimeException("Stage2 - Dependency without Artifact ID");
									}

									NodeList groupIdL = dep.getElementsByTagName("groupId");
									String groupId = groupIdL == null || groupIdL.getLength() == 0 ? null
											: groupIdL.item(0).getTextContent();

									if (groupId == null || groupId == "") {
										throw new RuntimeException("Stage2 - Dependency without Group ID");
									}

									NodeList versionL = dep.getElementsByTagName("version");
									String version = versionL == null || versionL.getLength() == 0 ? null
											: versionL.item(0).getTextContent();

									if (version == null || version == "") {
										throw new RuntimeException("Stage2 - Dependency without Version");
									}

									NodeList scopeL = dep.getElementsByTagName("scope");
									String scope = scopeL == null || scopeL.getLength() == 0 ? "compile"
											: scopeL.item(0).getTextContent().trim();

									// resolve
									if (!scope.equals("provided")) {

										// Resolving
										System.out
												.println("Resolving Dependencies for groupId=" + groupId + ",artifatId="
														+ artifactId + ",version=" + version + ",scope=" + scope);

										Artifact depArt = new DefaultArtifact(groupId, artifactId, null, "jar",
												version);
										List<URL> cp;
										try {
											cp = BootResolver.getInstance().resolveArtifactAndDependenciesAsURL(depArt,
													scope);
											// Add to CP List
											stage2Classpath.addAll(cp);
										} catch (ArtifactResolutionException | MalformedURLException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}

									}

								}

							}
						}

						// Resolve dependencies based on POM content

					}
					// Resolve Stage 2 based on IDs and remote repositories
					// --------------
					else {

						// -- Init Boot resolver
						BootResolver.getInstance().config.addRemoteRepositories();
						BootResolver.getInstance().init();

					}

					// Build Stage 2 ClassLoader
					// ----------------
					for (URL u : stage2Classpath) {
						System.out.println("-> CP: " + u);
					}
					stage2CL = URLClassLoader.newInstance(stage2Classpath.toArray(new URL[stage2Classpath.size()]),
							Thread.currentThread().getContextClassLoader());

					// Load Stage 2 Entry Point
					// ------------------
					setBootStep("Loading App", "Starting App...");
					Class<?> stage2Class = stage2CL.loadClass("org.indesign.boot.Stage2");
					stage2Class.getMethod("launch", Boot.class).invoke(null, Boot.this);
					// stage2Class.getMethod("start", Boot.class).invoke(stage2, this);
				} catch (Throwable e) {
					e.printStackTrace();
				}

			}

		});
		// EOF Thread def

		mainThread.setDaemon(true);
		mainThread.start();

	}

}
