package org.lunifera.ide.core.ui.shared.resource;

import org.eclipse.core.resources.IProject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.xtext.common.types.access.impl.IndexedJvmTypeAccess;
import org.eclipse.xtext.common.types.access.jdt.IJavaProjectProvider;
import org.eclipse.xtext.common.types.access.jdt.IWorkingCopyOwnerProvider;
import org.eclipse.xtext.common.types.access.jdt.JdtTypeProviderFactory;
import org.eclipse.xtext.ui.resource.IResourceSetInitializer;
import org.lunifera.dsl.xtext.cache.resource.IndexResolvingTypeResourceFactory;

import com.google.inject.Inject;

@SuppressWarnings("restriction")
public class JvmTypesAwareResourceSetInitializer implements
		IResourceSetInitializer {

	@Inject
	private JdtTypeProviderFactory typeProviderFactory;

	@Inject
	IndexedJvmTypeAccess indexedJvmTypeAccess;

	@Inject
	IWorkingCopyOwnerProvider copyOwnerProvider;

	@Inject
	private IJavaProjectProvider javaProjectProvider;

	@Override
	public void initialize(ResourceSet resourceSet, IProject project) {
		typeProviderFactory.findOrCreateTypeProvider(resourceSet);

		IJavaProject javaProject = javaProjectProvider
				.getJavaProject(resourceSet);
		if (javaProject != null) {
			new IndexResolvingTypeResourceFactory(javaProject, resourceSet,
					indexedJvmTypeAccess,
					copyOwnerProvider.getWorkingCopyOwner(javaProject,
							resourceSet));
		}
	}
}
