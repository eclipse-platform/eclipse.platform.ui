/************************************************************************
Copyright (c) 2000, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM - Initial implementation
************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.WorkingSetFilterActionGroup;

/**
 * Menu contribution item which shows a working set.
 * 
 * @since 2.1
 */
public class WorkingSetMenuContributionItem extends ContributionItem {
	private int id;
	private IWorkingSet workingSet;
	private WorkingSetFilterActionGroup actionGroup;

	/**
	 * Returns the id of this menu contribution item
	 * 
	 * @param id numerical id
	 * @return String string id
	 */
	public static String getId(int id) {
		return WorkingSetMenuContributionItem.class.getName() + "." + id;  //$NON-NLS-1$
	}
	/**
	 * Creates a new instance of the receiver.
	 * 
	 * @param id sequential id of the new instance
	 * @param actionGroup the action group this contribution item is created in
	 */
	public WorkingSetMenuContributionItem(int id, WorkingSetFilterActionGroup actionGroup, IWorkingSet workingSet) {
		super(getId(id));
		Assert.isNotNull(actionGroup);
		Assert.isNotNull(workingSet);
		this.id = id;
		this.actionGroup = actionGroup;
		this.workingSet = workingSet;
	}
	/**
	 * Adds a menu item for the working set.
	 * Overrides method from ContributionItem.
	 * 
	 * @see org.eclipse.jface.action.ContributionItem#fill(Menu,int)
	 */
	public void fill(Menu menu, int index) {
		MenuItem mi = new MenuItem(menu, SWT.RADIO, index);
		mi.setText("&" + id + " " + workingSet.getName());  //$NON-NLS-1$  //$NON-NLS-2$
		mi.setSelection(workingSet.equals(actionGroup.getWorkingSet()));
		mi.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IWorkingSetManager manager = PlatformUI.getWorkbench().getWorkingSetManager();
				actionGroup.setWorkingSet(workingSet);
				manager.addRecentWorkingSet(workingSet);
			}
		});
	}
	/**
	 * Overridden to always return true and force dynamic menu building.
	 */
	public boolean isDynamic() {
		return true;
	}

}
