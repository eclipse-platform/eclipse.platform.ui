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
import java.util.Comparator;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;

/**
 * @since 3.0
 */
public class PartListContributionItem extends ContributionItem {
	private WrappedTabsPartPresentation presentation;
	private IStackPresentationSite site;
	
	private static final String DATA_ITEM = "org.eclipse.ui.examples.presentation.wrappedtabs.PartListContributionItem.DATA_ITEM"; 
	
	private SelectionAdapter selectionListener = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			MenuItem item = (MenuItem)e.widget;
			
			IPresentablePart part = (IPresentablePart)item.getData(DATA_ITEM);
			
			if (part != null) {
				site.selectPart(part);
			}
		}
	};
	
	public PartListContributionItem(WrappedTabsPartPresentation presentation, IStackPresentationSite site) {
		this.presentation = presentation;
		this.site = site;
	}

    public void dispose() {
    	super.dispose();
        presentation = null;
        site = null;
        selectionListener = null;
    }
    
    public void fill(Menu menu, int index) {
    	IPresentablePart[] parts = presentation.getParts();
    	
    	// Don't include a part list if there's only one part
    	if (parts.length <= 1) {
    		return;
    	}
    	
    	new MenuItem(menu, SWT.SEPARATOR, index++);
    	
    	Arrays.sort(parts, new Comparator() {
			public int compare(Object arg0, Object arg1) {
				IPresentablePart part0 = (IPresentablePart)arg0;
				IPresentablePart part1 = (IPresentablePart)arg1;
				
				return part0.getName().compareToIgnoreCase(part1.getName());
			}
    	});
    	
    	for (int i = 0; i < parts.length; i++) {
			IPresentablePart part = parts[i];
			
			MenuItem item = new MenuItem(menu, SWT.NONE, index++);
			item.setText(part.getName());
			item.setImage(part.getTitleImage());
			item.addSelectionListener(selectionListener);
			item.setData(DATA_ITEM, part);
		}
    }
    
    public boolean isDynamic() {
        return true;
    }
}
