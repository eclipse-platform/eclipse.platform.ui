package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.action.*;
import org.eclipse.ui.*;
import org.eclipse.swt.SWT;

/**
 * Action to navigate to the next workbench
 * page listed in the shortcut bar.
 */
public class NextPageAction extends PageEventAction {
	private int increment = 1;
/**
 * NextPageAction constructor comment.
 */
public NextPageAction(String label, int increment, IWorkbenchWindow window) {
	super(label, window);
	this.increment = increment;
	setEnabled(false);
}
/* (non-Javadoc)
 * Method declared on IAction.
 *
 * Override the default implementation since we
 * want to show the user ALT+UP and ALT+DOWN in
 * the menu, not UP_ARROW and DOWN_ARROW.
 */
public int getAccelerator() {
	int accel = SWT.ALT;
	accel |= (increment < 0) ? SWT.ARROW_UP : SWT.ARROW_DOWN;
	return accel;
}
/**
 * The <code>NextPageAction</code> implementation of this 
 * <code>IPageListener</code> method enables the action
 * if two or more pages are open.
 */
public void pageClosed(IWorkbenchPage page) {
	super.pageClosed(page);
	setEnabled(getWorkbenchWindow().getPages().length > 1);
}
/**
 * The <code>PageEventAction</code> implementation of this 
 * <code>IPageListener</code> method enables the action
 * if two or more pages are open.
 */
public void pageOpened(IWorkbenchPage page) {
	super.pageOpened(page);
	setEnabled(getWorkbenchWindow().getPages().length > 1);
}
/**
 * Implementation of method defined on <code>IAction</code>.
 */
public void run() {
	IWorkbenchPage[] pages = getWorkbenchWindow().getPages();
	if (pages.length < 2)
		return;

	IWorkbenchPage activePage = getActivePage();
	if (activePage == null)
		getWorkbenchWindow().setActivePage(pages[0]);

	int index;
	for (index = 0; index < pages.length; index++) {
		if (pages[index] == activePage)
			break;
	}
	index += increment;
	if (index >= pages.length)
		index = 0;
	if (index < 0)
		index = pages.length - 1;
	getWorkbenchWindow().setActivePage(pages[index]);
}
}
