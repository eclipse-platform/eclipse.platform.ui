package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.dialogs.*;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.Action;

/**
 * Creates an About dialog and opens it.
 */
public class OpenWorkbenchAction extends Action {
	private IWorkbenchWindow workbenchWindow;
/**
 * Creates a new <code>AboutAction</code> with the given label
 */
public OpenWorkbenchAction(IWorkbenchWindow window) {
	super(WorkbenchMessages.getString("OpenWorkbench.text")); //$NON-NLS-1$
	setToolTipText(WorkbenchMessages.getString("OpenWorkbench.toolTip"));
	this.workbenchWindow = window;
	WorkbenchHelp.setHelp(this, new Object[] {IHelpContextIds.ABOUT_ACTION});
}
/**
 * Perform the action: show about dialog.
 */
public void run() {
	try {
		workbenchWindow.getWorkbench().openWorkbenchWindow(
			ResourcesPlugin.getWorkspace().getRoot());
	} catch (WorkbenchException e) {
	}
}
}
