/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.ui.synchronize;

import java.util.Iterator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.*;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.SyncInfo;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.internal.ui.jobs.JobBusyCursor;
import org.eclipse.team.internal.ui.synchronize.actions.*;
import org.eclipse.team.internal.ui.synchronize.sets.SubscriberInput;
import org.eclipse.team.internal.ui.synchronize.views.*;
import org.eclipse.team.ui.synchronize.actions.SubscriberAction;
import org.eclipse.team.ui.synchronize.actions.SyncInfoFilter;
import org.eclipse.ui.*;
import org.eclipse.ui.part.*;
import org.eclipse.ui.views.navigator.ResourceSorter;

/**
 * A synchronize view page that works with participants that are subclasses of 
 * {@link TeamSubscriberParticipant}. It shows changes in the tree or table view
 * and supports navigation, opening, and filtering changes.
 * <p>
 * Clients can subclass to extend the label decoration or add action bar 
 * contributions. For more extensive modifications, clients should create
 * their own custom control.
 * </p> 
 * @since 3.0
 */
public class TeamSubscriberParticipantPage implements IPageBookViewPage, IPropertyChangeListener {
	// The viewer that is shown in the view. Currently this can be either a table or tree viewer.
	private StructuredViewer viewer;
	
	// Parent composite of this view. It is remembered so that we can dispose of its children when 
	// the viewer type is switched.
	private Composite composite = null;
	private boolean settingWorkingSet = false;
	
	// Viewer type constants
	private int layout;
	
	// Remembering the current input and the previous.
	private SubscriberInput input = null;
	
	// A set of common actions. They are hooked to the active SubscriberInput and must 
	// be reset when the input changes.
	// private SyncViewerActions actions;
	
	private JobBusyCursor busyCursor;
	private ISynchronizeView view;
	private TeamSubscriberParticipant participant;
	private IPageSite site;
	
	public final static int[] INCOMING_MODE_FILTER = new int[] {SyncInfo.CONFLICTING, SyncInfo.INCOMING};
	public final static int[] OUTGOING_MODE_FILTER = new int[] {SyncInfo.CONFLICTING, SyncInfo.OUTGOING};
	public final static int[] BOTH_MODE_FILTER = new int[] {SyncInfo.CONFLICTING, SyncInfo.INCOMING, SyncInfo.OUTGOING};
	public final static int[] CONFLICTING_MODE_FILTER = new int[] {SyncInfo.CONFLICTING};
	
	// Actions
	private OpenWithActionGroup openWithActions;
	private NavigateAction gotoNext;
	private NavigateAction gotoPrevious;
	private Action toggleLayoutTree;
	private Action toggleLayoutTable;
	private RefactorActionGroup refactorActions;
	private SyncViewerShowPreferencesAction showPreferences;
	private RefreshAction refreshAction;
	private ComparisonCriteriaActionGroup comparisonCriteria;
	private Action collapseAll;
	private Action expandAll;
	private WorkingSetFilterActionGroup workingSetGroup;
	private StatusLineContributionGroup statusLine;
	
