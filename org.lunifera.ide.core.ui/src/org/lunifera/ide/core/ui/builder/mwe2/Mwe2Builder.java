package org.lunifera.ide.core.ui.builder.mwe2;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.ProgressMonitorWrapper;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This builder copies the .project from project root to the target/ folder.
 * Otherwise mwe2 will crash.
 */
public class Mwe2Builder extends IncrementalProjectBuilder {

	public static final String BUILDER_ID = "org.lunifera.ide.core.ui.shared.Mwe2Builder";

	private static final Logger LOGGER = LoggerFactory
			.getLogger(Mwe2Builder.class);

	public Mwe2Builder() {

	}

	@Override
	protected IProject[] build(int kind, Map<String, String> args,
			IProgressMonitor monitor) throws CoreException {

		try {
			IProject project = getProject();
			if (project.hasNature("org.eclipse.xtext.ui.shared.xtextNature")) {
				if (monitor != null) {
					final String taskName = "Copy .project to target/ for mwe2: "
							+ getProject().getName() + ": "; //$NON-NLS-1$
					monitor = new ProgressMonitorWrapper(monitor) {
						@Override
						public void subTask(String name) {
							super.subTask(taskName + name);
						}
					}; 

					IJavaProject javaProject = JavaCore.create(project);
					IPath outputLocation = javaProject.getOutputLocation();
					if (outputLocation.toString().endsWith("target/classes")) {
						IPath targetFile = outputLocation.removeLastSegments(1)
								.addTrailingSeparator().append(".project");
						if (!project.getFile(targetFile.removeFirstSegments(1))
								.exists()) {
							IFile sourceFile = project.getFile(".project");
							if (sourceFile.exists()) {
								sourceFile.copy(targetFile, true, monitor);
							}
						}
					}
				}

			}
			monitor.worked(8);
		} catch (OperationCanceledException e) {
			forgetLastBuiltState();
			throw e;
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}

		return null;
	}
}
