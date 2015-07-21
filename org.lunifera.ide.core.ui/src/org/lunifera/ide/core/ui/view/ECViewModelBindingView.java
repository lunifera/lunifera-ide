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

package org.lunifera.ide.core.ui.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.xtext.builder.builderState.IBuilderState;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.ui.editor.model.IXtextModelListener;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;
import org.lunifera.ecview.core.common.model.binding.YBeanValueBindingEndpoint;
import org.lunifera.ecview.core.common.model.binding.YBinding;
import org.lunifera.ecview.core.common.model.binding.YBindingSet;
import org.lunifera.ecview.core.common.model.binding.YDetailValueBindingEndpoint;
import org.lunifera.ecview.core.common.model.binding.YECViewModelListBindingEndpoint;
import org.lunifera.ecview.core.common.model.binding.YECViewModelValueBindingEndpoint;
import org.lunifera.ecview.core.common.model.binding.YEnumListBindingEndpoint;
import org.lunifera.ecview.core.common.model.binding.YVisibilityProcessorValueBindingEndpoint;
import org.lunifera.ecview.core.common.model.core.YActivatedEndpoint;
import org.lunifera.ecview.core.common.model.core.YBeanSlotListBindingEndpoint;
import org.lunifera.ecview.core.common.model.core.YBeanSlotValueBindingEndpoint;
import org.lunifera.ecview.core.common.model.core.YContextValueBindingEndpoint;
import org.lunifera.ecview.core.common.model.core.YElement;
import org.lunifera.ecview.core.common.model.core.YEmbeddableCollectionEndpoint;
import org.lunifera.ecview.core.common.model.core.YEmbeddableMultiSelectionEndpoint;
import org.lunifera.ecview.core.common.model.core.YEmbeddableSelectionEndpoint;
import org.lunifera.ecview.core.common.model.core.YEmbeddableValueEndpoint;
import org.lunifera.ecview.core.common.model.core.YView;

import com.google.inject.Inject;

