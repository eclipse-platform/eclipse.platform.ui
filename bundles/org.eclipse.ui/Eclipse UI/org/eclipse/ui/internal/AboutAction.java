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
import org.eclipse.jface.action.Action;

/**
 * Creates an About dialog and opens it.
 */
public class AboutAction extends Action {
	private IWorkbenchWindow workbenchWindow;
/**
 * Creates a new <code>AboutAction</code> with the given label
 */
public AboutAction(IWorkbenchWindow window) {
	super(WorkbenchMessages.format("AboutAction.text", new Object[] {((Workbench)PlatformUI.getWorkbench()).getProductInfo().getName()})); //$NON-NLS-1$
	setToolTipText(WorkbenchMessages.format("AboutAction.toolTip", new Object[] {((Workbench)PlatformUI.getWorkbench()).getProductInfo().getName()})); //$NON-NLS-1$
	setId(IWorkbenchActionConstants.ABOUT);
	this.workbenchWindow = window;
	WorkbenchHelp.setHelp(this, new Object[] {IHelpContextIds.ABOUT_ACTION});
}
/**
 * Perform the action: show about dialog.
 */
public void run() {
	new AboutDialog(workbenchWindow.getShell()).open();
}
}
