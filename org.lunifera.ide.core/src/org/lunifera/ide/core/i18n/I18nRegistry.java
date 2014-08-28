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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.inject.Singleton;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.lunifera.ide.core.api.i18n.CoreUtil;
import org.lunifera.ide.core.api.i18n.II18nRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class I18nRegistry implements II18nRegistry {

	private static final ProjectDescription EMPY_PROJECT_DESCRIPTION = new ProjectDescription(
			null);

	private static final Logger LOGGER = LoggerFactory
			.getLogger(I18nRegistry.class);

	private Map<IProject, ProjectDescription> cache = Collections
			.synchronizedMap(new HashMap<IProject, ProjectDescription>());

	@Override
	public String getText(IProject project, Locale locale, String key) {

		String result = findTranslation(project, locale, key, locale);

		return result;
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

}
