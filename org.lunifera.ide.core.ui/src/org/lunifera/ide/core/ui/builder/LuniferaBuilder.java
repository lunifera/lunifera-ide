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
package org.lunifera.ide.core.ui.builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.ProgressMonitorWrapper;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IReferenceDescription;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.eclipse.xtext.resource.SaveOptions;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.ui.editor.findrefs.IReferenceFinder;
import org.eclipse.xtext.ui.resource.IStorage2UriMapper;
import org.eclipse.xtext.ui.resource.XtextResourceSetProvider;
import org.eclipse.xtext.util.IAcceptor;
import org.eclipse.xtext.util.Pair;
import org.lunifera.dsl.semantic.common.types.LAttribute;
import org.lunifera.dsl.semantic.common.types.LPackage;
import org.lunifera.dsl.semantic.common.types.LReference;
import org.lunifera.dsl.semantic.common.types.LType;
import org.lunifera.dsl.semantic.common.types.LTypedPackage;
import org.lunifera.dsl.semantic.dto.LAutoInheritDto;
import org.lunifera.dsl.semantic.dto.LDto;
import org.lunifera.dsl.semantic.dto.LDtoFeature;
import org.lunifera.dsl.semantic.dto.LDtoInheritedAttribute;
import org.lunifera.dsl.semantic.dto.LDtoInheritedReference;
import org.lunifera.dsl.semantic.dto.LunDtoFactory;
import org.lunifera.dsl.semantic.dto.LunDtoPackage;
import org.lunifera.dsl.semantic.entity.LBean;
import org.lunifera.dsl.semantic.entity.LBeanAttribute;
import org.lunifera.dsl.semantic.entity.LBeanFeature;
import org.lunifera.dsl.semantic.entity.LBeanReference;
import org.lunifera.dsl.semantic.entity.LEntity;
import org.lunifera.dsl.semantic.entity.LEntityAttribute;
import org.lunifera.dsl.semantic.entity.LEntityFeature;
import org.lunifera.dsl.semantic.entity.LEntityModel;
import org.lunifera.dsl.semantic.entity.LEntityReference;
import org.lunifera.dsl.xtext.lazyresolver.api.ISemanticLoadingResource;
import org.lunifera.ide.core.api.i18n.CoreUtil;
import org.lunifera.ide.core.api.i18n.II18nRegistry;
import org.lunifera.ide.core.ui.CoreUiActivator;
import org.lunifera.xtext.builder.ui.access.IXtextUtilService;
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
	@Inject
	private XtextResourceSetProvider rsProvider;
	@Inject
	private IResourceDescriptions resourceDescriptions;
	@SuppressWarnings("restriction")
	@Inject
	private IReferenceFinder referenceFinder;
	@Inject
	private IStorage2UriMapper uriStorageMapper;

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

		monitor.subTask("Building I18n");
		incrementalBuildI18n(delta, progress);

		monitor.subTask("Building Dtos");
		incrementalBuildDtos(delta, progress);

		if (progress.isCanceled())
			throw new OperationCanceledException();
		progress.worked(4);

	}

	/**
	 * Builds I18n.
	 * 
	 * @param delta
	 * @param progress
	 * @throws CoreException
	 */
	protected void incrementalBuildI18n(IResourceDelta delta,
			final SubMonitor progress) throws CoreException {
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
	}

	/**
	 * Builds I18n.
	 * 
	 * @param delta
	 * @param progress
	 * @throws CoreException
	 */
	protected void incrementalBuildDtos(IResourceDelta delta,
			final SubMonitor progress) throws CoreException {
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
					if (file.getFileExtension().equals("entitymodel")) {
						LEntityModel lEntityModel = loadSemanticModel(file);
						if (lEntityModel != null) {
							buildDtos(lEntityModel);
						}
					}
				} else if (delta.getResource() instanceof IFolder) {
					String name = delta.getResource().getName();
					return name.equals("target") || name.equals("bin")
							|| name.equals("classes") ? false : true;
				}
				return false;
			}
		});
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
		monitor.subTask("Building I18n");
		fullBuildI18n(project);

		monitor.subTask("Building Dtos");
		fullBuildDtos(project);
		monitor.worked(6);
	}

	/**
	 * Builds I18n stuff.
	 * 
	 * @param project
	 * @return
	 * @throws CoreException
	 */
	protected void fullBuildI18n(IProject project) throws CoreException {
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

		i18nRegistry.cache(projectDescription);
	}

	/**
	 * Builds I18n stuff.
	 * 
	 * @param project
	 * @return
	 * @throws CoreException
	 */
	protected II18nRegistry.ProjectDescription fullBuildDtos(
			final IProject project) throws CoreException {
		final II18nRegistry.ProjectDescription projectDescription = new II18nRegistry.ProjectDescription(
				getProject());
		project.accept(new IResourceVisitor() {
			@Override
			public boolean visit(IResource resource) throws CoreException {
				if (resource == getProject()) {
					return true;
				} else if (resource instanceof IFile) {
					IFile file = (IFile) resource;
					if (file.getFileExtension().equals("entitymodel")) {
						LEntityModel lEntityModel = loadSemanticModel(file);
						if (lEntityModel != null) {
							buildDtos(lEntityModel);
						}
					}
				} else if (resource instanceof IFolder) {
					String name = resource.getName();
					return name.equals("target") || name.equals("bin")
							|| name.equals("classes") ? false : true;
				}
				return false;
			}
		});
		return projectDescription;
	}

	@SuppressWarnings("restriction")
	private void buildDtos(LEntityModel lEntityModel) {
		IXtextUtilService service = CoreUiActivator.getDefault()
				.getUtilService();
		if (service == null) {
			LOGGER.error("Skipping dto build since IXtextUtilService is not available!");
			return;
		}

		Set<URI> entities = new HashSet<URI>();
		for (LTypedPackage lPkg : lEntityModel.getPackages()) {
			for (LType lType : lPkg.getTypes()) {
				if (lType instanceof LEntity || lType instanceof LBean) {
					entities.add(EcoreUtil.getURI(lType));
				}
			}
		}

		final List<IReferenceDescription> referenceTargets = new ArrayList<IReferenceDescription>();
		referenceFinder.findAllReferences(entities, null,
				new IAcceptor<IReferenceDescription>() {
					@Override
					public void accept(IReferenceDescription t) {
						if (t.getEReference() == LunDtoPackage.Literals.LDTO__WRAPPED_TYPE) {
							referenceTargets.add(t);
						}
					}
				}, null);

		List<Resource> affectedResources = new LinkedList<Resource>();
		Map<IProject, XtextResourceSet> dtoResourceSets = new HashMap<IProject, XtextResourceSet>();
		for (IReferenceDescription desc : referenceTargets) {
			IProject dtoProject = null;
			Iterable<Pair<IStorage, IProject>> pairs = uriStorageMapper
					.getStorages(desc.getSourceEObjectUri());
			if (pairs.iterator().hasNext()) {
				dtoProject = pairs.iterator().next().getSecond();
			}

			if (dtoProject == null) {
				LOGGER.error("No project could be found for "
						+ desc.getSourceEObjectUri());
				continue;
			}

			XtextResourceSet resourceSet = null;
			if (dtoResourceSets.containsKey(dtoProject)) {
				resourceSet = dtoResourceSets.get(dtoProject);
			} else {
				resourceSet = (XtextResourceSet) rsProvider.get(getProject());
				// resourceSet.getLoadOptions().put(
				// ResourceDescriptionsProvider.NAMED_BUILDER_SCOPE,
				// Boolean.TRUE);
				// ((ResourceSetImpl) resourceSet).setURIResourceMap(Maps
				// .<URI, Resource> newHashMap());
				dtoResourceSets.put(dtoProject, resourceSet);
			}

			// load the resource by the resource set
			Resource dtoModelResource = resourceSet.getResource(desc
					.getSourceEObjectUri().trimFragment(), true);

			LDto lDto = null;
			if (dtoModelResource instanceof ISemanticLoadingResource) {
				lDto = (LDto) ((ISemanticLoadingResource) dtoModelResource)
						.getSemanticElement(desc.getSourceEObjectUri()
								.fragment());
			} else {
				lDto = (LDto) dtoModelResource.getEObject(desc
						.getSourceEObjectUri().fragment());
			}

			if (!(lDto instanceof LAutoInheritDto)) {
				continue;
			}

			if (!affectedResources.contains(dtoModelResource)) {
				affectedResources.add(dtoModelResource);
			}

			// now remove all inherited features. Will be added
			// again
			for (Iterator<LDtoFeature> iterator = lDto.getFeatures().iterator(); iterator
					.hasNext();) {
				LDtoFeature lFeature = iterator.next();
				if ((lFeature instanceof LDtoInheritedAttribute)
						|| (lFeature instanceof LDtoInheritedReference)) {
					iterator.remove();
				}
			}

			if (lDto.getWrappedType() instanceof LEntity) {
				LEntity currentEntity = (LEntity) lDto.getWrappedType();
				// now add all features from the entity as inherited
				// feature

				// also add features from supertype, if dto does not
				// extend any dto.
				List<LEntityFeature> features = lDto.getSuperType() == null ? currentEntity
						.getAllFeatures() : currentEntity.getFeatures();
				for (LEntityFeature lEntityFeature : features) {
					if (lEntityFeature instanceof LEntityAttribute) {
						LDtoInheritedAttribute lNewAtt = LunDtoFactory.eINSTANCE
								.createLDtoInheritedAttribute();
						lNewAtt.setInheritedFeature((LAttribute) lEntityFeature);
						LDtoFeature lAnnTarget = LunDtoFactory.eINSTANCE
								.createLDtoFeature();
						lNewAtt.setAnnotationInfo(lAnnTarget);
						lDto.getFeatures().add(lNewAtt);
					} else if (lEntityFeature instanceof LEntityReference) {
						// Mapped dto
						LDto mapToDto = getMapToDto((LEntityReference) lEntityFeature);
						if (mapToDto == null) {
							LOGGER.error("No Mapping-DTO could be found for "
									+ lEntityFeature.getEntity());
							continue;
						}

						LDtoInheritedReference lNewRef = LunDtoFactory.eINSTANCE
								.createLDtoInheritedReference();
						lNewRef.setInheritedFeature((LReference) lEntityFeature);
						LDtoFeature lAnnTarget = LunDtoFactory.eINSTANCE
								.createLDtoFeature();
						lNewRef.setAnnotationInfo(lAnnTarget);
						lDto.getFeatures().add(lNewRef);
						lNewRef.setType(mapToDto);
					}
				}
			} else if (lDto.getWrappedType() instanceof LBean) {
				LBean currentBean = (LBean) lDto.getWrappedType();
				// now add all features from the entity as inherited
				// feature

				// also add features from supertype, if dto does not
				// extend any dto.
				List<LBeanFeature> features = lDto.getSuperType() == null ? currentBean
						.getAllFeatures() : currentBean.getFeatures();
				for (LBeanFeature lBeanFeature : features) {
					if (lBeanFeature instanceof LBeanAttribute) {
						LDtoInheritedAttribute lNewAtt = LunDtoFactory.eINSTANCE
								.createLDtoInheritedAttribute();
						lNewAtt.setInheritedFeature((LAttribute) lBeanFeature);
						LDtoFeature lAnnTarget = LunDtoFactory.eINSTANCE
								.createLDtoFeature();
						lNewAtt.setAnnotationInfo(lAnnTarget);
						lDto.getFeatures().add(lNewAtt);
					} else if (lBeanFeature instanceof LBeanReference) {
						// Mapped dto
						LDto mapToDto = getMapToDto((LEntityReference) lBeanFeature);
						if (mapToDto == null) {
							LOGGER.error("No Mapping-DTO could be found for "
									+ lBeanFeature.getBean());
							continue;
						}

						LDtoInheritedReference lNewRef = LunDtoFactory.eINSTANCE
								.createLDtoInheritedReference();
						lNewRef.setInheritedFeature((LReference) lBeanFeature);
						LDtoFeature lAnnTarget = LunDtoFactory.eINSTANCE
								.createLDtoFeature();
						lNewRef.setAnnotationInfo(lAnnTarget);
						lDto.getFeatures().add(lNewRef);
						lNewRef.setType(mapToDto);
					}
				}
			}
		}

		try {
			for (Resource resource : affectedResources) {
				resource.save(SaveOptions.newBuilder().format().getOptions()
						.toOptionsMap());
				resource.unload();
			}
		} catch (IOException e) {
			LOGGER.error("{}", e);
		}
	}

	/**
	 * Returns the mapped DTO or <code>null</code>.
	 * 
	 * @param lEntityFeature
	 * @return
	 */
	private LDto getMapToDto(LEntityReference lEntityFeature) {
		LEntity entity = lEntityFeature.getType();
		LPackage lPkg = (LPackage) entity.eContainer();

		String pkgName = lPkg.getName();
		String dtoFQN = String.format("%s.dtos.%sDto", pkgName,
				entity.getName());

		QualifiedName name = QualifiedName.create(dtoFQN.split("\\."));

		for (IEObjectDescription result : resourceDescriptions
				.getExportedObjects(LunDtoPackage.Literals.LDTO, name, false)) {
			return (LDto) result.getEObjectOrProxy();
		}

		for (IEObjectDescription result : resourceDescriptions
				.getExportedObjects(LunDtoPackage.Literals.LAUTO_INHERIT_DTO,
						name, false)) {
			return (LDto) result.getEObjectOrProxy();
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	private <A extends EObject> A loadSemanticModel(IFile file) {
		LOGGER.info("loading:" + file.getName());
		org.eclipse.emf.common.util.URI entityDSLURI = org.eclipse.emf.common.util.URI
				.createPlatformResourceURI(file.getFullPath().toString(), false);
		XtextResourceSet rs = (XtextResourceSet) rsProvider.get(getProject());
		Resource entityResource = rs.getResource(entityDSLURI, true);
		try {
			entityResource.load(null);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		A entityModel = null;
		if (entityResource instanceof ISemanticLoadingResource) {
			entityModel = (A) ((ISemanticLoadingResource) entityResource)
					.getSemanticElement();
		} else {
			entityModel = (A) entityResource.getContents().get(0);
		}

		LOGGER.info("finished loading:" + file.getName());
		return entityModel;
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
