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

import org.eclipse.jface.action.*;
import org.eclipse.jface.util.*;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.ui.synchronize.ISynchronizeView;
import org.eclipse.team.ui.synchronize.subscribers.SubscriberParticipant;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.ActionGroup;

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
	private IActionBars bars;
	private IContributionItem item;
	private String id;

	/**
	 * Creates a new instance of the receiver
	 * 
	 * @param shell shell to open dialogs and wizards on
	 * @param workingSetUpdater property change listener notified when a 
	 * 	working set is set
	 */
	public WorkingSetFilterActionGroup(Shell shell, IPropertyChangeListener workingSetUpdater, ISynchronizeView view, SubscriberParticipant participant) {
		Assert.isNotNull(shell);
		this.id = participant.toString();
		this.workingSetUpdater = workingSetUpdater;
		this.workingSet = participant.getWorkingSet();
		clearWorkingSetAction = new ClearWorkingSetAction(this);
		selectWorkingSetAction = new SelectWorkingSetAction(this, shell);
		editWorkingSetAction = new EditWorkingSetAction(this, shell);
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
		menuManager.add(new Separator(id));
		updateMruContribution(menuManager);
	};
	
	private void updateMruContribution(IMenuManager menuManager) {
		IWorkingSet[] sets = PlatformUI.getWorkbench().getWorkingSetManager().getRecentWorkingSets();
		if(sets.length > 0) {
			if(item == null) {
				item = new WorkingSetMenuContributionItem(id, this);
				menuManager.prependToGroup(id, item);
			}
		} else {
			if(item != null) {
				menuManager.remove(item);
			}
			item = null;
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
		
		// Trick to get dynamic menu contribution for most-recent list to
		// be updated. These are action contributions and must be added/removed
		// before the menu is shown.
		// It is also quite possible that this menu hasn't been created when a
		// setWorking set property change occurs.
		if(bars.getMenuManager().find(id) != null) {
			updateMruContribution(bars.getMenuManager());
		}
		//bars.updateActionBars();
	}	
}