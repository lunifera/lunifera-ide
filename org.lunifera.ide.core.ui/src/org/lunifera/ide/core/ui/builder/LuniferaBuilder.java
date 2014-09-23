/**
 * Copyright (c) 2012 Lunifera GmbH (Austria) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Florian Pirchner - initial API and implementation
 */
package org.lunifera.ide.core.ui.builder;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.ProgressMonitorWrapper;
import org.eclipse.core.runtime.SubMonitor;
import org.lunifera.ide.core.api.i18n.CoreUtil;
import org.lunifera.ide.core.api.i18n.II18nRegistry;
import org.lunifera.ide.core.ui.CoreUiActivator;
import org.osgi.service.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

public class LuniferaBuilder extends IncrementalProjectBuilder {

	public static final String BUILDER_ID = CoreUtil.BUILDER_ID;

	private static final Logger LOGGER = LoggerFactory
			.getLogger(LuniferaBuilder.class);

	@Inject
	private II18nRegistry i18nRegistry;

	private boolean firstBuild = true;

	public LuniferaBuilder() {

	}

	@Override
	protected IProject[] build(int kind, Map<String, String> args,
			IProgressMonitor monitor) throws CoreException {

		long startTime = System.currentTimeMillis();
		try {
			if (monitor != null) {
				final String taskName = "Building" + getProject().getName()
						+ ": "; //$NON-NLS-1$
				monitor = new ProgressMonitorWrapper(monitor) {
					@Override
					public void subTask(String name) {
						super.subTask(taskName + name);
					}
				};
			}
			SubMonitor progress = SubMonitor.convert(monitor, 8);
			if (kind == FULL_BUILD || firstBuild) {
				firstBuild = false;
				fullBuild(progress.newChild(1));
			} else {
				IResourceDelta delta = getDelta(getProject());
				if (delta == null || isOpened(delta)) {
					fullBuild(progress.newChild(1));
				} else {
					incrementalBuild(delta, progress.newChild(1));
				}
			}
			monitor.worked(8);
		} catch (CoreException e) {
			LOGGER.error(e.getMessage(), e);
			throw e;
		} catch (OperationCanceledException e) {
			forgetLastBuiltState();
			throw e;
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		} finally {
			if (monitor != null) {
				monitor.done();
			}
			LOGGER.info("Build " + getProject().getName() + " in "
					+ (System.currentTimeMillis() - startTime) + " ms");
		}

		sendBuildEvent();

		return getProject().getReferencedProjects();
	}

	/**
	 * Sends an event that a build was done.
	 */
	private void sendBuildEvent() {
		Event event = new Event(CoreUtil.EVENT_TOPIC__BUILDER,
				Collections.<String, String> emptyMap());
		CoreUiActivator.getDefault().getEventAdmin().sendEvent(event);
	}

