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
public class SwitchToWindowMenu extends ContributionItem {
	private WorkbenchWindow fWindow;
	private boolean showSeparator;	
/**
 * Create a SwitchToMenuItem.
 * The argument window is used to retrieve the WindowManager
 * which maintains the list of all browsers.
 */
public SwitchToWindowMenu(WorkbenchWindow window, boolean showSeparator) {
	super("Switch To Window");//$NON-NLS-1$
	fWindow = window;
	this.showSeparator = showSeparator;
}
/**
 * Fills the given menu with
 * menu items for all windows.
 */
public void fill(Menu menu, int index) {
	// Get workbench windows.
	Workbench workbench = (Workbench) fWindow.getWorkbench();
	IWorkbenchWindow [] array = workbench.getWorkbenchWindows();

	// If only 1 window return.
	if (array.length <= 1)
		return;

	// Add separator.
	if (showSeparator) {
		new MenuItem(menu, SWT.SEPARATOR, index);
		++ index;
	}

	// Add one item for each window.
	for (int i = 0; i < array.length; i++) {
		final IWorkbenchWindow window = array[i];
		String name = window.getShell().getText();
		if (name != null) {
			MenuItem mi = new MenuItem(menu, SWT.RADIO, index);
			++ index;
			mi.setText(name);
			mi.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					Shell windowShell = window.getShell();
					if(windowShell.getMinimized())
						windowShell.setMinimized(false);
					windowShell.forceFocus();
					windowShell.moveAbove(null);
				}
			});
			mi.setSelection(window == fWindow);
		}
	}
}
/**
 * Overridden to always return true and force dynamic menu building.
 */
public boolean isDynamic() {
	return true;
}
}
