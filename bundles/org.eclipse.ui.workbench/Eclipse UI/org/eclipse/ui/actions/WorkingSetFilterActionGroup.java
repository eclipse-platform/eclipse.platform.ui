/************************************************************************
Copyright (c) 2000, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.actions;

import java.util.*;

import org.eclipse.jface.action.*;
import org.eclipse.jface.util.*;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.WorkingSetComparator;
import org.eclipse.ui.internal.WorkingSetMenuContributionItem;
import org.eclipse.ui.internal.actions.*;

/**
 * Adds working set filter actions (set / clear / edit)
 * 
 * @since 2.1 
 */
public class WorkingSetFilterActionGroup extends ActionGroup {
	public static final String CHANGE_WORKING_SET = "changeWorkingSet"; //$NON-NLS-1$
	
	private static final String SEPARATOR_ID = "workingSetGroupSeparator"; //$NON-NLS-1$

	private IWorkingSet workingSet = null;
	
	private ClearWorkingSetAction clearWorkingSetAction;
	private SelectWorkingSetAction selectWorkingSetAction;
	private EditWorkingSetAction editWorkingSetAction;
	
	private IPropertyChangeListener workingSetUpdater;
	
	private int mruMenuCount;
	private IMenuManager menuManager;
	private IMenuListener menuListener;

	/**
	 * Creates a new instance of the receiver
	 * 
	 * @param shell shell to open dialogs and wizards on
	 * @param workingSetUpdater property change listener notified when a 
	 * 	working set is set
	 */
	public WorkingSetFilterActionGroup(Shell shell, IPropertyChangeListener workingSetUpdater) {
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
		mruMenuCount = 0;
		while (iter.hasNext()) {
			IWorkingSet workingSet = (IWorkingSet)iter.next();
			if (workingSet != null) {
				IContributionItem item = new WorkingSetMenuContributionItem(++mruMenuCount, this, workingSet);
				menuManager.insertBefore(SEPARATOR_ID, item);
			}
		}
	}
	/**
	 * Removes the menu listener
	 * 
	 * @see ActionGroup#dispose()
	 */
	public void dispose() {
		if (menuManager != null)
			menuManager.removeMenuListener(menuListener);
		super.dispose();
	}
	/**
	 * Adds working set actions to the specified action bar.
	 * 
	 * @param actionBars action bar to add working set actions to.
	 * @see ActionGroup#fillActionBars(IActionBars)
	 */
	public void fillActionBars(IActionBars actionBars) {
		menuManager = actionBars.getMenuManager();
		menuManager.add(selectWorkingSetAction);
		menuManager.add(clearWorkingSetAction);
		menuManager.add(editWorkingSetAction);
		menuManager.add(new Separator());
		menuManager.add(new Separator(SEPARATOR_ID));
		
		menuListener = new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				removePreviousMruWorkingSetActions(manager);
				addMruWorkingSetActions(manager);
			}
		};
		menuManager.addMenuListener(menuListener);
	};
	/**
	 * Returns the working set which is currently selected.
	 * 
	 * @return the working set which is currently selected.
	 */
	public IWorkingSet getWorkingSet() {
		return workingSet;
	}
	/**
	 * Removes the most recently used working set actions that were
	 * added to the specified menu.
	 * 
	 * @param menuManager menu manager to remove actions from
	 */
	private void removePreviousMruWorkingSetActions(IMenuManager menuManager) {
		for (int i = 1; i <= mruMenuCount; i++)
			menuManager.remove(WorkingSetMenuContributionItem.getId(i));
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
		
		// Update viewer
		if (workingSetUpdater != null) {
			workingSetUpdater.propertyChange(
				new PropertyChangeEvent(
					this, 
					WorkingSetFilterActionGroup.CHANGE_WORKING_SET, 
					oldWorkingSet, 
					newWorkingSet));
		}
	}
	

}