package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * The <code>ResetToolBarAction</code> is used to reset the toolbar for the
 * workbench.  The toolbar for the current perspectives is reset.
 */
public class ResetToolBarAction extends Action {
	private WorkbenchWindow window;
	
	/**
	 * Create a new instance of <code>ResetToolBarAction</code>
	 * 
	 * @param window the workbench window this action applies to
	 */
	public ResetToolBarAction(WorkbenchWindow window) {
		super(WorkbenchMessages.getString("ResetToolBarAction.text")); //$NON-NLS-1$
		this.window = window;
		WorkbenchHelp.setHelp(this, IHelpContextIds.RESET_TOOLBAR_ACTION);
	}
	/* (non-Javadoc)
	 * Method declared on IAction.
	 */
	public void run() {
		window.resetToolBar();
	}
}