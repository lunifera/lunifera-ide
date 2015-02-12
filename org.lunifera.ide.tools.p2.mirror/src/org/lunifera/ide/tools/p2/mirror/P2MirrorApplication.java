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
