package org.eclipse.help.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;

/**
 * Base page class for topics and search pages
 */
public abstract class NavigationPage implements ISelectionProvider {
	private NavigationWorkbook workbook;
	private CTabItem tabItem;

	public NavigationPage(NavigationWorkbook workbook, String label) {
		super();
		this.workbook = workbook;
		tabItem = new CTabItem(workbook.getTabFolder(), SWT.NONE);
		tabItem.setData(this);
		tabItem.setText(label);
		tabItem.setToolTipText(label);
	}
	public void activate() {
		if (tabItem.getControl() == null)
			tabItem.setControl(createControl(tabItem.getParent()));
	}
	protected abstract Control createControl(Composite parent);
	public boolean deactivate() {
		return true;
	}
	public void dispose() {
		if (tabItem == null)
			return;

		CTabItem oldItem = tabItem;
		tabItem = null;
		oldItem.dispose();
	}
	protected CTabItem getTabItem() {
		return tabItem;
	}
}