@SuppressWarnings("restriction")
public class ECViewModelBindingView extends ViewPart implements IPartListener,
		IXtextModelListener {

	public static final String ID = "org.lunifera.ide.core.ui.view.xtextindex";

	@Inject
	private IBuilderState builderState;

	// private Image indexImage = CoreUiActivator.imageDescriptorFromPlugin(
	// CoreUiActivator.PLUGIN_ID, "icons/Index-Resource.gif")
	// .createImage();
	// private Image exportedContainerImage = CoreUiActivator
	// .imageDescriptorFromPlugin(CoreUiActivator.PLUGIN_ID,
	// "icons/Index-ExportedContainer.gif").createImage();
	// private Image importedNameContainerImage = CoreUiActivator
	// .imageDescriptorFromPlugin(CoreUiActivator.PLUGIN_ID,
	// "icons/Index-ImportedNameContainer.gif").createImage();
	// private Image referenceContainerImage = CoreUiActivator
	// .imageDescriptorFromPlugin(CoreUiActivator.PLUGIN_ID,
	// "icons/Index-ReferenceContainer.gif").createImage();
	// private Image referenceImage = CoreUiActivator.imageDescriptorFromPlugin(
	// CoreUiActivator.PLUGIN_ID, "icons/Index-ReferenceDescription.gif")
	// .createImage();

	private TreeViewer treeViewer;

	private IXtextDocument lastActiveDocument;

	private YBindingSet yBindingSet;

	public ECViewModelBindingView() {

	}

	@Override
	public void createPartControl(Composite parent) {

		treeViewer = new TreeViewer(parent);
		treeViewer.setContentProvider(new ContentProvider());
		treeViewer.setLabelProvider(new LabelProvider() {
			@Override
			public Image getImage(Object element) {
				// if (element instanceof IResourceDescription) {
				// return indexImage;
				// } else if (element instanceof ExportedObjects) {
				// return exportedContainerImage;
				// } else if (element instanceof ImportedNames) {
				// return importedNameContainerImage;
				// } else if (element instanceof ReferenceDescriptions) {
				// return referenceContainerImage;
				// } else if (element instanceof IReferenceDescription) {
				// return referenceImage;
				// }
				return super.getImage(element);
			}

			@Override
			public String getText(Object element) {
				try {
					if (element instanceof String) {
						return (String) element;
					} else if (element instanceof YBinding) {
						return element.getClass().getSimpleName();
					} else if (element instanceof YActivatedEndpoint) {
						YActivatedEndpoint ep = (YActivatedEndpoint) element;
						return "ActivatedEndpoint: "
								+ ep.getElement().getClass().getSimpleName();
					} else if (element instanceof YBeanSlotValueBindingEndpoint) {
						YBeanSlotValueBindingEndpoint ep = (YBeanSlotValueBindingEndpoint) element;
						return "YBeanSlotValueBindingEndpoint: "
								+ ep.getBeanSlot().getName() + ":"
								+ ep.getAttributePath();
					} else if (element instanceof YBeanValueBindingEndpoint) {
						YBeanValueBindingEndpoint ep = (YBeanValueBindingEndpoint) element;
						return "YBeanValueBindingEndpoint: "
								+ ep.getPropertyPath();
					} else if (element instanceof YContextValueBindingEndpoint) {
						YContextValueBindingEndpoint ep = (YContextValueBindingEndpoint) element;
						return "YContextValueBindingEndpoint: "
								+ ep.getUrlString();
					} else if (element instanceof YContextValueBindingEndpoint) {
						YDetailValueBindingEndpoint ep = (YDetailValueBindingEndpoint) element;
						return "YDetailValueBindingEndpoint: ("
								+ getText(ep.getMasterObservable()) + ")."
								+ ep.getPropertyPath();
					} else if (element instanceof YECViewModelValueBindingEndpoint) {
						YECViewModelValueBindingEndpoint ep = (YECViewModelValueBindingEndpoint) element;
						return "YECViewModelValueBindingEndpoint: "
								+ ep.getElement().getClass().getSimpleName()
								+ ":" + getId(ep.getElement()) + "."
								+ ep.getPropertyPath();
					} else if (element instanceof YEmbeddableSelectionEndpoint) {
						YEmbeddableSelectionEndpoint ep = (YEmbeddableSelectionEndpoint) element;
						return "YEmbeddableSelectionEndpoint: "
								+ ep.getElement().getClass().getSimpleName()
								+ ":" + getId(ep.getElement()) + "."
								+ ep.getAttributePath();
					} else if (element instanceof YEmbeddableValueEndpoint) {
						YEmbeddableValueEndpoint ep = (YEmbeddableValueEndpoint) element;
						return "YEmbeddableValueEndpoint: "
								+ ep.getElement().getClass().getSimpleName()
								+ ":" + getId(ep.getElement()) + ".value";
					} else if (element instanceof YVisibilityProcessorValueBindingEndpoint) {
						YVisibilityProcessorValueBindingEndpoint ep = (YVisibilityProcessorValueBindingEndpoint) element;
						return "YVisibilityProcessorValueBindingEndpoint: "
								+ ep.getProcessorInstance().getClass()
										.getSimpleName() + ":"
								+ ep.getProperty();
					} else if (element instanceof YBeanSlotListBindingEndpoint) {
						YBeanSlotListBindingEndpoint ep = (YBeanSlotListBindingEndpoint) element;
						return "YBeanSlotListBindingEndpoint: "
								+ ep.getBeanSlot().getName() + ":"
								+ ep.getAttributePath();
					} else if (element instanceof YECViewModelListBindingEndpoint) {
						YECViewModelListBindingEndpoint ep = (YECViewModelListBindingEndpoint) element;
						return "YECViewModelListBindingEndpoint: "
								+ ep.getElement().getClass().getSimpleName()
								+ ":" + getId(ep.getElement()) + "."
								+ ep.getPropertyPath();
					} else if (element instanceof YEmbeddableCollectionEndpoint) {
						YEmbeddableCollectionEndpoint ep = (YEmbeddableCollectionEndpoint) element;
						return "YEmbeddableCollectionEndpoint: "
								+ ep.getElement().getClass().getSimpleName()
								+ ":" + getId(ep.getElement());
					} else if (element instanceof YEmbeddableMultiSelectionEndpoint) {
						YEmbeddableMultiSelectionEndpoint ep = (YEmbeddableMultiSelectionEndpoint) element;
						return "YEmbeddableMultiSelectionEndpoint: "
								+ ep.getElement().getClass().getSimpleName()
								+ ":" + getId(ep.getElement());
					} else if (element instanceof YEnumListBindingEndpoint) {
						YEnumListBindingEndpoint ep = (YEnumListBindingEndpoint) element;
						return "YEnumListBindingEndpoint: "
								+ ep.getEnum().getClass().getSimpleName();
					}
				} catch (Exception e) {
					return "exception in this line.";
				}
				return "";
			}

			private String getId(EObject element) {
				if (element instanceof YElement) {
					return ((YElement) element).getId();
				}
				return "noId";
			}
		});

		start(getViewSite());
	}

	@Override
	public void setFocus() {

	}

	@Override
	public void dispose() {

		stop(getViewSite());

		// exportedContainerImage.dispose();
		// importedNameContainerImage.dispose();
		// referenceContainerImage.dispose();
		// referenceImage.dispose();
		// indexImage.dispose();

		super.dispose();
	}

	public void start(IWorkbenchPartSite site) {
		updateView(site.getPage().getActiveEditor());
		site.getWorkbenchWindow().getPartService().addPartListener(this);
	}

	public void stop(IWorkbenchPartSite site) {
		site.getWorkbenchWindow().getPartService().removePartListener(this);
		lastActiveDocument = null;
	}

	public void partActivated(IWorkbenchPart part) {
		updateView(part);
	}

	private void updateView(IWorkbenchPart part) {
		if (part instanceof XtextEditor) {
			XtextEditor xtextEditor = (XtextEditor) part;
			IXtextDocument xtextDocument = xtextEditor.getDocument();
			if (xtextDocument != lastActiveDocument) {
				if (lastActiveDocument != null) {
					lastActiveDocument.removeModelListener(this);
				}

				lastActiveDocument = xtextDocument;
				lastActiveDocument.addModelListener(this);
				lastActiveDocument
						.readOnly(new IUnitOfWork<Boolean, XtextResource>() {
							@Override
							public Boolean exec(XtextResource state)
									throws Exception {
								modelChanged(state);
								return true;
							}
						});
			}
		}
	}

	public void partBroughtToTop(IWorkbenchPart part) {
	}

	public void partClosed(IWorkbenchPart part) {
	}

	public void partDeactivated(IWorkbenchPart part) {
	}

	public void partOpened(IWorkbenchPart part) {
	}

	public void modelChanged(final XtextResource resource) {
		if (resource == null) {
			return;
		}
		getSite().getWorkbenchWindow().getShell().getDisplay()
				.asyncExec(new Runnable() {
					@Override
					public void run() {
						if (treeViewer != null && resource != null) {
							if (resource.getURI().fileExtension()
									.equals("uimodel")) {
								YView yView = (YView) resource.getContents()
										.get(1);
								treeViewer.setInput(yView.getBindingSet());
							}
						}
					}
				});

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
			YBindingSet yBindingSet = (YBindingSet) inputElement;
			return yBindingSet != null ? yBindingSet.getBindings().toArray()
					: new Object[0];
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof YBinding) {
				YBinding yBinding = (YBinding) parentElement;
				List<Object> objects = new ArrayList<Object>();
				objects.add(yBinding.getModelEndpoint());
				objects.add(yBinding.getTargetEndpoint());
				return objects.toArray();
			}
			return null;
		}

		@Override
		public Object getParent(Object element) {
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			if (element instanceof YBinding) {
				return true;
			}
			return false;
		}
	}
}