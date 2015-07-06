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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.equinox.p2.internal.repository.mirroring.IArtifactMirrorLog;
import org.eclipse.equinox.p2.repository.artifact.IArtifactDescriptor;
import org.eclipse.osgi.framework.console.CommandInterpreter;

public class MirrorLog implements IArtifactMirrorLog {

	private static final String INDENT = "\t"; //$NON-NLS-1$
	private static final String SEPARATOR = System
			.getProperty("line.separator"); //$NON-NLS-1$
	private boolean consoleMessage = false;
	private int minSeverity = IStatus.OK;
	private boolean hasRoot = false;
	private CommandInterpreter ci;

	public MirrorLog(CommandInterpreter ci) {
		this.minSeverity = minSeverity;
		this.ci = ci;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.equinox.internal.p2.artifact.mirror.IArtifactMirrorLog#log
	 * (org.eclipse.equinox.internal.provisional.p2.artifact.repository.
	 * IArtifactDescriptor, org.eclipse.core.runtime.IStatus)
	 */
	public void log(IArtifactDescriptor descriptor, IStatus status) {
		if (status.getSeverity() >= minSeverity) {
			log(descriptor.toString());
			log(status, INDENT);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.equinox.internal.p2.artifact.mirror.IArtifactMirrorLog#log
	 * (org.eclipse.core.runtime.IStatus)
	 */
	public void log(IStatus status) {
		log(status, ""); //$NON-NLS-1$
	}

	/*
	 * Write a status to the log, indenting it based on status depth.
	 * 
	 * @param status the status to log
	 * 
	 * @param depth the depth of the status
	 */
	private void log(IStatus status, String prefix) {
		if (status.getSeverity() >= minSeverity) {
			// Write status to log
			log(prefix + status.getMessage());

			// Write exception to log if applicable
			String exceptionMessage = status.getException() != null ? status
					.getException().getMessage() : null;
			if (exceptionMessage != null)
				log(prefix + exceptionMessage);

			// Write the children of the status to the log
			IStatus[] nestedStatus = status.getChildren();
			if (nestedStatus != null)
				for (int i = 0; i < nestedStatus.length; i++)
					log(nestedStatus[i], prefix + INDENT);
		}
	}

	/*
	 * Write a message to the log
	 * 
	 * @param message the message to write
	 */
	private void log(String message) {
		ci.print((hasRoot ? INDENT : "") + message + SEPARATOR); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.equinox.internal.p2.artifact.mirror.IArtifactMirrorLog#close
	 * ()
	 */
	public void close() {
	}
}
