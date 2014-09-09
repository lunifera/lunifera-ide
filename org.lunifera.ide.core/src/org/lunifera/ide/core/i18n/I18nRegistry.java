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
package org.lunifera.ide.core.i18n;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.lunifera.ide.core.api.i18n.CoreUtil;
import org.lunifera.ide.core.api.i18n.II18nRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;

@Singleton
public class I18nRegistry implements II18nRegistry {

	private static final ProjectDescription EMPY_PROJECT_DESCRIPTION = new ProjectDescription(
			null);

	private static final Logger LOGGER = LoggerFactory
			.getLogger(I18nRegistry.class);

	private Map<IProject, ProjectDescription> cache = Collections
			.synchronizedMap(new HashMap<IProject, ProjectDescription>());

	public I18nRegistry() {

	}

	@Override
	public String getText(IProject project, Locale locale, String key) {
		String result = findTranslation(project, locale, key, locale);
		return result;
	}

	@Override
	public List<Proposal> findProposals(IProject project, Locale locale,
			String packageName, String searchValue) {
		AccessPath accessPath = computeAccessPath(project, locale, packageName,
				searchValue);
		return accessPath.getProposals();
	}

	@Override
	public Proposal findBestMatch(IProject project, Locale locale,
			String packageName, String searchValue) {
		AccessPath accessPath = computeBestMatchAccessPath(project, locale,
				packageName, searchValue);
		return accessPath.getBestMatch();
	}

	/**
	 * Computes the access path.
	 * <p>
	 * Following order will be used:
	 * <ol>
	 * <li>Find all entries in current project
	 * <ul>
	 * <li>Use given project and locale</li>
	 * <li>Create more specific locale and repeat until default locale is
	 * reached.</li>
	 * </ul>
	 * </li>
	 * <li>Iterate the referenced projects and start at 1) for each project with
	 * the requested locale.</li>
	 * </ol>
	 * 
	 * @param project
	 * @param locale
	 * @param packageName
	 * @param searchValue
	 * @return
	 */
	private AccessPath computeAccessPath(IProject project, Locale locale,
			String packageName, String searchValue) {

		String matchingPackage = null;
		String valuePatternString = null;
		// search in given package
		if (searchValue.startsWith(".")) {
			valuePatternString = searchValue.replaceFirst(".", "");
			matchingPackage = packageName;
		} else {
			valuePatternString = searchValue;
		}

		Matcher valueMatcher = null;
		if (valuePatternString != null && !valuePatternString.equals("")) {
			valuePatternString = Pattern
					.quote(valuePatternString.toLowerCase());
			// if (!valuePatternString.endsWith("*")) {
			// valuePatternString = valuePatternString.concat("*");
			// }
			Pattern valuePattern = Pattern.compile(valuePatternString
					.toString());
			valueMatcher = valuePattern.matcher("");
			valueMatcher.reset();
		}

		AccessPath path = new AccessPath();
		int prio = 0;

		// all locales for the given project
		List<IProject> computedProjects = computeProjects(project);
		List<Locale> computedLocales = computeLocales(locale);

		for (IProject computedProject : computedProjects) {
			for (Locale computedLocale : computedLocales) {
				Accessor accessor = new Accessor(computedProject,
						computedLocale, ++prio, searchValue, matchingPackage,
						valueMatcher);
				path.addAccessor(accessor);
			}
		}

		return path;
	}

	/**
	 * Computes the access path to find the best matching element. See
	 * {@link #computeAccessPath(IProject, Locale, String, String)}.
	 *
	 * @param project
	 * @param locale
	 * @param packageName
	 * @param searchValue
	 * @return
	 */
	private AccessPath computeBestMatchAccessPath(IProject project,
			Locale locale, String packageName, String searchValue) {
		return computeAccessPath(project, locale, packageName, searchValue);
	}

	/**
	 * Computes all projects that should be added to AccessPath
	 * 
	 * @param project
	 * @return
	 */
	private List<IProject> computeProjects(IProject project) {
		List<IProject> projects = new LinkedList<IProject>();
		// Add first root project
		projects.add(project);

		try {
			for (IProject referenced : project.getReferencedProjects()) {
				if (CoreUtil.hasNature(referenced)) {
					projects.add(referenced);
				}
			}
		} catch (CoreException e) {
			LOGGER.error("{}", e);
		}

		return projects;
	}

	/**
	 * Computes all locales that should be added to AccessPath
	 * 
	 * @param locale
	 * @return
	 */
	private List<Locale> computeLocales(Locale locale) {
		List<Locale> locales = new LinkedList<Locale>();

		// Add first locale
		locales.add(locale);

		Locale temp = locale;
		while (true) {
			String tag = temp.toLanguageTag();
			String[] segments = tag.split("-");
			if (segments.length > 1) {
				StringBuilder builder = new StringBuilder();
				for (int i = 0; i < segments.length - 1; i++) {
					if (builder.length() != 0) {
						builder.append("-");
					}
					builder.append(segments[i]);
				}
				Locale moreGeneral = Locale.forLanguageTag(builder.toString());
				locales.add(moreGeneral);
				temp = moreGeneral;
			} else {
				break;
			}
		}

		locales.add(new Locale(""));

		return locales;
	}

