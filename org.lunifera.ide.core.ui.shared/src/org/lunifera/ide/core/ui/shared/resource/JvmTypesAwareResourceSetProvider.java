package org.lunifera.ide.core.ui.shared.resource;

import org.eclipse.core.resources.IProject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.ui.resource.XtextResourceSetProvider;

import com.google.inject.Inject;

public class JvmTypesAwareResourceSetProvider extends XtextResourceSetProvider {

	@Inject
	private JvmTypesAwareResourceSetInitializer intializer;

	@Override
	public ResourceSet get(IProject project) {
		ResourceSet resourceSet = super.get(project);
		intializer.initialize(resourceSet, project);
		return resourceSet;
	}

}
