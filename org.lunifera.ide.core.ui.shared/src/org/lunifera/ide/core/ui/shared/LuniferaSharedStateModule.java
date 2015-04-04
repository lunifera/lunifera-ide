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
package org.lunifera.ide.core.ui.shared;

import org.eclipse.xtext.service.AbstractGenericModule;
import org.eclipse.xtext.ui.wizard.IProjectCreator;
import org.lunifera.ide.core.api.i18n.II18nRegistry;
import org.lunifera.ide.core.ui.project.LuniferaProjectCreator;

import com.google.inject.Provider;

/**
 * Shared state module defines the bindings for the injector of this bundle.
 */
public class LuniferaSharedStateModule extends AbstractGenericModule {

	public Provider<II18nRegistry> provideII18nRegistry() {
		return Access.getII18nRegistry();
	}

	@SuppressWarnings("restriction")
	public Class<? extends IProjectCreator> bindIProjectCreator() {
		return LuniferaProjectCreator.class;
	}
}