	/**
	 * Tries to find the translation key.
	 * <p>
	 * <ol>
	 * <li>Tries to find the translation in all resources in the given project.</li>
	 * <li>Creates a more general locale and continues 1</li>
	 * <li>If locale is most general, then all references projects are used to
	 * look for a translation. Continues 1</li>
	 * <ol>
	 * 
	 * @param project
	 * @param locale
	 * @param key
	 * @param originalLocale
	 * @return
	 */
	private String findTranslation(IProject project, Locale locale, String key,
			Locale originalLocale) {
		String result = null;
		// first try to find a translation with the given locale in the project
		//
		ProjectDescription projectDescription = getProjectDescription(project);
		List<ResourceDescription> descs = projectDescription
				.getResourceDescriptions(locale);

		for (ResourceDescription desc : descs) {
			result = desc.getProperties().getProperty(key);
			if (isValid(result)) {
				return result;
			}
		}

		// if no translation available, then try to use a more general one
		//
		String tag = locale.toLanguageTag();
		String[] segments = tag.split("-");
		if (segments.length > 1) {
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < segments.length - 1; i++) {
				if (builder.length() != 0) {
					builder.append("-");
				}
				builder.append(segments[i]);
			}

			Locale moreGeneral = Locale.forLanguageTag(builder.toString());
			result = findTranslation(project, moreGeneral, key, originalLocale);
			if (isValid(result)) {
				return result;
			}
		} else {
			try {
				for (IProject referenced : project.getReferencedProjects()) {
					if (referenced.getDescription().hasNature(
							CoreUtil.NATURE_ID)) {
						result = getText(referenced, originalLocale, key);
						if (isValid(result)) {
							return result;
						}
					}
				}
			} catch (CoreException e) {
				LOGGER.error("{}", e);
			}
		}
		return result;
	}

	private boolean isValid(String result) {
		return result != null && !result.equals("");
	}

	private ProjectDescription getProjectDescription(IProject project) {
		ProjectDescription description = cache.get(project);
		return description != null ? description : EMPY_PROJECT_DESCRIPTION;
	}

	@Override
	public void cache(ProjectDescription description) {
		cache.put(description.getProject(), description);
	}

	@Override
	public void cache(ResourceDescription description) {
		if (cache.containsKey(description.getProject())) {
			ProjectDescription projectDesc = cache
					.get(description.getProject());
			if (projectDesc != null) {
				projectDesc.putResource(description);
			}
		} else {
			ProjectDescription projectDesc = new ProjectDescription(
					description.getProject());
			projectDesc.putResource(description);
			cache(projectDesc);
		}
	}

	@Override
	public void removeResource(IProject project, Locale locale, IPath location) {
		ProjectDescription def = getProjectDescription(project);
		def.removeResource(locale, location);
	}

	@Override
	public Set<ProjectDescription> getProjectDescriptions() {
		Set<ProjectDescription> result = new HashSet<II18nRegistry.ProjectDescription>(
				cache.values());
		return Collections.unmodifiableSet(result);
	}

	@Override
	public void removeProject(IProject project) {
		cache.remove(project);
	}

	/**
	 * Defines how the registry should be searched. For instance the ordering of
	 * locales, the ordering of projects,...
	 * 
	 * @author admin
	 *
	 */
	private static class AccessPath {
		private List<Accessor> accessors = new LinkedList<I18nRegistry.Accessor>();

		public AccessPath() {
		}

		public void addAccessor(Accessor accessor) {
			accessors.add(accessor);
		}

		public List<Proposal> getProposals() {
			List<Proposal> proposals = new LinkedList<II18nRegistry.Proposal>();
			for (Accessor accessor : accessors) {
				proposals.addAll(accessor.getProposals());
			}

			return proposals;
		}

		/**
		 * Best match returns the first found translation. "Best" is ensured by
		 * the accessor order. If no proposal was found then <code>null</code>
		 * is returned.
		 * 
		 * @return
		 */
		public Proposal getBestMatch() {
			List<Proposal> proposals = new LinkedList<II18nRegistry.Proposal>();
			for (Accessor accessor : accessors) {
				List<Proposal> result = accessor.getProposals();
				if (!result.isEmpty()) {
					return result.get(0);
				}
			}

			return null;
		}
	}

	/**
	 * This class will access the registry.
	 */
	private class Accessor {

		private final IProject project;
		private final Locale locale;
		private final Matcher matcher;
		private final String searchValue;
		private final String keyPackage;
		private final int prio;

		public Accessor(IProject project, Locale locale, int prio,
				String searchValue, String keyPackage, Matcher matcher) {
			super();
			this.project = project;
			this.locale = locale;
			this.searchValue = searchValue;
			this.keyPackage = keyPackage;
			this.matcher = matcher;
			this.prio = prio;
		}

		/**
		 * Returns all proposals for the defined values. Must never return
		 * <code>null</code>.
		 * 
		 * @return
		 */
		public List<Proposal> getProposals() {
			ProjectDescription projectDesc = getProjectDescription(project);
			if (projectDesc == I18nRegistry.EMPY_PROJECT_DESCRIPTION) {
				return Collections.emptyList();
			}

			List<Proposal> proposals = new LinkedList<II18nRegistry.Proposal>();
			List<ResourceDescription> descs = projectDesc
					.getResourceDescriptions(locale);
			for (ResourceDescription desc : descs) {
				for (Map.Entry<Object, Object> entry : desc.getProperties()
						.entrySet()) {
					if (keyPackage != null
							&& !((String) entry.getKey())
									.startsWith(keyPackage)) {
						continue;
					}

					if (matcher == null
							|| matcher.reset(
									((String) entry.getValue()).toLowerCase())
									.find()
							|| matcher.reset(
									((String) entry.getKey()).toLowerCase())
									.find()) {
						proposals.add(new Proposal((String) entry.getKey(),
								(String) entry.getValue(), desc, prio));
					}
				}
			}
			return proposals;
		}
	}
}
