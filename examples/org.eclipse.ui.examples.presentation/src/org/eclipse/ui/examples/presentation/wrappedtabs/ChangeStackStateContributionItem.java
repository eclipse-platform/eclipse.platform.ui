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
package org.eclipse.ui.examples.presentation.wrappedtabs;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.presentations.IStackPresentationSite;

/**
 * Contributions for supporting maximizing, minimizing, and restoring the state of
 * a stack presentation.
 * 
 * @since 3.0
 */
public class ChangeStackStateContributionItem extends ContributionItem {

	private IStackPresentationSite site;
	
	public ChangeStackStateContributionItem(IStackPresentationSite site) {
		this.site = site;
	}

    public void dispose() {
    	super.dispose();
    }
    
    public void fill(Menu menu, int index) {
		if(site.supportsState(IStackPresentationSite.STATE_MAXIMIZED) && site.getState() != IStackPresentationSite.STATE_MAXIMIZED) {
			MenuItem item1 = new MenuItem(menu, SWT.PUSH, index);
			item1.setText("&Maximize");
			item1.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					site.setState(IStackPresentationSite.STATE_MAXIMIZED);
				}
			});
		}
		if(site.getState() != IStackPresentationSite.STATE_RESTORED)  {
			MenuItem item = new MenuItem(menu, SWT.PUSH, index);
			item.setText("&Restore");
			item.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					site.setState(IStackPresentationSite.STATE_RESTORED);
				}
			});
		}		
    }
    
	public boolean isDynamic() {
		return true;
	}
}
