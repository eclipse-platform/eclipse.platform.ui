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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
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
import org.eclipse.swt.events.TreeAdapter;
import org.eclipse.swt.events.TreeEvent;
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
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.internal.ide.StatusUtil;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.eclipse.ui.progress.WorkbenchJob;
import org.eclipse.ui.statushandlers.StatusManager;
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
		 * @param markerEntry
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

	/**
	 * Return the next secondary id.
	 * 
	 * @return String
	 */
	static String newSecondaryID() {
		return String.valueOf(instanceCount);
	}

	private CachedMarkerBuilder builder;
	Collection categoriesToExpand = new HashSet();
	private Clipboard clipboard;
	Collection preservedSelection = new ArrayList();

	private MarkerState state;

	private Job updateJob;
	private TreeViewer viewer;

	/**
	 * Return a new instance of the receiver.
	 */
	public ExtendedMarkersView() {
		super();
		instanceCount++;
	}

	/**
	 * Add the category to the list of expanded categories.
	 * 
	 * @param category
	 */
	public void addExpandedCategory(MarkerCategory category) {
		categoriesToExpand.add(category.getName());

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
		int totalWeight = 0;
		for (int i = 0; i < fields.length; i++) {
			totalWeight += fields[i].getColumnWeight();
		}

		// Scale the percentages based on weight
		float multiplier = 100 / totalWeight;

		for (int i = 0; i < fields.length; i++) {
			MarkerField markerField = fields[i];
			layout.addColumnData(new ColumnWeightData((int) (markerField
					.getColumnWeight() * multiplier), true));
			TreeViewerColumn column;
			if (i < currentColumns.length)
				column = new TreeViewerColumn(viewer, currentColumns[i]);
			else
				column = new TreeViewerColumn(viewer, SWT.NONE);
			column.setLabelProvider(new MarkerColumnLabelProvider(markerField));
			column.getColumn().setText(markerField.getColumnHeaderText());
			if (state.isPrimarySortField(markerField))
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());

		viewer = new TreeViewer(new Tree(parent, SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.MULTI | SWT.FULL_SELECTION | SWT.VIRTUAL));
		viewer.getTree().setLinesVisible(true);
		viewer.setUseHashlookup(true);

		createColumns(new TreeColumn[0]);

		viewer.setContentProvider(getContentProvider(viewer));
		viewer.getTree().setItemCount(builder.getElements().length);

		viewer.setInput(builder);

		Scrollable scrollable = (Scrollable) viewer.getControl();
		ScrollBar bar = scrollable.getVerticalBar();
		if (bar != null) {
			bar.setSelection(getIntValue(TAG_VERTICAL_POSITION));
		}
		bar = scrollable.getHorizontalBar();
		if (bar != null) {
			bar.setSelection(getIntValue(TAG_HORIZONTAL_POSITION));
		}

		getSite().setSelectionProvider(viewer);

		// Initialise any selection based filtering
		ISelectionListener listener = getPageSelectionListener();
		getSite().getPage().addSelectionListener(listener);
		listener.selectionChanged(getSite().getPage().getActivePart(),
				getSite().getPage().getSelection());

		viewer.addOpenListener(new IOpenListener() {
			public void open(OpenEvent event) {
				IMarker[] markers = getSelectedMarkers();
				for (int i = 0; i < markers.length; i++) {
					IMarker marker = markers[i];

					// optimization: if the active editor has the same input as
					// the
					// selected marker then
					// RevealMarkerAction would have been run and we only need
					// to
					// activate the editor
					IEditorPart editor = getSite().getPage().getActiveEditor();
					if (editor != null) {
						IEditorInput input = editor.getEditorInput();
						IFile file = ResourceUtil.getFile(input);
						if (file != null) {
							if (marker.getResource().equals(file)) {
								getSite().getPage().activate(editor);
							}
						}
					}

					if (marker.getResource() instanceof IFile) {
						try {
							IDE.openEditor(getSite().getPage(), marker,
									OpenStrategy.activateOnOpen());
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
										StatusUtil.newStatus(IStatus.ERROR, e
												.getMessage(), e),
										StatusManager.SHOW);

							else
								StatusManager.getManager().handle(status,
										StatusManager.SHOW);

						}
					}
				}
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
	}

	/**
	 * Get all of the filters for the receiver.
	 * 
	 * @return Collection of {@link MarkerFieldFilterGroup}
	 */
	public Collection getAllFilters() {
		return builder.getGenerator().getAllFilters();
	}

	/**
	 * Get all of the fields visible in the receiver.
	 * 
	 * @return MarkerField[]
	 */
	public MarkerField[] getVisibleFields() {
		return builder.getGenerator().getVisibleFields();
	}

	/**
	 * Return the clipboard for the receiver.
	 * 
	 * @return
	 */
	Clipboard getClipboard() {
		if (clipboard == null)
			clipboard = new Clipboard(viewer.getControl().getDisplay());
		return clipboard;
	}

	/**
	 * Return the content provider for the receiver.
	 * 
	 * @return ILazyTreeContentProvider
	 */
	private ILazyTreeContentProvider getContentProvider(final TreeViewer viewer) {
		return new ILazyTreeContentProvider() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
			 */
			public void dispose() {
				// TODO Auto-generated method stub

			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.ILazyTreeContentProvider#getParent(java.lang.Object)
			 */
			public Object getParent(Object element) {
				return ((MarkerItem) element).getParent();
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
			 *      java.lang.Object, java.lang.Object)
			 */
			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
				// TODO Auto-generated method stub

			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.ILazyTreeContentProvider#updateChildCount(java.lang.Object,
			 *      int)
			 */
			public void updateChildCount(Object element, int currentChildCount) {

				MarkerItem[] children;

				if (element instanceof MarkerItem) {
					MarkerItem item = (MarkerItem) element;
					children = item.getChildren();

				} else {
					// If it is not a MarkerItem it is the root
					CachedMarkerBuilder builder = (CachedMarkerBuilder) element;
					children = builder.getElements();
				}

				// No updates if it hasn't changed
				if (children.length == currentChildCount)
					return;
				viewer.setChildCount(element, children.length);

			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.ILazyTreeContentProvider#updateElement(java.lang.Object,
			 *      int)
			 */
			public void updateElement(Object parent, int index) {
				Object newItem;

				if (parent instanceof MarkerItem)
					newItem = ((MarkerItem) parent).getChildren()[index];
				else
					newItem = ((CachedMarkerBuilder) parent).getElements()[index];

				viewer.replace(parent, index, newItem);
				updateChildCount(newItem, -1);

			}
		};
	}

	/**
	 * Get the int value for the tag.
	 * 
	 * @param tag
	 * @return int
	 */
	private int getIntValue(String tag) {
		if (state.useDefaults())
			return 0;

		Integer intValue = state.getInteger(tag);
		return (intValue == null) ? 0 : intValue.intValue();
	}

	/**
	 * Return the selection listener for the page selection change.
	 * 
	 * @return
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
	protected IMarker[] getSelectedMarkers() {
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
				return MarkerUtilities.EMPTY_MARKER_ARRAY;
			IMarker[] markers = new IMarker[result.size()];
			result.toArray(markers);
			return markers;
		}
		return MarkerUtilities.EMPTY_MARKER_ARRAY;

	}

	/**
	 * Return a job for updating the receiver.
	 * 
	 * @return
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

				getViewer().refresh(true);

				updateTitle();
				// Expand all if the list is small
				if (builder.getVisibleMarkers().getSize() < 20) {
					viewer.expandAll();
				} else {// Re-expand the old categories
					MarkerCategory[] categories = builder.getCategories();
					if (categories == null)
						categoriesToExpand.clear();
					else {
						if (categories.length == 1) {// Expand if there is
							// only
							// one
							getViewer().expandAll();
							categoriesToExpand.clear();
							if (monitor.isCanceled())
								return Status.CANCEL_STATUS;
							categoriesToExpand.add(categories[0].getName());
						} else {
							Collection newCategories = new HashSet();
							for (int i = 0; i < categories.length; i++) {
								if (monitor.isCanceled())
									return Status.CANCEL_STATUS;
								MarkerCategory category = categories[i];
								if (categoriesToExpand.contains(category
										.getName())) {
									getViewer().expandToLevel(category,
											AbstractTreeViewer.ALL_LEVELS);
									newCategories.add(category.getName());
								}

							}
							categoriesToExpand = newCategories;
						}
					}

				}

				if (preservedSelection.size() > 0) {

					Collection newSelection = new ArrayList();
					MarkerItem[] markerEntries = builder.getVisibleMarkers()
							.toArray();

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite,
	 *      org.eclipse.ui.IMemento)
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		MarkerContentGenerator generator = null;
		if (memento != null)
			generator = MarkerSupportRegistry.getInstance().getGenerator(
					memento.getString(TAG_GENERATOR));
		if (generator == null)
			generator = MarkerSupportRegistry.getInstance().generatorFor(
					site.getPage().getPerspective());
		generator.loadSettings(memento);
		builder = new CachedMarkerBuilder(generator);
		builder.setUpdateJob(getUpdateJob(builder));
		Object service = site.getAdapter(IWorkbenchSiteProgressService.class);
		if (service != null)
			builder.setProgressService((IWorkbenchSiteProgressService) service);
		state = new MarkerState(memento);
		setPartName(generator.getName());
	}

	/**
	 * Return whether or not group is enabled.
	 * 
	 * @param group
	 * @return boolean
	 */
	public boolean isEnabled(MarkerFieldFilterGroup group) {
		return builder.getGenerator().getEnabledFilters().contains(group);
	}

	/**
	 * Return whether or not generator is the selected one.
	 * 
	 * @param generator
	 * @return boolean
	 */
	public boolean isShowing(MarkerContentGenerator generator) {
		return this.builder.getGenerator().equals(generator);
	}

	/**
	 * Open the filters dialog for the receiver.
	 */
	public void openFiltersDialog() {
		FiltersConfigurationDialog dialog = new FiltersConfigurationDialog(
				new SameShellProvider(getSite().getWorkbenchWindow().getShell()),
				builder.getGenerator().getAllFilters(), builder.getGenerator()
						.getFilterConfigurationFields());
		if (dialog.open() == Window.OK)
			builder.setFilters(dialog.getFilters());

	}

	/**
	 * Register the context menu for the receiver so that commands may be added
	 * to it.
	 */
	private void registerContextMenu() {
		MenuManager contextMenu = new MenuManager();
		getSite().registerContextMenu(contextMenu, viewer);
		Control control = viewer.getControl();
		Menu menu = contextMenu.createContextMenu(control);
		control.setMenu(menu);
	}

	/**
	 * Remove the category from the list of expanded ones.
	 * 
	 * @param category
	 */
	public void removeExpandedCategory(MarkerCategory category) {
		categoriesToExpand.remove(category.getName());

	}

	/**
	 * Preserve the selection for re-selection after the next update.
	 * 
	 * @param selection
	 */
	public void saveSelection(ISelection selection) {
		preservedSelection.clear();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structured = (IStructuredSelection) selection;
			Iterator iterator = structured.iterator();
			while (iterator.hasNext()) {
				MarkerItem next = (MarkerItem) iterator.next();
				if (next.isConcrete()) {
					preservedSelection.add(new MarkerSelectionEntry(next));
				}
			}
		}

	}

	/**
	 * Set the content generator for the receiver.
	 * 
	 * @param generator
	 */
	public void setContentGenerator(MarkerContentGenerator generator) {
		builder.setGenerator(generator);
		createColumns(viewer.getTree().getColumns());
		setPartName(generator.getName());
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
	public void toggleFilter(MarkerFieldFilterGroup group) {
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
		if (state.getSortDirection(field) == MarkerField.ASCENDING)
			viewer.getTree().setSortDirection(SWT.UP);
		else
			viewer.getTree().setSortDirection(SWT.DOWN);
	}

	/**
	 * Update the title of the view.
	 */
	protected void updateTitle() {

		String status = MarkerUtilities.EMPTY_STRING;
		int filteredCount = builder.getVisibleMarkers().getSize();
		int totalCount = builder.getTotalMarkerCount();
		if (filteredCount == totalCount) {
			status = NLS.bind(MarkerMessages.filter_itemsMessage, new Integer(
					totalCount));
		} else {
			status = NLS.bind(MarkerMessages.filter_matchedMessage,
					new Integer(filteredCount), new Integer(totalCount));
		}
		setContentDescription(status);

	}

	/**
	 * Select all of the elements in the receiver.
	 */
	void selectAll() {
		viewer.getTree().selectAll();

	}

}
