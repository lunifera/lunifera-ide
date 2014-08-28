/**
 * Copyright (c) 2012 Lunifera GmbH (Austria) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Florian Pirchner - initial API and implementation
 */
package org.lunifera.ide.core.api.i18n;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

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
	 * Removes the given resource from the project description.
	 * 
	 * @param project
	 * @param locale
	 * @param location
	 */
	void removeResource(IProject project, Locale locale, IPath location);
	
	
	/**
	 * Removes the project from the cache.
	 * 
	 * @param project
	 */
	void removeProject(IProject project);

	/**
	 * Returns all project descriptions.
	 * 
	 * @return
	 */
	Set<ProjectDescription> getProjectDescriptions();

	/**
	 * Contains all required information about a project. It contains all
	 * resources.
	 */
	public static class ProjectDescription {

		private final Map<Locale, Set<ResourceDescription>> resources = Collections
				.synchronizedMap(new HashMap<Locale, Set<ResourceDescription>>());

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

		private ResourceDescription collectByLocale(Locale locale,
				List<ResourceDescription> result) {
			Set<ResourceDescription> descs = resources.get(locale);
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
		public void putResources(Locale key, Set<ResourceDescription> value) {
			resources.put(key, value);
		}

		/**
		 * Puts a single resource into the resources map.
		 * 
		 * @param key
		 * @param value
		 * @return
		 */
		public void putResource(ResourceDescription value) {

			Set<ResourceDescription> descriptions = resources.get(value.getLocale());
			if (descriptions == null) {
				descriptions = new HashSet<II18nRegistry.ResourceDescription>();
				descriptions.add(value);
				resources.put(value.getLocale(), descriptions);
			} else {
				// if the hash key of given value is contained in set, then
				// remove it
				descriptions.remove(value);
				// and add it again
				descriptions.add(value);
			}
		}

		/**
		 * Removes the given resource from the internal list of resources.
		 * 
		 * @param resource
		 * @return
		 */
		public void removeResource(ResourceDescription value) {
			synchronized (resources) {
				Locale locale = value.getLocale();
				IPath path = value.getPath();

				removeResource(locale, path);
			}
		}

		/**
		 * Removes the resource for the given path.
		 * 
		 * @param locale
		 * @param location
		 */
		public void removeResource(Locale locale, IPath location) {
			Set<ResourceDescription> descriptions = resources.get(locale);
			for (Iterator<ResourceDescription> iterator = descriptions
					.iterator(); iterator.hasNext();) {
				ResourceDescription resourceDescription = iterator.next();
				if (resourceDescription.getPath().equals(location)) {
					iterator.remove();
					break;
				}
			}
		}

		/**
		 * Returns all resource descriptions.
		 * 
		 * @return
		 */
		public Set<ResourceDescription> getResourceDescriptions() {
			Set<II18nRegistry.ResourceDescription> result = new HashSet<II18nRegistry.ResourceDescription>();
			for (Set<ResourceDescription> set : resources.values()) {
				for (ResourceDescription desc : set) {
					result.add(desc);
				}
			}
			return result;
		}
	}

	/**
	 * A resource is a file containing all translations for a file.
	 */
	public static class ResourceDescription {

		private final IProject project;
		private final Locale locale;
		private final IPath path;
		private final Properties properties;

		public ResourceDescription(IProject project, Locale locale, IPath path,
				Properties properties) {
			this.project = project;
			this.locale = locale;
			this.path = path;
			this.properties = properties;
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

		public Properties getProperties() {
			return properties;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((path == null) ? 0 : path.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ResourceDescription other = (ResourceDescription) obj;
			if (path == null) {
				if (other.path != null)
					return false;
			} else if (!path.equals(other.path))
				return false;
			return true;
		}

	}
}
