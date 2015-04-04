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
package org.lunifera.ide.core.ui;

import java.util.Hashtable;
import java.util.Map;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoreUiActivator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.lunifera.ide.core.ui"; //$NON-NLS-1$
	private static final Logger LOGGER = LoggerFactory
			.getLogger(CoreUiActivator.class);
	private static CoreUiActivator plugin;

	public static CoreUiActivator getDefault() {
		return plugin;
	}

	private EventAdmin eventAdmin;

	@Override
	public void start(BundleContext context) throws Exception {
		try {
			super.start(context);
			plugin = this;

			ServiceReference<EventAdmin> ref = context
					.getServiceReference(EventAdmin.class);
			eventAdmin = context.getService(ref);

		} catch (Exception e) {
			LOGGER.error(
					"Error initializing " + PLUGIN_ID + ":" + e.getMessage(), e);
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the event admin.
	 * 
	 * @return
	 */
	public EventAdmin getEventAdmin() {
		return eventAdmin;
	}

	/**
	 * Registers a eventhandler at the event admin.
	 * 
	 * @param handler
	 * @param props
	 * @return
	 */
	public ServiceRegistration<EventHandler> registerEventHandler(
			EventHandler handler, Map<String, String> props) {
		Hashtable<String, String> castedProps = new Hashtable<String, String>(
				props);
		return getBundle().getBundleContext().registerService(
				EventHandler.class, handler, castedProps);
	}

}
