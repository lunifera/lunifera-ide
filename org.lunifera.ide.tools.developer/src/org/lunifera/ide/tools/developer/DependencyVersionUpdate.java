package org.lunifera.ide.tools.developer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

public class DependencyVersionUpdate {

	private static final String FILENAME = "/.depVersions";
	private static final String REQUIREBUNDLE_OLD_VERSION_REGEX = "bundle-version=\"[\\w\\.\\[\\),]*\"";
	private static final String REQUIREBUNDLE_NEW_VERSION = "bundle-version=\"%s\"";

	private static final String IMPORTPACKAGE_OLD_VERSION_REGEX = "version=\"[\\w\\.\\[\\),]*\"";
	private static final String IMPORTPACKAGE_NEW_VERSION = "version=\"%s\"";

	private static final String FEATURE_OLD_VERSION_REGEX = "version=\"[\\w\\.]*\"";
	private static final String FEATURE_NEW_VERSION = "version=\"%s\"";

	private List<VersionDef> defs;

	public void execute(IProject aggregator) throws Exception {
		IFile file = aggregator.getFile(FILENAME);
		if (!file.exists()) {
			return;
		}

		defs = parseDefs(file);

		processProject(aggregator);
	}

	private void processProject(IContainer project) throws CoreException,
			IOException {

		IResource manifest = project.getFile(new Path("/META-INF/MANIFEST.MF"));
		if (manifest != null && manifest.exists()) {
			processManifest((IFile) manifest);
		}

		IResource featureXml = project.getFile(new Path("/feature.xml"));
		if (featureXml != null && featureXml.exists()) {
			processFeatureXml((IFile) featureXml);
		}

		for (IResource member : project.members()) {
			if (member instanceof IContainer) {
				if (isChildProject((IContainer) member)) {
					// also process childs
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

	private StringBuilder generateManifest(IFile manifest)
			throws CoreException, IOException {
		// Open the file
		InputStream inputStream = manifest.getContents();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				inputStream));
		String line = null;
		String newLine = null;
		StringBuilder fileContent = new StringBuilder();
		String header = "";

		// Read file line by line
		while ((line = reader.readLine()) != null) {
			newLine = line;

			// Find out in which section we are, store this info
			if (line.startsWith("Import-Package")) {
				header = "ip";
			} else if (line.startsWith("Require-Bundle")) {
				header = "rb";
			} else if (!line.startsWith(" ")) { // any other section >>> no
												// changes
				header = "";
				fileContent.append(line);
				fileContent.append("\n");
				continue;
			}

			// Changes necessary only in Import or Require sections
			if (!header.equals("")) {

				// Get bundle name (after first space)
				String tokens[] = line.split(" ");
				if (tokens.length > 0) {
					String name = tokens[1];

					// Replace version with version in .depVersions file
					VersionDef def = findVersionDef(name);
					if (def != null) {
						switch (header) {
						case "ip":
							newLine = def.createImportPackage(line);
							break;
						case "rb":
							newLine = def.createRequireBundle(line);
							break;
						default:
							throw new IllegalStateException();
						}
					}
				}
			}

			// write result to file
			fileContent.append(newLine);
			fileContent.append("\n");
		}
		return fileContent;
	}

	private void processFeatureXml(IFile featureXml) throws CoreException,
			IOException {

		StringBuilder fileContent = generateFeatureXml(featureXml);

		featureXml.setContents(new ByteArrayInputStream(fileContent.toString()
				.getBytes()), true, true, new NullProgressMonitor());

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
			} else if (line.contains("</plugin>")) {
				inPlugin = false;
			}

			// change versions of dependencies in "requires"
			if (inRequire && !inPlugin) {

				// Get bundle name (after first space)
				String[] tokens = line.split("\"");
				if (tokens.length > 1) {
					String name = tokens[1];

					// Replace version with version in .depVersions file
					VersionDef def = findVersionDef(name);
					newLine = def.createFeatureRequires(line);
				}
			}

			// write result to file
			fileContent.append(newLine);
			fileContent.append("\n");
		}
		return fileContent;
	}

	private VersionDef findVersionDef(String name) {

		for (VersionDef def : defs) {
			if (name.startsWith(def.getName())) {
				return def;
			}
		}

		return null;
	}

	private List<VersionDef> parseDefs(IFile file) throws CoreException,
			IOException {

		List<VersionDef> defs = new ArrayList<VersionDef>();

		// Open the file
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				file.getContents()));
		String line = null;
		// Read file line by line
		while ((line = reader.readLine()) != null) {
			VersionDef def = new VersionDef(line);
			defs.add(def);
		}

		return defs;
	}

	private static class VersionDef {
		private String name;
		private String minVersion;
		private String maxVersion;

		public VersionDef(String line) {
			String[] tokens = line.split(";");
			for (int i = 0; i < tokens.length; i++) {
				String token = tokens[i];
				if (i == 0) {
					name = token;
				} else if (token.startsWith("min")) {
					minVersion = token.replace("min=", "");
				} else if (token.startsWith("max")) {
					maxVersion = token.replace("max=", "");
				}
			}
		}

		public String getName() {
			return name;
		}

		public String getMinVersion() {
			return minVersion;
		}

		public String getMaxVersion() {
			return maxVersion;
		}

		public String createImportPackage(String input) {
			String newLine = input.replaceFirst(
					IMPORTPACKAGE_OLD_VERSION_REGEX, String.format(
							IMPORTPACKAGE_NEW_VERSION, createVersionRange()));
			return newLine;
		}

		public String createRequireBundle(String input) {
			String newLine = input.replaceFirst(
					REQUIREBUNDLE_OLD_VERSION_REGEX, String.format(
							REQUIREBUNDLE_NEW_VERSION, createVersionRange()));
			return newLine;
		}

		public String createFeatureRequires(String input) {
			String newLine = input.replaceFirst(FEATURE_OLD_VERSION_REGEX,
					String.format(FEATURE_NEW_VERSION, minVersion));
			return newLine;
		}

		private String createVersionRange() {
			StringBuilder sb = new StringBuilder();
			if (useMaxVersion()) {
				sb.append("[");
			}

			sb.append(minVersion);

			if (useMaxVersion()) {
				sb.append(",");
				sb.append(maxVersion);
			}

			if (useMaxVersion()) {
				sb.append(")");
			}

			return sb.toString();
		}

		private boolean useMaxVersion() {
			return maxVersion != null;
		}
	}

}
