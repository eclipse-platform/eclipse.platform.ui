package org.eclipse.debug.internal.ui.launchConfigurations;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/
 
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;

public class LaunchConfigurationWorkingSetMenuContributionItem extends ContributionItem {

	private int fId;
	private IWorkingSet fWorkingSet;
	private LaunchConfigurationWorkingSetActionManager fActionMgr;

	public LaunchConfigurationWorkingSetMenuContributionItem(int id, LaunchConfigurationWorkingSetActionManager actionMgr, IWorkingSet workingSet) {
		super(getId(id));
		fId= id;
		fActionMgr = actionMgr;
		fWorkingSet= workingSet;
	}

	/**
	 * @see org.eclipse.jface.action.IContributionItem#fill(Menu, int)
	 */
	public void fill(Menu menu, int index) {
		MenuItem mi= new MenuItem(menu, SWT.RADIO, index);
		mi.setText("&" + fId + " " + fWorkingSet.getName());  //$NON-NLS-1$  //$NON-NLS-2$
		mi.setSelection(fWorkingSet.equals(fActionMgr.getWorkingSet()));
		mi.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IWorkingSetManager manager= PlatformUI.getWorkbench().getWorkingSetManager();
				fActionMgr.setWorkingSet(fWorkingSet, true);
				manager.addRecentWorkingSet(fWorkingSet);
			}
		});
	}
	
	/**
	 * Overridden to always return true and force dynamic menu building.
	 * 
	 * @see org.eclipse.jface.action.IContributionItem#isDynamic()
	 */
	public boolean isDynamic() {
		return true;
	}

	static String getId(int id) {
		return LaunchConfigurationWorkingSetMenuContributionItem.class.getName() + "." + id;  //$NON-NLS-1$
	}

}
