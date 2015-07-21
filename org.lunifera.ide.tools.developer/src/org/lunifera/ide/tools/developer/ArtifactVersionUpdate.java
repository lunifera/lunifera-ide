/**
 * Copyright (c) 2011 - 2015, Lunifera GmbH (Gross Enzersdorf), Loetz KG (Heidelberg)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *         Florian Pirchner - Initial implementation
 */

package org.lunifera.ide.tools.developer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

public class ArtifactVersionUpdate {

	private static final String FILENAME = "/.artifactVersions";
	private static final String VERSION_OLD_VERSION_REGEX = "Bundle-Version: [\\w\\.]*";
	private static final String VERSION_NEW_VERSION = "Bundle-Version: %s";
	private static final String EXPORTPACKAGE_OLD_VERSION_REGEX = "version=\"[\\w\\.]*\"";
	private static final String EXPORTPACKAGE_NEW_VERSION = "version=\"%s\"";
	private static final String POM_ARTIFACT_OLD_VERSION_REGEX = "<version>[\\w\\.-]*</version>";
	private static final String POM_ARTIFACT_NEW_VERSION = "<version>%s</version>";
	private static final String FEATURE_OLD_VERSION_REGEX = "version=\"[\\w\\.]*\"";
	private static final String FEATURE_NEW_VERSION = "version=\"%s\"";

	private String pomArtifactVersion;
	private String pomRelengVersion;
	private String pomRelengP2Version;
	private String pomRelengGroupId;

	public void execute(IProject aggregator) throws Exception {
		IFile file = aggregator.getFile(FILENAME);
		if (!file.exists()) {
			return;
		}

		Properties properties = new Properties();
		properties.load(file.getContents());

		pomArtifactVersion = properties.getProperty("version");
		pomRelengVersion = properties.getProperty("relengVersion");
		pomRelengP2Version = properties.getProperty("relengP2Version");
		pomRelengGroupId = properties.getProperty("relengGroupId");

		processProject(aggregator);

	}

	private boolean isSnapshot(String version) {
		return version.endsWith("SNAPSHOT");
	}

	private void processProject(IContainer project) throws CoreException,
			IOException {

		IResource manifest = project.getFile(new Path("/META-INF/MANIFEST.MF"));
		if (manifest != null && manifest.exists()) {
			processManifest((IFile) manifest);
		}

		IResource pom = project.getFile(new Path("/pom.xml"));
		if (pom != null && pom.exists()) {
			processPom((IFile) pom);
		}

		IResource featureXml = project.getFile(new Path("/feature.xml"));
		if (featureXml != null && featureXml.exists()) {
			processFeatureXml((IFile) featureXml);
		}

		for (IResource member : project.members()) {
			if (member instanceof IContainer) {
				if (isChildProject((IContainer) member)) {
					// process children recursively
					processProject((IContainer) member);
				}
			}
		}

	}

	private boolean isChildProject(IContainer member) {
		IResource projectFile = member.getFile(new Path("/.project"));
		return projectFile != null && projectFile.exists();
	}

	private void processManifest(IFile manifest) throws CoreException,
			IOException {

		StringBuilder fileContent = generateManifest(manifest);

		manifest.setContents(new ByteArrayInputStream(fileContent.toString()
				.getBytes()), true, true, new NullProgressMonitor());
	}

	private void processPom(IFile pom) throws CoreException, IOException {

		StringBuilder fileContent = generatePom(pom);

		pom.setContents(new ByteArrayInputStream(fileContent.toString()
				.getBytes()), true, true, new NullProgressMonitor());
	}

	private void processFeatureXml(IFile featureXml) throws CoreException,
			IOException {

		StringBuilder fileContent = generateFeatureXml(featureXml);

		featureXml.setContents(new ByteArrayInputStream(fileContent.toString()
				.getBytes()), true, true, new NullProgressMonitor());
	}

