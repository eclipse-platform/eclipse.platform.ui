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
import org.eclipse.ui.presentations.IPresentablePart;

/**
 * @since 3.0
 */
public class CloseContributionItem extends ContributionItem {
	private WrappedTabsPartPresentation presentation; 
	
	private SelectionAdapter selectionListener = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			presentation.close(new IPresentablePart[] {presentation.getCurrent()});
		}
	};
	
	public CloseContributionItem(WrappedTabsPartPresentation presentation) {
		this.presentation = presentation;
	}

    public void dispose() {
    	super.dispose();
        presentation = null;
        selectionListener = null;
    }
    
    public void fill(Menu menu, int index) {
		MenuItem item = new MenuItem(menu, SWT.NONE, index);
		item.setText("Close");
		item.addSelectionListener(selectionListener);
		
		IPresentablePart current = presentation.getCurrent();
		item.setEnabled(current != null);
    }
    
    public boolean isDynamic() {
        return true;
    }
}