	/**
	 * Constructs a new SynchronizeView.
	 */
	public TeamSubscriberParticipantPage(TeamSubscriberParticipant page, ISynchronizeView view, SubscriberInput input) {
		this.participant = page;
		this.view = view;
		this.input = input;
		layout = getStore().getInt(IPreferenceIds.SYNCVIEW_VIEW_TYPE);
		if (layout != TeamSubscriberParticipant.TREE_LAYOUT) {
			layout = TeamSubscriberParticipant.TABLE_LAYOUT;
		}		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.IPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		composite = new Composite(parent, SWT.NONE); 
		GridLayout gridLayout= new GridLayout();
		gridLayout.makeColumnsEqualWidth= false;
		gridLayout.marginWidth= 0;
		gridLayout.marginHeight = 0;
		gridLayout.verticalSpacing = 0;
		composite.setLayout(gridLayout);
		
		// Create the busy cursor with no control to start with (createViewer will set it)
		busyCursor = new JobBusyCursor(null /* control */, SubscriberAction.SUBSCRIBER_JOB_TYPE);
		createViewer(composite);
				
		// create actions
		openWithActions = new OpenWithActionGroup(this);
		refactorActions = new RefactorActionGroup(view);
		gotoNext = new NavigateAction(view, this, INavigableControl.NEXT);		
		gotoPrevious = new NavigateAction(view, this, INavigableControl.PREVIOUS);
		comparisonCriteria = new ComparisonCriteriaActionGroup(input);
		
		toggleLayoutTable = new ToggleViewLayoutAction(participant, TeamSubscriberParticipant.TABLE_LAYOUT);
		toggleLayoutTree = new ToggleViewLayoutAction(participant, TeamSubscriberParticipant.TREE_LAYOUT);
		workingSetGroup = new WorkingSetFilterActionGroup(getSite().getShell(), this, view, participant);
		
		showPreferences = new SyncViewerShowPreferencesAction(view.getSite().getShell());
		
		refreshAction = new RefreshAction(getSite().getPage(), getParticipant(), true /* refresh all */);
		statusLine = new StatusLineContributionGroup(this.input);
		
		collapseAll = new Action() {
			public void run() {
				collapseAll();
			}
		};
		Utils.initAction(collapseAll, "action.collapseAll."); //$NON-NLS-1$
		
		expandAll = new Action() {
			public void run() {
				Viewer viewer = getViewer();
				ISelection selection = viewer.getSelection();
				if(viewer instanceof AbstractTreeViewer && ! selection.isEmpty()) {
					Iterator elements = ((IStructuredSelection)selection).iterator();
					while (elements.hasNext()) {
						Object next = elements.next();
						((AbstractTreeViewer) viewer).expandToLevel(next, AbstractTreeViewer.ALL_LEVELS);
					}
				}
			}
		};
		Utils.initAction(expandAll, "action.expandAll."); //$NON-NLS-1$
				
		participant.addPropertyChangeListener(this);
		TeamUIPlugin.getPlugin().getPreferenceStore().addPropertyChangeListener(this);
		updateMode(participant.getMode());		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
	 */
	public void init(IPageSite site) throws PartInitException {
		this.site = site;		
	}
	
	private void hookContextMenu() {
		if(getViewer() != null) {
			MenuManager menuMgr = new MenuManager(participant.getId()); //$NON-NLS-1$
			menuMgr.setRemoveAllWhenShown(true);
			menuMgr.addMenuListener(new IMenuListener() {
				public void menuAboutToShow(IMenuManager manager) {
					setContextMenu(manager);
				}
			});
			Menu menu = menuMgr.createContextMenu(viewer.getControl());
			viewer.getControl().setMenu(menu);			
			getSite().registerContextMenu(participant.getId(), menuMgr, viewer);
		}
	}	

	private void setContextMenu(IMenuManager manager) {
		openWithActions.fillContextMenu(manager);
		refactorActions.fillContextMenu(manager);
		manager.add(new Separator());
		manager.add(expandAll);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	/**
	 * Toggles between label/tree/table viewers. 
	 */
	public void switchViewerType(int viewerType) {
		if(viewer == null || viewerType != layout) {
			if (composite == null || composite.isDisposed()) return;
			IStructuredSelection oldSelection = null;
			if(viewer != null) {
				oldSelection = (IStructuredSelection)viewer.getSelection();
			}
			layout = viewerType;
			getStore().setValue(IPreferenceIds.SYNCVIEW_VIEW_TYPE, layout);
			disposeChildren(composite);
			createViewer(composite);
			composite.layout();
			if(oldSelection == null || oldSelection.size() == 0) {
				//gotoDifference(INavigableControl.NEXT);
			} else {
				viewer.setSelection(oldSelection, true);
			}
		}
	}
	
	/**
	 * Adds the listeners to the viewer.
	 */
	private void initializeListeners() {
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateStatusLine((IStructuredSelection)event.getSelection());
			}
		});
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				handleDoubleClick(event);
			}
		});
		viewer.addOpenListener(new IOpenListener() {
			public void open(OpenEvent event) {
				handleOpen(event);
			}
		});
	}	
	
	private void createViewer(Composite parent) {				
		//tbMgr.createControl(parent);
		switch(layout) {
			case TeamSubscriberParticipant.TREE_LAYOUT:
				createTreeViewerPartControl(parent); 
				break;
			case TeamSubscriberParticipant.TABLE_LAYOUT:
				createTableViewerPartControl(parent); 
				break;
		}		
		viewer.setInput(input);
		viewer.getControl().setFocus();
		initializeListeners();
		hookContextMenu();
		getSite().setSelectionProvider(getViewer());
		busyCursor.setControl(viewer.getControl());
	}
	
	protected ILabelProvider getLabelProvider() {
		return new TeamSubscriberParticipantLabelProvider();		
	}
	
	private void createTreeViewerPartControl(Composite parent) {
		GridData data = new GridData(GridData.FILL_BOTH);
		viewer = new SyncTreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setLabelProvider(getLabelProvider());
		viewer.setSorter(new SyncViewerSorter(ResourceSorter.NAME));
		((TreeViewer)viewer).getTree().setLayoutData(data);
	}
	
	private void createTableViewerPartControl(Composite parent) {
		// Create the table
		Table table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		GridData data = new GridData(GridData.FILL_BOTH);
		table.setLayoutData(data);
		
		// Set the table layout
		TableLayout layout = new TableLayout();
		table.setLayout(layout);
		
		// Create the viewer
		TableViewer tableViewer = new SyncTableViewer(table);
		
		// Create the table columns
		createColumns(table, layout, tableViewer);
		
		// Set the table contents
		viewer = tableViewer;
		viewer.setContentProvider(new SyncSetTableContentProvider());
		viewer.setLabelProvider(getLabelProvider());		
		viewer.setSorter(new SyncViewerTableSorter());
	}
	
	/**
	 * Creates the columns for the sync viewer table.
	 */
	private void createColumns(Table table, TableLayout layout, TableViewer viewer) {
		SelectionListener headerListener = SyncViewerTableSorter.getColumnListener(viewer);
		// revision
		TableColumn col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setText(Policy.bind("TeamSubscriberParticipantPage.7")); //$NON-NLS-1$
		col.addSelectionListener(headerListener);
		layout.addColumnData(new ColumnWeightData(30, true));
		
		// tags
		col = new TableColumn(table, SWT.NONE);
		col.setResizable(true);
		col.setText(Policy.bind("TeamSubscriberParticipantPage.8")); //$NON-NLS-1$
		col.addSelectionListener(headerListener);
		layout.addColumnData(new ColumnWeightData(50, true));
	}
	
	private void disposeChildren(Composite parent) {
		// Null out the control of the busy cursor while we are switching viewers
		busyCursor.setControl(null);
		Control[] children = parent.getChildren();
		for (int i = 0; i < children.length; i++) {
			Control control = children[i];
			control.dispose();
		}
	}
	
	private void handleOpen(OpenEvent event) {
		openWithActions.openInCompareEditor();
	}
	/**
	 * Handles a double-click event from the viewer.
	 * Expands or collapses a folder when double-clicked.
	 * 
	 * @param event the double-click event
	 * @since 2.0
	 */
	private void handleDoubleClick(DoubleClickEvent event) {
		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		Object element = selection.getFirstElement();	
		// Double-clicking should expand/collapse containers
		if (viewer instanceof TreeViewer) {
			TreeViewer tree = (TreeViewer)viewer;
			if (tree.isExpandable(element)) {
				tree.setExpandedState(element, !tree.getExpandedState(element));
			}
		}		
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.IPage#setFocus()
	 */
	public void setFocus() {
		if (viewer == null) return;
		viewer.getControl().setFocus();
	}
	
	public StructuredViewer getViewer() {
		return viewer;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		busyCursor.dispose();
		statusLine.dispose();
	}
	
	/*
	 * Return the current input for the view.
	 */
	public SubscriberInput getInput() {
		return input;
	}
	
	public void collapseAll() {
		if (viewer == null || !(viewer instanceof AbstractTreeViewer)) return;
		viewer.getControl().setRedraw(false);		
		((AbstractTreeViewer)viewer).collapseToLevel(viewer.getInput(), TreeViewer.ALL_LEVELS);
		viewer.getControl().setRedraw(true);
	}

	/**
	 * This method enables "Show In" support for this view
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class key) {
		if (key == IShowInSource.class) {
			return new IShowInSource() {
				public ShowInContext getShowInContext() {
					StructuredViewer v = getViewer();
					if (v == null) return null;
					return new ShowInContext(null, v.getSelection());
				}
			};
		}
		return null;
	}
	
	/**
	 * Updates the message shown in the status line.
	 *
	 * @param selection the current selection
	 */
	private void updateStatusLine(IStructuredSelection selection) {
		String msg = getStatusLineMessage(selection);
		getSite().getActionBars().getStatusLineManager().setMessage(msg);
	}
	
	/**
	 * Returns the message to show in the status line.
	 *
	 * @param selection the current selection
	 * @return the status line message
	 * @since 2.0
	 */
	private String getStatusLineMessage(IStructuredSelection selection) {
		if (selection.size() == 1) {
			IResource resource = getResource(selection.getFirstElement());
			if (resource == null) {
				return Policy.bind("SynchronizeView.12"); //$NON-NLS-1$
			} else {
				return resource.getFullPath().makeRelative().toString();
			}
		}
		if (selection.size() > 1) {
			return selection.size() + Policy.bind("SynchronizeView.13"); //$NON-NLS-1$
		}
		return ""; //$NON-NLS-1$
	}
	
	private IResource getResource(Object object) {
		return SyncSetContentProvider.getResource(object);
	}

	public void selectAll() {
		Viewer viewer = getViewer();
		if (viewer instanceof TableViewer) {
			TableViewer table = (TableViewer)viewer;
			table.getTable().selectAll();
		} else {
			// Select All in a tree doesn't really work well
		}
	}
	
	private IPreferenceStore getStore() {
		return TeamUIPlugin.getPlugin().getPreferenceStore();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.IPage#getControl()
	 */
	public Control getControl() {
		return composite;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.IPage#setActionBars(org.eclipse.ui.IActionBars)
	 */
	public void setActionBars(IActionBars actionBars) {
		if(actionBars != null) {
			IToolBarManager manager = actionBars.getToolBarManager();			
			
			// toolbar
			manager.add(refreshAction);
			manager.add(comparisonCriteria);
			manager.add(new Separator());		
			manager.add(gotoNext);
			manager.add(gotoPrevious);
			manager.add(collapseAll);
			manager.add(new Separator());

			// view menu
			updateViewMenu(actionBars);
			
			// status line
			statusLine.fillActionBars(actionBars);
		}		
	}

	private void updateViewMenu(IActionBars actionBars) {
		IMenuManager menu = actionBars.getMenuManager();
		MenuManager layoutMenu = new MenuManager(Policy.bind("action.layout.label")); //$NON-NLS-1$		
		layoutMenu.add(toggleLayoutTable);
		layoutMenu.add(toggleLayoutTree);
		workingSetGroup.fillActionBars(actionBars);
		menu.add(new Separator());
		menu.add(layoutMenu);
		menu.add(new Separator());
		menu.add(showPreferences);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.IPageBookViewPage#getSite()
	 */
	public IPageSite getSite() {
		return this.site;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		// Layout change
		if(event.getProperty().equals(TeamSubscriberParticipant.P_SYNCVIEWPAGE_LAYOUT)) {
			switchViewerType(((Integer)event.getNewValue()).intValue());
		// Direction mode change
		} else if(event.getProperty().equals(TeamSubscriberParticipant.P_SYNCVIEWPAGE_MODE)) {
			updateMode(((Integer)event.getNewValue()).intValue());
		// Working set changed via menu selection - notify participant and
		// do all the real work when we get the next workset changed event
		} else if(event.getProperty().equals(WorkingSetFilterActionGroup.CHANGE_WORKING_SET)) {
			if(settingWorkingSet) return;
			settingWorkingSet = true;
			participant.setWorkingSet((IWorkingSet)event.getNewValue());
			settingWorkingSet = false;
		// Working set changed programatically
		} else if(event.getProperty().equals(TeamSubscriberParticipant.P_SYNCVIEWPAGE_WORKINGSET)) {
			if(settingWorkingSet) return;
			settingWorkingSet = true;
			Object newValue = event.getNewValue();
			if (newValue instanceof IWorkingSet) {	
				workingSetGroup.setWorkingSet((IWorkingSet)newValue);
			} else if (newValue == null) {
				workingSetGroup.setWorkingSet(null);
			}
			settingWorkingSet = false;
		// Change to showing of sync state in text labels preference
		} else if(event.getProperty().equals(IPreferenceIds.SYNCVIEW_VIEW_SYNCINFO_IN_LABEL)) {
			getViewer().refresh(true /* update labels */);
		}
	}

	private void updateMode(int mode) {
		int[] modeFilter = BOTH_MODE_FILTER;
		switch(mode) {
			case TeamSubscriberParticipant.INCOMING_MODE:
				modeFilter = INCOMING_MODE_FILTER; break;
			case TeamSubscriberParticipant.OUTGOING_MODE:
				modeFilter = OUTGOING_MODE_FILTER; break;
			case TeamSubscriberParticipant.BOTH_MODE:
				modeFilter = BOTH_MODE_FILTER; break;
			case TeamSubscriberParticipant.CONFLICTING_MODE:
				modeFilter = CONFLICTING_MODE_FILTER; break;
		}
		try {
			input.setFilter(
					new SyncInfoFilter.AndSyncInfoFilter(
						new SyncInfoFilter[] {
						   new SyncInfoFilter.SyncInfoDirectionFilter(modeFilter), 
						   new SyncInfoFilter.SyncInfoChangeTypeFilter(new int[] {SyncInfo.ADDITION, SyncInfo.DELETION, SyncInfo.CHANGE}),
						   new SyncInfoFilter.PseudoConflictFilter()
			}), new NullProgressMonitor());
		} catch (TeamException e) {
			Utils.handleError(getSite().getShell(), e, Policy.bind("SynchronizeView.16"), e.getMessage()); //$NON-NLS-1$
		}
	}
	
	/**
	 * @return Returns the participant.
	 */
	public TeamSubscriberParticipant getParticipant() {
		return participant;
	}
	
	/**
	 * @return Returns the view.
	 */
	public ISynchronizeView getSynchronizeView() {
		return view;
	}
}