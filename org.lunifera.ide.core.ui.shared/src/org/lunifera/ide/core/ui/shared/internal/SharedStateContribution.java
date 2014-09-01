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
package org.lunifera.ide.core.ui.shared.internal;

import org.lunifera.ide.core.api.i18n.II18nRegistry;
import org.lunifera.ide.core.i18n.I18nRegistry;
import org.lunifera.ide.core.ui.shared.Access;

import com.google.inject.Binder;
import com.google.inject.Module;

/**
 * Contributes bindings to the Xtext framework. This module is registered by
 * plugin.xml. Xtext will use the bindings to create a child injector. <br>
 * <br>
 * Modules that want to use the singleton {@link II18nRegistry} needs to add
 * {@link Access#getII18nRegistry()} in their module description. It will create
 * a provider, that delegates to the extension cache. Then the
 * {@link II18nRegistry} will automatically become injected.<br>
 * <br>
 * If II18nRegistry is requested, the injector will take a look into the
 * extension registry. See
 * {@link org.eclipse.xtext.ui.shared.Access#contributedProvider(Class)}. So
 * only one instance is available at a time.
 */
public class SharedStateContribution implements Module {

	public void configure(Binder binder) {
		binder.bind(II18nRegistry.class).to(I18nRegistry.class);
	}
}
