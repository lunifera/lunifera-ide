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
package org.lunifera.ide.core.ui.project

import org.eclipse.xtext.ui.util.IProjectFactoryContributor
import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IFile

class DefaultProjectFactoryContributor implements IProjectFactoryContributor {

	override contributeFiles(IProject project, IFileCreator fileWriter) {}
	
	def protected IFile writeToFile(CharSequence chrSeq, IFileCreator fCreator, String fileName) {
		return fCreator.writeToFile(chrSeq,fileName);
	}
}