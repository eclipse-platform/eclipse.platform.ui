package org.eclipse.ant.ui.internal.toolscripts;

import org.eclipse.ant.core.toolscripts.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.actions.ActionDelegate;

/**
 * Action for running a global tool script.
 */
public class GlobalToolScriptAction extends ActionDelegate implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;
/**
 * The constructor.
 */
public GlobalToolScriptAction() {
}
/**
 * @see IWorkbenchWindowActionDelegate#dispose()
 */
public void dispose() {
}
/**
 * @see IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
 */
public void init(IWorkbenchWindow window) {
	this.window = window;
}
/**
 * Insert the method's description here.
 * @see IWorkbenchWindowActionDelegate#run
 */
public void run(IAction action)  {
	if (window != null) {
		RunToolScriptDialog dialog = new RunToolScriptDialog(window.getShell());
		dialog.open();
		ToolScript script = dialog.getToolScript();
		if (script != null) {
			try {
				script.run(null);
			} catch(CoreException e) {
				ErrorDialog.openError(window.getShell(), null, "Exception running tool script", e.getStatus());
			}
		}
	}
}
}
