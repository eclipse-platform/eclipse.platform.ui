/*******************************************************************************
 * Copyright (c) 2007, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Andrew Gvozdev -  Bug 364039 - Add "Delete All Markers"
 *******************************************************************************/
package org.eclipse.ui.internal.views.markers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.help.IContext;
import org.eclipse.help.IContextProvider;
import org.eclipse.jface.action.ContributionManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
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
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.OpenAndLinkWithEditorHelper;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.StatusUtil;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.operations.RedoActionHandler;
import org.eclipse.ui.operations.UndoActionHandler;
import org.eclipse.ui.part.MarkerTransfer;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.views.markers.MarkerField;
import org.eclipse.ui.views.markers.MarkerItem;
import org.eclipse.ui.views.markers.internal.ContentGeneratorDescriptor;
import org.eclipse.ui.views.markers.internal.MarkerGroup;
import org.eclipse.ui.views.markers.internal.MarkerMessages;
import org.eclipse.ui.views.markers.internal.MarkerSupportRegistry;

import com.ibm.icu.text.MessageFormat;


/**
 * The ExtendedMarkersView is the internal implementation of the view that shows
 * markers using the markerGenerators extension point.
 * 
 * The ExtendedMarkersView fully supports the markerSupport extension point and
 * is meant to be used as a view to complement them.
 * 
 * The markerContentGenerators to be used by the view can be specified by
 * appending a comma separated list of them after a colon in the class
 * specification of the view. If this list is left out the problems
 * markerContentProvider will be used.
 * 
 * @since 3.4
 * 
 */
public class ExtendedMarkersView extends ViewPart {

	/**
	 * The Markers View Update Job Family
	 * @since 3.6
	 */
	public final Object MARKERSVIEW_UPDATE_JOB_FAMILY = new Object();

	static final String MARKER_FIELD = "MARKER_FIELD"; //$NON-NLS-1$
	
	private static int instanceCount = 1;
	private static final String TAG_GENERATOR = "markerContentGenerator"; //$NON-NLS-1$

	private static final String TAG_EXPANDED = "expanded"; //$NON-NLS-1$

	private static final String TAG_CATEGORY = "category"; //$NON-NLS-1$

	private static final String TAG_PART_NAME = "partName"; //$NON-NLS-1$

	private static final String TAG_COLUMN_WIDTHS = "columnWidths"; //$NON-NLS-1$

	private MarkerContentGenerator generator;
	private CachedMarkerBuilder builder;
	private Collection categoriesToExpand;

	private UIUpdateJob uiUpdateJob;
	
	private MarkersTreeViewer viewer;

	/**
	 * Tells whether the tree has been painted.
	 * @since 3.7
	 */
	private boolean treePainted= false;
	
	private ISelectionListener pageSelectionListener;
	private IPartListener2 partListener;
	private Clipboard clipboard;

	// private IPropertyChangeListener preferenceListener;

	private IMemento memento;
	private String[] defaultGeneratorIds = new String[0];

	private UndoActionHandler undoAction;

	private RedoActionHandler redoAction;


	/**
	 * Return a new instance of the receiver.
	 * 
	 * @param contentGeneratorId
	 *            the id of the generator to load.
	 */
	public ExtendedMarkersView(String contentGeneratorId) {
		super();
		defaultGeneratorIds = new String[] { contentGeneratorId };
	}

	/**
	 * Create a preference listener for any preference updates.
	 */ 
	// TODO: this is not needed as the preference dialog will refresh anyway
//	private void initializePreferenceListener() {
//		preferenceListener = new IPropertyChangeListener() {
//			public void propertyChange(PropertyChangeEvent event) {
//				String propertyName = event.getProperty();
//				if (propertyName
//						.equals(IDEInternalPreferences.USE_MARKER_LIMITS)
//						|| propertyName
//								.equals(IDEInternalPreferences.MARKER_LIMITS_VALUE)) {
//					viewer.refresh();
//					updateTitle();
//				}
//			}
//		};
//		IDEWorkbenchPlugin.getDefault().getPreferenceStore()
//				.addPropertyChangeListener(preferenceListener);
//	}
	

	/**
	 * Add all concrete {@link MarkerSupportItem} elements associated with the
	 * receiver to allMarkers.
	 * 
	 * @param markerItem
	 * @param allMarkers
	 */
	private void addAllConcreteItems(MarkerSupportItem markerItem,
			Collection allMarkers) {
		if (markerItem.isConcrete()) {
			allMarkers.add(markerItem);
			return;
		}

		MarkerSupportItem[] children = markerItem.getChildren();
		for (int i = 0; i < children.length; i++) {
			addAllConcreteItems(children[i], allMarkers);
		}

	}

	/**
	 * Add the category to the list of expanded categories.
	 * 
	 * @param category
	 */
	void addExpandedCategory(MarkerCategory category) {
		getCategoriesToExpand().add(category.getName());

	}

	/**
	 * Add all of the markers in markerItem recursively.
	 * 
	 * @param markerItem
	 * @param allMarkers
	 *            {@link Collection} of {@link IMarker}
	 */
	private void addMarkers(MarkerSupportItem markerItem, Collection allMarkers) {
		if (markerItem.getMarker() != null)
			allMarkers.add(markerItem.getMarker());
		MarkerSupportItem[] children = markerItem.getChildren();
		for (int i = 0; i < children.length; i++) {
			addMarkers(children[i], allMarkers);

		}

	}

