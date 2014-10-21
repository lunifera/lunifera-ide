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
package org.lunifera.ide.core.ui.i18n;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;
import org.lunifera.ide.core.api.i18n.CoreUtil;
import org.lunifera.ide.core.api.i18n.II18nRegistry;
import org.lunifera.ide.core.api.i18n.II18nRegistry.ProjectDescription;
import org.lunifera.ide.core.api.i18n.II18nRegistry.ResourceDescription;
import org.lunifera.ide.core.ui.CoreUiActivator;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import com.google.inject.Inject;

public class I18nRegistryView extends ViewPart {
	
	public static final String ID = "org.lunifera.ide.core.ui.i18nview";

	@Inject
	private II18nRegistry registry;

	private Image projectImage = CoreUiActivator.imageDescriptorFromPlugin(
			CoreUiActivator.PLUGIN_ID, "icons/Project.gif").createImage();
	private Image resourceImage = CoreUiActivator.imageDescriptorFromPlugin(
			CoreUiActivator.PLUGIN_ID, "icons/Resource.gif").createImage();
	private Image propertyImage = CoreUiActivator.imageDescriptorFromPlugin(
			CoreUiActivator.PLUGIN_ID, "icons/Property.gif").createImage();

	private TreeViewer treeViewer;

	private ServiceRegistration<EventHandler> registration;

	@Override
	public void createPartControl(Composite parent) {

		treeViewer = new TreeViewer(parent);
		treeViewer.setContentProvider(new ContentProvider());
		treeViewer.setLabelProvider(new LabelProvider() {
			@Override
			public Image getImage(Object element) {
				if (element instanceof II18nRegistry.ProjectDescription) {
					return projectImage;
				} else if (element instanceof II18nRegistry.ResourceDescription) {
					return resourceImage;
				} else if (element instanceof Entry) {
					return propertyImage;
				}
				return super.getImage(element);
			}

			@Override
			public String getText(Object element) {
				if (element instanceof II18nRegistry.ProjectDescription) {
					II18nRegistry.ProjectDescription desc = (ProjectDescription) element;
					return desc.getProject().getName();
				} else if (element instanceof II18nRegistry.ResourceDescription) {

					II18nRegistry.ResourceDescription desc = (II18nRegistry.ResourceDescription) element;

					String languageTag = desc.getLocale().toLanguageTag();
					if (desc.getLocale().toString().equals("")) {
						languageTag = "empty";
					}
					return String.format("%s: %s", languageTag, desc.getPath()
							.makeRelative().toString());
				} else if (element instanceof Entry) {
					@SuppressWarnings("unchecked")
					Entry<String, String> entry = (Entry<String, String>) element;
					return String.format("%s = %s", entry.getKey(),
							entry.getValue());
				}
				return "";
			}
		});

		treeViewer.setInput(registry);

		registerEventAdmin();

	}

	/**
	 * Registers for notification about the build.
	 */
	private void registerEventAdmin() {
		EventHandler handler = new EventHandler() {
			@Override
			public void handleEvent(Event event) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						treeViewer.refresh();
					}
				});
			}
		};
		Map<String, String> props = new HashMap<String, String>();
		props.put(EventConstants.EVENT_TOPIC, CoreUtil.EVENT_TOPIC__BUILDER);
		registration = CoreUiActivator.getDefault().registerEventHandler(
				handler, props);
	}

	@Override
	public void setFocus() {

	}

	@Override
	public void dispose() {
		projectImage.dispose();
		resourceImage.dispose();
		propertyImage.dispose();

		registration.unregister();

		super.dispose();
	}

	private class ContentProvider implements ITreeContentProvider {

		@Override
		public void dispose() {

		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		@Override
		public Object[] getElements(Object inputElement) {
			Set<ProjectDescription> temp = registry.getProjectDescriptions();
			return temp.toArray();
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof ProjectDescription) {
				Set<ResourceDescription> descs = ((ProjectDescription) parentElement)
						.getResourceDescriptions();
				return descs.toArray();
			} else if (parentElement instanceof ResourceDescription) {
				Properties props = ((ResourceDescription) parentElement)
						.getProperties();
				return props.entrySet().toArray();
			}
			return null;
		}

		@Override
		public Object getParent(Object element) {
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			if (element instanceof ProjectDescription) {
				Set<ResourceDescription> descs = ((ProjectDescription) element)
						.getResourceDescriptions();
				return !descs.isEmpty();
			} else if (element instanceof ResourceDescription) {
				Properties props = ((ResourceDescription) element)
						.getProperties();
				return !props.isEmpty();
			}
			return false;
		}
	}
}
