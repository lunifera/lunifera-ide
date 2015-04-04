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

import java.util.StringTokenizer;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.xtext.ui.wizard.IProjectInfo;
import org.eclipse.xtext.util.Strings;

/**
 * Simple value object class containing all relevant attributes necessary for
 * the creation of new Xtext projects.
 * 
 * @author Michael Clay - Initial contribution and API
 */
public class LuniferaProjectInfo implements IProjectInfo {

	private String projectName;
	private String modelName;
	private String fileExtension;
	private String encoding;
	private boolean createTestProject = false;
	private IWorkingSet[] workingSets;
	private IWorkbench workbench;
	private IPath projectsRootLocation;
	private boolean createEclipseRuntimeLaunchConfig;
	private boolean createFeatureProject;
	private WizardContribution wizardContribution;
	private String applicationName;

	public boolean isCreateEclipseRuntimeLaunchConfig() {
		return createEclipseRuntimeLaunchConfig;
	}

	public void setCreateEclipseRuntimeLaunchConfig(
			boolean createEclipseRuntimeLaunchConfig) {
		this.createEclipseRuntimeLaunchConfig = createEclipseRuntimeLaunchConfig;
	}

	/**
	 * @return the applicationName
	 */
	public String getApplicationName() {
		return applicationName;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getEncoding() {
		return encoding;
	}

	public boolean isCreateTestProject() {
		return createTestProject;
	}

	public void setCreateTestProject(boolean createTestProject) {
		this.createTestProject = createTestProject;
	}

	public boolean isCreateFeatureProject() {
		return createFeatureProject;
	}

	public void setCreateFeatureProject(boolean createFeatureProject) {
		this.createFeatureProject = createFeatureProject;
	}

	public String getFileExtension() {
		return fileExtension;
	}

	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}

	public String getModelName() {
		return modelName;
	}

	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;

		String[] tokens = projectName.split("\\.");
		if (tokens.length > 0) {
			applicationName = tokens[tokens.length - 1];
		} else {
			applicationName = projectName;
		}

	}

	public String getAggregatorProjectName() {
		return getProjectName() + ".aggregator"; //$NON-NLS-1$
	}

	public String getEntityProjectName() {
		return getProjectName() + ".entities"; //$NON-NLS-1$
	}

	public String getEntityPackageName() {
		return getEntityProjectName();
	}

	public String getTestProjectName() {
		return getProjectName() + ".tests"; //$NON-NLS-1$
	}

	public String getFeatureProjectName() {
		return getProjectName() + ".feature"; //$NON-NLS-1$;
	}
	
	public String getP2ProjectName() {
		return getProjectName() + ".p2"; //$NON-NLS-1$;
	}

	public String getDtoServicesProjectName() {
		return getProjectName() + ".dtos"; //$NON-NLS-1$
	}

	protected String getDtoPackageName() {
		return getDtoServicesProjectName();
	}

	protected String getDtoMapperPackageName() {
		return getDtoPackageName() + ".mapper";
	}

	protected String getServicesPackageName() {
		return getDtoPackageName() + ".services";
	}

	public String getUiProjectName() {
		return getProjectName() + ".ui"; //$NON-NLS-1$
	}

	public String getBasePackagePath() {
		return getBasePackage().replaceAll("\\.", "/"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public String getBasePackage() {
		int lastIndexOf = getModelName().lastIndexOf("."); //$NON-NLS-1$
		return getModelName().substring(0,
				(lastIndexOf == -1 ? getModelName().length() : lastIndexOf));
	}

	public String getModelNameAbbreviation() {
		String[] packageNames = modelName.split("\\."); //$NON-NLS-1$
		return Strings.toFirstUpper(packageNames[packageNames.length - 1]);
	}

	public String getNsURI() {
		String[] strings = modelName.split("\\."); //$NON-NLS-1$
		if (strings.length < 2) {
			return "http://www." + modelName; //$NON-NLS-1$
		}
		String s = "http://www." + strings[1] + "." + strings[0]; //$NON-NLS-1$ //$NON-NLS-2$
		for (int i = 2; i < strings.length; i++) {
			s += "/" + strings[i]; //$NON-NLS-1$
		}
		return s;
	}

	/**
	 * @return the firstFileExtension
	 */
	public String getFirstFileExtension() {
		String delim = ","; //$NON-NLS-1$
		if (getFileExtension() != null && getFileExtension().contains(delim)) {
			StringTokenizer tokenizer = new StringTokenizer(getFileExtension(),
					delim, false);
			if (tokenizer.hasMoreTokens())
				return tokenizer.nextToken().trim();
		}
		return fileExtension;
	}

	public void setWorkingSets(IWorkingSet[] workingSets) {
		this.workingSets = workingSets;
	}

	public IWorkingSet[] getWorkingSets() {
		return workingSets;
	}

	public void setWorkbench(IWorkbench workbench) {
		this.workbench = workbench;
	}

	public IWorkbench getWorkbench() {
		return workbench;
	}

	public void setProjectsRootLocation(IPath projectsRootLocation) {
		this.projectsRootLocation = projectsRootLocation;
	}

	public IPath getEntityProjectLocation() {
		return getAggregatorProjectLocation().append(getEntityProjectName());
	}

	public IPath getDtoServicesProjectLocation() {
		return getAggregatorProjectLocation().append(
				getDtoServicesProjectName());
	}

	public IPath getUiProjectLocation() {
		return getAggregatorProjectLocation().append(getUiProjectName());
	}

	public IPath getTestProjectLocation() {
		return getAggregatorProjectLocation().append(getTestProjectName());
	}

	public IPath getFeatureProjectLocation() {
		return getAggregatorProjectLocation().append(getFeatureProjectName());
	}
	
	public IPath getP2ProjectLocation() {
		return getAggregatorProjectLocation().append(getP2ProjectName());
	}

	public IPath getAggregatorProjectLocation() {
		return projectsRootLocation.append(getAggregatorProjectName());
	}

	public void setWizardContribution(WizardContribution wizardContribution) {
		this.wizardContribution = wizardContribution;
	}

	public WizardContribution getWizardContribution() {
		return wizardContribution;
	}

	/**
	 * @return Source Folder relative path to the Grammar file
	 */
	public String getEntityFilePath() {
		return getModelNameAbbreviation() + ".entitymodel";
	}

}
