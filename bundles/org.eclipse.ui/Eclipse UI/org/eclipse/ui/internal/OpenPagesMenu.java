package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchPage;

/**
 * A dynamic menu item which shows all open pages in the window.
 */
public class OpenPagesMenu extends ContributionItem {
	private WorkbenchWindow window;
	private boolean showSeparator;

	private static final int MAX_TEXT_LENGTH = 40;

/**
 * Create a new instance.
 */
public OpenPagesMenu(WorkbenchWindow window, boolean showSeparator) 
{
	super("Open page");//$NON-NLS-1$
	this.window = window;
	this.showSeparator = showSeparator;
}
/**
 * Returns the text for a page.  This may be truncated to fit
 * within the MAX_TEXT_LENGTH.
 */
private String calcText(int index, IWorkbenchPage page) {
	StringBuffer sb = new StringBuffer();
	sb.append(index+1);
	sb.insert(sb.length()-1, '&');  // make the last digit the mnemonic, not the first
	sb.append(' ');
	String suffix = page.getLabel();
	if (suffix.length() <= MAX_TEXT_LENGTH) {
		sb.append(suffix);
	}
	else {
		sb.append(suffix.substring(0, MAX_TEXT_LENGTH/2));
		sb.append("..."); //$NON-NLS-1$
		sb.append(suffix.substring(suffix.length() - MAX_TEXT_LENGTH/2));
	}
	return sb.toString();
}

/**
 * Fills the given menu with menu items for all pages.
 */
public void fill(Menu menu, int index) {
	// Get items.
	IWorkbenchPage[] pages = window.getPages();

	// If no items return.
	if (pages.length <= 0)
		return;

	// Add separator.
	if (showSeparator) {
		new MenuItem(menu, SWT.SEPARATOR, index);
		++index;
	}

	// Add one item for each item.
	IWorkbenchPage activePage = window.getActivePage();
	for (int i = 0; i < pages.length; i++) {
		final IWorkbenchPage page = pages[i];
		MenuItem mi = new MenuItem(menu, SWT.RADIO, index);
		mi.setSelection(page == activePage);
		++index;
		mi.setText(calcText(i, page));
		mi.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				window.setActivePage(page);
			}
		});
	}
}
/**
 * Overridden to always return true and force dynamic menu building.
 */
public boolean isDynamic() {
	return true;
}
}
