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

import org.lunifera.ide.core.api.i18n.II18nRegistry;

import com.google.inject.Provider;

public class Access extends org.eclipse.xtext.ui.shared.Access {

	public static Provider<II18nRegistry> getII18nRegistry() {
		return Access.<II18nRegistry> contributedProvider(II18nRegistry.class);
	}
}
 