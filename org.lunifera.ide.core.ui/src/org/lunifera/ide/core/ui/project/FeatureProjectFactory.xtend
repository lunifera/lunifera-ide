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

import java.util.ArrayList
import java.util.List
import org.eclipse.core.resources.IProject
import org.eclipse.core.runtime.CoreException
import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.core.runtime.SubMonitor
import org.eclipse.swt.widgets.Shell
import org.eclipse.xtext.ui.util.ProjectFactory

/**
 * Creates a simple feature project.<br>
 * Created project contains .project, build.properties and feature.xml files<br>
 * Plugin entries can be added using {@link FeatureProjectFactory#addBundle(String)} method.<br>
 * 
 * @author Dennis Huebner - Initial contribution and API
 * @since 2.3
 */
class FeatureProjectFactory extends ProjectFactory {
	
	static String BUILD_PROPS_FILE_NAME = "build.properties";
	
	List<String> containedBundles = new ArrayList()
	List<String> includedFeatures = new ArrayList()
	String mainCategoryName
	
	String featureLabel
	
	def void setFeatureLabel(String label) {
		featureLabel = label
	}
	
	/**
	 * Adds a new plugin entry
	 */
	def FeatureProjectFactory addBundle(String bundleId) {
		containedBundles.add(bundleId);
		return this;
	}
	
	/**
	 * Adds a new included feature entry
	 */
	def FeatureProjectFactory addFeature(String featureId) {
		includedFeatures.add(featureId);
		return this;
	}
	

	
	/**
	 * @param mainCategoryName If not null or empty a category.xml will be created 
	 */
	def FeatureProjectFactory withCategoryFile(String mainCategoryName) {
		this.mainCategoryName = mainCategoryName
		return this;
	}
	
	@Override
	override protected void enhanceProject(IProject project, SubMonitor subMonitor, Shell shell) throws CoreException {
		super.enhanceProject(project, subMonitor, shell);
		createBuildProperties(project, subMonitor.newChild(1));
	}

	def private void createBuildProperties(IProject project, IProgressMonitor monitor) {
		'''
			bin.includes =�MANIFEST_FILENAME�
		'''
		.writeToFile(BUILD_PROPS_FILE_NAME, project, monitor);
	}

	
}