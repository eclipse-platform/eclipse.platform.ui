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
package org.eclipse.team.internal.ui.sync.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.sync.views.INavigableControl;
import org.eclipse.team.internal.ui.sync.views.SubscriberInput;
import org.eclipse.team.internal.ui.sync.views.SyncViewer;
import org.eclipse.team.ui.sync.AndSyncInfoFilter;
import org.eclipse.team.ui.sync.PseudoConflictFilter;
import org.eclipse.team.ui.sync.SyncInfoChangeTypeFilter;
import org.eclipse.team.ui.sync.SyncInfoDirectionFilter;
import org.eclipse.team.ui.sync.SyncInfoFilter;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IKeyBindingService;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.actions.WorkingSetFilterActionGroup;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;

/**
 * This class managers the actions associated with the SyncViewer class.
 */
public class SyncViewerActions extends SyncViewerActionGroup {
		
	// action groups for view filtering
	private SyncViewerDirectionFilters directionsFilters;
	private SyncViewerChangeFilters changeFilters;
	private SyncViewerComparisonCriteria comparisonCriteria;
	private SyncViewerSubscriberListActions subscriberInputs;
	private SyncViewerSubscriberActions subscriberActions;
	
	private WorkingSetFilterActionGroup workingSetGroup;
	private OpenWithActionGroup openWithActionGroup;
	
	private SyncViewerToolbarDropDownAction chooseSubscriberAction;
	private SyncViewerToolbarDropDownAction chooseChangeFilterAction;
	
	private IWorkingSet workingSet;
	
	// other view actions
	private Action collapseAll;
	private Action refreshSelectionAction;
	private Action toggleViewerType;
	private Action open;
	private ExpandAllAction expandAll;
	private SelectAllAction selectAllAction;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionGroup#updateActionBars()
	 */
	public void updateActionBars() {
		super.updateActionBars();
		changeFilters.updateActionBars();
		directionsFilters.updateActionBars();
		comparisonCriteria.updateActionBars();
		subscriberInputs.updateActionBars();
		subscriberActions.updateActionBars();		
		expandAll.update();
	}

	public SyncViewerActions(SyncViewer viewer) {
		super(viewer);
		createActions();
	}
	
