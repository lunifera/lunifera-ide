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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
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
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.resource.IReferenceDescription;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.eclipse.xtext.resource.SaveOptions;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.resource.impl.ResourceDescriptionsProvider;
import org.eclipse.xtext.ui.editor.findrefs.IReferenceFinder;
import org.eclipse.xtext.ui.resource.IStorage2UriMapper;
import org.eclipse.xtext.ui.resource.XtextLiveScopeResourceSetProvider;
import org.eclipse.xtext.util.IAcceptor;
import org.eclipse.xtext.util.Pair;
import org.lunifera.dsl.semantic.common.types.LAttribute;
import org.lunifera.dsl.semantic.common.types.LReference;
import org.lunifera.dsl.semantic.common.types.LType;
import org.lunifera.dsl.semantic.common.types.LTypedPackage;
import org.lunifera.dsl.semantic.common.types.LunTypesFactory;
import org.lunifera.dsl.semantic.dto.LAutoInheritDto;
import org.lunifera.dsl.semantic.dto.LDto;
import org.lunifera.dsl.semantic.dto.LDtoFeature;
import org.lunifera.dsl.semantic.dto.LDtoInheritedAttribute;
import org.lunifera.dsl.semantic.dto.LDtoInheritedReference;
import org.lunifera.dsl.semantic.dto.LDtoModel;
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
import org.lunifera.dsl.semantic.service.LDTOService;
import org.lunifera.dsl.semantic.service.LService;
import org.lunifera.dsl.semantic.service.LServiceModel;
import org.lunifera.dsl.semantic.service.LunServiceFactory;
import org.lunifera.dsl.semantic.service.LunServicePackage;
import org.lunifera.dsl.xtext.lazyresolver.api.ISemanticLoadingResource;
import org.lunifera.dsl.xtext.lazyresolver.api.logger.TimeLogger;
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
	private XtextLiveScopeResourceSetProvider rsProvider;
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
		incrementalBuildDtosAndServices(delta, progress);

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
	protected void incrementalBuildDtosAndServices(IResourceDelta delta,
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
					} else if (file.getFileExtension().equals("dtos")) {
						LDtoModel lDtoModel = loadSemanticModel(file);
						if (lDtoModel != null) {
							buildServices(lDtoModel);
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
		fullBuildDtosAndServices(project);
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
	 * Builds service stuff.
	 * 
	 * @param project
	 * @return
	 * @throws CoreException
	 */
	protected void fullBuildDtosAndServices(final IProject project)
			throws CoreException {
		project.accept(new IResourceVisitor() {
			@Override
			public boolean visit(IResource resource) throws CoreException {
				if (resource == getProject()) {
					return true;
				} else if (resource instanceof IFile) {
					IFile file = (IFile) resource;
					if (file != null && file.getFileExtension() != null
							&& file.getFileExtension().equals("entitymodel")) {
						LEntityModel lEntityModel = loadSemanticModel(file);
						if (lEntityModel != null) {
							buildDtos(lEntityModel);
						}
					} else if (file.getFileExtension().equals("dtos")) {
						LDtoModel lDtoModel = loadSemanticModel(file);
						if (lDtoModel != null) {
							buildServices(lDtoModel);
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
	}

	private void buildDtos(LEntityModel tempLEntityModel) {
		IXtextUtilService service = CoreUiActivator.getDefault()
				.getUtilService();
		if (service == null) {
			LOGGER.error("Skipping dto build since IXtextUtilService is not available!");
			return;
		}

		TimeLogger timeLogger = TimeLogger.start(getClass());

		// find referencing dto model
		Set<URI> entityURIs = findEntityURIs(tempLEntityModel);
		final List<IReferenceDescription> targetDtoReferences = findTargetDtoReferences(entityURIs);
		if (targetDtoReferences.isEmpty()) {
			return;
		}

		// access entity resource
		ISemanticLoadingResource tempEntityResource = (ISemanticLoadingResource) tempLEntityModel
				.eResource();
		ResourceSet readonlyResourceSet = tempEntityResource.getResourceSet();

		// access dto resource based on entityResourceSet
		IReferenceDescription firstDesc = targetDtoReferences.get(0);
		LDto tempDto = (LDto) readonlyResourceSet.getEObject(
				firstDesc.getSourceEObjectUri(), true);

		// create the proper dto resourceSet to save the dto
		ResourceSet writeableResourceSet = getProjectResourceSet(EcoreUtil
				.getURI(tempDto));

		// load the dtoResource based on the dto resourceSet
		ISemanticLoadingResource dtoResource = (ISemanticLoadingResource) writeableResourceSet
				.getResource(EcoreUtil.getURI(tempDto).trimFragment(), true);
		ISemanticLoadingResource entityResource = (ISemanticLoadingResource) writeableResourceSet
				.getResource(tempLEntityModel.eResource().getURI(), true);

		List<LType> entities = collectEntities((LEntityModel) entityResource
				.getSemanticElement());
		List<LType> tempDtos = collectDtos((LDtoModel) dtoResource
				.getSemanticElement());

		List<LType> dtosToPersist = new ArrayList<LType>();

		// create all dtos -> no linking
		//
		Set<LTypedPackage> touchedPackages = new HashSet<LTypedPackage>();
		for (LType lType : entities) {
			if (lType instanceof LEntity) {
				LEntity lEntity = (LEntity) lType;
				LDto lDto = findDto(lEntity, tempDtos);
				if (lDto == null) {
					lDto = LunDtoFactory.eINSTANCE.createLAutoInheritDto();
					lDto.setAnnotationInfo(LunDtoFactory.eINSTANCE.createLDto());
					tempDtos.add(lDto);
				} else {
					// remove from package for a while
					LTypedPackage lPkg = (LTypedPackage) lDto.eContainer();
					lPkg.getTypes().remove(lDto);
					// collect all touched packages -> They need cleanup later
					touchedPackages.add(lPkg);
				}
				lDto.setName(getDtoName(lEntity));
				lDto.setWrappedType(lEntity);

				dtosToPersist.add(lDto);

			} else if (lType instanceof LBean) {
				LBean lBean = (LBean) lType;
				LDto lDto = findDto(lBean, tempDtos);
				if (lDto == null) {
					lDto = LunDtoFactory.eINSTANCE.createLAutoInheritDto();
					lDto.setAnnotationInfo(LunDtoFactory.eINSTANCE.createLDto());
					tempDtos.add(lDto);
				} else {
					// remove from package for a while
					LTypedPackage lPkg = (LTypedPackage) lDto.eContainer();
					lPkg.getTypes().remove(lDto);

					// collect all touched packages -> They need cleanup later
					touchedPackages.add(lPkg);
				}
				lDto.setName(getDtoName(lBean));
				lDto.setWrappedType(lBean);

				dtosToPersist.add(lDto);
			}
		}

		// clean touched packages -> Also remove dtos that are not based on an
		// entity anymore
		for (LTypedPackage lPkg : touchedPackages) {
			for (Iterator<LType> iterator = lPkg.getTypes().iterator(); iterator
					.hasNext();) {
				LType lType = (LType) iterator.next();
				if (lType instanceof LAutoInheritDto) {
					LDto lDto = (LDto) lType;
					if (lDto.getWrappedType() == null
							|| lDto.getWrappedType().eIsProxy()) {
						iterator.remove();
					}
				}
			}
		}

		// remove all dtos having no wrapped type anymore
		//
		for (LType lType : new ArrayList<>(tempDtos)) {
			LDto lDto = (LDto) lType;
			if (lDto.getWrappedType() == null
					|| lDto.getWrappedType().eIsProxy()) {
				tempDtos.remove(lDto);
			}
		}

		// link super types
		//
		for (LType lType : entities) {
			if (lType instanceof LEntity) {
				LEntity lEntity = (LEntity) lType;
				if (lEntity.getSuperType() != null
						&& !lEntity.getSuperType().eIsProxy()) {
					LEntity lEntitySuperType = lEntity.getSuperType();
					LDto lDto = findDto(lEntity, tempDtos);
					LDto lDtoSuperType = findDto(lEntitySuperType, tempDtos);
					lDto.setSuperType(lDtoSuperType);
				}
			} else if (lType instanceof LBean) {
				LBean lBean = (LBean) lType;
				if (lBean.getSuperType() != null
						&& !lBean.getSuperType().eIsProxy()) {
					LBean lBeanSuperType = lBean.getSuperType();
					LDto lDto = findDto(lBean, tempDtos);
					LDto lDtoSuperType = findDto(lBeanSuperType, tempDtos);
					lDto.setSuperType(lDtoSuperType);
				}
			}
		}

		// fix features
		//
		for (LType lType : dtosToPersist) {
			LDto lDto = (LDto) lType;
			fixFeatures(lDto, dtosToPersist);
		}

		// add the dtos to the package in the proper order
		//
		Set<String> dtoNames = new HashSet<String>();
		for (LType lType : dtosToPersist) {
			LDto lDto = (LDto) lType;

			// filter dtos
			if (dtoNames.contains(lDto.getName())) {
				// remove duplicate dtos
				continue;
			} else if (lDto.getWrappedType().eIsProxy()) {
				// remove dto without a proper entity reference
				continue;
			}
			dtoNames.add(lDto.getName());

			LTypedPackage lEntityPkg = (LTypedPackage) lDto.getWrappedType()
					.eContainer();
			addDtoToPackage(getDtoPackageName(lEntityPkg), lDto,
					(LDtoModel) dtoResource.getSemanticElement());
		}

		try {
			Diagnostic diagnostic = Diagnostician.INSTANCE.validate(dtoResource
					.getSemanticElement());
			int sev = diagnostic.getSeverity();
			if (sev <= Diagnostic.WARNING) {
				dtoResource.save(SaveOptions.newBuilder().format().getOptions()
						.toOptionsMap());
			} else {
				LOGGER.error(dtoResource.getErrors().get(0).toString());
			}
			dtoResource.unload();
		} catch (IOException e) {
			LOGGER.error("{}", e);
		}

		timeLogger.stop(LOGGER, "Finished LuniferaBuilder#buildDtos in ");
	}

	private void buildServices(LDtoModel tempLDtoModel) {
		IXtextUtilService service = CoreUiActivator.getDefault()
				.getUtilService();
		if (service == null) {
			LOGGER.error("Skipping dto build since IXtextUtilService is not available!");
			return;
		}

		TimeLogger timeLogger = TimeLogger.start(getClass());

		// find referencing dto model
		Set<URI> dtoURIs = findDtoURIs(tempLDtoModel);
		final List<IReferenceDescription> targetServiceReferences = findTargetServiceReferences(dtoURIs);
		if (targetServiceReferences.isEmpty()) {
			return;
		}

		// access dto resource
		ISemanticLoadingResource tempDtoResource = (ISemanticLoadingResource) tempLDtoModel
				.eResource();
		ResourceSet readonlyResourceSet = tempDtoResource.getResourceSet();

		// access service resource based on dtoResourceSet
		IReferenceDescription firstDesc = targetServiceReferences.get(0);
		LService tempService = (LService) readonlyResourceSet.getEObject(
				firstDesc.getSourceEObjectUri(), true);

		// create the proper dto resourceSet to save the dto
		ResourceSet writeableResourceSet = getProjectResourceSet(EcoreUtil
				.getURI(tempService));

		// load the serviceResource based on the service resourceSet
		ISemanticLoadingResource serviceResource = (ISemanticLoadingResource) writeableResourceSet
				.getResource(EcoreUtil.getURI(tempService).trimFragment(), true);
		ISemanticLoadingResource dtoResource = (ISemanticLoadingResource) writeableResourceSet
				.getResource(tempLDtoModel.eResource().getURI(), true);

		List<LType> dtos = collectDtos((LDtoModel) dtoResource
				.getSemanticElement());
		List<LType> tempServices = collectServices((LServiceModel) serviceResource
				.getSemanticElement());

		List<LType> servicesToPersist = new ArrayList<LType>();

		// create all services -> no linking
		//
		Set<LTypedPackage> touchedPackages = new HashSet<LTypedPackage>();
		for (LType lType : dtos) {
			if (lType instanceof LDto) {
				LDto lDto = (LDto) lType;
				LDTOService lService = findService(lDto, tempServices);
				if (lService == null) {
					lService = LunServiceFactory.eINSTANCE.createLDTOService();
					lService.setAnnotationInfo(LunServiceFactory.eINSTANCE
							.createLDTOService());
					lService.setInjectedServices(LunServiceFactory.eINSTANCE
							.createLInjectedServices());
					tempServices.add(lService);
				} else {
					// remove from package for a while
					LTypedPackage lPkg = (LTypedPackage) lService.eContainer();
					lPkg.getTypes().remove(lService);
					// collect all touched packages -> They need cleanup later
					touchedPackages.add(lPkg);
				}
				lService.setName(getServiceName(lDto));
				lService.setDto(lDto);

				servicesToPersist.add(lService);
			}
		}

		// clean touched packages -> Also remove services that are not based on
		// an
		// dto anymore
		for (LTypedPackage lPkg : touchedPackages) {
			for (Iterator<LType> iterator = lPkg.getTypes().iterator(); iterator
					.hasNext();) {
				LType lType = (LType) iterator.next();
				if (lType instanceof LDTOService) {
					LDTOService lService = (LDTOService) lType;
					if (lService.getDto() == null
							|| lService.getDto().eIsProxy()) {
						iterator.remove();
					}
				}
			}
		}

		// remove all services having no wrapped type anymore
		//
		for (LType lType : new ArrayList<>(tempServices)) {
			LDTOService lService = (LDTOService) lType;
			if (lService.getDto() == null || lService.getDto().eIsProxy()) {
				tempServices.remove(lService);
			}
		}

		// link super types
		//
		for (LType lType : dtos) {
			if (lType instanceof LEntity) {
				LEntity lEntity = (LEntity) lType;
				if (lEntity.getSuperType() != null
						&& !lEntity.getSuperType().eIsProxy()) {
					LEntity lEntitySuperType = lEntity.getSuperType();
					LDto lDto = findDto(lEntity, tempServices);
					LDto lDtoSuperType = findDto(lEntitySuperType, tempServices);
					lDto.setSuperType(lDtoSuperType);
				}
			} else if (lType instanceof LBean) {
				LBean lBean = (LBean) lType;
				if (lBean.getSuperType() != null
						&& !lBean.getSuperType().eIsProxy()) {
					LBean lBeanSuperType = lBean.getSuperType();
					LDto lDto = findDto(lBean, tempServices);
					LDto lDtoSuperType = findDto(lBeanSuperType, tempServices);
					lDto.setSuperType(lDtoSuperType);
				}
			}
		}

		// fix features
		//
		for (LType lType : servicesToPersist) {
			LDTOService lService = (LDTOService) lType;
			LDto lDto = lService.getDto();
			// can never be a bean!
			LEntity lEntity = (LEntity) lDto.getWrappedType();
			String pu = lEntity.getPersistenceUnit();
			if (pu != null && !pu.equals("")) {
				lService.setMutablePersistenceId(true);
				lService.setPersistenceId(pu);
			}
		}

		// add the services to the package in the proper order
		//
		Set<String> serviceNames = new HashSet<String>();
		for (LType lType : servicesToPersist) {
			LDTOService lService = (LDTOService) lType;

			// filter dtos
			if (serviceNames.contains(lService.getName())) {
				// remove duplicate dtos
				continue;
			} else if (lService.getDto().eIsProxy()) {
				// remove dto without a proper entity reference
				continue;
			}
			serviceNames.add(lService.getName());

			LTypedPackage lDtoPkg = (LTypedPackage) lService.getDto()
					.eContainer();
			addServicesToPackage(getServicePackageName(lDtoPkg), lService,
					(LServiceModel) serviceResource.getSemanticElement());
		}

		try {
			Diagnostic diagnostic = Diagnostician.INSTANCE
					.validate(serviceResource.getSemanticElement());
			int sev = diagnostic.getSeverity();
			if (sev <= Diagnostic.WARNING) {
				serviceResource.save(SaveOptions.newBuilder().format()
						.getOptions().toOptionsMap());
			} else {
				LOGGER.error(serviceResource.getErrors().get(0).toString());
			}
			serviceResource.unload();
		} catch (IOException e) {
			LOGGER.error("{}", e);
		}

		timeLogger.stop(LOGGER, "Finished LuniferaBuilder#buildDtos in ");
	}

	private LDto findDto(LType lEntity, List<LType> dtos) {
		for (LType lType : dtos) {
			if (lType instanceof LDto) {
				LDto lDto = (LDto) lType;
				if (lDto.getWrappedType() == lEntity) {
					return lDto;
				}
			}
		}
		return null;
	}

	private LDTOService findService(LType lDto, List<LType> services) {
		for (LType lType : services) {
			if (lType instanceof LDTOService) {
				LDTOService lService = (LDTOService) lType;
				if (lService.getDto() == lDto) {
					return lService;
				}
			}
		}
		return null;
	}

	protected String getDtoPackageName(LTypedPackage lTypePkg) {
		if(lTypePkg == null) {
			return "notDefined";
		}
		if (lTypePkg.getName().contains("entities")) {
			return lTypePkg.getName().replace("entities", "dtos");
		} else {
			return lTypePkg.getName() + ".dtos";
		}
	}

	protected String getServicePackageName(LTypedPackage lTypePkg) {
		return lTypePkg.getName() + ".services";
	}

	/**
	 * Adds the dto to the package maching the packageName
	 * 
	 * @param packageName
	 * @param newDto
	 * @param lDtoModel
	 * @param index
	 */
	private void addDtoToPackage(String packageName, LDto newDto,
			LDtoModel lDtoModel) {

		Optional<LTypedPackage> optPkg = lDtoModel.getPackages().stream()
				.filter(p -> p.getName().equals(packageName)).findFirst();
		LTypedPackage pkg = null;
		if (optPkg.isPresent()) {
			pkg = optPkg.get();
		}
		if (pkg == null) {
			pkg = LunTypesFactory.eINSTANCE.createLTypedPackage();
			pkg.setName(packageName);
			lDtoModel.getPackages().add(pkg);
		}

		pkg.getTypes().add(newDto);
	}

	/**
	 * Adds the service to the package maching the packageName
	 * 
	 * @param packageName
	 * @param newService
	 * @param lModel
	 */
	private void addServicesToPackage(String packageName, LService newService,
			LServiceModel lModel) {

		Optional<LTypedPackage> optPkg = lModel.getPackages().stream()
				.filter(p -> p.getName().equals(packageName)).findFirst();
		LTypedPackage pkg = null;
		if (optPkg.isPresent()) {
			pkg = optPkg.get();
		}
		if (pkg == null) {
			pkg = LunTypesFactory.eINSTANCE.createLTypedPackage();
			pkg.setName(packageName);
			lModel.getPackages().add(pkg);
		}

		pkg.getTypes().add(newService);
	}

	protected String getDtoName(LType lType) {
		return lType.getName() + "Dto";
	}

	protected String getServiceName(LType lType) {
		return lType.getName() + "Service";
	}

	/**
	 * Removes all inherited features and adds them again.
	 * 
	 * @param lDto
	 * @param dtos
	 */
	protected void fixFeatures(LDto lDto, List<LType> dtos) {
		removeAllInheritedFeatures(lDto);
		addInheritedFeaturesFromEntity(lDto, dtos);
	}

	/**
	 * Adds all inherited features from the entity or bean.
	 * 
	 * @param lDto
	 * @param dtos
	 */
	protected void addInheritedFeaturesFromEntity(LDto lDto, List<LType> dtos) {
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
					LDto mapToDto = getMapToDto(
							(LEntityReference) lEntityFeature, dtos);
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
				} else if (lBeanFeature instanceof LEntityReference) {
					LDto mapToDto = getMapToDto(
							(LEntityReference) lBeanFeature, dtos);
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
				} else if (lBeanFeature instanceof LBeanReference) {
					LDto mapToDto = getMapToDto((LBeanReference) lBeanFeature,
							dtos);
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

	/**
	 * Removes all inherited features.
	 * 
	 * @param lDto
	 */
	protected void removeAllInheritedFeatures(LDto lDto) {
		for (Iterator<LDtoFeature> iterator = lDto.getFeatures().iterator(); iterator
				.hasNext();) {
			LDtoFeature lFeature = iterator.next();
			if ((lFeature instanceof LDtoInheritedAttribute)
					|| (lFeature instanceof LDtoInheritedReference)) {
				iterator.remove();
			}
		}
	}

	/**
	 * Loads the dto by the given targetDtoReference.
	 * 
	 * @param targetDtoReference
	 * @param dtoModelResource
	 * @return
	 */
	protected LDto loadDtoByTargetReference(
			IReferenceDescription targetDtoReference,
			ISemanticLoadingResource dtoModelResource) {
		LDto lDto = null;
		lDto = (LDto) ((ISemanticLoadingResource) dtoModelResource)
				.getSemanticElement(targetDtoReference.getSourceEObjectUri()
						.fragment());
		return lDto;
	}

	/**
	 * Returns the resource set for the given targetDtoReference.
	 * 
	 * @param dtoResourceSets
	 * @param targetDtoReference
	 * @return
	 */
	protected XtextResourceSet getDtosProjectResourceSet(
			Map<IProject, XtextResourceSet> dtoResourceSets,
			IReferenceDescription targetDtoReference) {
		IProject dtoProject = null;
		Iterable<Pair<IStorage, IProject>> pairs = uriStorageMapper
				.getStorages(targetDtoReference.getSourceEObjectUri());
		if (pairs.iterator().hasNext()) {
			dtoProject = pairs.iterator().next().getSecond();
		}

		if (dtoProject == null) {
			LOGGER.error("No project could be found for "
					+ targetDtoReference.getSourceEObjectUri());
			return null;
		}

		XtextResourceSet resourceSet = null;
		if (dtoResourceSets.containsKey(dtoProject)) {
			resourceSet = dtoResourceSets.get(dtoProject);
		} else {
			resourceSet = (XtextResourceSet) rsProvider.get(getProject());
			resourceSet.getLoadOptions().put(
					ResourceDescriptionsProvider.LIVE_SCOPE, Boolean.TRUE);
			dtoResourceSets.put(dtoProject, resourceSet);
		}
		return resourceSet;
	}

	protected XtextResourceSet getProjectResourceSet(URI uri) {
		IProject dtoProject = null;
		Iterable<Pair<IStorage, IProject>> pairs = uriStorageMapper
				.getStorages(uri);
		if (pairs.iterator().hasNext()) {
			dtoProject = pairs.iterator().next().getSecond();
		}

		if (dtoProject == null) {
			LOGGER.error("No project could be found for " + uri);
			return null;
		}

		XtextResourceSet resourceSet = (XtextResourceSet) rsProvider
				.get(dtoProject);
		// resourceSet.getLoadOptions().put(
		// ResourceDescriptionsProvider.LIVE_SCOPE, Boolean.TRUE);
		return resourceSet;
	}

	protected Set<URI> findEntityURIs(LEntityModel lEntityModel) {
		Set<URI> entityURIs = new HashSet<URI>();
		for (LTypedPackage lPkg : lEntityModel.getPackages()) {
			for (LType lType : lPkg.getTypes()) {
				if (lType instanceof LEntity || lType instanceof LBean) {
					entityURIs.add(EcoreUtil.getURI(lType));
				}
			}
		}
		return entityURIs;
	}

	protected Set<URI> findDtoURIs(LDtoModel lDtoModel) {
		Set<URI> dtoURIs = new HashSet<URI>();
		for (LTypedPackage lPkg : lDtoModel.getPackages()) {
			for (LType lType : lPkg.getTypes()) {
				if (lType instanceof LDto) {
					dtoURIs.add(EcoreUtil.getURI(lType));
				}
			}
		}
		return dtoURIs;
	}

	protected List<LType> collectEntities(LEntityModel lEntityModel) {
		List<LType> entities = new ArrayList<LType>();
		for (LTypedPackage lPkg : lEntityModel.getPackages()) {
			for (LType lType : lPkg.getTypes()) {
				if (lType instanceof LEntity || lType instanceof LBean) {
					entities.add(lType);
				}
			}
		}
		return entities;
	}

	protected List<LType> collectDtos(LDtoModel lDtoModel) {
		List<LType> dtos = new ArrayList<LType>();
		for (LTypedPackage lPkg : lDtoModel.getPackages()) {
			for (LType lType : lPkg.getTypes()) {
				if (lType instanceof LDto) {
					LDto lDto = (LDto) lType;
					if (lDto.getWrappedType() != null) {
						dtos.add(lType);
					}
				}
			}
		}
		return dtos;
	}

	protected List<LType> collectServices(LServiceModel lServiceModel) {
		List<LType> services = new ArrayList<LType>();
		for (LTypedPackage lPkg : lServiceModel.getPackages()) {
			for (LType lType : lPkg.getTypes()) {
				if (lType instanceof LService) {
					services.add(lType);
				}
			}
		}
		return services;
	}

	@SuppressWarnings("restriction")
	protected List<IReferenceDescription> findTargetDtoReferences(
			Set<URI> entityURIs) {
		final List<IReferenceDescription> targetDtoReferences = new ArrayList<IReferenceDescription>();
		referenceFinder.findAllReferences(entityURIs, null,
				new IAcceptor<IReferenceDescription>() {
					@Override
					public void accept(IReferenceDescription t) {
						if (t.getEReference() == LunDtoPackage.Literals.LDTO__WRAPPED_TYPE) {
							targetDtoReferences.add(t);
						}
					}
				}, null);
		return targetDtoReferences;
	}

	@SuppressWarnings("restriction")
	protected List<IReferenceDescription> findTargetServiceReferences(
			Set<URI> dtoURIs) {
		final List<IReferenceDescription> targetServiceReferences = new ArrayList<IReferenceDescription>();
		referenceFinder.findAllReferences(dtoURIs, null,
				new IAcceptor<IReferenceDescription>() {
					@Override
					public void accept(IReferenceDescription t) {
						if (t.getEReference() == LunServicePackage.Literals.LDTO_SERVICE__DTO) {
							targetServiceReferences.add(t);
						}
					}
				}, null);
		return targetServiceReferences;
	}

	/**
	 * Returns the mapped DTO or <code>null</code>.
	 * 
	 * @param lEntityFeature
	 * @return
	 */
	private LDto getMapToDto(LEntityReference lEntityFeature, List<LType> dtos) {
		LEntity entity = lEntityFeature.getType();
		return findDto(entity, dtos);
	}

	/**
	 * Returns the mapped DTO or <code>null</code>.
	 * 
	 * @param lEntityFeature
	 * @param dtos
	 * @return
	 */
	private LDto getMapToDto(LBeanReference lEntityFeature, List<LType> dtos) {
		LEntity entity = (LEntity) lEntityFeature.getType();
		return findDto(entity, dtos);
	}

	@SuppressWarnings("unchecked")
	private <A extends EObject> A loadSemanticModel(IFile file) {
		LOGGER.info("loading:" + file.getName());
		org.eclipse.emf.common.util.URI modelURI = org.eclipse.emf.common.util.URI
				.createPlatformResourceURI(file.getFullPath().toString(), false);
		XtextResourceSet rs = (XtextResourceSet) rsProvider.get(getProject());
		Resource resource = rs.getResource(modelURI, true);
		try {
			resource.load(null);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		A semanticModel = null;
		if (resource instanceof ISemanticLoadingResource) {
			semanticModel = (A) ((ISemanticLoadingResource) resource)
					.getSemanticElement();
		} else {
			semanticModel = (A) resource.getContents().get(0);
		}

		LOGGER.info("finished loading:" + file.getName());
		return semanticModel;
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
