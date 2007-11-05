/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.provisional.views.markers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.help.IContext;
import org.eclipse.help.IContextProvider;
import org.eclipse.jface.action.ContributionManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.SameShellProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TreeAdapter;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.internal.ide.IDEInternalPreferences;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.StatusUtil;
import org.eclipse.ui.internal.provisional.views.markers.api.MarkerField;
import org.eclipse.ui.internal.provisional.views.markers.api.MarkerItem;
import org.eclipse.ui.internal.provisional.views.markers.api.MarkerSupportConstants;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.eclipse.ui.progress.WorkbenchJob;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.views.markers.internal.MarkerGroup;
import org.eclipse.ui.views.markers.internal.MarkerMessages;
import org.eclipse.ui.views.markers.internal.MarkerSupportRegistry;
import org.eclipse.ui.views.tasklist.ITaskListResourceAdapter;

/**
 * The ExtendedMarkersView is the view that shows markers using the
 * markerGenerators extension point.
 * 
 * @since 3.4
 * 
 */
public class ExtendedMarkersView extends ViewPart {

	static {
		Platform.getAdapterManager().registerAdapters(new IAdapterFactory() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object,
			 *      java.lang.Class)
			 */
			public Object getAdapter(Object adaptableObject, Class adapterType) {
				if(adapterType == IMarker.class && adaptableObject instanceof MarkerEntry)
						return ((MarkerEntry) adaptableObject).getMarker();
				
				return null;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
			 */
			public Class[] getAdapterList() {
				return new Class[] {IMarker.class};
			}
		}, MarkerEntry.class);
	}

	/**
	 * MarkerSelectionEntry is a cache of the values for a marker entry.
	 * 
	 * @since 3.4
	 * 
	 */
	final class MarkerSelectionEntry {

		Object[] cachedValues;

		MarkerSelectionEntry(MarkerItem item) {
			MarkerField[] fields = builder.getGenerator().getVisibleFields();
			cachedValues = new Object[fields.length];
			for (int i = 0; i < fields.length; i++) {
				cachedValues[i] = fields[i].getValue(item);
			}
		}

		/**
		 * Return whether or not the entry is equivalent to the cached state.
		 * 
		 * @param item
		 * @return boolean <code>true</code> if they are equivalent
		 */
		boolean isEquivalentTo(MarkerItem item) {
			MarkerField[] fields = builder.getGenerator().getVisibleFields();

			if (cachedValues.length != fields.length)
				return false;

			for (int i = 0; i < fields.length; i++) {
				if (cachedValues[i] == fields[i].getValue(item))
					continue;
				return false;
			}
			return true;
		}

	}

	private static int instanceCount = 0;
	private static final String TAG_GENERATOR = "markerContentGenerator"; //$NON-NLS-1$
	private static final String TAG_HORIZONTAL_POSITION = "horizontalPosition"; //$NON-NLS-1$
	private static final String TAG_VERTICAL_POSITION = "verticalPosition"; //$NON-NLS-1$
	private static final String MARKER_FIELD = "MARKER_FIELD"; //$NON-NLS-1$

	/**
	 * Return the next secondary id.
	 * 
	 * @return String
	 */
	static String newSecondaryID() {
		return String.valueOf(instanceCount);
	}

	/**
	 * Open the supplied marker in an editor in page
	 * 
	 * @param marker
	 * @param page
	 */
	public static void openMarkerInEditor(IMarker marker, IWorkbenchPage page) {
		// optimization: if the active editor has the same input as
		// the
		// selected marker then
		// RevealMarkerAction would have been run and we only need
		// to
		// activate the editor
		IEditorPart editor = page.getActiveEditor();
		if (editor != null) {
			IEditorInput input = editor.getEditorInput();
			IFile file = ResourceUtil.getFile(input);
			if (file != null) {
				if (marker.getResource().equals(file)) {
					page.activate(editor);
				}
			}
		}

		if (marker != null && marker.getResource() instanceof IFile) {
			try {
				IDE.openEditor(page, marker, OpenStrategy.activateOnOpen());
			} catch (PartInitException e) {

				// Check for a nested CoreException
				IStatus status = e.getStatus();
				if (status != null
						&& status.getException() instanceof CoreException) {
					status = ((CoreException) status.getException())
							.getStatus();
				}

				if (status == null)
					StatusManager.getManager().handle(
							StatusUtil.newStatus(IStatus.ERROR, e.getMessage(),
									e), StatusManager.SHOW);

				else
					StatusManager.getManager().handle(status,
							StatusManager.SHOW);

			}
		}
	}

	private CachedMarkerBuilder builder;
	Collection categoriesToExpand = new HashSet();

	private Clipboard clipboard;

	Collection preservedSelection = new ArrayList();

	private Job updateJob;

	private TreeViewer viewer;
	private IPropertyChangeListener preferenceListener;
	private IMemento memento;

	/**
	 * Return a new instance of the receiver.
	 */
	public ExtendedMarkersView() {
		super();
		instanceCount++;
		preferenceListener = new IPropertyChangeListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
			 */
			public void propertyChange(PropertyChangeEvent event) {
				String propertyName = event.getProperty();
				if (propertyName
						.equals(IDEInternalPreferences.USE_MARKER_LIMITS)
						|| propertyName
								.equals(IDEInternalPreferences.MARKER_LIMITS_VALUE)) {
					viewer.refresh();
				}
			}
		};
		IDEWorkbenchPlugin.getDefault().getPreferenceStore()
				.addPropertyChangeListener(preferenceListener);
	}

	/**
	 * Add the category to the list of expanded categories.
	 * 
	 * @param category
	 */
	void addExpandedCategory(MarkerCategory category) {
		categoriesToExpand.add(category.getName());

	}

	/**
	 * Add all of the markers in markerItem recursively.
	 * 
	 * @param markerItem
	 * @param allMarkers
	 *            {@link Collection} of {@link IMarker}
	 */
	private void addMarkers(MarkerItem markerItem, Collection allMarkers) {
		if (markerItem.getMarker() != null)
			allMarkers.add(markerItem.getMarker());
		MarkerItem[] children = markerItem.getChildren();
		for (int i = 0; i < children.length; i++) {
			addMarkers(children[i], allMarkers);

		}

	}

	/**
	 * Create the columns for the receiver.
	 * 
	 * @param currentColumns
	 *            the columns to refresh
	 */
	private void createColumns(TreeColumn[] currentColumns) {

		Tree tree = viewer.getTree();
		TableLayout layout = new TableLayout();

		MarkerField[] fields = builder.getGenerator().getVisibleFields();

		for (int i = 0; i < fields.length; i++) {
			MarkerField markerField = fields[i];

			// Take into account the expansion indicator
			int columnWidth = markerField.getDefaultColumnWidth(tree);

			if (i == 0) {
				// Compute and store a font metric
				GC gc = new GC(tree);
				gc.setFont(tree.getFont());
				FontMetrics fontMetrics = gc.getFontMetrics();
				gc.dispose();
				columnWidth = Math.max(columnWidth, fontMetrics
						.getAverageCharWidth() * 5);
			}

			layout.addColumnData(new ColumnPixelData(columnWidth, true));
			TreeViewerColumn column;
			if (i < currentColumns.length)
				column = new TreeViewerColumn(viewer, currentColumns[i]);
			else {
				column = new TreeViewerColumn(viewer, SWT.NONE);
				column.getColumn().setResizable(true);
				column.getColumn().setMoveable(true);
				column.getColumn().addSelectionListener(getHeaderListener());
			}

			column.getColumn().setData(MARKER_FIELD, markerField);
			// Show the help in the first column
			column.setLabelProvider(new MarkerColumnLabelProvider(markerField,
					i == 0));
			column.getColumn().setText(markerField.getColumnHeaderText());
			column.getColumn().setToolTipText(
					markerField.getColumnTooltipText());
			column.getColumn().setImage(markerField.getColumnHeaderImage());
			if (builder.generator.isPrimarySortField(markerField))
				updateDirectionIndicator(column.getColumn(), markerField);

		}

		// Remove extra columns
		if (currentColumns.length > fields.length) {
			for (int i = fields.length; i < currentColumns.length; i++) {
				currentColumns[i].dispose();

			}
		}

		viewer.getTree().setLayout(layout);
		tree.setLinesVisible(true);
		tree.setHeaderVisible(true);
		tree.layout(true);

	}

	/**
	 * Return the listener that updates sort values on selection.
	 * 
	 * @return SelectionListener
	 */
	private SelectionListener getHeaderListener() {

		return new SelectionAdapter() {
			/**
			 * Handles the case of user selecting the header area.
			 */
			public void widgetSelected(SelectionEvent e) {

				final TreeColumn column = (TreeColumn) e.widget;
				final MarkerField field = (MarkerField) column
						.getData(MARKER_FIELD);
				builder.getGenerator().setPrimarySortField(field);

				IWorkbenchSiteProgressService service = (IWorkbenchSiteProgressService) getViewSite()
						.getAdapter(IWorkbenchSiteProgressService.class);
				builder.refreshContents(service);
				updateDirectionIndicator(column, field);
				viewer.refresh();
			}

		};

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());

		viewer = new MarkersTreeViewer(new Tree(parent, SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION));
		viewer.getTree().setLinesVisible(true);
		viewer.setUseHashlookup(true);

		createColumns(new TreeColumn[0]);

		viewer.setContentProvider(getContentProvider());
		viewer.getTree().setItemCount(builder.getElements().length);

		viewer.setInput(builder);
		if (memento != null) {
			Scrollable scrollable = (Scrollable) viewer.getControl();
			ScrollBar bar = scrollable.getVerticalBar();
			if (bar != null) {
				Integer position = memento.getInteger(TAG_VERTICAL_POSITION);
				if (position != null)
					bar.setSelection(position.intValue());
			}
			bar = scrollable.getHorizontalBar();
			if (bar != null) {
				Integer position = memento.getInteger(TAG_HORIZONTAL_POSITION);
				if (position != null)
					bar.setSelection(position.intValue());
			}
		}
		getSite().setSelectionProvider(viewer);

		// Initialise any selection based filtering
		ISelectionListener listener = getPageSelectionListener();
		getSite().getPage().addSelectionListener(listener);
		listener.selectionChanged(getSite().getPage().getActivePart(),
				getSite().getPage().getSelection());

		viewer.addOpenListener(new IOpenListener() {
			public void open(OpenEvent event) {
				openSelectedMarkers();
			}

		});

		viewer.getTree().addTreeListener(new TreeAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.TreeAdapter#treeCollapsed(org.eclipse.swt.events.TreeEvent)
			 */
			public void treeCollapsed(TreeEvent e) {
				removeExpandedCategory((MarkerCategory) e.item.getData());
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.TreeAdapter#treeExpanded(org.eclipse.swt.events.TreeEvent)
			 */
			public void treeExpanded(TreeEvent e) {
				addExpandedCategory((MarkerCategory) e.item.getData());
			}
		});

		// Set help on the view itself
		viewer.getControl().addHelpListener(new HelpListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.HelpListener#helpRequested(org.eclipse.swt.events.HelpEvent)
			 */
			public void helpRequested(HelpEvent e) {
				Object provider = getAdapter(IContextProvider.class);
				if (provider == null)
					return;

				IContext context = ((IContextProvider) provider)
						.getContext(viewer.getControl());
				PlatformUI.getWorkbench().getHelpSystem().displayHelp(context);
			}

		});

		registerContextMenu();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	public void dispose() {
		super.dispose();
		updateJob.cancel();
		instanceCount--;
		if (clipboard != null)
			clipboard.dispose();
		IDEWorkbenchPlugin.getDefault().getPreferenceStore()
				.removePropertyChangeListener(preferenceListener);
	}

	/**
	 * Get all of the filters for the receiver.
	 * 
	 * @return Collection of {@link MarkerFieldFilterGroup}
	 */
	Collection getAllFilters() {
		return builder.getGenerator().getAllFilters();
	}

	/**
	 * Return all of the markers in the receiver.
	 * 
	 * @return IMarker[]
	 */
	IMarker[] getAllMarkers() {

		MarkerItem[] elements = builder.getElements();
		Collection allMarkers = new ArrayList();
		for (int i = 0; i < elements.length; i++) {
			addMarkers(elements[i], allMarkers);

		}
		IMarker[] markers = new IMarker[allMarkers.size()];
		allMarkers.toArray(markers);
		return markers;

	}

	/**
	 * Return all of the marker items in the receiver that are concrete.
	 * 
	 * @return MarkerItem[]
	 */
	MarkerItem[] getAllConcreteItems() {

		MarkerItem[] elements = builder.getElements();
		Collection allMarkers = new ArrayList();
		for (int i = 0; i < elements.length; i++) {
			addAllConcreteItems(elements[i], allMarkers);

		}
		MarkerItem[] markers = new MarkerItem[allMarkers.size()];
		allMarkers.toArray(markers);
		return markers;
	}

	/**
	 * Add all concrete {@link MarkerItem} elements associated with the receiver
	 * to allMarkers.
	 * 
	 * @param markerItem
	 * @param allMarkers
	 */
	private void addAllConcreteItems(MarkerItem markerItem,
			Collection allMarkers) {
		if (markerItem.isConcrete()) {
			allMarkers.add(markerItem);
			return;
		}

		MarkerItem[] children = markerItem.getChildren();
		for (int i = 0; i < children.length; i++) {
			addAllConcreteItems(children[i], allMarkers);
		}

	}

	/**
	 * Return the group used for categorisation.
	 * 
	 * @return MarkerGroup
	 */
	MarkerGroup getCategoryGroup() {
		return builder.getCategoryGroup();
	}

	/**
	 * Return the clipboard for the receiver.
	 * 
	 * @return Clipboard
	 */
	Clipboard getClipboard() {
		if (clipboard == null)
			clipboard = new Clipboard(viewer.getControl().getDisplay());
		return clipboard;
	}

	/**
	 * Return the content provider for the receiver.
	 * 
	 * @return ITreeContentProvider
	 * 
	 */
	private ITreeContentProvider getContentProvider() {
		return new ITreeContentProvider() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
			 */
			public void dispose() {

			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.ILazyTreeContentProvider#getParent(java.lang.Object)
			 */
			public Object getParent(Object element) {
				Object parent = ((MarkerItem) element).getParent();
				if (parent == null)
					return builder;
				return parent;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
			 *      java.lang.Object, java.lang.Object)
			 */
			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {

			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.ILazyTreeContentProvider#updateChildCount(java.lang.Object,
			 *      int)
			 */
			// public void updateChildCount(Object element, int
			// currentChildCount) {
			//
			// int length;
			// if (element instanceof MarkerItem)
			// length = ((MarkerItem) element).getChildren().length;
			// else
			// // If it is not a MarkerItem it is the root
			// length = ((CachedMarkerBuilder) element).getElements().length;
			//
			// int markerLimit = MarkerSupportInternalUtilities
			// .getMarkerLimit();
			// length = markerLimit > 0 ? Math.min(length, markerLimit)
			// : length;
			// if (currentChildCount == length)
			// return;
			// viewer.setChildCount(element, length);
			//
			// }
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.ILazyTreeContentProvider#updateElement(java.lang.Object,
			 *      int)
			 */
			// public void updateElement(Object parent, int index) {
			// MarkerItem newItem;
			//
			// if (parent instanceof MarkerItem)
			// newItem = ((MarkerItem) parent).getChildren()[index];
			// else
			// newItem = ((CachedMarkerBuilder) parent).getElements()[index];
			//
			// viewer.replace(parent, index, newItem);
			// updateChildCount(newItem, -1);
			//
			// if (!newItem.isConcrete()
			// && categoriesToExpand
			// .contains(((MarkerCategory) newItem).getName())) {
			// viewer.expandToLevel(newItem, 1);
			// categoriesToExpand.remove(newItem);
			// }
			//
			// }
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
			 */
			public Object[] getChildren(Object parentElement) {
				MarkerItem[] children = ((MarkerItem) parentElement)
						.getChildren();

				int newLength = MarkerSupportInternalUtilities.getMarkerLimit();
				if (newLength > 0 && newLength < children.length) {
					MarkerItem[] newChildren = new MarkerItem[newLength];
					System.arraycopy(children, 0, newChildren, 0, newLength);
					return newChildren;
				}
				return children;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
			 */
			public boolean hasChildren(Object element) {
				return ((MarkerItem) element).getChildren().length > 0;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
			 */
			public Object[] getElements(Object inputElement) {
				return ((CachedMarkerBuilder) inputElement).getElements();
			}
		};
	}

	/**
	 * Return the selection listener for the page selection change.
	 * 
	 * @return ISelectionListener
	 */
	private ISelectionListener getPageSelectionListener() {
		return new ISelectionListener() {
			/**
			 * Get an ITaskListResourceAdapter for use by the default/
			 * 
			 * @return ITaskListResourceAdapter
			 */
			private ITaskListResourceAdapter getDefaultTaskListAdapter() {
				return new ITaskListResourceAdapter() {

					/*
					 * (non-Javadoc)
					 * 
					 * @see org.eclipse.ui.views.tasklist.ITaskListResourceAdapter#getAffectedResource(org.eclipse.core.runtime.IAdaptable)
					 */
					public IResource getAffectedResource(IAdaptable adaptable) {
						Object resource = adaptable.getAdapter(IResource.class);
						if (resource == null)
							resource = adaptable.getAdapter(IFile.class);
						if (resource == null)
							return null;
						return (IResource) resource;

					}

				};
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart,
			 *      org.eclipse.jface.viewers.ISelection)
			 */
			public void selectionChanged(IWorkbenchPart part,
					ISelection selection) {

				List selectedElements = new ArrayList();
				if (part instanceof IEditorPart) {
					IEditorPart editor = (IEditorPart) part;
					IFile file = ResourceUtil.getFile(editor.getEditorInput());
					if (file == null) {
						IEditorInput editorInput = editor.getEditorInput();
						if (editorInput != null) {
							Object mapping = editorInput
									.getAdapter(ResourceMapping.class);
							if (mapping != null) {
								selectedElements.add(mapping);
							}
						}
					} else {
						selectedElements.add(file);
					}
				} else {
					if (selection instanceof IStructuredSelection) {
						for (Iterator iterator = ((IStructuredSelection) selection)
								.iterator(); iterator.hasNext();) {
							Object object = iterator.next();
							if (object instanceof IAdaptable) {
								ITaskListResourceAdapter taskListResourceAdapter;
								Object adapter = ((IAdaptable) object)
										.getAdapter(ITaskListResourceAdapter.class);
								if (adapter != null
										&& adapter instanceof ITaskListResourceAdapter) {
									taskListResourceAdapter = (ITaskListResourceAdapter) adapter;
								} else {
									taskListResourceAdapter = getDefaultTaskListAdapter();
								}

								IResource resource = taskListResourceAdapter
										.getAffectedResource((IAdaptable) object);
								if (resource == null) {
									Object mapping = ((IAdaptable) object)
											.getAdapter(ResourceMapping.class);
									if (mapping != null) {
										selectedElements.add(mapping);
									}
								} else {
									selectedElements.add(resource);
								}
							}
						}
					}
				}
				builder.updateForNewSelection(selectedElements.toArray());
			}

		};
	}

	/**
	 * Return all of the markers in the current selection
	 * 
	 * @return Array of {@link IMarker}
	 */
	IMarker[] getSelectedMarkers() {
		ISelection selection = getSite().getSelectionProvider().getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structured = (IStructuredSelection) selection;
			Iterator elements = structured.iterator();
			Collection result = new ArrayList();
			while (elements.hasNext()) {
				MarkerItem next = (MarkerItem) elements.next();
				if (next.isConcrete())
					result.add(((MarkerEntry) next).getMarker());
			}
			if (result.isEmpty())
				return MarkerSupportInternalUtilities.EMPTY_MARKER_ARRAY;
			IMarker[] markers = new IMarker[result.size()];
			result.toArray(markers);
			return markers;
		}
		return MarkerSupportInternalUtilities.EMPTY_MARKER_ARRAY;

	}

	/**
	 * Return a job for updating the receiver.
	 * 
	 * @return Job
	 */
	private Job getUpdateJob(final CachedMarkerBuilder builder) {
		updateJob = new WorkbenchJob(MarkerMessages.MarkerView_queueing_updates) {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.jobs.Job#belongsTo(java.lang.Object)
			 */
			public boolean belongsTo(Object family) {
				return family == MarkerContentGenerator.CACHE_UPDATE_FAMILY;
			}

			/**
			 * Return the viewer that is being updated.
			 * 
			 * @return TreeViewer
			 */
			private TreeViewer getViewer() {

				return viewer;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
			 */
			public IStatus runInUIThread(IProgressMonitor monitor) {

				if (viewer.getControl().isDisposed()) {
					return Status.CANCEL_STATUS;
				}

				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;

				// If there is only one category and the user has no saved state
				// show it
				if (builder.getGenerator().isShowingHierarchy()
						&& categoriesToExpand.isEmpty()) {
					MarkerCategory[] categories = builder.getCategories();
					if (categories.length == 1)
						categoriesToExpand.add(categories[0].getName());
				}

				getViewer().refresh(true);
				updateTitle();

				if (preservedSelection.size() > 0) {

					Collection newSelection = new ArrayList();
					MarkerItem[] markerEntries = builder.getMarkerEntries();

					for (int i = 0; i < markerEntries.length; i++) {
						Iterator preserved = preservedSelection.iterator();
						while (preserved.hasNext()) {
							MarkerSelectionEntry next = (MarkerSelectionEntry) preserved
									.next();
							if (next.isEquivalentTo(markerEntries[i])) {
								newSelection.add(markerEntries[i]);
								continue;
							}
						}
					}

					getViewer().setSelection(
							new StructuredSelection(newSelection.toArray()),
							true);
					preservedSelection.clear();
				}
				if (getViewer().getTree().getItemCount() > 0)
					getViewer().getTree().setTopItem(
							getViewer().getTree().getItem(0));

				if (!categoriesToExpand.isEmpty()
						&& builder.getGenerator().isShowingHierarchy()) {
					MarkerItem[] items = builder.getElements();
					for (int i = 0; i < items.length; i++) {
						String name = ((MarkerCategory) items[i]).getName();
						if (categoriesToExpand.contains(name))
							getViewer().expandToLevel(items[i], 2);

					}
				}
				return Status.OK_STATUS;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.ui.progress.WorkbenchJob#shouldRun()
			 */
			public boolean shouldRun() {
				return !builder.isBuilding();
			}

		};

		updateJob.setSystem(true);
		return updateJob;
	}

	/**
	 * Return the object that is the input to the viewer.
	 * 
	 * @return Object
	 */
	Object getViewerInput() {
		return viewer.getInput();
	}

	/**
	 * Get all of the fields visible in the receiver.
	 * 
	 * @return MarkerField[]
	 */
	MarkerField[] getVisibleFields() {
		return builder.getGenerator().getVisibleFields();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite,
	 *      org.eclipse.ui.IMemento)
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		MarkerContentGenerator generator = null;

		if (memento != null) {
			generator = MarkerSupportRegistry.getInstance().getGenerator(
					memento.getString(TAG_GENERATOR));

		}

		if (generator == null) {// Check for legacy ids
			String id = site.getId();
			if (id.equals(IPageLayout.ID_BOOKMARKS))
				generator = MarkerSupportRegistry.getInstance().getGenerator(
						MarkerSupportRegistry.BOOKMARKS_GENERATOR);
			else if (id.equals(IPageLayout.ID_TASK_LIST))
				generator = MarkerSupportRegistry.getInstance().getGenerator(
						MarkerSupportRegistry.TASKS_GENERATOR);
			else if (id.equals(IPageLayout.ID_PROBLEM_VIEW))
				generator = MarkerSupportRegistry.getInstance().getGenerator(
						MarkerSupportRegistry.PROBLEMS_GENERATOR);
		}
		if (generator == null)
			generator = MarkerSupportRegistry.getInstance().generatorFor(
					site.getPage().getPerspective());
		else
			generator.setMemento(memento);

		// Add in the entries common to all markers views
		IMenuService menuService = (IMenuService) site
				.getService(IMenuService.class);
		menuService.populateContributionManager((ContributionManager) site
				.getActionBars().getMenuManager(), "menu:" //$NON-NLS-1$
				+ MarkerSupportRegistry.ALL_MARKERS_ID);
		menuService.populateContributionManager((ContributionManager) site
				.getActionBars().getToolBarManager(),
				"toolbar:" + MarkerSupportRegistry.ALL_MARKERS_ID); //$NON-NLS-1$

		builder = new CachedMarkerBuilder(generator);
		builder.setUpdateJob(getUpdateJob(builder));
		Object service = site.getAdapter(IWorkbenchSiteProgressService.class);
		if (service != null)
			builder.setProgressService((IWorkbenchSiteProgressService) service);
		setPartName(generator.getName());
		this.memento = memento;
	}

	/**
	 * Return whether or not group is enabled.
	 * 
	 * @param group
	 * @return boolean
	 */
	boolean isEnabled(MarkerFieldFilterGroup group) {
		return builder.getGenerator().getEnabledFilters().contains(group);
	}

	/**
	 * Return whether or not generator is the selected one.
	 * 
	 * @param generator
	 * @return boolean
	 */
	boolean isShowing(MarkerContentGenerator generator) {
		return this.builder.getGenerator().equals(generator);
	}

	/**
	 * Open the filters dialog for the receiver.
	 */
	void openFiltersDialog() {
		FiltersConfigurationDialog dialog = new FiltersConfigurationDialog(
				new SameShellProvider(getSite().getWorkbenchWindow().getShell()),
				builder.getGenerator());
		if (dialog.open() == Window.OK) {
			builder.updateFrom(dialog);
		}

	}

	/**
	 * Open the selected markers
	 */
	void openSelectedMarkers() {
		IMarker[] markers = getSelectedMarkers();
		for (int i = 0; i < markers.length; i++) {
			IMarker marker = markers[i];
			IWorkbenchPage page = getSite().getPage();

			openMarkerInEditor(marker, page);
		}
	}

	/**
	 * Register the context menu for the receiver so that commands may be added
	 * to it.
	 */
	private void registerContextMenu() {
		MenuManager contextMenu = new MenuManager();
		contextMenu.setRemoveAllWhenShown(true);
		getSite().registerContextMenu(contextMenu, viewer);
		// Add in the entries for all markers views
		getSite().registerContextMenu(MarkerSupportRegistry.ALL_MARKERS_ID,
				contextMenu, viewer);
		Control control = viewer.getControl();
		Menu menu = contextMenu.createContextMenu(control);

		control.setMenu(menu);
	}

	/**
	 * Remove the category from the list of expanded ones.
	 * 
	 * @param category
	 */
	void removeExpandedCategory(MarkerCategory category) {
		categoriesToExpand.remove(category.getName());

	}

	/**
	 * Preserve the selection for re-selection after the next update.
	 * 
	 * @param selection
	 */
	void saveSelection(ISelection selection) {
		preservedSelection.clear();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structured = (IStructuredSelection) selection;
			Iterator iterator = structured.iterator();
			while (iterator.hasNext()) {
				MarkerItem next = (MarkerItem) iterator.next();
				if (next.isConcrete()) {
					preservedSelection.add(new MarkerSelectionEntry(next));
					categoriesToExpand.add(next.getParent());
				} else
					categoriesToExpand.add(next);
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.ViewPart#saveState(org.eclipse.ui.IMemento)
	 */
	public void saveState(IMemento memento) {
		super.saveState(memento);
		memento.putString(TAG_GENERATOR, builder.generator.getId());
		builder.saveState(memento);
	}

	/**
	 * Select all of the elements in the receiver.
	 */
	void selectAll() {
		viewer.getTree().selectAll();

	}

	/**
	 * Set the category group for the receiver.
	 * 
	 * @param group
	 */
	void setCategoryGroup(MarkerGroup group) {
		categoriesToExpand.clear();
		builder.setCategoryGroup(group);
	}

	/**
	 * Set the content generator for the receiver.
	 * 
	 * @param generator
	 */
	void setContentGenerator(MarkerContentGenerator generator) {
		viewer.setSelection(new StructuredSelection());
		builder.setGenerator(generator);
		createColumns(viewer.getTree().getColumns());
		setPartName(generator.getName());
		viewer.refresh();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	public void setFocus() {
		// Do nothing by default

	}

	/**
	 * Add group to the enabled filters.
	 * 
	 * @param group
	 */
	void toggleFilter(MarkerFieldFilterGroup group) {
		builder.toggleFilter(group);

	}

	/**
	 * Update the direction indicator as column is now the primary column.
	 * 
	 * @param column
	 * @field {@link MarkerField}
	 */
	void updateDirectionIndicator(TreeColumn column, MarkerField field) {
		viewer.getTree().setSortColumn(column);
		if (builder.getGenerator().getSortDirection(field) == MarkerComparator.ASCENDING)
			viewer.getTree().setSortDirection(SWT.UP);
		else
			viewer.getTree().setSortDirection(SWT.DOWN);
	}

	/**
	 * Update the title of the view.
	 */
	void updateTitle() {

		String status = MarkerSupportConstants.EMPTY_STRING;
		int filteredCount = MarkerSupportInternalUtilities.getMarkerLimit();
		int totalCount = builder.getTotalMarkerCount();
		if (filteredCount < 0 || filteredCount >= totalCount) {
			status = NLS.bind(MarkerMessages.filter_itemsMessage, new Integer(
					totalCount));
		} else {
			status = NLS.bind(MarkerMessages.filter_matchedMessage,
					new Integer(filteredCount), new Integer(totalCount));
		}
		setContentDescription(status);

	}

	/**
	 * Return the generator that is currently showing.
	 * 
	 * @return MarkerContentGenerator
	 */
	MarkerContentGenerator getContentGenerator() {
		return builder.getGenerator();
	}

}
