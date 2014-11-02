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
package org.lunifera.ide.core.ui.shared.internal;

import org.eclipse.xtext.builder.impl.IQueuedBuildDataContribution;
import org.eclipse.xtext.builder.impl.IToBeBuiltComputerContribution;
import org.eclipse.xtext.builder.impl.javasupport.JdtQueuedBuildData;
import org.eclipse.xtext.builder.impl.javasupport.JdtToBeBuiltComputer;
import org.eclipse.xtext.builder.impl.javasupport.JdtToBeBuiltComputer.ModificationStampCache;
import org.eclipse.xtext.builder.impl.javasupport.ProjectClasspathChangeListener;
import org.eclipse.xtext.builder.trace.IStorageAwareTraceContribution;
import org.eclipse.xtext.builder.trace.JarEntryAwareTrace;
import org.eclipse.xtext.common.types.access.IJvmTypeProvider;
import org.eclipse.xtext.common.types.access.impl.IndexedJvmTypeAccess;
import org.eclipse.xtext.common.types.access.jdt.IJavaProjectProvider;
import org.eclipse.xtext.common.types.access.jdt.IWorkingCopyOwnerProvider;
import org.eclipse.xtext.common.types.access.jdt.JdtTypeProviderFactory;
import org.eclipse.xtext.common.types.access.jdt.TypeURIHelper;
import org.eclipse.xtext.common.types.access.jdt.WorkingCopyOwnerProvider;
import org.eclipse.xtext.common.types.descriptions.EObjectDescriptionBasedStubGenerator;
import org.eclipse.xtext.common.types.xtext.ui.XtextResourceSetBasedProjectProvider;
import org.eclipse.xtext.generator.trace.TraceURIHelper;
import org.eclipse.xtext.resource.IResourceDescriptions;
import org.eclipse.xtext.resource.impl.ResourceDescriptionsProvider;
import org.eclipse.xtext.resource.impl.ResourceSetBasedResourceDescriptions;
import org.eclipse.xtext.ui.containers.JavaProjectsStateHelper;
import org.eclipse.xtext.ui.containers.StrictJavaProjectsState;
import org.eclipse.xtext.ui.generator.trace.ITraceURIConverterContribution;
import org.eclipse.xtext.ui.generator.trace.JavaProjectAwareTraceContribution;
import org.eclipse.xtext.ui.resource.IResourceSetInitializer;
import org.eclipse.xtext.ui.resource.IStorage2UriMapperContribution;
import org.eclipse.xtext.ui.resource.IStorage2UriMapperJdtExtensions;
import org.eclipse.xtext.ui.resource.JarEntryLocator;
import org.eclipse.xtext.ui.resource.JavaProjectResourceSetInitializer;
import org.eclipse.xtext.ui.resource.Storage2UriMapperJavaImpl;
import org.eclipse.xtext.ui.shared.contribution.IEagerContribution;
import org.eclipse.xtext.ui.shared.internal.JavaCoreListenerRegistrar;
import org.lunifera.ide.core.api.i18n.II18nRegistry;
import org.lunifera.ide.core.i18n.I18nRegistry;
import org.lunifera.ide.core.ui.shared.Access;
import org.lunifera.ide.core.ui.shared.resource.JvmTypesAwareResourceSetInitializer;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

/**
 * Contributes bindings to the Xtext framework. This module is registered by
 * plugin.xml. Xtext will use the bindings to create a child injector. <br>
 * <br>
 * Modules that want to use the singleton {@link II18nRegistry} needs to add
 * {@link Access#getII18nRegistry()} in their module description. It will create
 * a provider, that delegates to the extension cache. Then the
 * {@link II18nRegistry} will automatically become injected.<br>
 * <br>
 * If II18nRegistry is requested, the injector will take a look into the
 * extension registry. See
 * {@link org.eclipse.xtext.ui.shared.Access#contributedProvider(Class)}. So
 * only one instance is available at a time.
 */
public class SharedStateContribution implements Module {

	@SuppressWarnings("restriction")
	public void configure(Binder binder) {
		binder.bind(II18nRegistry.class).to(I18nRegistry.class);

		binder.bind(JarEntryLocator.class);

		binder.bind(ProjectClasspathChangeListener.class);

		binder.bind(JavaProjectsStateHelper.class);
		// binder.bind(JavaProjectsState.class);
		binder.bind(StrictJavaProjectsState.class);

		binder.bind(IEagerContribution.class).to(
				JavaCoreListenerRegistrar.class);

		binder.bind(IStorage2UriMapperJdtExtensions.class).to(
				Storage2UriMapperJavaImpl.class);
		binder.bind(IStorage2UriMapperContribution.class).to(
				Storage2UriMapperJavaImpl.class);
		binder.bind(Storage2UriMapperJavaImpl.class).in(Scopes.SINGLETON);

		binder.bind(IStorageAwareTraceContribution.class).to(
				JarEntryAwareTrace.class);

//		binder.bind(IResourceSetInitializer.class).to(
//				JavaProjectResourceSetInitializer.class);

		binder.bind(IndexedJvmTypeAccess.class);
		
		binder.bind(IToBeBuiltComputerContribution.class).to(
				JdtToBeBuiltComputer.class);
		binder.bind(IQueuedBuildDataContribution.class).to(
				JdtQueuedBuildData.class);
		binder.bind(TypeURIHelper.class);
		binder.bind(ModificationStampCache.class);

		binder.bind(TraceURIHelper.class);
		binder.bind(ITraceURIConverterContribution.class).to(
				JavaProjectAwareTraceContribution.class);

		binder.bind(IResourceDescriptions.class)
				.annotatedWith(
						Names.named(ResourceDescriptionsProvider.NAMED_BUILDER_SCOPE))
				.to(ResourceSetBasedResourceDescriptions.class);
		binder.bind(IResourceDescriptions.class)
				.annotatedWith(
						Names.named(ResourceDescriptionsProvider.LIVE_SCOPE))
				.to(ResourceSetBasedResourceDescriptions.class);
		binder.bind(IResourceDescriptions.class)
				.annotatedWith(
						Names.named(ResourceDescriptionsProvider.PERSISTED_DESCRIPTIONS))
				.to(ResourceSetBasedResourceDescriptions.class);
		// binder.bind(IResourceDescriptions.class).to(
		// ResourceSetBasedResourceDescriptions.class);

		binder.bind(ResourceDescriptionsProvider.class);

		binder.bind(IJavaProjectProvider.class).to(
				XtextResourceSetBasedProjectProvider.class);
		binder.bind(IResourceSetInitializer.class).to(
				JvmTypesAwareResourceSetInitializer.class);
		binder.bind(IJvmTypeProvider.Factory.class).to(
				JdtTypeProviderFactory.class);
		binder.bind(JdtTypeProviderFactory.class);
		binder.bind(IWorkingCopyOwnerProvider.class).to(
				WorkingCopyOwnerProvider.class);
		binder.bind(EObjectDescriptionBasedStubGenerator.class);
	}
}
