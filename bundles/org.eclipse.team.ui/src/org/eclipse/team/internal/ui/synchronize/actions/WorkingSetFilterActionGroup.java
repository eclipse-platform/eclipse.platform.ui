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
package org.eclipse.team.internal.ui.synchronize.actions;

import java.util.*;

import org.eclipse.jface.action.*;
import org.eclipse.jface.util.*;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipant;
import org.eclipse.team.ui.synchronize.ISynchronizeView;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.ActionGroup;
import org.eclipse.ui.internal.WorkingSetComparator;

/**
 * Adds working set filter actions (set / clear / edit)
 * 
 * @since 2.1  
 */
public class WorkingSetFilterActionGroup extends ActionGroup {
	public static final String CHANGE_WORKING_SET = "changeWorkingSet"; //$NON-NLS-1$
	
	private IWorkingSet workingSet = null;
	
	private ClearWorkingSetAction clearWorkingSetAction;
	private SelectWorkingSetAction selectWorkingSetAction;
	private EditWorkingSetAction editWorkingSetAction;
	private IPropertyChangeListener workingSetUpdater;
	private int mruMenuCount;
	private IActionBars bars;

	private final String MRULIST_SEPARATOR = "mruList"; //$NON-NLS-1$

	/**
	 * Creates a new instance of the receiver
	 * 
	 * @param shell shell to open dialogs and wizards on
	 * @param workingSetUpdater property change listener notified when a 
	 * 	working set is set
	 */
	public WorkingSetFilterActionGroup(Shell shell, IPropertyChangeListener workingSetUpdater, ISynchronizeView view, ISynchronizeParticipant page) {
		Assert.isNotNull(shell);

		this.workingSetUpdater = workingSetUpdater;
		clearWorkingSetAction = new ClearWorkingSetAction(this);
		selectWorkingSetAction = new SelectWorkingSetAction(this, shell);
		editWorkingSetAction = new EditWorkingSetAction(this, shell);
	}
	/**
	 * Adds actions for the most recently used working sets to the 
	 * specified menu manager.
	 *  
	 * @param menuManager menu manager to add actions to
	 */
	private void addMruWorkingSetActions(IMenuManager menuManager) {
		IWorkingSet[] workingSets = PlatformUI.getWorkbench().getWorkingSetManager().getRecentWorkingSets();
		List sortedWorkingSets = Arrays.asList(workingSets);
		Collections.sort(sortedWorkingSets, new WorkingSetComparator());
		
		Iterator iter = sortedWorkingSets.iterator();
		mruMenuCount = sortedWorkingSets.size();
		int i = mruMenuCount;
		while (iter.hasNext()) {
			IWorkingSet workingSet = (IWorkingSet)iter.next();
			if (workingSet != null) {
				IContributionItem item = new WorkingSetMenuContributionItem(i--, this, workingSet);
				menuManager.prependToGroup(MRULIST_SEPARATOR, item);
			}
		}
	}
	/**
	 * Adds working set actions to the specified action bar.
	 * 
	 * @param actionBars action bar to add working set actions to.
	 * @see ActionGroup#fillActionBars(IActionBars)
	 */
	public void fillActionBars(IActionBars actionBars) {
		bars = actionBars;
		IMenuManager menuManager = actionBars.getMenuManager();
		menuManager.add(selectWorkingSetAction);
		menuManager.add(clearWorkingSetAction);
		menuManager.add(editWorkingSetAction);
		menuManager.add(new Separator(MRULIST_SEPARATOR));
		updateMruList();
	};
	
	public void updateMruList() {
		removePreviousMruWorkingSetActions(bars.getMenuManager());
		addMruWorkingSetActions(bars.getMenuManager());
		bars.updateActionBars();
	}
	
	/**
	 * Removes the most recently used working set actions that were
	 * added to the specified menu.
	 * 
	 * @param menuManager menu manager to remove actions from
	 */
	private void removePreviousMruWorkingSetActions(IMenuManager menuManager) {
		for (int i = 1; i <= mruMenuCount; i++) {
			String id = WorkingSetMenuContributionItem.getId(i);
			if(menuManager.find(id) != null) {
				menuManager.remove(id);
			}
		}
	}
	/**
	 * Returns the working set which is currently selected.
	 * 
	 * @return the working set which is currently selected.
	 */
	public IWorkingSet getWorkingSet() {
		return workingSet;
	}
	/**
	 * Sets the current working set.
	 * 
	 * @param newWorkingSet the new working set
	 */
	public void setWorkingSet(IWorkingSet newWorkingSet) {
		IWorkingSet oldWorkingSet = workingSet;
		
		workingSet = newWorkingSet;
		// Update action
		clearWorkingSetAction.setEnabled(newWorkingSet != null);
		editWorkingSetAction.setEnabled(newWorkingSet != null);
		
		if(workingSet != null)
			PlatformUI.getWorkbench().getWorkingSetManager().addRecentWorkingSet(newWorkingSet);
		// Update viewer
		if (workingSetUpdater != null) {
			workingSetUpdater.propertyChange(
				new PropertyChangeEvent(
					this, 
					WorkingSetFilterActionGroup.CHANGE_WORKING_SET, 
					oldWorkingSet, 
					newWorkingSet));
		}
		updateMruList();
	}	
}