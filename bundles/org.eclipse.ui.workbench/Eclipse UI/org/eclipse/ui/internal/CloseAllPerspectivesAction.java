package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.action.Action;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * The <code>CloseAllPerspectivesAction</code> is used to close all of 
 * the opened perspectives in the workbench window's active page.
 */
public class CloseAllPerspectivesAction extends Action {
	private WorkbenchWindow window;
	
	/**
	 * Create a new instance of <code>CloseAllPerspectivesAction</code>
	 * 
	 * @param window the workbench window this action applies to
	 */
	public CloseAllPerspectivesAction(WorkbenchWindow window) {
		super(WorkbenchMessages.getString("CloseAllPerspectivesAction.text")); //$NON-NLS-1$
		setToolTipText(WorkbenchMessages.getString("CloseAllPerspectivesAction.toolTip")); //$NON-NLS-1$
		WorkbenchHelp.setHelp(this, IHelpContextIds.CLOSE_ALL_PAGES_ACTION);
		setEnabled(false);
		this.window = window;
	}
	
	/* (non-Javadoc)
	 * Method declared on IAction.
	 */
	public void run() {
		WorkbenchPage page = (WorkbenchPage)window.getActivePage();
		if (page != null)
			page.close();
	}
}