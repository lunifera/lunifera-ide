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

import java.util.concurrent.ArrayBlockingQueue;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

public class P2MirrorApplication implements IApplication {

	private ArrayBlockingQueue<String> queue;

	@Override
	public Object start(IApplicationContext context) throws Exception {
		queue = new ArrayBlockingQueue<String>(10);

		String msg;
		while (!(msg = queue.take()).equals("exit")) {
			System.out.println("Application shutdowned");
		}

		return EXIT_OK;
	}

	@Override
	public void stop() {

	}

}
