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
package org.eclipse.ui.internal.misc;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

public abstract class WorkbookPage {
	public TabItem tabItem;
/**
 * WorkbookPage constructor comment.
 */
public WorkbookPage(Workbook parent) {
	TabFolder folder = parent.getTabFolder();
	tabItem = new TabItem(folder,SWT.NONE);
	tabItem.setData(this);
}
public void activate() {

	if (tabItem.getControl() == null)
		tabItem.setControl(createControl(tabItem.getParent()));
			
}
protected abstract Control createControl (Composite parent);
public boolean deactivate() {
	return true;
}
public void dispose() {

	if (tabItem == null)
		return;

	TabItem oldItem = tabItem;
	tabItem = null;
	oldItem.dispose();
}
public TabItem getTabItem() {
	return tabItem;
}
}
