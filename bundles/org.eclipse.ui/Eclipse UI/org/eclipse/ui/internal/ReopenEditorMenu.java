package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
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
	super("Reopen Editor");//$NON-NLS-1$
	fWindow = window;
	this.history = history;
	this.showSeparator = showSeparator;
}
/**
 * Returns the text for a history item.  This may be truncated to fit
 * within the MAX_TEXT_LENGTH.
 */
private String calcText(int index, EditorHistoryItem item) {
	StringBuffer sb = new StringBuffer();
	sb.append(index+1);
	sb.insert(sb.length()-1, '&');  // make the last digit the mnemonic, not the first
	sb.append(' ');
	String suffix = item.getInput().getToolTipText();
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
 * Fills the given menu with
 * menu items for all windows.
 */
public void fill(Menu menu, int index) {
	if(fWindow.getActivePage() == null)
		return;
	
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
		MenuItem mi = new MenuItem(menu, SWT.PUSH, index);
		++ index;
		mi.setText(calcText(i, item));
		mi.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				open(item);
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
/**
 * Reopens the editor for the given history item.
 */
void open(EditorHistoryItem item) {
	IWorkbenchPage page = fWindow.getActivePage();
	if (page != null) {
		try {
			// Fix for 1GF6HQ1: ITPUI:WIN2000 - NullPointerException: opening a .ppt file
			// Descriptor is null if opened on OLE editor.  .
			IEditorInput input = item.getInput();
			IEditorDescriptor desc = item.getDescriptor();
			if (desc == null) {
				// There's no openEditor(IEditorInput) call, and openEditor(IEditorInput, String)
				// doesn't allow null id.
				// However, if id is null, the editor input must be an IFileEditorInput,
				// so we can use openEditor(IFile).  
				// Do nothing if for some reason input was not an IFileEditorInput.
				if (input instanceof IFileEditorInput) {
					page.openEditor(((IFileEditorInput) input).getFile());
				}
			}
			else {
				page.openEditor(input, desc.getId());
			}
		} catch (PartInitException e2) {
		}
	}
}
}
