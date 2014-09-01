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
package org.lunifera.ide.core.ui.shared;

import org.eclipse.xtext.service.AbstractGenericModule;
import org.lunifera.ide.core.api.i18n.II18nRegistry;

import com.google.inject.Provider;

/**
 * Shared state module defines the bindings for the injector of this bundle.
 */
public class LuniferaSharedStateModule extends AbstractGenericModule {

	public Provider<II18nRegistry> provideII18nRegistry() {
		return Access.getII18nRegistry();
	}
}
