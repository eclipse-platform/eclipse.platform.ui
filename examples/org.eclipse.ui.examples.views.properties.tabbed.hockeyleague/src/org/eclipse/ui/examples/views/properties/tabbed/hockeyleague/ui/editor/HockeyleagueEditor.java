/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.editor;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CommandStack;
import org.eclipse.emf.common.command.CommandStackListener;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.ui.ViewerPane;
import org.eclipse.emf.common.ui.viewer.IViewerProvider;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.emf.edit.provider.AdapterFactoryItemDelegator;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;
import org.eclipse.emf.edit.ui.action.EditingDomainActionBarContributor;
import org.eclipse.emf.edit.ui.celleditor.AdapterFactoryTreeEditor;
import org.eclipse.emf.edit.ui.dnd.EditingDomainViewerDropAdapter;
import org.eclipse.emf.edit.ui.dnd.LocalTransfer;
import org.eclipse.emf.edit.ui.dnd.ViewerDragAdapter;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.provider.HockeyleagueItemProviderAdapterFactory;
import org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.HockeyleaguePropertySheetPage;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.views.contentoutline.ContentOutline;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheet;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * This is the editor for the Hockeyleague model editor.
 * 
 * @author Anthony Hunter
 */
public class HockeyleagueEditor
	extends MultiPageEditorPart
	implements IEditingDomainProvider, ISelectionProvider, IMenuListener,
	IViewerProvider, ITabbedPropertySheetPageContributor {

	/**
	 * This keeps track of the editing domain that is used to track all changes
	 * to the model. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected AdapterFactoryEditingDomain editingDomain;

	/**
	 * This is the one adapter factory used for providing views of the model.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected ComposedAdapterFactory adapterFactory;

	/**
	 * This is the content outline page. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 */
	protected IContentOutlinePage contentOutlinePage;

	/**
	 * This is a kludge... <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected IStatusLineManager contentOutlineStatusLineManager;

	/**
	 * This is the content outline page's viewer. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 */
	protected TreeViewer contentOutlineViewer;

	/**
	 * This is the property sheet page. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated protected TabbedPropertySheetPage propertySheetPage;
	 */

	/**
	 * This shows how a tree view works. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 */
	protected TreeViewer treeViewer;

	/**
	 * This keeps track of the active viewer pane, in the book. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected ViewerPane currentViewerPane;

	/**
	 * This keeps track of the active content viewer, which may be either one of
	 * the viewers in the pages or the content outline viewer. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected Viewer currentViewer;

	/**
	 * This listens to which ever viewer is active. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 */
	protected ISelectionChangedListener selectionChangedListener;

	/**
	 * This keeps track of all the
	 * {@link org.eclipse.jface.viewers.ISelectionChangedListener}s that are
	 * listening to this editor. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected Collection selectionChangedListeners = new ArrayList();

	/**
	 * This keeps track of the selection of the editor as a whole. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected ISelection editorSelection;

	/**
	 * This listens for when the outline becomes active <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected IPartListener partListener = new IPartListener() {

		public void partActivated(IWorkbenchPart p) {
			if (p instanceof ContentOutline) {
				if (((ContentOutline) p).getCurrentPage() == contentOutlinePage) {
					getActionBarContributor().setActiveEditor(
						HockeyleagueEditor.this);

					setCurrentViewer(contentOutlineViewer);
				}
			} else if (p instanceof PropertySheet) {
				if (((PropertySheet) p).getCurrentPage() == getPropertySheetPage()) {
					getActionBarContributor().setActiveEditor(
						HockeyleagueEditor.this);
					handleActivate();
				}
			} else if (p == HockeyleagueEditor.this) {
				handleActivate();
			}
		}

		public void partBroughtToTop(IWorkbenchPart p) {
		}

		public void partClosed(IWorkbenchPart p) {
		}

		public void partDeactivated(IWorkbenchPart p) {
		}

		public void partOpened(IWorkbenchPart p) {
		}
	};

	private HockeyleaguePropertySheetPage propertySheetPage;
	
	/**
	 * Resources that have been removed since last activation.
	 * 
	 * @generated
	 */
	Collection removedResources = new ArrayList();

	/**
	 * Resources that have been changed since last activation.
	 * 
	 * @generated
	 */
	Collection changedResources = new ArrayList();

	/**
	 * Resources that have been saved.
	 * 
	 * @generated
	 */
	Collection savedResources = new ArrayList();

	/**
	 * This listens for workspace changes. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 */
	protected IResourceChangeListener resourceChangeListener = new IResourceChangeListener() {

		public void resourceChanged(IResourceChangeEvent event) {
			//ResourceSet resourceSet = editingDomain.getResourceSet();
			// Only listening to these.
			// if (event.getType() == IResourceDelta.POST_CHANGE)
			{
				IResourceDelta delta = event.getDelta();
				try {
					class ResourceDeltaVisitor
						implements IResourceDeltaVisitor {

						protected ResourceSet resourceSet = editingDomain
							.getResourceSet();

						//protected Collection changedResources = new
						// ArrayList();
						//protected Collection removedResources = new
						// ArrayList();

						public boolean visit(IResourceDelta delta1) {
							if (delta1.getFlags() != IResourceDelta.MARKERS
								&& delta1.getResource().getType() == IResource.FILE) {
								if ((delta1.getKind() & (IResourceDelta.CHANGED | IResourceDelta.REMOVED)) != 0) {
									Resource resource = resourceSet
										.getResource(URI.createURI(delta1
											.getFullPath().toString()), false);
									if (resource != null) {
										if ((delta1.getKind() & IResourceDelta.REMOVED) != 0) {
											removedResources.add(resource);
										} else {
											changedResources.add(resource);
										}
									}
								}
							}

							return true;
						}

						public Collection getChangedResources() {
							return changedResources;
						}

						public Collection getRemovedResources() {
							return removedResources;
						}
					}

					ResourceDeltaVisitor visitor = new ResourceDeltaVisitor();
					delta.accept(visitor);

					removedResources.addAll(visitor.getRemovedResources());
					if (!visitor.getRemovedResources().isEmpty() && !isDirty()) {
						getSite().getShell().getDisplay().asyncExec(
							new Runnable() {

								public void run() {
									getSite().getPage().closeEditor(
										HockeyleagueEditor.this, false);
									HockeyleagueEditor.this.dispose();
								}
							});
					}

					changedResources.addAll(visitor.getChangedResources());
				} catch (CoreException exception) {
					exception.printStackTrace();
				}
			}
		}
	};

	/**
	 * Handles activation of the editor or it's associated views.
	 * 
	 * @generated
	 */
	protected void handleActivate() {
		if (!removedResources.isEmpty()) {
			if (handleDirtyConflict()) {
				getSite().getPage().closeEditor(HockeyleagueEditor.this, false);
				HockeyleagueEditor.this.dispose();
			} else {
				removedResources.clear();
				changedResources.clear();
				savedResources.clear();
			}
		} else if (!changedResources.isEmpty()) {
			changedResources.removeAll(savedResources);
			handleChangedResources();
			changedResources.clear();
			savedResources.clear();
		}
	}

	/**
	 * Handles what to do with changed resources on activation.
	 * 
	 * @generated
	 */
	protected void handleChangedResources() {
		if (!changedResources.isEmpty()
			&& (!isDirty() || handleDirtyConflict())) {
			editingDomain.getCommandStack().flush();

			for (Iterator i = changedResources.iterator(); i.hasNext();) {
				Resource resource = (Resource) i.next();
				if (resource.isLoaded()) {
					resource.unload();
					try {
						resource.load(Collections.EMPTY_MAP);
					} catch (IOException exception) {
						exception.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * Shows a dialog that asks if conflicting changes should be discarded.
	 * 
	 * @generated
	 */
	protected boolean handleDirtyConflict() {
		return MessageDialog
			.openQuestion(
				getSite().getShell(),
				"File Conflict",//$NON-NLS-1$
				"There are unsaved changes that conflict with changes made outside the editor.  Do you wish to discard this editor's changes?");//$NON-NLS-1$
	}

	/**
	 * This creates a model editor. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @generated
	 */
	public HockeyleagueEditor() {
		super();

		// Create an adapter factory that yields item providers.
		//
		List factories = new ArrayList();
		factories.add(new ResourceItemProviderAdapterFactory());
		factories.add(new HockeyleagueItemProviderAdapterFactory());
		factories.add(new ReflectiveItemProviderAdapterFactory());

		adapterFactory = new ComposedAdapterFactory(factories);

		// Create the command stack that will notify this editor as commands are
		// executed.
		//
		BasicCommandStack commandStack = new BasicCommandStack();

		// Add a listener to set the most recent command's affected objects to
		// be the selection of the viewer with focus.
		//
		commandStack.addCommandStackListener(new CommandStackListener() {

			public void commandStackChanged(final EventObject event) {
				getContainer().getDisplay().asyncExec(new Runnable() {

					public void run() {
						firePropertyChange(IEditorPart.PROP_DIRTY);

						// Try to select the affected objects.
						//
						Command mostRecentCommand = ((CommandStack) event
							.getSource()).getMostRecentCommand();
						if (mostRecentCommand != null) {
							setSelectionToViewer(mostRecentCommand
								.getAffectedObjects());
						}
						if (getPropertySheetPage() != null) {
							getPropertySheetPage().refresh();
						}
					}
				});
			}
		});

		// Create the editing domain with a special command stack.
		//
		editingDomain = new AdapterFactoryEditingDomain(adapterFactory,
			commandStack);
	}

	/**
	 * This is here for the listener to be able to call it. <!-- begin-user-doc
	 * --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected void firePropertyChange(int action) {
		super.firePropertyChange(action);
	}

	/**
	 * This sets the selection into whichever viewer is active. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void setSelectionToViewer(Collection collection) {
		final Collection theSelection = collection;
		// Make sure it's okay.
		//
		if (theSelection != null && !theSelection.isEmpty()) {
			// I don't know if this should be run this deferred
			// because we might have to give the editor a chance to process the
			// viewer update events
			// and hence to update the views first.
			//
			//
			Runnable runnable = new Runnable() {

				public void run() {
					// Try to select the items in the current content viewer of
					// the editor.
					//
					if (currentViewer != null) {
						currentViewer.setSelection(new StructuredSelection(
							theSelection.toArray()), true);
					}
				}
			};
			runnable.run();
		}
	}

	/**
	 * This returns the editing domain as required by the
	 * {@link IEditingDomainProvider}interface. This is important for
	 * implementing the static methods of {@link AdapterFactoryEditingDomain}
	 * and for supporting {@link org.eclipse.emf.edit.ui.action.CommandAction}.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public EditingDomain getEditingDomain() {
		return editingDomain;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public class ReverseAdapterFactoryContentProvider
		extends AdapterFactoryContentProvider {

		public ReverseAdapterFactoryContentProvider(
				AdapterFactory adapterFactory) {
			super(adapterFactory);
		}

		public Object[] getElements(Object object) {
			Object parent = super.getParent(object);
			return (parent == null ? Collections.EMPTY_SET
				: Collections.singleton(parent)).toArray();
		}

		public Object[] getChildren(Object object) {
			Object parent = super.getParent(object);
			return (parent == null ? Collections.EMPTY_SET
				: Collections.singleton(parent)).toArray();
		}

		public boolean hasChildren(Object object) {
			Object parent = super.getParent(object);
			return parent != null;
		}

		public Object getParent(Object object) {
			return null;
		}
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void setCurrentViewerPane(ViewerPane viewerPane) {
		if (currentViewerPane != viewerPane) {
			if (currentViewerPane != null) {
				currentViewerPane.showFocus(false);
			}
			currentViewerPane = viewerPane;
		}
		setCurrentViewer(currentViewerPane.getViewer());
	}

	/**
	 * This makes sure that one content viewer, either for the current page or
	 * the outline view, if it has focus, is the current one. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void setCurrentViewer(Viewer viewer) {
		// If it is changing...
		//
		if (currentViewer != viewer) {
			if (selectionChangedListener == null) {
				// Create the listener on demand.
				//
				selectionChangedListener = new ISelectionChangedListener() {

					// This just notifies those things that are affected by the
					// section.
					//
					public void selectionChanged(
							SelectionChangedEvent selectionChangedEvent) {
						setSelection(selectionChangedEvent.getSelection());
					}
				};
			}

			// Stop listening to the old one.
			//
			if (currentViewer != null) {
				currentViewer
					.removeSelectionChangedListener(selectionChangedListener);
			}

			// Start listening to the new one.
			//
			if (viewer != null) {
				viewer.addSelectionChangedListener(selectionChangedListener);
			}

			// Remember it.
			//
			currentViewer = viewer;

			// Set the editors selection based on the current viewer's
			// selection.
			//
			setSelection(currentViewer == null ? StructuredSelection.EMPTY
				: currentViewer.getSelection());
		}
	}

	/**
	 * This returns the viewer as required by the {@link IViewerProvider}
	 * interface. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public Viewer getViewer() {
		return currentViewer;
	}

	/**
	 * This creates a context menu for the viewer and adds a listener as well
	 * registering the menu for extension. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 */
	protected void createContextMenuFor(StructuredViewer viewer) {
		MenuManager contextMenu = new MenuManager("#PopUp");//$NON-NLS-1$
		contextMenu.add(new Separator("additions"));//$NON-NLS-1$
		contextMenu.setRemoveAllWhenShown(true);
		contextMenu.addMenuListener(this);
		Menu menu = contextMenu.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(contextMenu, viewer);

		int dndOperations = DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK;
		Transfer[] transfers = new Transfer[] {LocalTransfer.getInstance()};
		viewer.addDragSupport(dndOperations, transfers, new ViewerDragAdapter(
			viewer));
		viewer.addDropSupport(dndOperations, transfers,
			new EditingDomainViewerDropAdapter(editingDomain, viewer));
	}

	/**
	 * This is the method used by the framework to install your own controls.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void createPages() {
		// I assume that the input is a file object.
		//
		IFileEditorInput modelFile = (IFileEditorInput) getEditorInput();

		try {
			// Load the resource through the editing domain.
			//
			editingDomain.loadResource(URI.createPlatformResourceURI(
				modelFile.getFile().getFullPath().toString()).toString());
		} catch (Exception exception) {
			exception.printStackTrace();
		}

		// Create a page for the selection tree view.
		//
		{
			ViewerPane viewerPane = new ViewerPane(getSite().getPage(),
				HockeyleagueEditor.this) {

				public Viewer createViewer(Composite composite) {
					Tree tree = new Tree(composite, SWT.MULTI);
					TreeViewer newTreeViewer = new TreeViewer(tree);
					return newTreeViewer;
				}

				public void requestActivation() {
					super.requestActivation();
					setCurrentViewerPane(this);
				}
			};
			viewerPane.createControl(getContainer());

			treeViewer = (TreeViewer) viewerPane.getViewer();
			treeViewer.setContentProvider(new AdapterFactoryContentProvider(
				adapterFactory));

			treeViewer.setLabelProvider(new AdapterFactoryLabelProvider(
				adapterFactory));
			treeViewer.setInput(editingDomain.getResourceSet());
			viewerPane.setTitle(editingDomain.getResourceSet());

			new AdapterFactoryTreeEditor(treeViewer.getTree(), adapterFactory);

			createContextMenuFor(treeViewer);
			int pageIndex = addPage(viewerPane.getControl());
			setPageText(pageIndex, "tree");//$NON-NLS-1$
		}

		setActivePage(0);

		getContainer().addControlListener(new ControlAdapter() {

			boolean guard = false;

			public void controlResized(ControlEvent event) {
				if (!guard) {
					guard = true;
					hideTabs();
					guard = false;
				}
			}
		});
	}

	/**
	 * If there is just one page in the multi-page editor part, this hides the
	 * single tab at the bottom. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected void hideTabs() {
		if (getPageCount() <= 1) {
			setPageText(0, "");//$NON-NLS-1$
			if (getContainer() instanceof CTabFolder) {
				((CTabFolder) getContainer()).setTabHeight(1);
				Point point = getContainer().getSize();
				getContainer().setSize(point.x, point.y + 6);
			}
		}
	}

	/**
	 * This is used to track the active viewer. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 */
	protected void pageChange(int pageIndex) {
		super.pageChange(pageIndex);

		// This is a temporary workaround... EATM
		//
		Control control = getControl(pageIndex);
		if (control != null) {
			control.setVisible(true);
			control.setFocus();
		}

		if (contentOutlinePage != null) {
			handleContentOutlineSelection(contentOutlinePage.getSelection());
		}
	}

	/**
	 * This is how the framework determines which interfaces we implement. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public Object getAdapter(Class key) {
		if (key.equals(IContentOutlinePage.class)) {
			return getContentOutlinePage();
		} else if (key.equals(IPropertySheetPage.class)) {
			return getPropertySheetPage();
		} else {
			return super.getAdapter(key);
		}
	}

	/**
	 * This accesses a cached version of the content outliner. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public IContentOutlinePage getContentOutlinePage() {
		if (contentOutlinePage == null) {
			// The content outline is just a tree.
			//
			class MyContentOutlinePage
				extends ContentOutlinePage {

				public void createControl(Composite parent) {
					super.createControl(parent);
					contentOutlineViewer = getTreeViewer();
					contentOutlineViewer.addSelectionChangedListener(this);

					// Set up the tree viewer.
					//
					contentOutlineViewer
						.setContentProvider(new AdapterFactoryContentProvider(
							adapterFactory));
					contentOutlineViewer
						.setLabelProvider(new AdapterFactoryLabelProvider(
							adapterFactory));
					contentOutlineViewer.setInput(editingDomain
						.getResourceSet());

					// Make sure our popups work.
					//
					createContextMenuFor(contentOutlineViewer);

					if (!editingDomain.getResourceSet().getResources()
						.isEmpty()) {
						// Select the root object in the view.
						//
						ArrayList selection = new ArrayList();
						selection.add(editingDomain.getResourceSet()
							.getResources().get(0));
						contentOutlineViewer.setSelection(
							new StructuredSelection(selection), true);
					}
				}

				public void makeContributions(IMenuManager menuManager,
						IToolBarManager toolBarManager,
						IStatusLineManager statusLineManager) {
					super.makeContributions(menuManager, toolBarManager,
						statusLineManager);
					contentOutlineStatusLineManager = statusLineManager;
				}

				public void setActionBars(IActionBars actionBars) {
					super.setActionBars(actionBars);
					getActionBarContributor().shareGlobalActions(this,
						actionBars);
				}
			}

			contentOutlinePage = new MyContentOutlinePage();

			// Listen to selection so that we can handle it is a special way.
			//
			contentOutlinePage
				.addSelectionChangedListener(new ISelectionChangedListener() {

					// This ensures that we handle selections correctly.
					//
					public void selectionChanged(SelectionChangedEvent event) {
						handleContentOutlineSelection(event.getSelection());
					}
				});
		}

		return contentOutlinePage;
	}

	/**
	 * Get the new property sheet page for this editor. 
	 * @return the new property sheet page.
	 */
	public TabbedPropertySheetPage getPropertySheetPage() {
		if (propertySheetPage == null || propertySheetPage.getControl() == null) {
			propertySheetPage = new HockeyleaguePropertySheetPage(this);
		}

		return propertySheetPage;
	}

	/**
	 * This deals with how we want selection in the outliner to affect the other
	 * views. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void handleContentOutlineSelection(ISelection selection) {
		if (currentViewerPane != null && !selection.isEmpty()
			&& selection instanceof IStructuredSelection) {
			Iterator selectedElements = ((IStructuredSelection) selection)
				.iterator();
			if (selectedElements.hasNext()) {
				// Get the first selected element.
				//
				Object selectedElement = selectedElements.next();

				ArrayList selectionList = new ArrayList();
				selectionList.add(selectedElement);
				while (selectedElements.hasNext()) {
					selectionList.add(selectedElements.next());
				}

				// Set the selection to the widget.
				//
				treeViewer.setSelection(new StructuredSelection(selectionList));
			}
		}
	}

	/**
	 * This is for implementing {@link IEditorPart}and simply tests the command
	 * stack. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public boolean isDirty() {
		return ((BasicCommandStack) editingDomain.getCommandStack())
			.isSaveNeeded();
	}

	/**
	 * This is for implementing {@link IEditorPart}and simply saves the model
	 * file. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void doSave(IProgressMonitor progressMonitor) {
		// Do the work within an operation because this is a long running
		// activity that modifies the workbench.
		//
		WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {

			// This is the method that gets invoked when the operation runs.
			//
			protected void execute(IProgressMonitor monitor)
				throws CoreException {
				try {
					// Save the resource to the file system.
					//
					Resource savedResource = (Resource) editingDomain
						.getResourceSet().getResources().get(0);
					savedResources.add(savedResource);
					savedResource.save(Collections.EMPTY_MAP);
				} catch (Exception exception) {
					exception.printStackTrace();
				}
			}
		};

		try {
			// This runs the options, and shows progress.
			// (It appears to be a bad thing to fork this onto another thread.)
			//
			new ProgressMonitorDialog(getSite().getShell()).run(false, false,
				operation);

			// Refresh the necessary state.
			//
			((BasicCommandStack) editingDomain.getCommandStack()).saveIsDone();
			firePropertyChange(IEditorPart.PROP_DIRTY);
		} catch (Exception exception) {
			// Something went wrong that shouldn't.
			//
			exception.printStackTrace();
		}
	}

	/**
	 * This always returns true because it is not currently supported. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public boolean isSaveAsAllowed() {
		return true;
	}

	/**
	 * This also changes the editor's input. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 */
	public void doSaveAs() {
		SaveAsDialog saveAsDialog = new SaveAsDialog(getSite().getShell());
		saveAsDialog.open();
		IPath path = saveAsDialog.getResult();
		if (path != null) {
			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
			if (file != null) {
				((Resource) editingDomain.getResourceSet().getResources()
					.get(0)).setURI(URI.createPlatformResourceURI(file
					.getFullPath().toString()));
				IFileEditorInput modelFile = new FileEditorInput(file);
				setInput(modelFile);
				doSave(getActionBars().getStatusLineManager()
					.getProgressMonitor());
			}
		}
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void gotoMarker(IMarker marker) {
		//
	}

	/**
	 * This is called during startup. <!-- begin-user-doc --> <!-- end-user-doc
	 * -->
	 * 
	 * @generated
	 */
	public void init(IEditorSite site, IEditorInput editorInput)
		throws PartInitException {
		if (editorInput instanceof IFileEditorInput) {
			setSite(site);
			setInput(editorInput);
			site.setSelectionProvider(this);
			site.getPage().addPartListener(partListener);
			ResourcesPlugin.getWorkspace().addResourceChangeListener(
				resourceChangeListener, IResourceChangeEvent.POST_CHANGE);
		} else {
			throw new PartInitException(
				"Invalid Input: Must be IFileEditorInput.");//$NON-NLS-1$
		}
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void setFocus() {
		getControl(getActivePage()).setFocus();
	}

	/**
	 * This implements {@link org.eclipse.jface.viewers.ISelectionProvider}.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		selectionChangedListeners.add(listener);
	}

	/**
	 * This implements {@link org.eclipse.jface.viewers.ISelectionProvider}.
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) {
		selectionChangedListeners.remove(listener);
	}

	/**
	 * This implements {@link org.eclipse.jface.viewers.ISelectionProvider}to
	 * return this editor's overall selection. <!-- begin-user-doc --> <!--
	 * end-user-doc -->
	 * 
	 * @generated
	 */
	public ISelection getSelection() {
		return editorSelection;
	}

	/**
	 * This implements {@link org.eclipse.jface.viewers.ISelectionProvider}to
	 * set this editor's overall selection. Calling this result will notify the
	 * listeners. <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void setSelection(ISelection selection) {
		editorSelection = selection;
		for (Iterator listeners = selectionChangedListeners.iterator(); listeners
			.hasNext();) {
			ISelectionChangedListener listener = (ISelectionChangedListener) listeners
				.next();
			listener
				.selectionChanged(new SelectionChangedEvent(this, selection));
		}
		setStatusLineManager(selection);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void setStatusLineManager(ISelection selection) {
		IStatusLineManager statusLineManager = getActionBars()
			.getStatusLineManager();
		if (currentViewer == contentOutlineViewer) {
			statusLineManager = contentOutlineStatusLineManager;
		}

		if (selection instanceof IStructuredSelection) {
			Collection collection = ((IStructuredSelection) selection).toList();
			switch (collection.size()) {
				case 0: {
					statusLineManager.setMessage("No Object Selected");//$NON-NLS-1$
					break;
				}
				case 1: {
					String text = new AdapterFactoryItemDelegator(
						adapterFactory).getText(collection.iterator().next());
					statusLineManager.setMessage(MessageFormat.format(
						"Selected Object: {0}", new Object[] {text}));//$NON-NLS-1$
					break;
				}
				default: {
					statusLineManager.setMessage(MessageFormat.format(
						"Selected {0} Objects", new Object[] {Integer//$NON-NLS-1$
							.toString(collection.size())}));
					break;
				}
			}
		} else {
			statusLineManager.setMessage("");//$NON-NLS-1$
		}
	}

	/**
	 * This implements {@link org.eclipse.jface.action.IMenuListener}to help
	 * fill the context menus with contributions from the Edit menu. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void menuAboutToShow(IMenuManager menuManager) {
		((IMenuListener) getEditorSite().getActionBarContributor())
			.menuAboutToShow(menuManager);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public EditingDomainActionBarContributor getActionBarContributor() {
		return (EditingDomainActionBarContributor) getEditorSite()
			.getActionBarContributor();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public IActionBars getActionBars() {
		return getActionBarContributor().getActionBars();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public AdapterFactory getAdapterFactory() {
		return adapterFactory;
	}

	/**
	 * 
	 */
	public String getContributorId() {
		return "org.eclipse.ui.examples.views.properties.tabbed.hockeyleague";//$NON-NLS-1$
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(
			resourceChangeListener);

		getSite().getPage().removePartListener(partListener);

		adapterFactory.dispose();

		getActionBarContributor().setActiveEditor(null);

		if (getPropertySheetPage() != null) {
			getPropertySheetPage().dispose();
		}

		if (contentOutlinePage != null) {
			contentOutlinePage.dispose();
		}

		super.dispose();
	}
}