	/**
	 * Create the columns for the receiver.
	 * 
	 * @param parent
	 */
	private void createViewer(Composite parent) {
		parent.setLayout(new FillLayout());

		viewer = new MarkersTreeViewer(new Tree(parent, SWT.H_SCROLL
				/*| SWT.VIRTUAL */| SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION));
		viewer.getTree().setLinesVisible(true);
		viewer.setUseHashlookup(true);
		createColumns(new TreeColumn[0], new int[0]);
		viewer.setContentProvider(getContentProvider());

		/*
		 * Workaround for TeeColumn.getWidth() returning 0 in some cases, see
		 * https://bugs.eclipse.org/341865 for details.
		 */
		viewer.getTree().addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				treePainted= true;
				viewer.getTree().removePaintListener(this);
			}
		});
	}

	/**
	 * Create the columns for the receiver.
	 * 
	 * @param currentColumns
	 *            the columns to refresh
	 * @param widths 
	 */
	private void createColumns(TreeColumn[] currentColumns, int[] widths) {

		Tree tree = viewer.getTree();
		TableLayout layout = new TableLayout();

		MarkerField[] fields = generator.getVisibleFields();

		for (int i = 0; i < fields.length; i++) {
			MarkerField markerField = fields[i];
			TreeViewerColumn column = null;
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
			column.setLabelProvider(new MarkerColumnLabelProvider(markerField));
			column.getColumn().setText(markerField.getColumnHeaderText());
			column.getColumn().setToolTipText(
					markerField.getColumnTooltipText());
			column.getColumn().setImage(markerField.getColumnHeaderImage());

			EditingSupport support = markerField.getEditingSupport(viewer);
			if (support != null)
				column.setEditingSupport(support);

			if (builder.getPrimarySortField().equals(markerField))
				updateDirectionIndicator(column.getColumn(), markerField);

			IMemento columnWidths = null;
			if (memento != null){
				columnWidths = memento.getChild(TAG_COLUMN_WIDTHS);
			}
			
			//adjust the column width
			int columnWidth = i < widths.length ? widths[i] : -1;
			columnWidth = getFieldWidth(markerField, columnWidth, false);
			if (columnWidths != null) {
				// save it
				columnWidths.putInteger(
						markerField.getConfigurationElement().getAttribute(
						MarkerSupportInternalUtilities.ATTRIBUTE_ID), columnWidth);
			}
			// Take trim into account if we are using the default value, but not
			// if it is restored.
			if (columnWidth < 0)
				layout.addColumnData(new ColumnPixelData(markerField
						.getDefaultColumnWidth(tree), true, true));
			else
				layout.addColumnData(new ColumnPixelData(columnWidth, true));
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
	 * 
	 * @param markerField
	 * @param preferredWidth
	 * @param considerUIWidths
	 * @return desired width for the column representing markerField
	 */
	int getFieldWidth(MarkerField markerField, int preferredWidth, boolean considerUIWidths) {
		Tree tree = getViewer().getTree();

		if (considerUIWidths) {
			TreeColumn[] columns= tree.getColumns();
			for (int i= 0; i < columns.length; i++) {
				if (markerField.equals(columns[i].getData(MARKER_FIELD))) {
					return columns[i].getWidth();
				}
			}
		}

		if (preferredWidth < 0 && memento != null) {
			IMemento columnWidths = memento.getChild(TAG_COLUMN_WIDTHS);
			if (columnWidths != null) {
				Integer value = columnWidths.getInteger(markerField
						.getConfigurationElement().getAttribute(
								MarkerSupportInternalUtilities.ATTRIBUTE_ID));
				// Make sure we get a useful value
				if (value != null && value.intValue() >= 0)
					preferredWidth = value.intValue();
			}
		}
		if (preferredWidth <= 0) {
			// Compute and store a font metric
			GC gc = new GC(tree);
			gc.setFont(tree.getFont());
			FontMetrics fontMetrics = gc.getFontMetrics();
			gc.dispose();
			preferredWidth = Math.max(markerField.getDefaultColumnWidth(tree),
					fontMetrics.getAverageCharWidth() * 5);
		}
		return preferredWidth;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	public void createPartControl(Composite parent) {

		createViewer(parent);

		addDoubleClickListener();
		
		addPageAndPartSelectionListener();

		addLinkWithEditorSupport();

		addExpansionListener();

		addHelpListener();

		addSelectionListener();

		registerContextMenu();

		initDragAndDrop();

		getSite().setSelectionProvider(viewer);

		IUndoContext undoContext= getUndoContext();
		undoAction= new UndoActionHandler(getSite(), undoContext);
		undoAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_UNDO);
		redoAction= new RedoActionHandler(getSite(), undoContext);
		redoAction.setActionDefinitionId(IWorkbenchCommandConstants.EDIT_REDO);
		getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.UNDO.getId(), undoAction);
		getViewSite().getActionBars().setGlobalActionHandler(ActionFactory.REDO.getId(), redoAction);

		startView();

	}

	/**
	 * 
	 */
	private void startView() {
		viewer.setInput(builder.getMarkers());
		//always use a clone for Thread safety
		IContentProvider contentProvider = viewer.getContentProvider();
		Markers clone = createViewerInputClone();
		if (clone == null) {
			clone = builder.getMarkers().getClone();
		}
		contentProvider.inputChanged(viewer, getViewerInput(), clone);
		builder.start();
	}

	/**
	 * Attaches an {@link IDoubleClickListener} to expand items that are not openable
	 * @since 3.8
	 */
	private void addDoubleClickListener() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				ISelection selection = event.getSelection();
				if(selection instanceof ITreeSelection) {
					ITreeSelection ss = (ITreeSelection) selection;
					if(ss.size() == 1) {
						Object obj = ss.getFirstElement();
						if(viewer.isExpandable(obj)) {
							viewer.setExpandedState(obj, !viewer.getExpandedState(obj));
						}
					}
				}
			}
		});
	}
	
	/**
	 * 
	 */
	private void addPageAndPartSelectionListener() {
		// Initialise any selection based filtering
		pageSelectionListener = new ViewerPageSelectionListener(this);
		getSite().getPage().addPostSelectionListener(pageSelectionListener);

		partListener = getPartListener();
		getSite().getPage().addPartListener(partListener);

		pageSelectionListener.selectionChanged(getSite().getPage()
				.getActivePart(), getSite().getPage().getSelection());
	}

	/**
	 * 
	 */
	private void addSelectionListener() {
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				if (selection instanceof IStructuredSelection){
					updateStatusLine((IStructuredSelection)selection);
				}
			}
		});
	}

	/**
	 * 
	 */
	private void addHelpListener() {
		// Set help on the view itself
		viewer.getControl().addHelpListener(new HelpListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.swt.events.HelpListener#helpRequested(org.eclipse
			 * .swt.events.HelpEvent)
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
	}

	/**
	 * 
	 */
	private void addExpansionListener() {
		viewer.getTree().addTreeListener(new TreeAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.swt.events.TreeAdapter#treeCollapsed(org.eclipse.
			 * swt.events.TreeEvent)
			 */
			public void treeCollapsed(TreeEvent e) {
				removeExpandedCategory((MarkerCategory) e.item.getData());
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.swt.events.TreeAdapter#treeExpanded(org.eclipse.swt
			 * .events.TreeEvent)
			 */
			public void treeExpanded(TreeEvent e) {
				addExpandedCategory((MarkerCategory) e.item.getData());
			}
		});
	}

	/**
	 * 
	 */
	private void addLinkWithEditorSupport() {
		new OpenAndLinkWithEditorHelper(viewer) {
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.ui.OpenAndLinkWithEditorHelper#activate(org.eclipse
			 * .jface.viewers.ISelection )
			 */
			protected void activate(ISelection selection) {
				final int currentMode = OpenStrategy.getOpenMethod();
				try {
					OpenStrategy.setOpenMethod(OpenStrategy.DOUBLE_CLICK);
					openSelectedMarkers();
				} finally {
					OpenStrategy.setOpenMethod(currentMode);
				}
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.ui.OpenAndLinkWithEditorHelper#linkToEditor(org.eclipse
			 * .jface.viewers .ISelection)
			 */
			protected void linkToEditor(ISelection selection) {
				// Not supported by this part
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.ui.OpenAndLinkWithEditorHelper#open(org.eclipse.jface
			 * .viewers.ISelection, boolean)
			 */
			protected void open(ISelection selection, boolean activate) {
				openSelectedMarkers();
			}
		};
	}
	
	/**
	 * Returns the complete list of selected {@link IMarker}s from the view.
	 * 
	 * @return the complete list of selected {@link IMarker}s or an empty array, never <code>null</code>
	 * @since 3.8
	 */
	IMarker[] getOpenableMarkers() {
		ISelection selection = viewer.getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structured = (IStructuredSelection) selection;
			Iterator elements = structured.iterator();
			HashSet result = new HashSet();
			while (elements.hasNext()) {
				MarkerSupportItem next = (MarkerSupportItem) elements.next();
				if (next.isConcrete()) {
					result.add(((MarkerEntry) next).getMarker());
				}
			}
			if (result.isEmpty()) {
				return MarkerSupportInternalUtilities.EMPTY_MARKER_ARRAY;
			}
			IMarker[] markers = new IMarker[result.size()];
			result.toArray(markers);
			return markers;
		}
		return MarkerSupportInternalUtilities.EMPTY_MARKER_ARRAY;
	}
	
	/**
	 * Turn off all filters in the builder.
	 */
	void disableAllFilters() {
		generator.disableAllFilters();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	public void dispose() {
		builder.cancelUpdate();
		cancelQueuedUpdates();
		
		builder.dispose();
		generator.dispose();
		if (instanceCount > 1)
			instanceCount--;
		if (clipboard != null)
			clipboard.dispose();

		/*
		 * IDEWorkbenchPlugin.getDefault().getPreferenceStore()
		 * .removePropertyChangeListener(preferenceListener);
		 */

		getSite().getPage().removePostSelectionListener(pageSelectionListener);
		getSite().getPage().removePartListener(partListener);

		undoAction.dispose();
		redoAction.dispose();

		super.dispose();
	}

	/**
	 * Return all of the marker items in the receiver that are concrete.
	 * 
	 * @return MarkerSupportItem[]
	 */
	MarkerSupportItem[] getAllConcreteItems() {
		MarkerSupportItem[] elements =getActiveViewerInputClone().getElements();
		Collection allMarkers = new ArrayList();
		for (int i = 0; i < elements.length; i++) {
			addAllConcreteItems(elements[i], allMarkers);

		}
		MarkerSupportItem[] markers = new MarkerSupportItem[allMarkers.size()];
		allMarkers.toArray(markers);
		return markers;
	}

	/**
	 * Get all of the filters for the receiver.
	 * 
	 * @return Collection of {@link MarkerFieldFilterGroup}
	 */
	Collection getAllFilters() {
		return generator.getAllFilters();
	}

	/**
	 * Return all of the markers in the receiver.
	 * 
	 * @return IMarker[]
	 */
	IMarker[] getAllMarkers() {

		MarkerSupportItem[] elements =getActiveViewerInputClone().getElements();
		Collection allMarkers = new ArrayList();
		for (int i = 0; i < elements.length; i++) {
			addMarkers(elements[i], allMarkers);

		}
		IMarker[] markers = new IMarker[allMarkers.size()];
		allMarkers.toArray(markers);
		return markers;

	}

	/**
	 * Return the builder for the receiver.
	 * 
	 * @return CachedMarkerBuilder
	 */
	CachedMarkerBuilder getBuilder() {
		return builder;
	}

	/**
	 * Get the categories to expand for the receiver.
	 * 
	 * @return Collection of MarkerCategory.
	 */
	Collection getCategoriesToExpand() {
		if (categoriesToExpand == null) {
			categoriesToExpand = new HashSet();
			if (this.memento != null) {
				IMemento expanded = this.memento.getChild(TAG_EXPANDED);
				if (expanded != null) {
					IMemento[] mementoCategories = expanded
							.getChildren(TAG_CATEGORY);
					MarkerCategory[] markerCategories =getActiveViewerInputClone().getCategories();
					if (markerCategories != null) {
						for (int i = 0; i < markerCategories.length; i++) {
							for (int j = 0; j < mementoCategories.length; j++) {
								if (markerCategories[i].getName().equals(
										mementoCategories[j].getID()))
									categoriesToExpand.add(markerCategories[i]
											.getName());
							}
						}
					}
				}
			}
		}
		return categoriesToExpand;
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
	private IContentProvider getContentProvider() {
		return new MarkerViewerContentProvider(this);
	}

	/**
	 * Get the id of the marker field in treeColumn.
	 * 
	 * @param treeColumn
	 * @return String
	 */
	private String getFieldId(TreeColumn treeColumn) {
		return ((MarkerField) treeColumn.getData(MARKER_FIELD))
				.getConfigurationElement().getAttribute(
						MarkerSupportInternalUtilities.ATTRIBUTE_ID);
	}

	/**
	 * Return the ids of the generators specified for the receiver.
	 * 
	 * @return String[]
	 */
	String[] getGeneratorIds() {
		return defaultGeneratorIds;
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
				setPrimarySortField(field, column);
			}

		};

	}

	/**
	 * Return a part listener for the receiver.
	 * 
	 * @return IPartListener2
	 */
	private IPartListener2 getPartListener() {
		return new IPartListener2() {

			/*
			 * (non-Javadoc)
			 * 
			 * @seeorg.eclipse.ui.IPartListener2#partActivated(org.eclipse.ui.
			 * IWorkbenchPartReference)
			 */
			public void partActivated(IWorkbenchPartReference partRef) {
				// Do nothing by default
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.ui.IPartListener2#partBroughtToTop(org.eclipse.ui
			 * .IWorkbenchPartReference)
			 */
			public void partBroughtToTop(IWorkbenchPartReference partRef) {
				// Do nothing by default
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @seeorg.eclipse.ui.IPartListener2#partClosed(org.eclipse.ui.
			 * IWorkbenchPartReference)
			 */
			public void partClosed(IWorkbenchPartReference partRef) {
				// Do nothing by default
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.ui.IPartListener2#partDeactivated(org.eclipse.ui.
			 * IWorkbenchPartReference)
			 */
			public void partDeactivated(IWorkbenchPartReference partRef) {
				// Do nothing by default
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @seeorg.eclipse.ui.IPartListener2#partHidden(org.eclipse.ui.
			 * IWorkbenchPartReference)
			 */
			public void partHidden(IWorkbenchPartReference partRef) {
				if (partRef.getId().equals(getSite().getId())) {
					Markers markers = getActiveViewerInputClone();
					Integer[] counts = markers.getMarkerCounts();
					setTitleToolTip(getStatusMessage(markers, counts));
				}

			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.ui.IPartListener2#partInputChanged(org.eclipse.ui
			 * .IWorkbenchPartReference)
			 */
			public void partInputChanged(IWorkbenchPartReference partRef) {
				// Do nothing by default
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @seeorg.eclipse.ui.IPartListener2#partOpened(org.eclipse.ui.
			 * IWorkbenchPartReference)
			 */
			public void partOpened(IWorkbenchPartReference partRef) {
				// Do nothing by default
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @seeorg.eclipse.ui.IPartListener2#partVisible(org.eclipse.ui.
			 * IWorkbenchPartReference)
			 */
			public void partVisible(IWorkbenchPartReference partRef) {
				if (partRef.getId().equals(getSite().getId())) {
					pageSelectionListener.selectionChanged(getSite().getPage()
							.getActivePart(), getSite().getPage()
							.getSelection());
					setTitleToolTip(null);
				}

			}

		};
	}

	/**
	 * Return all of the markers in the current selection
	 * 
	 * @return Array of {@link IMarker}
	 */
	public IMarker[] getSelectedMarkers() {
		ISelection selection = viewer.getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structured = (IStructuredSelection) selection;
			Iterator elements = structured.iterator();
			Collection result = new HashSet();
			while (elements.hasNext()) {
				MarkerSupportItem next = (MarkerSupportItem) elements.next();
				if (next.isConcrete()) {
					result.add(((MarkerEntry) next).getMarker());
				} else {
					MarkerSupportItem[] children = next.getChildren();
					for (int i = 0; i < children.length; i++) {
						if (children[i].isConcrete())
							result.add(((MarkerEntry) children[i]).getMarker());
					}
				}
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
	 * Return the sort direction.
	 * 
	 * @return boolean
	 */
	public boolean getSortAscending() {
		return viewer.getTree().getSortDirection() == SWT.TOP;
	}

	/**
	 * Get the status message for the title and status line.
	 * 
	 * @param markers the markers for which to get the status message
	 * @param counts an array of {@link Integer} where index indicates
	 *            [errors,warnings,infos,others]
	 * @return String
	 */
	private String getStatusMessage(Markers markers, Integer[] counts) {
		String status = MarkerSupportInternalUtilities.EMPTY_STRING;
		int totalCount = builder.getTotalMarkerCount(markers);
		int filteredCount = 0;
		boolean markerLimitsEnabled = generator.isMarkerLimitsEnabled();
		int markerLimit = generator.getMarkerLimits();
		MarkerSupportItem[] categories = markers.getCategories();
		// Categories might be null if building is still happening
		if (categories != null && builder.isShowingHierarchy()) {

			for (int i = 0; i < categories.length; i++) {
				
				int childCount = categories[i].getChildrenCount();
				if (markerLimitsEnabled)
					childCount = Math.min(childCount, markerLimit);

				filteredCount += childCount;
				
			}
		} else {
			if(markerLimitsEnabled)
				filteredCount = markerLimit;
			else
				filteredCount = -1;
		}

		// Any errors or warnings? If not then send the filtering message
		if (counts[0].intValue() == 0 && counts[1].intValue() == 0) {
			if (filteredCount < 0 || filteredCount >= totalCount) {
				status = NLS.bind(MarkerMessages.filter_itemsMessage,
						new Integer(totalCount));
			} else {
				status = NLS.bind(MarkerMessages.filter_matchedMessage,
						new Integer(filteredCount), new Integer(totalCount));
			}
			return status;
		}
		// combine counts for infos and others
		counts = new Integer[] { counts[0], counts[1],
				new Integer(counts[2].intValue() + counts[3].intValue()) };
		if (filteredCount < 0 || filteredCount >= totalCount)
			return MessageFormat.format(
					MarkerMessages.errorsAndWarningsSummaryBreakdown, counts);
		return NLS
				.bind(
						MarkerMessages.problem_filter_matchedMessage,
						new Object[] {
								MessageFormat
										.format(
												MarkerMessages.errorsAndWarningsSummaryBreakdown,
												counts),
								new Integer(filteredCount),
								new Integer(totalCount) });
	}

	/**
	 * Return the Markers that is the input to the viewer.
	 * 
	 * @return Object
	 */
	Markers getViewerInput() {
		return (Markers) viewer.getInput();
	}
	/**
	 * Return the active clone currently in use by UI.
	 * 
	 * @return Object
	 */
	Markers getActiveViewerInputClone() {
		/*The ideal place to hold the reference for the
		 clone is the view,as it is a for-ui-only clone*/
		return builder.getClonedMarkers();
	}

	/**
	 * Return a new clone to use in UI.Can return
	 * null if markers are changing or building.
	 * @see CachedMarkerBuilder#createMarkersClone()
	 * 
	 * 
	 * @return Object
	 */
	Markers createViewerInputClone() {
		return builder.createMarkersClone();
	}

	/**
	 * Get all of the fields visible in the receiver.
	 * 
	 * @return MarkerField[]
	 */
	MarkerField[] getVisibleFields() {
		return generator.getVisibleFields();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite,
	 * org.eclipse.ui.IMemento)
	 */
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		ContentGeneratorDescriptor generatorDescriptor = null;
		if (memento != null) {
			generatorDescriptor = MarkerSupportRegistry.getInstance()
					.getContentGenDescriptor(memento.getString(TAG_GENERATOR));
		}

		if (generatorDescriptor == null && defaultGeneratorIds.length > 0) {
			generatorDescriptor = MarkerSupportRegistry.getInstance()
					.getContentGenDescriptor(defaultGeneratorIds[0]);
			if (generatorDescriptor == null)
				logInvalidGenerator(defaultGeneratorIds[0]);
		}

		if (generatorDescriptor == null)
			generatorDescriptor = MarkerSupportRegistry.getInstance()
					.getDefaultContentGenDescriptor();

		builder = new CachedMarkerBuilder(this);
		generator = new MarkerContentGenerator(generatorDescriptor, builder,
				getViewsEffectiveId());
		generator.restoreState(memento);

		// Add in the entries common to all markers views
		IMenuService menuService = (IMenuService) site
				.getService(IMenuService.class);

		// Add in the markers view actions

		menuService.populateContributionManager((ContributionManager) site
				.getActionBars().getMenuManager(), "menu:" //$NON-NLS-1$
				+ MarkerSupportRegistry.MARKERS_ID);
		menuService.populateContributionManager((ContributionManager) site
				.getActionBars().getToolBarManager(),
				"toolbar:" + MarkerSupportRegistry.MARKERS_ID); //$NON-NLS-1$

		builder.restoreState(memento);

		Object service = site.getAdapter(IWorkbenchSiteProgressService.class);
		if (service != null)
			builder.setProgressService((IWorkbenchSiteProgressService) service);
		this.memento = memento;

		if (memento == null || memento.getString(TAG_PART_NAME) == null)
			return;

		setPartName(memento.getString(TAG_PART_NAME));
	}

	/**
	 * @return viewId
	 * 
	 */
	String getViewsEffectiveId() {
		IViewSite site = (IViewSite) getSite();
		String viewId = site.getId();
		if (site.getSecondaryId() != null) {
			viewId = viewId + site.getSecondaryId();
		}
		return viewId;
	}

	/**
	 * @return viewsPrimaryId
	 * 
	 */
	String getViewsPrimaryId() {
		IViewSite site = (IViewSite) getSite();
		return site.getId();
	}
	
	/**
	 * @return viewsSecondaryId
	 * 
	 */
	String getViewsSecondaryId() {
		IViewSite site = (IViewSite) getSite();
		return site.getSecondaryId();
	}

	/**
	 * Initialize the title based on the name
	 * 
	 * @param name
	 */
	void initializeTitle(String name) {
		setPartName(name);

	}

	/**
	 * Return whether or not group is enabled.
	 * 
	 * @param group
	 * @return boolean
	 */
	boolean isEnabled(MarkerFieldFilterGroup group) {
		return generator.getEnabledFilters().contains(group);
	}

	/**
	 * Return the main sort field for the receiver.
	 * 
	 * @return {@link MarkerField}
	 */
	boolean isPrimarySortField(MarkerField field) {
		return builder.getPrimarySortField().equals(field);
	}

	/**
	 * Return whether or not generator is the selected one.
	 * 
	 * @param generator
	 * @return boolean
	 */
	boolean isShowing(MarkerContentGenerator generator) {
		return generator != null ? generator.equals(generator) : false;
	}

	/**
	 * @return Returns the generator.
	 */
	MarkerContentGenerator getGenerator() {
		return generator;
	}

	/**
	 * Log that a generator id is invalid.
	 * 
	 * @param id
	 */
	void logInvalidGenerator(String id) {
		StatusManager.getManager().handle(
				new Status(IStatus.WARNING, IDEWorkbenchPlugin.IDE_WORKBENCH,
						NLS.bind("Invalid markerContentGenerator {0} ", //$NON-NLS-1$
								id)));
	}

	/**
	 * Open the filters dialog for the receiver.
	 */
	void openFiltersDialog() {
		FiltersConfigurationDialog dialog = new FiltersConfigurationDialog(
				getSite().getWorkbenchWindow().getShell(), generator);
		if (dialog.open() == Window.OK) {
			generator.updateFilters(dialog.getFilters(), dialog.andFilters());
		}

	}
	
	/**
	 * Open the selected markers
	 */
	void openSelectedMarkers() {
		IMarker[] markers = getOpenableMarkers();
		for (int i = 0; i < markers.length; i++) {
			IWorkbenchPage page = getSite().getPage();
			openMarkerInEditor(markers[i], page);
		}
	}

	/**
	 * Restore the expanded categories.
	 * 
	 */
	void reexpandCategories() {
		if (!getCategoriesToExpand().isEmpty() && builder.isShowingHierarchy()) {
			MarkerItem[] items =getActiveViewerInputClone().getElements();
			IContentProvider provider = viewer.getContentProvider();
			for (int i = 0; i < items.length; i++) {
				String name = ((MarkerCategory) items[i]).getName();
				if (getCategoriesToExpand().contains(name)) {
					if (provider instanceof ILazyTreeContentProvider) {
						((ILazyTreeContentProvider) provider).updateElement(
								builder.getMarkers(), i);
						viewer.setExpandedState(items[i], true);
					} else {
						if (!viewer.getExpandedState(items[i])) {
							viewer.expandToLevel(items[i], 2);
						}
					}
				}
			}
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
		// Add in the entries for all markers views if this has a different if
		if (!getSite().getId().equals(MarkerSupportRegistry.MARKERS_ID)) {
			getSite().registerContextMenu(MarkerSupportRegistry.MARKERS_ID,
					contextMenu, viewer);
		}
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
		getCategoriesToExpand().remove(category.getName());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.ViewPart#saveState(org.eclipse.ui.IMemento)
	 */
	public void saveState(IMemento memento) {
		super.saveState(memento);
		memento.putString(TAG_PART_NAME, getPartName());
		if (generator != null) {
			memento.putString(TAG_GENERATOR, builder.getGenerator().getId());
		}

		if (!getCategoriesToExpand().isEmpty()) {
			IMemento expanded = memento.createChild(TAG_EXPANDED);
			Iterator categories = getCategoriesToExpand().iterator();
			while (categories.hasNext()) {
				expanded.createChild(TAG_CATEGORY, (String) categories.next());
			}
		}
		IMemento columnEntry = memento.createChild(TAG_COLUMN_WIDTHS);
		MarkerField[] fields = new MarkerField[viewer.getTree()
				.getColumnCount()];
		int[] positions = viewer.getTree().getColumnOrder();
		for (int i = 0; i < fields.length; i++) {
			TreeColumn column = viewer.getTree().getColumn(i);
			MarkerField markerField= (MarkerField)column.getData(MARKER_FIELD);

			/*
			 * Workaround for TeeColumn.getWidth() returning 0 in some cases, see
			 * https://bugs.eclipse.org/341865 for details.
			 */
			int width= getFieldWidth(markerField, -1, treePainted);

			columnEntry.putInteger(getFieldId(column), width);
			fields[positions[i]]= markerField;
		}
		if (generator != null) {
			generator.saveState(memento, fields);
		}
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
		getCategoriesToExpand().clear();
		builder.setCategoryGroup(group);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	/**
	 * Set the primary sort field
	 * 
	 * @param field
	 */
	void setPrimarySortField(MarkerField field) {
		TreeColumn[] columns = viewer.getTree().getColumns();
		for (int i = 0; i < columns.length; i++) {
			TreeColumn treeColumn = columns[i];
			if (columns[i].getData(MARKER_FIELD).equals(field)) {
				setPrimarySortField(field, treeColumn);
				return;
			}
		}
		StatusManager.getManager().handle(
				StatusUtil.newStatus(IStatus.WARNING,
						"Sorting by non visible field " //$NON-NLS-1$
								+ field.getName(), null));
	}

	/**
	 * Set the primary sort field to field and update the column.
	 * 
	 * @param field
	 * @param column
	 */
	private void setPrimarySortField(MarkerField field, TreeColumn column) {
		builder.setPrimarySortField(field);

		IWorkbenchSiteProgressService service = (IWorkbenchSiteProgressService) getViewSite()
				.getAdapter(IWorkbenchSiteProgressService.class);
		builder.refreshContents(service);
		updateDirectionIndicator(column, field);
	}

	/**
	 * Set the selection of the receiver. reveal the item if reveal is true.
	 * 
	 * @param structuredSelection
	 * @param reveal
	 */
	void setSelection(StructuredSelection structuredSelection, boolean reveal) {
		List newSelection = new ArrayList(structuredSelection.size());
		for (Iterator i = structuredSelection.iterator(); i.hasNext();) {
			Object next = i.next();
			if (next instanceof IMarker) {
				MarkerItem marker = builder.getMarkers().getMarkerItem(
						(IMarker) next);
				if (marker != null) {
					newSelection.add(marker);
				}
			}
		}
		IStructuredSelection structured = new StructuredSelection(newSelection);
		viewer.setSelection(structured, reveal);
		updateStatusLine(structured);
	}

	/**
	 * Add group to the enabled filters.
	 * 
	 * @param group
	 */
	void toggleFilter(MarkerFieldFilterGroup group) {
		generator.toggleFilter(group);
	}

	/**
	 * Toggle the sort direction of the primary field
	 */
	void toggleSortDirection() {
		setPrimarySortField(builder.getPrimarySortField());

	}

	/**
	 * Update the direction indicator as column is now the primary column.
	 * 
	 * @param column
	 * @field {@link MarkerField}
	 */
	void updateDirectionIndicator(TreeColumn column, MarkerField field) {
		viewer.getTree().setSortColumn(column);
		if (builder.getSortDirection(field) == MarkerComparator.ASCENDING)
			viewer.getTree().setSortDirection(SWT.UP);
		else
			viewer.getTree().setSortDirection(SWT.DOWN);
	}

	/**
	 * Update the status line with the new selection
	 * 
	 * @param newSelection
	 */
	void updateStatusLine(IStructuredSelection newSelection) {
		String message;

		if (newSelection == null || newSelection.size() == 0) {
			message = MarkerSupportInternalUtilities.EMPTY_STRING;
		} else if (newSelection.size() == 1) {
			// Use the Message attribute of the marker
			message = ((MarkerSupportItem) newSelection.getFirstElement())
					.getDescription();

		} else {
			Iterator elements = newSelection.iterator();
			Collection result = new ArrayList();
			while (elements.hasNext()) {
				MarkerSupportItem next = (MarkerSupportItem) elements.next();
				if (next.isConcrete())
					result.add(next);
			}
			MarkerEntry[] entries = new MarkerEntry[result.size()];
			result.toArray(entries);
			// Show stats on only those items in the selection
			message = getStatusSummary(entries);
		}
		getViewSite().getActionBars().getStatusLineManager()
				.setMessage(message);
	}

	/**
	 * Get the status line summary of markers.
	 * 
	 * @param entries
	 */
	private String getStatusSummary(MarkerEntry[] entries) {
		Integer[] counts = Markers.getMarkerCounts(entries);
		// combine counts for infos and others
		counts = new Integer[] { counts[0], counts[1],
				new Integer(counts[2].intValue() + counts[3].intValue()) };
		if (counts[0].intValue() == 0 && counts[1].intValue() == 0) {
			// In case of tasks view and bookmarks view, show only selection
			// count
			return MessageFormat.format(
					MarkerMessages.marker_statusSelectedCount,
					new Object[] { new Integer(entries.length) });
		}
		return MessageFormat
				.format(
						MarkerMessages.marker_statusSummarySelected,
						new Object[] {
								new Integer(entries.length),
								MessageFormat
										.format(
												MarkerMessages.errorsAndWarningsSummaryBreakdown,
												counts) });
	}

	/**
	 * Update the title and description of the view.
	 */
	void updateTitle() {
		Markers markers = getActiveViewerInputClone();
		Integer[] counts = markers.getMarkerCounts();
		String statusMessage = getStatusMessage(markers, counts);

		setContentDescription(statusMessage);

		if (!"".equals(getTitleToolTip())) { //$NON-NLS-1$
			setTitleToolTip(statusMessage);
		}

		updateTitleImage(counts);
	}

	/**
	 * Updates this view's title image.
	 * 
	 * @param counts an array of {@link Integer} where index indicates
	 *            [errors,warnings,infos,others]
	 * @since 3.7
	 */
	void updateTitleImage(Integer[] counts) {
	}

	/**
	 * Initialize drag and drop for the receiver.
	 */
	private void initDragAndDrop() {
		int operations = DND.DROP_COPY;
		Transfer[] transferTypes = new Transfer[] {
				MarkerTransfer.getInstance(), TextTransfer.getInstance() };
		DragSourceListener listener = new DragSourceAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.swt.dnd.DragSourceAdapter#dragSetData(org.eclipse
			 * .swt.dnd.DragSourceEvent)
			 */
			public void dragSetData(DragSourceEvent event) {
				performDragSetData(event);
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.swt.dnd.DragSourceAdapter#dragFinished(org.eclipse
			 * .swt.dnd.DragSourceEvent)
			 */
			public void dragFinished(DragSourceEvent event) {
			}
		};

		viewer.addDragSupport(operations, transferTypes, listener);
	}

	/**
	 * The user is attempting to drag marker data. Add the appropriate data to
	 * the event depending on the transfer type.
	 */
	private void performDragSetData(DragSourceEvent event) {
		if (MarkerTransfer.getInstance().isSupportedType(event.dataType)) {

			event.data = getSelectedMarkers();
			return;
		}
		if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
			IMarker[] markers = getSelectedMarkers();
			if (markers != null)
				event.data = MarkerCopyHandler
						.createMarkerReport(this, markers);
		}
	}

	/**
	 * Return the fields that are not hidden.
	 * 
	 * @return Object[]
	 */
	Object[] getHiddenFields() {
		return generator.getHiddenFields();
	}

	/**
	 * @param visible
	 */
	void setVisibleFields(Collection visible,int[] widths) {
		generator.setVisibleFields(visible);
		//viewer.setSelection(new StructuredSelection());
		//viewer.removeAndClearAll();
		createColumns(viewer.getTree().getColumns(), widths);
		scheduleUpdate(0L);
	}

	//See Bug 294303
	//void indicateUpdating(final String message, final boolean updateLabels) {
	//	Display display = getSite().getShell().getDisplay();
	//	if (Display.getCurrent() == display) {
	//		setContentDescription(message != null ? message
	//				: getStatusMessage());
	//		if (updateLabels) {
	//			updateCategoryLabels();
	//		}
	//		return;
	//	}
	//	WorkbenchJob job = new WorkbenchJob(display,
	//			MarkerMessages.MarkerView_queueing_updates) {
	//		public IStatus runInUIThread(IProgressMonitor monitor) {
	//				setContentDescription(message != null ? message
	//						: getStatusMessage());
	//				if (updateLabels) {
	//					updateCategoryLabels();
	//				}
	//				return Status.OK_STATUS;
	//		}
	//	};
	//	job.setPriority(Job.INTERACTIVE);
	//	job.schedule();
	//  //see Bug 293305
	//	//	if (block) {
	//	//		try {
	//	//			if (display.getSyncThread() != Thread.currentThread()) {
	//	//				job.join();
	//	//			}
	//	//		} catch (InterruptedException e) {
	//	//		}
	//	//	}
	//}
	//
	//void updateCategoryLabels() {
	//	if (builder.isShowingHierarchy()) {
	//		MarkerCategory[] categories =getActiveViewerInputClone().getCategories();
	//		boolean refreshing = builder.isBuilding()
	//				|| builder.getMarkerListener().isUpdating() 
	//				|| builder.getMarkerListener().workspaceBuilding();
	//		for (int i = 0; i < categories.length; i++) {
	//			categories[i].refreshing = refreshing;
	//		}
	//		if (categories != null && categories.length > 1) {
	//			viewer.update(categories, null);
	//		}
	//	}
	//}

	/**
	 * @return the viewer
	 */
	TreeViewer getViewer() {
		return viewer;
	}

	/**
	 * The method should not be called directly, see
	 * {@link MarkerUpdateScheduler}
	 * 
	 * Cancel a scheduled delay
	 */
	void cancelQueuedUpdates() {
		synchronized (builder.getUpdateScheduler().getSchedulingLock()) {
			if (uiUpdateJob != null) {
				uiUpdateJob.cancel();
			}
		}
	}

	/**
	 * The method should not be called directly, see
	 * {@link MarkerUpdateScheduler}
	 * 
	 * @param delay
	 * @return UIUpdateJob
	 */
	UIUpdateJob scheduleUpdate(long delay) {
		synchronized (builder.getUpdateScheduler().getSchedulingLock()) {
			if (uiUpdateJob != null) {
				// ensure cancellation before calling the method
				// uiUpdateJob.cancel();
			} else {
				uiUpdateJob = new UIUpdateJob(this);
				// uiUpdateJob.setPriority(Job.SHORT);
				uiUpdateJob.setSystem(true);
			}
			IWorkbenchSiteProgressService progressService = builder
					.getProgressService();
			if (progressService != null) {
				progressService.schedule(uiUpdateJob, delay);
			} else {
				uiUpdateJob.schedule(delay);
			}
			return uiUpdateJob;
		}
	}

	/**
	 * @return lastUiRefreshTime
	 * 
	 */
	long getLastUIRefreshTime() {
		if (uiUpdateJob != null) {
			return uiUpdateJob.getLastUpdateTime();
		}
		return -1;
	}
	/**
	 * @return true if the UI isUpdating
	 * 
	 */
	boolean isUIUpdating() {
		return uiUpdateJob!=null?uiUpdateJob.isUpdating():false;
	}

	/**
	 * Return the next secondary id that has not been opened for a primary id of
	 * a part.
	 * 
	 * @return part
	 */
	static String newSecondaryID(IViewPart part) {
		while (part.getSite().getPage().findViewReference(
				part.getSite().getId(), String.valueOf(instanceCount)) != null) {
			instanceCount++;
		}

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
				if (marker.getResource().equals(file)
						&& OpenStrategy.activateOnOpen()) {
					page.activate(editor);
				}
			}
		}

		if (marker != null && marker.getResource() instanceof IFile) {
			try {
				IDE.openEditor(page, marker, OpenStrategy.activateOnOpen());
			} catch (PartInitException e) {
				MarkerSupportInternalUtilities.showViewError(e);
			}
		}
	}

	/**
	 * Return The selection listener for the page selection change.
	 * 
	 */
	private class ViewerPageSelectionListener implements ISelectionListener {
		private ExtendedMarkersView view;

		ViewerPageSelectionListener(ExtendedMarkersView view) {
			this.view = view;
		}

		public void selectionChanged(IWorkbenchPart part, ISelection selection) {

			// Do not respond to our own selections or if we are not
			// visible
			if (part == ExtendedMarkersView.this
					|| !(getSite().getPage().isPartVisible(part)))
				return;

			// get Objects to adapt
			List objectsToAdapt = new ArrayList();
			if (part instanceof IEditorPart) {
				IEditorPart editor = (IEditorPart) part;
				objectsToAdapt.add(editor.getEditorInput());
			} else {
				if (selection instanceof IStructuredSelection) {
					for (Iterator iterator = ((IStructuredSelection) selection)
							.iterator(); iterator.hasNext();) {
						Object object = iterator.next();
						objectsToAdapt.add(object);
					}
				}
			}
			// try to adapt them in resources and add it to the
			// selectedElements
			List selectedElements = new ArrayList();
			for (Iterator iterator = objectsToAdapt.iterator(); iterator
					.hasNext();) {
				Object object = iterator.next();
				Object resElement = MarkerResourceUtil.adapt2ResourceElement(object);
				if (resElement != null) {
					selectedElements.add(resElement);
				}
			}
			MarkerContentGenerator generator = view.getGenerator();
			generator.updateSelectedResource(selectedElements.toArray());
		}
		
	}

	/**
	 * Return the undo context associated with operations performed in this view. By default, return
	 * the workspace undo context. Subclasses should override if a more specific undo context should
	 * be used.
	 *
	 * @since 3.7
	 */
	protected IUndoContext getUndoContext() {
		return (IUndoContext)ResourcesPlugin.getWorkspace().getAdapter(IUndoContext.class);
	}

	/**
	 * Returns the name of the delete operation.
	 * 
	 * @param markers the markers to be deleted, must have at least one element
	 * @since 3.7
	 */
	protected String getDeleteOperationName(IMarker[] markers) {
		Assert.isLegal(markers.length > 0);
		return markers.length == 1 ? MarkerMessages.deleteMarker_operationName : MarkerMessages.deleteMarkers_operationName;
	}

}
