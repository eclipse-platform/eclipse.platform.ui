package org.eclipse.ui.internal;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.window.*;
import org.eclipse.jface.action.*;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;

/**
 * A dynamic menu item which supports to switch to other Windows.
 */
public class ReopenEditorMenu extends ContributionItem {
	private WorkbenchWindow fWindow;
	private EditorHistory history;
	private boolean showSeparator;

	private static final int MAX_TEXT_LENGTH = 40;
/**
 * Create a new instance.
 */
public ReopenEditorMenu(WorkbenchWindow window, EditorHistory history,
	boolean showSeparator) 
{
	super("Reopen Editor");
	fWindow = window;
	this.history = history;
	this.showSeparator = showSeparator;
}
/**
 * Returns the text for a history item.  This may be truncated to fit
 * within the MAX_TEXT_LENGTH.
 */
private String calcText(int index, EditorHistoryItem item) {
	String prefix = "&" + Integer.toString(index + 1) + " ";
	String suffix = item.input.getToolTipText();
	if (suffix.length() > MAX_TEXT_LENGTH) {
		suffix = "..." + suffix.substring(suffix.length() - MAX_TEXT_LENGTH);
	}
	return prefix + suffix;
}
/**
 * Fills the given menu with
 * menu items for all windows.
 */
public void fill(Menu menu, int index) {
	// Get items.
	EditorHistoryItem [] array = history.getItems();

	// If no items return.
	if (array.length <= 0)
		return;

	// Add separator.
	if (showSeparator) {
		new MenuItem(menu, SWT.SEPARATOR, index);
		++ index;
	}

	// Add one item for each item.
	for (int i = 0; i < array.length; i++) {
		final EditorHistoryItem item = array[i];
		MenuItem mi = new MenuItem(menu, SWT.RADIO, index);
		++ index;
		mi.setText(calcText(i, item));
		mi.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IWorkbenchPage page = fWindow.getActivePage();
				if (page != null) {
					try {
						page.openEditor(item.input, item.desc.getId());
					} catch (PartInitException e2) {
					}
				}
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
