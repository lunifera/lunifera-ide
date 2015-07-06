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

package org.lunifera.ide.tools.p2.mirror;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.osgi.framework.Bundle;

public class DummyApplicationContext implements IApplicationContext {

	private Map<String, Object> args = new HashMap<String, Object>();

	@Override
	public Map<String, Object> getArguments() {
		return args;
	}

	@Override
	public void applicationRunning() {

	}

	@Override
	public String getBrandingApplication() {
		return null;
	}

	@Override
	public String getBrandingName() {
		return null;
	}

	@Override
	public String getBrandingDescription() {
		return null;
	}

	@Override
	public String getBrandingId() {
		return null;
	}

	@Override
	public String getBrandingProperty(String key) {
		return null;
	}

	@Override
	public Bundle getBrandingBundle() {
		return null;
	}

	@Override
	public void setResult(Object result, IApplication application) {
		// TODO Auto-generated method stub

	}

}
