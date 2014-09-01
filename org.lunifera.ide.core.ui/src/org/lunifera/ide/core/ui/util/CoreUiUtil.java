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
package org.lunifera.ide.core.ui.util;

import java.util.Locale;

import org.eclipse.core.resources.IProject;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.xtext.common.types.access.jdt.IJavaProjectProvider;

import com.google.inject.Inject;

/**
 * Util class that offers convenient methods.
 */
public class CoreUiUtil {

	@Inject
	private IJavaProjectProvider projectProvider;

	/**
	 * Returns the project where the xtextModel is located in.
	 * 
	 * @param xtextElement
	 * @return
	 */
	public IProject getProject(EObject xtextElement) {
		IJavaProject javaProject = getJavaProject(xtextElement);
		if (javaProject == null) {
			return null;
		}
		return javaProject.getProject();
	}

	/**
	 * Returns the javaProject where the xtextModel is located in.
	 * 
	 * @param xtextElement
	 * @return
	 */
	public IJavaProject getJavaProject(EObject xtextElement) {
		return projectProvider.getJavaProject(xtextElement.eResource()
				.getResourceSet());
	}

	/**
	 * Returns the current locale. For now {@link Locale#getDefault()} is used.
	 * Later maybe a value from preferences is returned.
	 * 
	 * @return
	 */
	public Locale getLocale() {
		return Locale.getDefault();
	}

}
