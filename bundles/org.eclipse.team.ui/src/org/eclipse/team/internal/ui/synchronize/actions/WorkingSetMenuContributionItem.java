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

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.WorkingSetComparator;

/**
 * Menu contribution item which shows all the most recent working
 * sets.
 * 
 * @since 3.0
 */
public class WorkingSetMenuContributionItem extends ContributionItem {
	private WorkingSetFilterActionGroup actionGroup;
	
	/**
	 * Creates a new instance of the receiver.
	 * 
	 * @param id sequential id of the new instance
	 * @param actionGroup the action group this contribution item is created in
	 */
	public WorkingSetMenuContributionItem(String id, WorkingSetFilterActionGroup actionGroup) {
		super(id + TeamUIPlugin.ID + "working_set_contribution"); //$NON-NLS-1$
		Assert.isNotNull(actionGroup);
		this.actionGroup = actionGroup;
	}
	
	/**
	 * Adds a menu item for the working set.
	 * Overrides method from ContributionItem.
	 * 
	 * @see org.eclipse.jface.action.ContributionItem#fill(Menu,int)
	 */
	public void fill(Menu menu, int index) {
		IWorkingSet[] workingSets = PlatformUI.getWorkbench().getWorkingSetManager().getRecentWorkingSets();
		List sortedWorkingSets = Arrays.asList(workingSets);
		Collections.sort(sortedWorkingSets, new WorkingSetComparator());
		
		Iterator iter = sortedWorkingSets.iterator();
		int mruMenuCount = sortedWorkingSets.size();
		int i = 0;
		while (iter.hasNext()) {
			final IWorkingSet workingSet = (IWorkingSet)iter.next();
			if (workingSet != null) {
				MenuItem mi = new MenuItem(menu, SWT.RADIO, index + i);
				mi.setText("&" + (++i) + " " + workingSet.getName());  //$NON-NLS-1$  //$NON-NLS-2$
				mi.setSelection(workingSet.equals(actionGroup.getWorkingSet()));
				mi.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						IWorkingSetManager manager = PlatformUI.getWorkbench().getWorkingSetManager();
						actionGroup.setWorkingSet(workingSet);
						manager.addRecentWorkingSet(workingSet);
					}
				});
			}
		}
	}
	
	/**
	 * Overridden to always return true and force dynamic menu building.
	 */
	public boolean isDynamic() {
		return true;
	}
}

