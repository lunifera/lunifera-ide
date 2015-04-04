package org.lunifera.ide.tools.p2.mirror;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.equinox.p2.internal.repository.tools.MirrorApplication;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.osgi.service.application.ApplicationDescriptor;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

@Component
public class ConsoleCommands implements
		org.eclipse.osgi.framework.console.CommandProvider {

	private ComponentContext context;
	private Set<Mirror> mirrors = new HashSet<ConsoleCommands.Mirror>();
	private ApplicationDescriptor artifactsDesc;
	private ApplicationDescriptor metadataDesc;

	@Override
	public String getHelp() {
		return "Lunifera P2 mirror commands";
	}

	@Activate
	protected void activate(ComponentContext context) {
		this.context = context;

		loadMirrors(context);
	}

	@Reference(cardinality = ReferenceCardinality.MANDATORY, target = "(service.pid=org.eclipse.equinox.p2.metadata.repository.mirrorApplication)")
	protected void bindMetadata(ApplicationDescriptor metadataDesc) {
		this.metadataDesc = metadataDesc;
	}

	@Reference(cardinality = ReferenceCardinality.MANDATORY, target = "(service.pid=org.eclipse.equinox.p2.artifact.repository.mirrorApplication)")
	protected void bindArtifacts(ApplicationDescriptor artifactsDesc) {
		this.artifactsDesc = artifactsDesc;
	}

	/**
	 * Loads all mirrors.
	 * 
	 * @param context
	 */
	private void loadMirrors(ComponentContext context) {
		try {
			File settings = context.getBundleContext().getDataFile(
					"lunp2mirrors");
			if (!settings.exists()) {
				settings.createNewFile();
			}

			InputStream inputStream = new FileInputStream(settings);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream));
			String line = null;
			// Read file line by line
			while ((line = reader.readLine()) != null) {
				mirrors.add(Mirror.parse(line));
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Deactivate
	protected void deactivate(ComponentContext context) {
		this.context = null;
	}

	public void _lunp2mirror(final CommandInterpreter ci) throws Exception {
		String argument = ci.nextArgument();
		if (argument == null) {
			ci.println(getHelp());
		} else if (argument.equals("ls")) {
			printMirrors(ci);
		} else if (argument.equals("add")) {
			addMirror(ci);
		} else if (argument.equals("update")) {
			update(ci);
		} else {
			ci.println("ERROR - not a valid command!");
			ci.println(getHelp());
		}
	}

	// lunp2mirror add luniferaP2 http://....com
	private void addMirror(CommandInterpreter ci) {

		String id = ci.nextArgument();
		if (id == null) {
			ci.println("\tERROR: No id specified!");
			return;
		}

		String url = ci.nextArgument();
		if (url == null) {
			ci.println("\tERROR: No URL specified!");
			return;
		}

		String destination = ci.nextArgument();
		if (destination == null) {
			ci.println("\tERROR: No destination specified!");
			return;
		}

		Mirror newMirror = new Mirror(id, url, destination);
		mirrors.add(newMirror);

		saveFile();

	}

	private void saveFile() {
		StringBuilder b = new StringBuilder();
		for (Mirror mirror : mirrors) {
			b.append(mirror.toString());
			b.append("\n");
		}

		try {
			File settings = context.getBundleContext().getDataFile(
					"lunp2mirrors");
			FileWriter writer = new FileWriter(settings);
			writer.append(b.toString());
			writer.flush();
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void update(CommandInterpreter ci) {
		String id = ci.nextArgument();
		if (id == null) {
			ci.println("\tERROR: No id specified!");
			return;
		}

		for (Mirror mirror : mirrors) {
			if (mirror.id.equals(id)) {
				try {
					run(ci, mirror);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
		}

	}

	private void printMirrors(CommandInterpreter ci) {

		StringBuilder b = new StringBuilder();
		for (Mirror mirror : mirrors) {
			b.append("\tId: ");
			b.append(mirror.id);
			b.append("\tURL: ");
			b.append(mirror.url);
			b.append("\tDestination: ");
			b.append(mirror.destination);
			b.append("\n");
		}

		ci.println("\t---- Available mirrors ----\n");
		ci.println(b.toString());
	}

	@SuppressWarnings("restriction")
	public void run(CommandInterpreter ci, Mirror mirror) throws Exception {
		try {
			// Map<String, Object> launchArgs = new HashMap<>(1);

			String[] args = new String[] { "-source", mirror.url,
					"-destination", mirror.destination, "-writeMode", "clean", "-references", "false" };
			// launchArgs.put(IApplicationContext.APPLICATION_ARGS, args);

			DummyApplicationContext context = new DummyApplicationContext();
			context.getArguments().put(IApplicationContext.APPLICATION_ARGS,
					args);
			MirrorApplication metadataApplication = new MirrorApplication();
			Map<String, String> initArgs = new HashMap<String, String>(1);
			initArgs.put("metadataOrArtifacts", "metadata");
			metadataApplication.setInitializationData(null, null, initArgs);
			MirrorLog log = new MirrorLog(ci);
			metadataApplication.setLog(log);
			metadataApplication.start(context);

			MirrorApplication artifactsApplication = new MirrorApplication();
			initArgs = new HashMap<String, String>(1);
			initArgs.put("metadataOrArtifacts", "artifacts");
			artifactsApplication.setInitializationData(null, null, initArgs);
			artifactsApplication.setLog(log);
			artifactsApplication.start(context);

		} finally {
		}
		return;
	}

	private static class Mirror {
		public final String id;
		public final String url;
		public final String destination;

		public Mirror(String id, String url, String destination) {
			super();
			this.id = id;
			this.url = url;
			this.destination = destination;
		}

		public static Mirror parse(String line) {
			String[] tokens = line.split(";");
			return new Mirror(tokens[0], tokens[1], tokens[2]);
		}

		public String toString() {
			return String.format("%s;%s;%s", id, url, destination);
		}

	}

}
