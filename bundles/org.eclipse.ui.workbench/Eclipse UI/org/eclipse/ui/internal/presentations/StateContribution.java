/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.presentations;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.presentations.IPresentationSite;
import org.eclipse.ui.presentations.IStackPresentationSite;

/**
 * This contribution item contains the standard set of items that
 * a StackPresentation will contribute to the system menu.
 * 
 * @since 3.0
 */
public class StateContribution extends ContributionItem {
	
	private IStackPresentationSite site;
	
	public StateContribution(IStackPresentationSite site) {
		this.site = site;
	}
	
	public boolean isDynamic() {
		return true;
	}
	
	public void fill(Menu menu, int index) {
		// add view context menu items
		addStateContribution(menu, WorkbenchMessages.getString("PartPane.maximize"), IPresentationSite.STATE_MAXIMIZED); //$NON-NLS-1$
		addStateContribution(menu, WorkbenchMessages.getString("PartPane.restore"), IPresentationSite.STATE_RESTORED); //$NON-NLS-1$
		addStateContribution(menu, WorkbenchMessages.getString("ViewPane.minimizeView"), IPresentationSite.STATE_MINIMIZED); //$NON-NLS-1$
	}	
	
	protected void addStateContribution(Menu menu, String name, final int state) {
		MenuItem item = new MenuItem(menu, SWT.NONE);
		item.setText(WorkbenchMessages.getString(name));
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				site.setState(state);
			}
		});
		item.setEnabled(site.getState() != state);
	}

}
