/**
 * Copyright (c) 2012 Lunifera GmbH (Austria) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Code based on org.eclipse.xtext.ui.XtextProjectHelper
 * 
 * Contributors:
 *    Florian Pirchner - initial API and implementation
 */
package org.lunifera.ide.core.api.i18n;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoreUtil {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(CoreUtil.class);

	public static String NATURE_ID = "org.lunifera.ide.core.ui.LuniferaNature";
	public static final String BUILDER_ID = "org.lunifera.ide.core.ui.LuniferaBuilder";

	/**
	 * Returns true, if the given project contains the Lunifera Nature.
	 * 
	 * @param project
	 * @return
	 */
	public static boolean hasNature(IProject project) {
		try {
			if (project.isAccessible()) {
				return project.hasNature(NATURE_ID);
			}
		} catch (CoreException e) {
			LOGGER.error(e.getMessage(), e);
		}
		return false;
	}

	/**
	 * Returns true, if the given project contains the Lunifera Builder.
	 * 
	 * @param project
	 * @return
	 */
	public static boolean hasBuilder(IProject project) {
		if (project.isAccessible()) {
			try {
				for (ICommand command : project.getDescription().getBuildSpec()) {
					if (BUILDER_ID.equals(command.getBuilderName())) {
						return true;
					}
				}
			} catch (CoreException e) {
				LOGGER.error("Can't build due to an exception.", e);
			}
		}
		return false;
	}

}
