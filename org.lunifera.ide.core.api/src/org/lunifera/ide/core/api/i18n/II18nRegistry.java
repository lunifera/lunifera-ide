package org.lunifera.ide.core.api.i18n;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

/**
 * Caches the I18n keys and their values based in {@link IProject workspace
 * proejcts}.
 */
public interface II18nRegistry {

	/**
	 * Returns the translated value for the given project, locale and key.
	 * 
	 * @param project
	 * @param locale
	 * @param key
	 * @return
	 */
	String getText(IProject project, Locale locale, String key);

	/**
	 * Caches the ProjectDescription.
	 * 
	 * @param project
	 * @param resource
	 */
	void cache(ProjectDescription description);

	/**
	 * Caches the ResourceDescription.
	 * 
	 * @param project
	 * @param resource
	 */
	void cache(ResourceDescription description);

	/**
	 * Contains all required information about a project. It contains all
	 * resources.
	 */
	public static class ProjectDescription {

		private final Map<Locale, List<ResourceDescription>> resources = Collections
				.synchronizedMap(new HashMap<Locale, List<ResourceDescription>>());

		private final IProject project;

		public ProjectDescription(IProject project) {
			this.project = project;
		}

		public IProject getProject() {
			return project;
		}

		/**
		 * Returns all resource descriptions contained in the project for the
		 * given locale.
		 * 
		 * @param locale
		 * @return
		 */
		public List<ResourceDescription> getResourceDescriptions(Locale locale) {
			List<ResourceDescription> result = new ArrayList<ResourceDescription>();
			collectByLocale(locale, result);
			return result;
		}

		public ResourceDescription collectByLocale(Locale locale,
				List<ResourceDescription> result) {
			List<ResourceDescription> descs = resources.get(locale);
			if (descs != null) {
				result.addAll(descs);
			}
			return null;
		}

		/**
		 * Puts the list of resources into the resources map.
		 * 
		 * @param key
		 * @param value
		 */
		public void putResources(Locale key, List<ResourceDescription> value) {
			resources.put(key, value);
		}

		/**
		 * Puts a single resource into the resources map.
		 * 
		 * @param key
		 * @param value
		 * @return
		 */
		public void putResource(Locale key, ResourceDescription value) {

			List<ResourceDescription> descriptions = resources.get(key);
			if (descriptions == null) {
				descriptions = new ArrayList<II18nRegistry.ResourceDescription>();
				descriptions.add(value);
				resources.put(key, descriptions);
			} else {
				descriptions.add(value);
			}
		}

	}

	/**
	 * A resource is a file containing all translations for a file.
	 */
	@SuppressWarnings("serial")
	public static class ResourceDescription extends HashMap<String, String> {

		private final IProject project;
		private final Locale locale;
		private final IPath path;

		public ResourceDescription(IProject project, Locale locale, IPath path) {
			this.project = project;
			this.locale = locale;
			this.path = path;
		}

		public IProject getProject() {
			return project;
		}

		public Locale getLocale() {
			return locale;
		}

		public IPath getPath() {
			return path;
		}

	}
}
