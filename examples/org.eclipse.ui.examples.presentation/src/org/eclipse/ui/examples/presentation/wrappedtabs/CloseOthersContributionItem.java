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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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
public class CloseOthersContributionItem extends ContributionItem {
	private WrappedTabsPartPresentation presentation; 
	
	private SelectionAdapter selectionListener = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			IPresentablePart current = presentation.getCurrent();
			List parts = Arrays.asList(presentation.getParts());
			List otherParts = new LinkedList();
			otherParts.addAll(parts);
			otherParts.remove(current);
			presentation.close((IPresentablePart[]) otherParts.toArray(new IPresentablePart[otherParts.size()]));
		}
	};
	
	public CloseOthersContributionItem(WrappedTabsPartPresentation presentation) {
		this.presentation = presentation;
	}

    public void dispose() {
    	super.dispose();
        presentation = null;
        selectionListener = null;
    }
    
    public void fill(Menu menu, int index) {
    	if (presentation.getParts().length > 1) {
			MenuItem item = new MenuItem(menu, SWT.NONE, index);
			item.setText("Close others");
			item.addSelectionListener(selectionListener);
			
			IPresentablePart current = presentation.getCurrent();
			item.setEnabled(current != null);
    	}
    }
    
    public boolean isDynamic() {
        return true;
    }
}
