package org.lunifera.ide.core.ui;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.xtext.ui.shared.SharedStateModule;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class Activator extends Plugin {

	public static final String PLUGIN_ID = "org.lunifera.ide.core.ui"; //$NON-NLS-1$
	private static final Logger LOGGER = LoggerFactory
			.getLogger(Activator.class);
	private static Activator plugin;

	public static Activator getDefault() {
		return plugin;
	}

	private Injector injector;

	public Injector getInjector() {
		return injector;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		try {
			super.start(context);
			plugin = this;

			injector = Guice.createInjector(new SharedStateModule());
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
