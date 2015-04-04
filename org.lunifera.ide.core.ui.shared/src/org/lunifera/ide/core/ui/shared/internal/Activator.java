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
package org.lunifera.ide.core.ui.shared.internal;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.xtext.ui.shared.SharedStateModule;
import org.lunifera.ide.core.ui.shared.LuniferaSharedStateModule;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

public class Activator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.lunifera.ide.core.ui.shared"; //$NON-NLS-1$
	private static final Logger LOGGER = LoggerFactory
			.getLogger(Activator.class);
	private static Activator plugin;

	public static Activator getDefault() {
		return plugin;
	}

	private Injector injector;

	public Injector getInjector() {
		if (injector == null) {
			injector = Guice.createInjector(Modules.override(
					new SharedStateModule()).with(new LuniferaSharedStateModule()));
		}
		return injector;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		try {
			super.start(context);
			plugin = this;

		} catch (Exception e) {
			LOGGER.error(
					"Error initializing " + PLUGIN_ID + ":" + e.getMessage(), e);
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		injector = null;
		super.stop(context);
	}

}
