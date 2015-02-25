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

import static java.util.Collections.singletonList;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.eclipse.xtext.builder.EclipseOutputConfigurationProvider;
import org.eclipse.xtext.ui.XtextProjectHelper;
import org.eclipse.xtext.ui.editor.preferences.FixedScopedPreferenceStore;
import org.eclipse.xtext.ui.editor.preferences.PreferenceConstants;
import org.eclipse.xtext.ui.util.IProjectFactoryContributor;
import org.eclipse.xtext.ui.util.ProjectFactory;
import org.lunifera.ide.core.ui.builder.LuniferaBuilder;
import org.lunifera.ide.core.ui.nature.LuniferaNature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class LuniferaProjectCreator extends AbstractProjectCreator {

	protected static final String[] ENTITY_PROJECT_NATURES = new String[] {
			JavaCore.NATURE_ID, "org.eclipse.pde.PluginNature", //$NON-NLS-1$
			XtextProjectHelper.NATURE_ID, LuniferaNature.NATURE_ID //$NON-NLS-1$  
			, "org.eclipse.m2e.core.maven2Nature"//$NON-NLS-1$
	};

	protected static final String[] DTO_SERVICES_PROJECT_NATURES = new String[] {
			JavaCore.NATURE_ID,
			"org.eclipse.pde.PluginNature",//$NON-NLS-1$
			XtextProjectHelper.NATURE_ID, LuniferaNature.NATURE_ID,
			"org.eclipse.m2e.core.maven2Nature" }; //$NON-NLS-1$

	protected static final String[] UI_PROJECT_NATURES = new String[] {
			JavaCore.NATURE_ID,
			"org.eclipse.pde.PluginNature",//$NON-NLS-1$
			XtextProjectHelper.NATURE_ID, LuniferaNature.NATURE_ID,
			"org.eclipse.m2e.core.maven2Nature" };//$NON-NLS-1$

	protected static final String[] AGGREGATOR_PROJECT_NATURES = new String[] { "org.eclipse.m2e.core.maven2Nature" };//$NON-NLS-1$

	protected static final String[] BUILDERS = new String[] {
			JavaCore.BUILDER_ID, "org.eclipse.pde.ManifestBuilder", //$NON-NLS-1$
			"org.eclipse.m2e.core.maven2Builder", //$NON-NLS-1$
			"org.eclipse.pde.SchemaBuilder", //$NON-NLS-1$
			XtextProjectHelper.BUILDER_ID, LuniferaBuilder.BUILDER_ID };

	protected static final String[] AGGREGATOR_BUILDERS = new String[] { "org.eclipse.m2e.core.maven2Builder" };

	protected static final String[] TEST_PROJECT_NATURES = DTO_SERVICES_PROJECT_NATURES;

	protected static final String SRC_GEN_ROOT = "src-gen"; //$NON-NLS-1$
	protected static final String SRC_ROOT = "src"; //$NON-NLS-1$
	protected static final String XTEND_GEN_ROOT = "xtend-gen"; //$NON-NLS-1$
	protected static final String MODEL_ROOT = "models"; //$NON-NLS-1$
	protected static final List<String> SRC_FOLDER_LIST = ImmutableList.of(
			SRC_ROOT, SRC_GEN_ROOT);

	private static final Logger LOGGER = LoggerFactory
			.getLogger(LuniferaProjectCreator.class);

	@Inject
	private Provider<BundleProjectFactory> projectFactoryProvider;
	@Inject
	private Provider<FeatureProjectFactory> featureProjFactoryProvider;
	@Inject
	private Provider<P2ProjectFactory> p2ProjFactoryProvider;

	protected LuniferaProjectInfo getLuniferaProjectInfo() {
		return (LuniferaProjectInfo) getProjectInfo();
	}

	@Override
	protected void execute(final IProgressMonitor monitor)
			throws CoreException, InvocationTargetException,
			InterruptedException {
		SubMonitor subMonitor = SubMonitor.convert(monitor,
				getCreateModelProjectMessage(), getMonitorTicks());

		createAggregatorProject(subMonitor.newChild(1));
		IProject project = createEntityProject(subMonitor.newChild(1));
		createDtoServicesProject(subMonitor.newChild(1));
		createUiProject(subMonitor.newChild(1));

		if (getLuniferaProjectInfo().isCreateTestProject()) {
			createTestProject(subMonitor.newChild(1));
		}
		if (getLuniferaProjectInfo().isCreateFeatureProject()) {
			createFeatureProject(subMonitor.newChild(1));
		}

		createP2Project(subMonitor.newChild(1));

		IFile entityModelFile = project.getFile(getModelFolderName() + "/"
				+ getLuniferaProjectInfo().getEntityFilePath());
		BasicNewResourceWizard.selectAndReveal(entityModelFile, PlatformUI
				.getWorkbench().getActiveWorkbenchWindow());
		setResult(entityModelFile);
	}

	protected int getMonitorTicks() {
		int ticks = 2;
		ticks = getLuniferaProjectInfo().isCreateTestProject() ? ticks + 1
				: ticks;
		if (getLuniferaProjectInfo().isCreateFeatureProject()) {
			ticks++;
		}
		return ticks;
	}

	@Override
	protected BundleProjectFactory createProjectFactory() {
		return projectFactoryProvider.get();
	}

	protected FeatureProjectFactory createFeatureFactory() {
		return featureProjFactoryProvider.get();
	}

	protected P2ProjectFactory createP2Factory() {
		return p2ProjFactoryProvider.get();
	}

	@Override
	protected String getCreateModelProjectMessage() {
		return "Entity-Project " + getLuniferaProjectInfo().getProjectName();
	}

	protected IProject createDtoServicesProject(final IProgressMonitor monitor)
			throws CoreException {
		BundleProjectFactory factory = createProjectFactory();
		configureDtoServicesProjectFactory(factory);
		return factory.createProject(monitor, null);
	}

	protected IProject createUiProject(final IProgressMonitor monitor)
			throws CoreException {
		BundleProjectFactory factory = createProjectFactory();
		configureUiProjectFactory(factory);
		return factory.createProject(monitor, null);
	}

	protected void configureDtoServicesProjectFactory(
			BundleProjectFactory factory) {
		configureProjectFactory(factory);
		factory.addFolders(singletonList(MODEL_ROOT));
		List<String> requiredBundles = getDtoServicesProjectRequiredBundles();
		factory.setProjectName(getLuniferaProjectInfo()
				.getDtoServicesProjectName());
		factory.addProjectNatures(getDtoServicesProjectNatures());
		factory.addRequiredBundles(requiredBundles);
		factory.setProjectDefaultCharset(Charsets.UTF_8.name());
		factory.setLocation(getLuniferaProjectInfo()
				.getDtoServicesProjectLocation());

		factory.addExportedPackages(singletonList(getLuniferaProjectInfo()
				.getDtoPackageName()));
		factory.addExportedPackages(singletonList(getLuniferaProjectInfo()
				.getDtoMapperPackageName()));
		factory.addExportedPackages(singletonList(getLuniferaProjectInfo()
				.getServicesPackageName()));
		factory.addContributor(createDtoServiceProjectContributor());
	}

	protected void configureUiProjectFactory(BundleProjectFactory factory) {
		configureProjectFactory(factory);
		factory.addFolders(singletonList(MODEL_ROOT));
		List<String> requiredBundles = getUiProjectRequiredBundles();
		factory.setProjectName(getLuniferaProjectInfo().getUiProjectName());
		factory.addProjectNatures(getUiServicesProjectNatures());
		factory.addRequiredBundles(requiredBundles);
		factory.setProjectDefaultCharset(Charsets.UTF_8.name());
		factory.setLocation(getLuniferaProjectInfo().getUiProjectLocation());
		factory.addContributor(createUiProjectContributor());
	}

	protected void configureAggregatorProjectFactory(
			BundleProjectFactory factory) {
		factory.addWorkingSets(Arrays.asList(getLuniferaProjectInfo()
				.getWorkingSets()));
		factory.addFolders(Collections.<String> emptyList());
		factory.addBuilderIds(getAggregatorBuilders());
		factory.setProjectName(getLuniferaProjectInfo()
				.getAggregatorProjectName());
		factory.addProjectNatures(getAggregatorProjectNatures());
		factory.setProjectDefaultCharset(Charsets.UTF_8.name());
		factory.setLocation(getLuniferaProjectInfo()
				.getAggregatorProjectLocation());
		factory.addContributor(createAggregatorProjectContributor());
	}

	protected List<String> getDtoServicesProjectRequiredBundles() {
		List<String> requiredBundles = Lists.newArrayList(
				"org.lunifera.ecview.core.common;bundle-version=\"0.7.3\"", //$NON-NLS-1$
				"org.lunifera.dsl.datatype.lib;bundle-version=\"0.7.3\"", //$NON-NLS-1$
				"org.lunifera.dsl.dto.lib;bundle-version=\"0.7.3\"", //$NON-NLS-1$
				"com.google.guava;bundle-version=\"15.0.0\"", //$NON-NLS-1$
				"org.eclipse.xtext.xbase.lib;bundle-version=\"2.6.2\"", //$NON-NLS-1$
				"javax.persistence;bundle-version=\"2.1.0\"", //$NON-NLS-1$
				"org.lunifera.runtime.common;bundle-version=\"0.7.5\"", //$NON-NLS-1$
				getLuniferaProjectInfo().getEntityProjectName(), //$NON-NLS-1$
				"org.eclipse.xtend.lib;bundle-version=\"2.6.2\""); //$NON-NLS-1$
		return requiredBundles;
	}

	protected List<String> getUiProjectRequiredBundles() {
		List<String> requiredBundles = Lists.newArrayList(
				"org.lunifera.ecview.core.common;bundle-version=\"0.7.3\"", //$NON-NLS-1$
				"org.lunifera.dsl.datatype.lib;bundle-version=\"0.7.3\"", //$NON-NLS-1$
				"org.lunifera.dsl.dto.lib;bundle-version=\"0.7.3\"", //$NON-NLS-1$
				"com.google.guava;bundle-version=\"15.0.0\"", //$NON-NLS-1$
				"org.eclipse.xtext.xbase.lib;bundle-version=\"2.6.2\"", //$NON-NLS-1$
				"javax.persistence;bundle-version=\"2.1.0\"", //$NON-NLS-1$
				"org.lunifera.runtime.common;bundle-version=\"0.7.5\"", //$NON-NLS-1$
				getLuniferaProjectInfo().getEntityProjectName(), //$NON-NLS-1$
				getLuniferaProjectInfo().getDtoServicesProjectName(), //$NON-NLS-1$
				"org.eclipse.xtend.lib;bundle-version=\"2.6.2\""); //$NON-NLS-1$
		return requiredBundles;
	}

	protected String[] getAggregatorProjectNatures() {
		return AGGREGATOR_PROJECT_NATURES;
	}

	protected String[] getAggregatorBuilders() {
		return AGGREGATOR_BUILDERS;
	}

	protected String[] getDtoServicesProjectNatures() {
		return DTO_SERVICES_PROJECT_NATURES;
	}

	protected String[] getUiServicesProjectNatures() {
		return UI_PROJECT_NATURES;
	}

	protected IProject createAggregatorProject(final IProgressMonitor monitor)
			throws CoreException {
		BundleProjectFactory factory = createProjectFactory();
		configureAggregatorProjectFactory(factory);
		IProject result = factory.createProject(monitor, null);

		return result;
	}

	protected IProject createEntityProject(final IProgressMonitor monitor)
			throws CoreException {
		BundleProjectFactory factory = createProjectFactory();
		configureEntityProjectFactory(factory);
		IProject result = factory.createProject(monitor, null);

		IPreferenceStore store = getWritablePreferenceStore(result);
		store.setValue(
				getDtosPreferenceKey(EclipseOutputConfigurationProvider.OUTPUT_DIRECTORY),
				"../" + getLuniferaProjectInfo().getDtoServicesProjectName()
						+ "/models");
		return result;
	}

	@SuppressWarnings("deprecation")
	public IPreferenceStore getWritablePreferenceStore(IProject project) {
		ProjectScope projectScope = new ProjectScope(project);
		FixedScopedPreferenceStore result = new FixedScopedPreferenceStore(
				projectScope, "org.lunifera.dsl.entity.xtext.EntityGrammar");
		result.setSearchContexts(new IScopeContext[] { projectScope,
				new InstanceScope(), new ConfigurationScope() });
		return result;
	}

	public static String getDtosPreferenceKey(String preferenceName) {
		return EclipseOutputConfigurationProvider.OUTPUT_PREFERENCE_TAG
				+ PreferenceConstants.SEPARATOR + "DTOs"
				+ PreferenceConstants.SEPARATOR + preferenceName;
	}

	protected void configureEntityProjectFactory(BundleProjectFactory factory) {
		configureProjectFactory(factory);
		factory.addFolders(singletonList(MODEL_ROOT));
		List<String> requiredBundles = getEntityProjectRequiredBundles();
		factory.setProjectName(getLuniferaProjectInfo().getEntityProjectName());
		factory.addProjectNatures(getEntityProjectNatures());
		factory.addRequiredBundles(requiredBundles);
		factory.setLocation(getLuniferaProjectInfo().getEntityProjectLocation());
		factory.setProjectDefaultCharset(Charsets.UTF_8.name());
		factory.addContributor(createEntityProjectContributor());
		factory.addExportedPackages(singletonList(getLuniferaProjectInfo()
				.getEntityPackageName()));
	}

	/*
	 * WARNING!!! Before changing here something, look at the commit history and
	 * read following bug reports.
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=339004
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=370411
	 */
	protected List<String> getEntityProjectRequiredBundles() {
		List<String> requiredBundles = Lists.newArrayList(
				"org.lunifera.ecview.core.common;bundle-version=\"0.7.3\"", //$NON-NLS-1$
				"org.lunifera.dsl.datatype.lib;bundle-version=\"0.7.3\"", //$NON-NLS-1$
				"org.lunifera.dsl.dto.lib;bundle-version=\"0.7.3\"", //$NON-NLS-1$
				"com.google.guava;bundle-version=\"15.0.0\"", //$NON-NLS-1$
				"org.eclipse.xtext.xbase.lib;bundle-version=\"2.6.2\"", //$NON-NLS-1$
				"javax.persistence;bundle-version=\"2.1.0\"", //$NON-NLS-1$
				"org.lunifera.runtime.common;bundle-version=\"0.7.5\"", //$NON-NLS-1$
				"org.eclipse.xtend.lib;bundle-version=\"2.6.2\""); //$NON-NLS-1$

		String[] bundles = getLuniferaProjectInfo().getWizardContribution()
				.getRequiredBundles();
		for (String bundleId : bundles) {
			requiredBundles.add(bundleId.trim() + ";resolution:=optional"); //$NON-NLS-1$
		}
		for (String bundleId : getAdditionalRequiredBundles()) {
			requiredBundles.add(bundleId.trim());
		}
		return requiredBundles;
	}

	protected String[] getEntityProjectNatures() {
		return ENTITY_PROJECT_NATURES;
	}

	@Override
	protected BundleProjectFactory configureProjectFactory(
			ProjectFactory factory) {
		BundleProjectFactory result = (BundleProjectFactory) factory;
		result.addWorkingSets(Arrays.asList(getLuniferaProjectInfo()
				.getWorkingSets()));
		result.addBuilderIds(getBuilderIDs());
		result.addImportedPackages(getImportedPackages());
		result.addFolders(getAllFolders());
		return result;
	}

	protected String[] getBuilderIDs() {
		return BUILDERS;
	}

	protected String[] getTestProjectNatures() {
		return TEST_PROJECT_NATURES;
	}

	protected IProject createTestProject(final IProgressMonitor monitor)
			throws CoreException {
		BundleProjectFactory factory = createProjectFactory();
		configureTestProjectFactory(factory);
		factory.addContributor(createTestProjectContributor());
		return factory.createProject(monitor, null);
	}

	private TestProjectContributor createTestProjectContributor() {
		return new TestProjectContributor(getLuniferaProjectInfo());
	}

	protected IProject createFeatureProject(SubMonitor monitor)
			throws CoreException {
		FeatureProjectFactory factory = createFeatureFactory();
		configureFeatureProjectFactory(factory);
		return factory.createProject(monitor, null);
	}

	protected void configureFeatureProjectFactory(FeatureProjectFactory factory) {
		factory.setProjectName(getLuniferaProjectInfo().getFeatureProjectName());
		factory.setLocation(getLuniferaProjectInfo()
				.getFeatureProjectLocation());
		factory.setFeatureLabel(String.format("Feature for %s",
				getLuniferaProjectInfo().getModelNameAbbreviation()));
		factory.addProjectNatures("org.eclipse.pde.FeatureNature",
				"org.eclipse.m2e.core.maven2Nature");
		factory.addBuilderIds("org.eclipse.pde.FeatureBuilder",
				"org.eclipse.m2e.core.maven2Builder");
		factory.addWorkingSets(Arrays.asList(getLuniferaProjectInfo()
				.getWorkingSets()));
		factory.addContributor(createFeatureProjectContributor());
	}

	protected IProject createP2Project(SubMonitor monitor) throws CoreException {
		P2ProjectFactory factory = createP2Factory();
		configureP2ProjectFactory(factory);
		return factory.createProject(monitor, null);
	}

	protected void configureP2ProjectFactory(P2ProjectFactory factory) {
		factory.setProjectName(getLuniferaProjectInfo().getP2ProjectName());
		factory.setLocation(getLuniferaProjectInfo().getP2ProjectLocation());
		factory.setP2Label(String.format("P2  for %s", getLuniferaProjectInfo()
				.getModelNameAbbreviation()));
		factory.addProjectNatures("org.eclipse.m2e.core.maven2Nature");
		factory.addBuilderIds("org.eclipse.m2e.core.maven2Builder");
		factory.addFeature(getLuniferaProjectInfo().getFeatureProjectName());
		factory.addWorkingSets(Arrays.asList(getLuniferaProjectInfo()
				.getWorkingSets()));
		factory.addContributor(createP2ProjectContributor());
	}

	protected void configureTestProjectFactory(BundleProjectFactory factory) {
		configureProjectFactory(factory);
		factory.addFolders(singletonList(XTEND_GEN_ROOT));
		List<String> requiredBundles = getTestProjectRequiredBundles();
		factory.setProjectName(getLuniferaProjectInfo().getTestProjectName());
		factory.addProjectNatures(getTestProjectNatures());
		factory.addRequiredBundles(requiredBundles);
		factory.addImportedPackages(getTestProjectImportedPackages());
		factory.setProjectDefaultCharset(Charsets.UTF_8.name());
		factory.setLocation(getLuniferaProjectInfo().getTestProjectLocation());
	}

	protected List<String> getTestProjectImportedPackages() {
		return Lists.newArrayList("org.junit;version=\"4.5.0\"",
				"org.junit.runner;version=\"4.5.0\"",
				"org.junit.runner.manipulation;version=\"4.5.0\"",
				"org.junit.runner.notification;version=\"4.5.0\"",
				"org.junit.runners;version=\"4.5.0\"",
				"org.junit.runners.model;version=\"4.5.0\"",
				"org.hamcrest.core");
	}

	protected List<String> getTestProjectRequiredBundles() {
		List<String> requiredBundles = Lists.newArrayList(
				getLuniferaProjectInfo().getEntityProjectName(),
				getLuniferaProjectInfo().getDtoServicesProjectName(),
				"org.eclipse.core.runtime", //$NON-NLS-1$
				"org.junit;bundle-version=\"4.11.0\"", //$NON-NLS-1$
				"org.eclipse.equinox.ds" //$NON-NLS-1$
		); //$NON-NLS-1$
		return requiredBundles;
	}

	protected List<String> getImportedPackages() {
		return Lists.newArrayList("org.slf4j");
	}

	protected Collection<String> getAdditionalRequiredBundles() {
		return Collections.emptyList();
	}

	@Override
	protected String getModelFolderName() {
		return MODEL_ROOT;
	}

	@Override
	protected List<String> getAllFolders() {
		return SRC_FOLDER_LIST;
	}

	protected IProjectFactoryContributor createEntityProjectContributor() {
		EntityProjectContributor dslProjectContributor = new EntityProjectContributor(
				getLuniferaProjectInfo());
		dslProjectContributor.setSourceRoot(SRC_ROOT);
		dslProjectContributor.setModelRoot(MODEL_ROOT);
		return dslProjectContributor;
	}

	protected IProjectFactoryContributor createFeatureProjectContributor() {
		FeatureProjectContributor dslProjectContributor = new FeatureProjectContributor(
				getLuniferaProjectInfo());
		return dslProjectContributor;
	}

	protected IProjectFactoryContributor createDtoServiceProjectContributor() {
		DtoServicesProjectContributor dslProjectContributor = new DtoServicesProjectContributor(
				getLuniferaProjectInfo());
		dslProjectContributor.setSourceRoot(SRC_ROOT);
		return dslProjectContributor;
	}

	protected IProjectFactoryContributor createUiProjectContributor() {
		UiProjectContributor dslProjectContributor = new UiProjectContributor(
				getLuniferaProjectInfo());
		dslProjectContributor.setSourceRoot(SRC_ROOT);
		return dslProjectContributor;
	}

	protected IProjectFactoryContributor createAggregatorProjectContributor() {
		AggregatorProjectContributor dslProjectContributor = new AggregatorProjectContributor(
				getLuniferaProjectInfo());
		return dslProjectContributor;
	}

	protected IProjectFactoryContributor createP2ProjectContributor() {
		P2ProjectContributor dslProjectContributor = new P2ProjectContributor(
				getLuniferaProjectInfo());
		return dslProjectContributor;
	}

}
