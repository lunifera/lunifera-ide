/*******************************************************************************
 * Copyright (c) 2009 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contribution:
 * 		Florian Pirchner - Changed code for Lunifera
 *
 *******************************************************************************/
package org.lunifera.ide.core.ui.project;

import static org.eclipse.xtext.ui.util.JREContainerProvider.getDefaultJREContainerEntry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.BuildPathSupport;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.xtext.ui.util.JavaProjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
@SuppressWarnings("restriction")
public class BundleProjectFactory extends JavaProjectFactory {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(BundleProjectFactory.class);

	protected List<String> requiredBundles;
	protected List<String> exportedPackages;
	protected List<String> importedPackages;
	protected String activatorClassName;
	protected String version;

	public BundleProjectFactory addRequiredBundles(List<String> requiredBundles) {
		if (this.requiredBundles == null)
			this.requiredBundles = Lists.newArrayList();
		this.requiredBundles.addAll(requiredBundles);
		return this;
	}

	public BundleProjectFactory addExportedPackages(
			List<String> exportedPackages) {
		if (this.exportedPackages == null)
			this.exportedPackages = Lists.newArrayList();
		this.exportedPackages.addAll(exportedPackages);
		return this;
	}

	public BundleProjectFactory addImportedPackages(
			List<String> importedPackages) {
		if (this.importedPackages == null)
			this.importedPackages = Lists.newArrayList();
		this.importedPackages.addAll(importedPackages);
		return this;
	}

	public BundleProjectFactory setActivatorClassName(String activatorClassName) {
		this.activatorClassName = activatorClassName;
		return this;
	}

	@SuppressWarnings("restriction")
	@Override
	protected void enhanceProject(IProject project, SubMonitor monitor,
			Shell shell) throws CoreException {
		super.enhanceProject(project, monitor, shell);
		SubMonitor subMonitor = SubMonitor.convert(monitor, 10);
		try {
			subMonitor.subTask("Configures the bundle " + projectName);
			IJavaProject javaProject = JavaCore.create(project);
			List<IClasspathEntry> classpathEntries = new ArrayList<IClasspathEntry>();
			for (final IProject referencedProject : project
					.getReferencingProjects()) {
				final IClasspathEntry referencedProjectClasspathEntry = JavaCore
						.newProjectEntry(referencedProject.getFullPath());
				classpathEntries.add(referencedProjectClasspathEntry);
			}
			for (final String folderName : folders) {
				final IFolder sourceFolder = project.getFolder(folderName);
				final IClasspathEntry srcClasspathEntry = JavaCore
						.newSourceEntry(sourceFolder.getFullPath());
				classpathEntries.add(srcClasspathEntry);
			}

			IClasspathEntry defaultJREContainerEntry = getDefaultJREContainerEntry();
			classpathEntries.add(defaultJREContainerEntry);
			addMoreClasspathEntriesTo(classpathEntries);

			javaProject.setRawClasspath(classpathEntries
					.toArray(new IClasspathEntry[classpathEntries.size()]),
					subMonitor.newChild(1));
			javaProject
					.setOutputLocation(
							new Path(
									"/" + project.getName() + "/target/classes"), subMonitor.newChild(1)); //$NON-NLS-1$ //$NON-NLS-2$

			String executionEnvironmentId = JavaRuntime
					.getExecutionEnvironmentId(defaultJREContainerEntry
							.getPath());
			if (executionEnvironmentId != null) {
				BuildPathSupport.setEEComplianceOptions(javaProject,
						executionEnvironmentId, null);
			}
		} catch (JavaModelException e) {
			LOGGER.error(e.getMessage(), e);
		}

		// create bundle
		createManifest(project, subMonitor.newChild(1));
		createBuildProperties(project, subMonitor.newChild(1));
	}

	@Override
	protected void addMoreClasspathEntriesTo(
			List<IClasspathEntry> classpathEntries) {
		super.addMoreClasspathEntriesTo(classpathEntries);
		classpathEntries.add(JavaCore.newContainerEntry(new Path(
				"org.eclipse.pde.core.requiredPlugins"))); //$NON-NLS-1$
	}

	protected void createBuildProperties(IProject project,
			IProgressMonitor progressMonitor) {
		final StringBuilder content = new StringBuilder("source.. = ");
		for (final Iterator<String> iterator = folders.iterator(); iterator
				.hasNext();) {
			content.append(iterator.next()).append('/');
			if (iterator.hasNext()) {
				content.append(",\\\n");
				// source.. =
				content.append("          ");
			}
		}
		content.append("\n");
		content.append("bin.includes = META-INF/,\\\n");
		content.append("               .,\\\n");
		content.append("               plugin.xml");

		createFile("build.properties", project, content.toString(),
				progressMonitor);
	}

	protected void createManifest(IProject project,
			IProgressMonitor progressMonitor) throws CoreException {
		final StringBuilder content = new StringBuilder(
				"Manifest-Version: 1.0\n");
		content.append("Bundle-ManifestVersion: 2\n");
		content.append("Bundle-Name: " + projectName + "\n");
		content.append("Bundle-Vendor: Lunifera GmbH\n");
		content.append(String.format("Bundle-Version: %s\n", version));
		content.append("Bundle-SymbolicName: " + projectName
				+ "; singleton:=true\n");
		if (null != activatorClassName) {
			content.append("Bundle-Activator: " + activatorClassName + "\n");
		}
		content.append("Bundle-ActivationPolicy: lazy\n");

		addToContent(content, requiredBundles, "Require-Bundle");
		addToContent(content, exportedPackages, "Export-Package");
		addToContent(content, importedPackages, "Import-Package");

		content.append("Bundle-RequiredExecutionEnvironment: J2SE-1.7\n");

		final IFolder metaInf = project.getFolder("META-INF");
		SubMonitor subMonitor = SubMonitor.convert(progressMonitor, 2);
		try {
			if (metaInf.exists())
				metaInf.delete(false, progressMonitor);
			metaInf.create(false, true, subMonitor.newChild(1));
			createFile("MANIFEST.MF", metaInf, content.toString(),
					subMonitor.newChild(1));
		} finally {
			subMonitor.done();
		}
	}

	protected void addToContent(final StringBuilder content,
			List<String> entries, String prefix) {
		if (entries != null && !entries.isEmpty()) {
			content.append(prefix).append(": ").append(entries.get(0));
			for (int i = 1, x = entries.size(); i < x; i++) {
				content.append(",\n ").append(entries.get(i));
			}
			content.append("\n");
		}
	}

	@Override
	public BundleProjectFactory addBuilderIds(String... builderIds) {
		return (BundleProjectFactory) super.addBuilderIds(builderIds);
	}

	@Override
	public BundleProjectFactory addFolders(List<String> folders) {
		return (BundleProjectFactory) super.addFolders(folders);
	}

	@Override
	public BundleProjectFactory setProjectName(String projectName) {
		return (BundleProjectFactory) super.setProjectName(projectName);
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public BundleProjectFactory addProjectNatures(String... projectNatures) {
		return (BundleProjectFactory) super.addProjectNatures(projectNatures);
	}

	@Override
	public BundleProjectFactory addReferencedProjects(
			List<IProject> referencedProjects) {
		return (BundleProjectFactory) super
				.addReferencedProjects(referencedProjects);
	}

	@Override
	public BundleProjectFactory setLocation(IPath location) {
		return (BundleProjectFactory) super.setLocation(location);
	}

	@Override
	public BundleProjectFactory addWorkingSets(List<IWorkingSet> workingSets) {
		return (BundleProjectFactory) super.addWorkingSets(workingSets);
	}
}