	/**
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the
	 *            user. It is the caller's responsibility to call done() on the
	 *            given monitor. Accepts null, indicating that no progress
	 *            should be reported and that the operation cannot be cancelled.
	 */
	protected void incrementalBuild(IResourceDelta delta,
			final IProgressMonitor monitor) throws CoreException {
		final SubMonitor progress = SubMonitor.convert(monitor,
				"Collection i18n", 4);
		progress.subTask("Collecting i18n");
		if (progress.isCanceled())
			throw new OperationCanceledException();
		progress.worked(2);

		IProject project = getProject();
		if (!project.getDescription().hasNature(CoreUtil.NATURE_ID)) {
			return;
		}

		delta.accept(new IResourceDeltaVisitor() {
			@Override
			public boolean visit(IResourceDelta delta) throws CoreException {
				if (progress.isCanceled()) {
					throw new OperationCanceledException();
				}
				if (delta.getResource() instanceof IProject) {
					return delta.getResource() == getProject();
				} else if (delta.getResource() instanceof IFile) {
					IFile file = (IFile) delta.getResource();
					if (file.getFileExtension().equals("properties")) {
						String parentFolder = file.getParent().getName();
						if (!parentFolder.equals("l10n")
								&& !parentFolder.equals("i18n")) {
							return false;
						}
						String[] tokens = file.getName()
								.replace(".properties", "").split("_");
						StringBuilder builder = new StringBuilder();
						if (tokens.length >= 2) {
							for (int i = 1; i < tokens.length; i++) {
								if (builder.length() > 0) {
									builder.append("-");
								}
								builder.append(tokens[i]);
							}
						}
						Locale locale = Locale.forLanguageTag(builder
								.toString());
						if (delta.getKind() == IResourceDelta.REMOVED) {
							// remove the resource
							i18nRegistry.removeResource(getProject(), locale,
									file.getProjectRelativePath());
						} else if (delta.getKind() == IResourceDelta.ADDED
								|| delta.getKind() == IResourceDelta.CHANGED) {
							Properties properties = new Properties();
							try {
								properties.load(file.getContents());
								II18nRegistry.ResourceDescription resourceDesc = new II18nRegistry.ResourceDescription(
										getProject(), locale, file
												.getProjectRelativePath(),
										properties);
								// cache the resource description
								i18nRegistry.cache(resourceDesc);
							} catch (IOException e) {
								LOGGER.error("{}", e);
							}
							return false;
						}
					}
				} else if (delta.getResource() instanceof IFolder) {
					String foldername = delta.getResource().getName();
					if (foldername.equals("OSGI-INF")) {
						return true;
					} else if (foldername.equals("l10n")) {
						IFolder folder = (IFolder) delta.getResource();
						if (folder.getParent().getName().equals("OSGI-INF")) {
							return true;
						}
					} else if (foldername.equals("i18n")) {
						return true;
					}

				}
				return false;
			}
		});

		if (progress.isCanceled())
			throw new OperationCanceledException();
		progress.worked(4);

	}

	/**
	 * @param monitor
	 *            the progress monitor to use for reporting progress to the
	 *            user. It is the caller's responsibility to call done() on the
	 *            given monitor. Accepts null, indicating that no progress
	 *            should be reported and that the operation cannot be cancelled.
	 */
	protected void fullBuild(final IProgressMonitor monitor)
			throws CoreException {

		IProject project = getProject();
		if (!project.getDescription().hasNature(CoreUtil.NATURE_ID)) {
			return;
		}

		// getProject().getWorkspace().checkpoint(false);
		monitor.worked(2);
		final II18nRegistry.ProjectDescription projectDescription = new II18nRegistry.ProjectDescription(
				getProject());
		project.accept(new IResourceVisitor() {
			@Override
			public boolean visit(IResource resource) throws CoreException {
				if (resource == getProject()) {
					return true;
				} else if (resource instanceof IFile) {
					IFile file = (IFile) resource;
					if (file.getFileExtension().equals("properties")) {
						String parentFolder = file.getParent().getName();
						if (!parentFolder.equals("l10n")
								&& !parentFolder.equals("i18n")) {
							return false;
						}
						String[] tokens = file.getName()
								.replace(".properties", "").split("_");
						StringBuilder builder = new StringBuilder();
						if (tokens.length >= 2) {
							for (int i = 1; i < tokens.length; i++) {
								if (builder.length() > 0) {
									builder.append("-");
								}
								builder.append(tokens[i]);
							}
						}
						Locale locale = Locale.forLanguageTag(builder
								.toString());
						Properties properties = new Properties();
						try {
							properties.load(file.getContents());
							II18nRegistry.ResourceDescription resourceDesc = new II18nRegistry.ResourceDescription(
									getProject(), locale, file
											.getProjectRelativePath(),
									properties);
							projectDescription.putResource(resourceDesc);

						} catch (IOException e) {
							LOGGER.error("{}", e);
						}
						return false;
					}
				} else if (resource instanceof IFolder) {
					String foldername = resource.getName();
					if (foldername.equals("OSGI-INF")) {
						return true;
					} else if (foldername.equals("l10n")) {
						IFolder folder = (IFolder) resource;
						if (folder.getParent().getName().equals("OSGI-INF")) {
							return true;
						}
					} else if (foldername.equals("i18n")) {
						return true;
					}

				}
				return false;
			}
		});
		monitor.worked(6);
		i18nRegistry.cache(projectDescription);

	}

	protected boolean isOpened(IResourceDelta delta) {
		return delta.getResource() instanceof IProject
				&& (delta.getFlags() & IResourceDelta.OPEN) != 0
				&& ((IProject) delta.getResource()).isOpen();
	}

	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		try {
			i18nRegistry.removeProject(getProject());
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

}
