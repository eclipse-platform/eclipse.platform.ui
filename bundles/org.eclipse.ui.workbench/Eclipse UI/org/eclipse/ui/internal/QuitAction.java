package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.*;
import org.eclipse.ui.help.*;
import org.eclipse.jface.action.*;

/**
 * Try to quit the application.
 */
public class QuitAction extends Action {
	private IWorkbench workbench;
/**
 * Creates a new <code>QuitAction</code>. The action is
 * initialized from the <code>JFaceResources</code> bundle.
 */
public QuitAction(IWorkbench workbench) {
	setText(WorkbenchMessages.getString("Exit.text")); //$NON-NLS-1$
	setToolTipText(WorkbenchMessages.getString("Exit.toolTip")); //$NON-NLS-1$
	setId(IWorkbenchActionConstants.QUIT);
	WorkbenchHelp.setHelp(this, IHelpContextIds.QUIT_ACTION);
	this.workbench = workbench;
}
/**
 * Perform the action: quit the application.
 */
public void run() {
	workbench.close();
}
}
