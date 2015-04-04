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

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.xtext.ui.wizard.IProjectCreator;
import org.eclipse.xtext.ui.wizard.IProjectInfo;
import org.eclipse.xtext.ui.wizard.XtextNewProjectWizard;
import org.lunifera.ide.core.ui.CoreUiActivator;

import com.google.inject.Inject;

public class NewLuniferaProjectWizard extends XtextNewProjectWizard {

	private WizardNewLuniferaProjectCreationPage mainPage;

	/**
	 * Constructs a new wizard
	 */
	@Inject
	public NewLuniferaProjectWizard(IProjectCreator projectCreator) {
		super(projectCreator);
		setWindowTitle("");
		// TODO 
//		setDefaultPageImageDescriptor(CoreUiActivator.getDefault().getImageDescriptor("icons/wizban/newxprj_wiz.gif")); //$NON-NLS-1$
	}

	@Override
	public void addPages() {
		super.addPages();
		mainPage = new WizardNewLuniferaProjectCreationPage("mainPage", this.selection); //$NON-NLS-1$
		addPage(mainPage);
	}

	@Override
	protected IProjectInfo getProjectInfo() {
		LuniferaProjectInfo projectInfo = createProjectInfo();
		projectInfo.setCreateTestProject(true);
		projectInfo.setCreateFeatureProject(mainPage.isCreateFeatureProject());
		projectInfo.setModelName(mainPage.getModelName());
		projectInfo.setProjectName(mainPage.getProjectName());
		projectInfo.setWorkingSets(mainPage.getSelectedWorkingSets());
		Map<String, WizardContribution> contributions = WizardContribution.getFromRegistry();
		projectInfo.setWizardContribution(contributions.get(mainPage.getGeneratorConfig()));
		projectInfo.setProjectsRootLocation(mainPage.getLocationPath());
		projectInfo.setWorkbench(getWorkbench());
		projectInfo.setCreateEclipseRuntimeLaunchConfig(!existsEclipseRuntimeLaunchConfig());
		String encoding = null;
		try {
			encoding = ResourcesPlugin.getWorkspace().getRoot().getDefaultCharset();
		}
		catch (final CoreException e) {
			encoding = System.getProperty("file.encoding");
		}
		projectInfo.setEncoding(encoding);
		return projectInfo;
	}

	private boolean existsEclipseRuntimeLaunchConfig() {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (IProject p : projects) {
			try {
				if (p.isAccessible() && p.getFile(".launch/Launch Runtime Eclipse.launch").exists())
					return true;
			} catch (Exception e) {
				// ignore
			}
		}
		return false;
	}

	protected LuniferaProjectInfo createProjectInfo() {
		return new LuniferaProjectInfo();
	}
	
}