	private void createActions() {
		// initialize action groups
		SyncViewer syncView = getSyncView();
		directionsFilters = new SyncViewerDirectionFilters(syncView, this);
		changeFilters = new SyncViewerChangeFilters(syncView, this);
		subscriberActions = new SyncViewerSubscriberActions(syncView);
		
		// initialize the dropdown for choosing a subscriber
		subscriberInputs = new SyncViewerSubscriberListActions(syncView);
		comparisonCriteria = new SyncViewerComparisonCriteria(syncView);
		chooseSubscriberAction = new ChooseSubscriberAction(this, new SyncViewerActionGroup[] {subscriberInputs, comparisonCriteria});
		
		// initialize the dropdown for choosing a change type filter
		chooseChangeFilterAction = new ChooseChangeFilterAction(this, changeFilters);
		
		// initialize other actions
		refreshSelectionAction = new RefreshAction(this, false);
		refreshSelectionAction.setEnabled(false);
		
		selectAllAction = new SelectAllAction(getSyncView());
		getSyncView().getViewSite().getActionBars().setGlobalActionHandler(ITextEditorActionConstants.SELECT_ALL, selectAllAction);

		
		Action a= new Action() {
			public void  run() {
				getSyncView().collapseAll();
			}
		};
		Utils.initAction(a, "action.collapseAll."); //$NON-NLS-1$
		
		
		expandAll = new ExpandAllAction(this);
		
		IKeyBindingService kbs = getSyncView().getSite().getKeyBindingService();
		a= new Action() {
			public void run() {
				getSyncView().gotoDifference(INavigableControl.NEXT);
			}
		};
		Utils.initAction(a, "action.selectNextChange."); //$NON-NLS-1$
		Utils.registerAction(kbs, a, "org.eclipse.team.ui.syncview.selectNextChange");	//$NON-NLS-1$
		getSyncView().getViewSite().getActionBars().setGlobalActionHandler(IWorkbenchActionConstants.NEXT, a);
		
		a= new Action() {
			public void run() {
				getSyncView().gotoDifference(INavigableControl.PREVIOUS);
			}
		};
		Utils.initAction(a, "action.selectPreviousChange."); //$NON-NLS-1$
		Utils.registerAction(kbs, a, "org.eclipse.team.ui.syncview.selectPreviousChange");	//$NON-NLS-1$
		getSyncView().getViewSite().getActionBars().setGlobalActionHandler(IWorkbenchActionConstants.PREVIOUS, a);
		
		collapseAll = new Action() {
			public void run() {
				getSyncView().collapseAll();
			}
		};
		Utils.initAction(collapseAll, "action.collapseAll."); //$NON-NLS-1$
		
		toggleViewerType = new ToggleViewAction(getSyncView(), getSyncView().getCurrentViewType());
		open = new OpenInCompareAction(syncView);
				
		IPropertyChangeListener workingSetUpdater = new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				String property = event.getProperty();
				
				if (WorkingSetFilterActionGroup.CHANGE_WORKING_SET.equals(property)) {
					Object newValue = event.getNewValue();
					
					if (newValue instanceof IWorkingSet) {	
						setWorkingSet((IWorkingSet) newValue);
					}
					else 
					if (newValue == null) {
						setWorkingSet(null);
					}
				}
			}
		};
		workingSetGroup = new WorkingSetFilterActionGroup(syncView.getSite().getShell(), workingSetUpdater);
		openWithActionGroup = new OpenWithActionGroup(getSyncView());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionGroup#fillActionBars(org.eclipse.ui.IActionBars)
	 */
	public void fillActionBars(IActionBars actionBars) {
		super.fillActionBars(actionBars);
		
		IToolBarManager manager = actionBars.getToolBarManager();
		manager.add(chooseSubscriberAction);
		manager.add(new Separator());
		directionsFilters.fillActionBars(actionBars);
		manager.add(new Separator());
		manager.add(chooseChangeFilterAction);		
		manager.add(collapseAll);
		manager.add(toggleViewerType);
		
		IMenuManager dropDownMenu = actionBars.getMenuManager();
		workingSetGroup.fillActionBars(actionBars);
		dropDownMenu.add(new SyncViewerShowPreferencesAction(getSyncView().getSite().getShell()));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionGroup#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	public void fillContextMenu(IMenuManager manager) {
		super.fillContextMenu(manager);
		
		manager.add(open);
		openWithActionGroup.fillContextMenu(manager);		
		manager.add(new Separator());
		manager.add(expandAll);
		manager.add(new Separator());
		manager.add(refreshSelectionAction);
		// Subscriber menus go here
		subscriberActions.fillContextMenu(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator("Additions")); //$NON-NLS-1$
	}

	public void refreshFilters() {
	final SubscriberInput input = getSubscriberContext();
	if(input != null) {
		try {
			input.setFilter(new AndSyncInfoFilter(
			new SyncInfoFilter[] {
				new SyncInfoDirectionFilter(directionsFilters.getDirectionFilter()), 
				new SyncInfoChangeTypeFilter(changeFilters.getChangeFilters()),
				new PseudoConflictFilter()
			}), new NullProgressMonitor());
			} catch (TeamException e) {
				
			}
		}
	}
	
	public void open() {
		open.run();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.team.ccvs.syncviews.actions.SyncViewerActionGroup#restore(org.eclipse.ui.IMemento)
	 */
	public void restore(IMemento memento) {
		if(memento == null) return;
		super.restore(memento);
		changeFilters.restore(memento);
		directionsFilters.restore(memento);
		comparisonCriteria.restore(memento);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ccvs.syncviews.actions.SyncViewerActionGroup#save(org.eclipse.ui.IMemento)
	 */
	public void save(IMemento memento) {
		if(memento == null) return;
		super.save(memento);
		changeFilters.save(memento);
		directionsFilters.save(memento);
		comparisonCriteria.save(memento);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.sync.actions.SyncViewerActionGroup#initializeActions()
	 */
	protected void initializeActions() {
		SubscriberInput input = getSubscriberContext();
		refreshSelectionAction.setEnabled(input != null);
		// This is invoked before the subscriber input is initialized
		if (input.getWorkingSet() == null) {
			// set the input to use the last selected working set
			input.setWorkingSet(getWorkingSet());
		} else {
			// set the menu to select the set from the input
			// the callback will not prepare the input since the set
			// for the input is the same as the one being passed to the menu
			workingSetGroup.setWorkingSet(getWorkingSet());
		}
		
		// refresh the selected filter
		refreshFilters();
	}
	
	/* (non-Javadoc)
	 * @see ActionGroup#setContext(org.eclipse.ui.actions.ActionContext)
	 */
	public void setContext(ActionContext context) {
		changeFilters.setContext(context);
		directionsFilters.setContext(context);
		comparisonCriteria.setContext(context);
		subscriberInputs.setContext(context);
		subscriberActions.setContext(context);
		openWithActionGroup.setContext(context);
		
		// causes initializeActions to be called. Must be called after
		// setting the context for contained groups.
		super.setContext(context);
	}
	
	/* (non-Javadoc)
	 * @see ActionGroup#setContext(org.eclipse.ui.actions.ActionContext)
	 */
	public void addContext(ActionContext context) {
		subscriberInputs.addContext(context);
	}
	
	public void removeContext(ActionContext context) {
		subscriberInputs.removeContext(context);	
	}
	
	/*
	 * Get the selected working set from the subscriber input
	 */
	private IWorkingSet getWorkingSet() {
		SubscriberInput input = getSubscriberContext();
		// There's no subscriber input so use the last selected workingSet
		if (input == null) return workingSet;
		IWorkingSet set = input.getWorkingSet();
		// There's no subscriber working set so use the last selected workingSet
		if (set == null ) return workingSet;
		return set;
	}
	
	protected void setWorkingSet(IWorkingSet set) {
		// Keep track of the last working set selected
		if (set != null) workingSet = set;
		final SubscriberInput input = getSubscriberContext();
		if (input == null) return;
		if (workingSetsEqual(input.getWorkingSet(), set)) return;
		input.setWorkingSet(set);
		getSyncView().run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				try {
					// when the working set changes, recalculate the entire sync set based on
					// the new input.
					input.prepareInput(monitor);
					getSyncView().updateTitle();
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				}
			}
		});
	}

	private boolean workingSetsEqual(IWorkingSet set, IWorkingSet set2) {
		if (set == null && set2 == null) return true;
		if (set == null || set2 == null) return false;
		return set.equals(set2);
	}
}
