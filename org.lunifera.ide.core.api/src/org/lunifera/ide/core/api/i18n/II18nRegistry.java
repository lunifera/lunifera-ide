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
	 * Returns a list of proposals. If the searchValue matches parts of the
	 * value or the key for an i18n record, it is added to the list of
	 * proposals. Must never return <code>null</code>.
	 * 
	 * @param project
	 *            - The project which should be searched for matching I18n
	 *            values.
	 * @param locale
	 *            - The locale which should be used to search for matching I18n
	 *            values.
	 * @param packageName
	 *            - The package name. It can be used to search for I18n values,
	 *            if the searchValue starts with ".". For instance
	 *            ".SalesOrder". Then the matcher only looks for I18n Values
	 *            with a key starting with the given package name.<br>
	 *            Wildcards are allowed. For instance ".*Order". The matcher
	 *            tries to find any possible combination. A "*" wildcard at the
	 *            end of the searchValue will be added automatically by the
	 *            matcher.
	 * @param filterValue
	 *            - The filterValue.
	 *            <ul>
	 *            <li>"*Order" - Find all entries in all packages where "Order"
	 *            is contained</li>
	 *            <li>".*Order" - Find all entries starting with the given
	 *            package name where "Order" is contained. If package is
	 *            "org.my.example", then "org.my.example.SalesOrder" will be
	 *            matched.</li>
	 *            </ul>
	 * @return proposal - a List never <code>null</code>
	 */
	List<Proposal> findContentProposals(IProject project, Locale locale,
			String packageName, String searchValue);

	/**
	 * Returns a list of proposals. The searchValue must match the entire key
	 * for an i18n record.
	 * 
	 * @param project
	 *            - The project which should be searched for matching I18n
	 *            values.
	 * @param locale
	 *            - The locale which should be used to search for matching I18n
	 *            values.
	 * @param packageName
	 *            - The package name. It can be used to search for I18n values,
	 *            if the searchValue starts with ".". For instance
	 *            ".SalesOrder". Then the matcher only looks for I18n Values
	 *            with a key starting with the given package name.<br>
	 *            Wildcards are allowed. For instance ".*Order". The matcher
	 *            tries to find any possible combination. A "*" wildcard at the
	 *            end of the searchValue will be added automatically by the
	 *            matcher.
	 * @param key
	 *            - The key to look for.
	 * @return proposal - a List never <code>null</code>
	 */
	List<Proposal> findStrictKeyMatchingProposals(IProject project,
			Locale locale, String packageName, String key);

	/**
	 * Returns the best matching proposal.
	 * 
	 * @param project
	 *            - The project which should be searched for matching I18n
	 *            values.
	 * @param locale
	 *            - The locale which should be used to search for matching I18n
	 *            values.
	 * @param packageName
	 *            - The package name. It can be used to search for I18n values,
	 *            if the searchValue starts with ".". For instance
	 *            ".SalesOrder". Then the matcher only looks for I18n Values
	 *            with a key starting with the given package name.<br>
	 *            Wildcards are allowed. For instance ".*Order". The matcher
	 *            tries to find any possible combination. A "*" wildcard at the
	 *            end of the searchValue will be added automatically by the
	 *            matcher.
	 * @param key
	 *            - The key to look for.
	 * @return proposal - a List never <code>null</code>
	 */
	Proposal findBestMatch(IProject project, Locale locale, String packageName,
			String key);

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

			Set<ResourceDescription> descriptions = resources.get(value
					.getLocale());
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

	public static class Proposal {

		private final String i18nKey;
		private final String i18nValue;
		private final ResourceDescription resourceDescription;
		private final int priority;

		public Proposal(String i18nKey, String i18nValue,
				ResourceDescription resourceDescription, int priority) {
			super();
			this.i18nKey = i18nKey;
			this.i18nValue = i18nValue;
			this.resourceDescription = resourceDescription;
			this.priority = priority;
		}

		/**
		 * Returns the i18nKey.
		 * 
		 * @return
		 */
		public String getI18nKey() {
			return i18nKey;
		}

		/**
		 * Returns the i18nValue.
		 * 
		 * @return
		 */
		public String getI18nValue() {
			return i18nValue;
		}

		/**
		 * Returns the locale where key and value had been found.
		 * 
		 * @return
		 */
		public Locale getLocale() {
			return resourceDescription.getLocale();
		}

		/**
		 * Returns the resource description, where the i18n entry was contained.
		 * 
		 * @return
		 */
		public ResourceDescription getResourceDescription() {
			return resourceDescription;
		}

	}
}
