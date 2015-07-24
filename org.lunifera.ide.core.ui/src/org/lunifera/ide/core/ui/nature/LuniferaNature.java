/**
 * Copyright (c) 2011 - 2015, Lunifera GmbH (Gross Enzersdorf), Loetz KG (Heidelberg)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *         Florian Pirchner - Initial implementation
 */
package org.lunifera.ide.core.ui.nature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.xtext.builder.impl.XtextBuilder;
import org.lunifera.ide.core.api.i18n.CoreUtil;
import org.lunifera.ide.core.ui.builder.LuniferaBuilder;

@SuppressWarnings("restriction")
public class LuniferaNature implements IProjectNature {

	public static String NATURE_ID = CoreUtil.NATURE_ID;

	private IProject project;

	public void configure() throws CoreException {
		IProjectDescription desc = project.getDescription();
		ICommand[] commands = desc.getBuildSpec();

		List<ICommand> temp = new ArrayList<ICommand>(Arrays.asList(commands));
		if (!contains(temp, LuniferaBuilder.BUILDER_ID)) {
			ICommand command = desc.newCommand();
			command.setBuilderName(LuniferaBuilder.BUILDER_ID);
			temp.add(command);
		}

		temp.sort(new Comparator<ICommand>() {
			@Override
			public int compare(ICommand o1, ICommand o2) {
				// xtext builder must go first
				if (o1.getBuilderName().equals(XtextBuilder.BUILDER_ID)) {
					return -1;
				}
				if (o1.getBuilderName().equals(LuniferaBuilder.BUILDER_ID)) {
					return +1;
				}
				return 0;
			}
		});

		desc.setBuildSpec(temp.toArray(new ICommand[temp.size()]));
		project.setDescription(desc, null);
	}

	private boolean contains(List<ICommand> temp, String builderId) {
		for (ICommand cmd : temp) {
			if (cmd.getBuilderName().equals(builderId)) {
				return true;
			}
		}
		return false;
	}
	
	private void remove(List<ICommand> temp, String builderId) {
		for (Iterator<ICommand> iterator = temp.iterator(); iterator.hasNext();) {
			ICommand iCommand = iterator.next();
			if (iCommand.getBuilderName().equals(builderId)) {
				iterator.remove();
				return;
			}
		}
	}

	public void deconfigure() throws CoreException {
		IProjectDescription description = getProject().getDescription();
		ICommand[] commands = description.getBuildSpec();
		List<ICommand> temp = new ArrayList<ICommand>(Arrays.asList(commands));
		
		remove(temp, LuniferaBuilder.BUILDER_ID);
		
		description.setBuildSpec(temp.toArray(new ICommand[temp.size()]));
		project.setDescription(description, null);
	}

	public IProject getProject() {
		return project;
	}

	public void setProject(IProject project) {
		this.project = project;
	}

}
