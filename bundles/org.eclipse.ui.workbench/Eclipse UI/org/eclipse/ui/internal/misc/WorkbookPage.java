package org.eclipse.ui.internal.misc;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
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
