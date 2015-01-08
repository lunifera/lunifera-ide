package org.lunifera.ide.core.ui.view;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
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
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.resource.IReferenceDescription;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.ui.editor.model.IXtextModelListener;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;
import org.eclipse.xtext.xbase.lib.Functions;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.lunifera.ide.core.ui.CoreUiActivator;

import com.google.inject.Inject;

@SuppressWarnings("restriction")
public class XtextIndexView extends ViewPart implements IPartListener,
		IXtextModelListener {

	public static final String ID = "org.lunifera.ide.core.ui.view.xtextindex";

	@Inject
	private IBuilderState builderState;

//	private Image indexImage = CoreUiActivator.imageDescriptorFromPlugin(
//			CoreUiActivator.PLUGIN_ID, "icons/Index-Resource.gif")
//			.createImage();
//	private Image exportedContainerImage = CoreUiActivator
//			.imageDescriptorFromPlugin(CoreUiActivator.PLUGIN_ID,
//					"icons/Index-ExportedContainer.gif").createImage();
//	private Image importedNameContainerImage = CoreUiActivator
//			.imageDescriptorFromPlugin(CoreUiActivator.PLUGIN_ID,
//					"icons/Index-ImportedNameContainer.gif").createImage();
//	private Image referenceContainerImage = CoreUiActivator
//			.imageDescriptorFromPlugin(CoreUiActivator.PLUGIN_ID,
//					"icons/Index-ReferenceContainer.gif").createImage();
//	private Image referenceImage = CoreUiActivator.imageDescriptorFromPlugin(
//			CoreUiActivator.PLUGIN_ID, "icons/Index-ReferenceDescription.gif")
//			.createImage();

	private TreeViewer treeViewer;

	private IXtextDocument lastActiveDocument;

	public XtextIndexView() {

	}

	@Override
	public void createPartControl(Composite parent) {

		treeViewer = new TreeViewer(parent);
		treeViewer.setContentProvider(new ContentProvider());
		treeViewer.setLabelProvider(new LabelProvider() {
			@Override
			public Image getImage(Object element) {
//				if (element instanceof IResourceDescription) {
//					return indexImage;
//				} else if (element instanceof ExportedObjects) {
//					return exportedContainerImage;
//				} else if (element instanceof ImportedNames) {
//					return importedNameContainerImage;
//				} else if (element instanceof ReferenceDescriptions) {
//					return referenceContainerImage;
//				} else if (element instanceof IReferenceDescription) {
//					return referenceImage;
//				}
				return super.getImage(element);
			}

			@Override
			public String getText(Object element) {
				if (element instanceof String) {
					return (String) element;
				} else if (element instanceof IResourceDescription) {
					return ((IResourceDescription) element).getURI().toString();
				} else if (element instanceof ExportedObjects) {
					return "exported Objects";
				} else if (element instanceof ImportedNames) {
					return "imported Names";
				} else if (element instanceof ReferenceDescriptions) {
					return "reference Descriptions";
				} else if (element instanceof IReferenceDescription) {
					IReferenceDescription desc = (IReferenceDescription) element;
					return desc.getEReference().getName() + "--"
							+ (desc.getContainerEObjectURI() != null ? desc.getContainerEObjectURI().toString() : "null");
				} else if (element instanceof Entry) {
					Entry entry = (Entry) element;
					return entry.key + ":  " + entry.value;
				} else if (element instanceof IEObjectDescription) {
					IEObjectDescription desc = (IEObjectDescription) element;
					StringBuilder b = new StringBuilder();
					b.append(" - ");
					b.append(desc.getEClass().getName());
					b.append("(");
					b.append(desc.getName());
					b.append(")  -->  ");
					b.append(desc.getEObjectURI());
					return b.toString();
				} else if (element instanceof URI) {
					return element.toString();
				}
				return "";
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

//		exportedContainerImage.dispose();
//		importedNameContainerImage.dispose();
//		referenceContainerImage.dispose();
//		referenceImage.dispose();
//		indexImage.dispose();

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
							treeViewer.setInput(resource);
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
			Resource resource = (Resource) inputElement;
			IResourceDescription desc = builderState
					.getResourceDescription(resource.getURI());
			return desc != null ? new Object[] { desc } : new Object[0];
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof IResourceDescription) {
				IResourceDescription desc = (IResourceDescription) parentElement;
				List<Object> objects = new ArrayList<Object>();
				objects.add(new ExportedObjects(desc.getExportedObjects()));
				objects.add(new ImportedNames(desc.getImportedNames()));
				objects.add(new ReferenceDescriptions(desc
						.getReferenceDescriptions()));
				return objects.toArray();
			} else if (parentElement instanceof ExportedObjects) {
				ExportedObjects exportedObjects = (ExportedObjects) parentElement;
				return exportedObjects.exported.toArray();
			} else if (parentElement instanceof ImportedNames) {
				ImportedNames names = (ImportedNames) parentElement;
				return names.importedNames.toArray();
			} else if (parentElement instanceof ReferenceDescriptions) {
				ReferenceDescriptions resDescs = (ReferenceDescriptions) parentElement;
				return resDescs.refDescs.toArray();
			} else if (parentElement instanceof IReferenceDescription) {
				IReferenceDescription desc = (IReferenceDescription) parentElement;
				List<Object> elements = new ArrayList<Object>();
				elements.add(new Entry(" - eReference", desc.getEReference()
						.getName()));
				elements.add(new Entry(" - index", Integer.toString(desc
						.getIndexInList())));
				elements.add(new Entry(" - container", desc
						.getContainerEObjectURI() != null ? desc
						.getContainerEObjectURI().toString() : "null"));
				elements.add(new Entry(" - source", desc.getSourceEObjectUri()
						.toString()));
				elements.add(new Entry(" - target", desc.getTargetEObjectUri()
						.toString()));
				return elements.toArray();
			} else if (parentElement instanceof IEObjectDescription) {
				IEObjectDescription desc = (IEObjectDescription) parentElement;
				List<String> userData = new ArrayList<String>();
				for (String key : desc.getUserDataKeys()) {
					String data = key + " : " + desc.getUserData(key);
					userData.add(data);
				}
				return userData.toArray();
			}
			return null;
		}

		@Override
		public Object getParent(Object element) {
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			if (element instanceof IResourceDescription) {
				return true;
			} else if (element instanceof ExportedObjects) {
				ExportedObjects exportedObjects = (ExportedObjects) element;
				return !exportedObjects.exported.isEmpty();
			} else if (element instanceof ImportedNames) {
				ImportedNames names = (ImportedNames) element;
				return !names.importedNames.isEmpty();
			} else if (element instanceof ReferenceDescriptions) {
				ReferenceDescriptions resDescs = (ReferenceDescriptions) element;
				return !resDescs.refDescs.isEmpty();
			} else if (element instanceof IReferenceDescription) {
				return true;
			} else if (element instanceof IEObjectDescription) {
				return ((IEObjectDescription) element).getUserDataKeys().length > 0;
			}
			return false;
		}
	}

	private static class ExportedObjects {

		private List<IEObjectDescription> exported;

		public ExportedObjects(Iterable<IEObjectDescription> exportedObjects) {
			exported = IterableExtensions.toList(exportedObjects);
		}

	}

	private static class ImportedNames {
		private List<String> importedNames;

		public ImportedNames(Iterable<QualifiedName> importedNames) {
			Iterable<String> mapped = IterableExtensions
					.<QualifiedName, String> map(importedNames,
							new Functions.Function1<QualifiedName, String>() {
								@Override
								public String apply(QualifiedName input) {
									return " - " + input.toString();
								}
							});
			this.importedNames = IterableExtensions.toList(mapped);
		}
	}

	private static class ReferenceDescriptions {

		private List<IReferenceDescription> refDescs;

		public ReferenceDescriptions(
				Iterable<IReferenceDescription> referenceDescriptions) {
			refDescs = IterableExtensions.toList(referenceDescriptions);
		}
	}

	private static class Entry {
		private String key;
		private String value;

		public Entry(String key, String value) {
			this.key = key;
			this.value = value;
		}
	}
}