	private StringBuilder generateManifest(IFile manifest)
			throws CoreException, IOException {
		// Open the file
		InputStream inputStream = manifest.getContents();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				inputStream));
		String line = null;
		String newLine = null;
		StringBuilder fileContent = new StringBuilder();
		boolean inEP = false;

		String symbolicName = null;
		// Read file line by line
		while ((line = reader.readLine()) != null) {
			newLine = line;

			if (line.startsWith("Bundle-SymbolicName")) {
				symbolicName = (String) line.subSequence(line.indexOf(" ") + 1,
						line.contains(";") ? line.indexOf(";") : line.length());
				inEP = false;
			} else if (line.startsWith("Bundle-Version")) {
				newLine = line.replaceFirst(VERSION_OLD_VERSION_REGEX,
						String.format(VERSION_NEW_VERSION, getOSGiVersion()));
				inEP = false;
			} else if (line.startsWith("Export-Package")) {
				inEP = true;
			} else if (!line.startsWith(" ")) {
				newLine = line;
				inEP = false;
			}

			if (inEP) {
				if (symbolicName == null) {
					throw new IllegalStateException(
							"Symbolic name must be located before export packages.");
				}
				String tokens[] = line.split(" ");
				if (tokens.length > 0) {
					if (tokens[1].startsWith(symbolicName)) {
						newLine = line.replaceFirst(
								EXPORTPACKAGE_OLD_VERSION_REGEX, String.format(
										EXPORTPACKAGE_NEW_VERSION,
										getOSGiEPVersion()));
					} else {
						newLine = line;
					}
				}
			}

			// update content as it is
			fileContent.append(newLine);
			fileContent.append("\n");
		}
		return fileContent;
	}

	private StringBuilder generatePom(IFile pom) throws CoreException,
			IOException {
		// Open the file
		InputStream inputStream = pom.getContents();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				inputStream));
		String line = null;
		String newLine = null;
		StringBuilder fileContent = new StringBuilder();
		boolean isParentCoordinate = false;
		boolean isPluginDef = false;
		boolean isProfileDef = false;
		boolean isDependencyDef = false;
		boolean parentIsReleng = false;

		// Read file line by line
		while ((line = reader.readLine()) != null) {
			newLine = line;

			// Remember if we are inside certain tags
			if (line.contains("<parent>")) {
				isParentCoordinate = true;
			} else if (line.contains("</parent>")) {
				isParentCoordinate = false;
				parentIsReleng = false;
			} else if (line.contains("<profile>")) {
				isProfileDef = true;
			} else if (line.contains("</profile>")) {
				isProfileDef = false;
			} else if (line.contains("<plugin>")) {
				isPluginDef = true;
			} else if (line.contains("</plugin>")) {
				isPluginDef = false;
			} else if (line.contains("<dependency>")) {
				isDependencyDef = true;
			} else if (line.contains("</dependency>")) {
				isDependencyDef = false;
			}

			// Remember if parent is an external releng project
			if (isParentCoordinate && line.contains("groupId")
					&& line.contains(pomRelengGroupId)) {
				parentIsReleng = true;
			}

			// Changes are necessary only for <version> tags outside of plugin,
			// dependency or profile definitions
			if (!isProfileDef && !isPluginDef && !isDependencyDef
					&& line.contains("<version>")) {

				// Definitions of a parent POM version
				if (isParentCoordinate) {

					// Parent is P2
					if (pom.getFullPath().toString().endsWith("p2")) {
						newLine = line.replaceFirst(
								POM_ARTIFACT_OLD_VERSION_REGEX, String.format(
										POM_ARTIFACT_NEW_VERSION,
										pomRelengP2Version));
					} else {
						// Parent is external releng project
						if (parentIsReleng) {
							newLine = line.replaceFirst(
									POM_ARTIFACT_OLD_VERSION_REGEX, String
											.format(POM_ARTIFACT_NEW_VERSION,
													pomRelengVersion));
							// Parent is within same repo
						} else {
							newLine = line.replaceFirst(
									POM_ARTIFACT_OLD_VERSION_REGEX, String
											.format(POM_ARTIFACT_NEW_VERSION,
													pomArtifactVersion));
						}
					}

					// Version of this POM (outside <parent> tags)
				} else {
					newLine = line.replaceFirst(POM_ARTIFACT_OLD_VERSION_REGEX,
							String.format(POM_ARTIFACT_NEW_VERSION,
									pomArtifactVersion));
				}
			}

			// write changes to file
			fileContent.append(newLine);
			fileContent.append("\n");
		}
		return fileContent;
	}

	private StringBuilder generateFeatureXml(IFile featureXml)
			throws CoreException, IOException {
		// Open the file
		InputStream inputStream = featureXml.getContents();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				inputStream));
		String line = null;
		String newLine = null;
		StringBuilder fileContent = new StringBuilder();
		boolean inRequire = false;
		boolean inPlugin = false;

		String symbolicName = null;
		// Read file line by line
		while ((line = reader.readLine()) != null) {
			newLine = line;

			// remember which section we are in
			if (line.contains("<requires>")) {
				inRequire = true;
			} else if (line.contains("</requires>")) {
				inRequire = false;
			} else if (line.contains("<plugin")) {
				inPlugin = true;
			} else if (inPlugin && line.contains("/>")) {
				inPlugin = false;
			}

			// only change version of feature itself
			if (!inRequire && !inPlugin && !line.contains("<?xml")) {
				newLine = line.replaceFirst(FEATURE_OLD_VERSION_REGEX,
						String.format(FEATURE_NEW_VERSION, getOSGiVersion()));
			}

			// update content as it is
			fileContent.append(newLine);
			fileContent.append("\n");
		}
		return fileContent;
	}

	private Object getOSGiVersion() {
		return pomArtifactVersion.replace("-SNAPSHOT", ".qualifier");
	}

	private Object getOSGiEPVersion() {
		return pomArtifactVersion.replace("-SNAPSHOT", "");
	}

}
