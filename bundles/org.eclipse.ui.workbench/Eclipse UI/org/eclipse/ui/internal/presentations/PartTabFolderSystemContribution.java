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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.internal.PartPane;
import org.eclipse.ui.internal.ViewPane;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.presentations.IStackPresentationSite;

/**
 * @since 3.0
 */
public class PartTabFolderSystemContribution extends PartPaneSystemContribution {

	/**
	 * @param site
	 */
	public PartTabFolderSystemContribution(IStackPresentationSite site) {
		super(site);
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IContributionItem#fill(org.eclipse.swt.widgets.Menu, int)
	 */
	public void fill(Menu menu, int index) {
        if (fastViewsEnabled() && getSite().isMoveable(getPart())) {
            addFastViewMenuItem(menu);
        }
		super.fill(menu, index);
	}

	private boolean fastViewsEnabled() {
		WorkbenchWindow window = (WorkbenchWindow) getPane().getPage().getWorkbenchWindow();
		return window.getFastViewBar() != null;
	}
    protected void addFastViewMenuItem(Menu menu) {
        MenuItem item = new MenuItem(menu, SWT.NONE);
		item.setText(WorkbenchMessages.getString("ViewPane.fastView")); //$NON-NLS-1$
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				PartPane pane = getPane();
				
				if (pane instanceof ViewPane) {
					((ViewPane)pane).doMakeFast();
				}
			}
		});
    }
}
