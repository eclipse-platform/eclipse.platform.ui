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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.team.core.subscribers.SyncInfo;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.sync.views.SyncViewer;
import org.eclipse.ui.IMemento;

/**
 * This class provides a set of actions that support sync set filtering by 
 * change type. Changing the change type only requires setting a new
 * filter on the sync set.
 */
public class SyncViewerChangeFilters extends SyncViewerActionGroup {
	
	private static final String MEMENTO_KEY_PREFIX = "SyncViewerChangeFilters"; //$NON-NLS-1$
	
	// array of actions for filtering by change type (additions, deletions and changes)
	private ChangeFilterAction[] actions;	
	private SyncViewerActions actionGroup;
	
	/**
	 * Action for filtering by change type.
	 */
	class ChangeFilterAction extends Action {
		// The SyncInfo change constant associated with the change type
		private int changeFilter;
		public ChangeFilterAction(String prefix, int changeFilter) {
			this.changeFilter = changeFilter;
			Utils.initAction(this, prefix);
		}
		public void run() {
			refreshFilters();
		}
		public int getChangeFilter() {
			return changeFilter;
		}
	}
	
	protected SyncViewerChangeFilters(SyncViewer viewer, SyncViewerActions actionGroup) {
		super(viewer);
		this.actionGroup = actionGroup;
		createActions();
	}
	
	private void createActions() {
		ChangeFilterAction additions = new ChangeFilterAction("action.changeFilterShowAdditions.", SyncInfo.ADDITION); //$NON-NLS-1$
		additions.setChecked(true);
		ChangeFilterAction deletions = new ChangeFilterAction("action.changeFilterShowDeletions.", SyncInfo.DELETION); //$NON-NLS-1$
		deletions.setChecked(true);
		ChangeFilterAction changes = new ChangeFilterAction("action.changeFilterShowChanges.", SyncInfo.CHANGE); //$NON-NLS-1$
		changes.setChecked(true);
		actions = new ChangeFilterAction[] { additions, deletions, changes };
	}

	/**
	 * Get the current set of active change filters
	 */
	public int[] getChangeFilters() {
		// Determine how many change types are checked
		int count = 0;
		for (int i = 0; i < actions.length; i++) {
			ChangeFilterAction action = actions[i];
			if (action.isChecked()) {
				count++;
			}
		}
		// Create an array of checked change types
		int[] changeFilters = new int[count];
		count = 0;
		for (int i = 0; i < actions.length; i++) {
			ChangeFilterAction action = actions[i];
			if (action.isChecked()) {
				changeFilters[count++] = action.getChangeFilter();
			}
		}
		return changeFilters;
	}
	
	public void fillMenu(SyncViewerToolbarDropDownAction action) {
		super.fillMenu(action);
		for (int i = 0; i < actions.length; i++) {
			action.add(actions[i]);			
		}
	}

	/**
	 * Return all the actions for filtering by change type
	 * @return
	 */
	public Action[] getFilters() {
		return actions;
	}

	/**
	 * Return all the active change types
	 * @return
	 */
	public Action[] getActiveFilters() {
		List result = new ArrayList();
		for (int i = 0; i < actions.length; i++) {
			Action action = actions[i];
			if (action.isChecked()) {
				result.add(action);
			}
		}
		return (Action[]) result.toArray(new Action[result.size()]);
	}

	/**
	 * Change the active change types to those in the provided array
	 * @param results
	 */
	public void setActiveFilters(Object[] results) {
		for (int i = 0; i < actions.length; i++) {
			Action action = actions[i];
			boolean active = false;
			for (int j = 0; j < results.length; j++) {
				Object object = results[j];
				if (object == action) {
					active = true;
					break;
				}
			}
			action.setChecked(active);
		}
	}
	
	public void setAllEnabled() {
		for (int i = 0; i < actions.length; i++) {
			actions[i].setChecked(true);
		}
	}

	/* (non-Javadoc)
	 * @see SyncViewerActionGroup#restore(org.eclipse.ui.IMemento)
	 */
	public void restore(IMemento memento) {
		super.restore(memento);
		// Uncheck everything
		setActiveFilters(new ChangeFilterAction[0]);
		// Check those in the memento
		Integer i;
		int count = 0;
		do {
			i = memento.getInteger(MEMENTO_KEY_PREFIX + "." + count); //$NON-NLS-1$
			if (i != null) {
				count++;
				actions[i.intValue()].setChecked(true);
			}
		} while (i != null);
		
		// Make sure at least one is checked
		if (count == 0) {
			setActiveFilters(actions);
		}
	}

	/* (non-Javadoc)
	 * 
	 * Save each active change filter as follows:
	 * 
	 *    MEMENTO_KEY_PREFIX.0 = ?
	 *    MEMENTO_KEY_PREFIX.1 = ?
	 * 
	 * @see org.eclipse.team.ccvs.syncviews.actions.SyncViewerActionGroup#save(org.eclipse.ui.IMemento)
	 */
	public void save(IMemento memento) {
		super.save(memento);
		int count = 0;
		for (int i = 0; i < actions.length; i++) {
			Action action = actions[i];
			if (action.isChecked()) {
				memento.putInteger(MEMENTO_KEY_PREFIX + "." + count++, i); //$NON-NLS-1$
			}
		}
	}
	
	public void refreshFilters() {
		actionGroup.refreshFilters();
	}
}
