package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.action.Action;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * The <code>ClosePerspectiveAction</code> is used to close the
 * active perspective in the workbench window's active page.
 */
public class ClosePerspectiveAction extends Action {
	private WorkbenchWindow window;
	
	/**
	 * Create a new instance of <code>ClosePerspectiveAction</code>
	 * 
	 * @param window the workbench window this action applies to
	 */
	public ClosePerspectiveAction(WorkbenchWindow window) {
		super(WorkbenchMessages.getString("ClosePerspectiveAction.text")); //$NON-NLS-1$
		setToolTipText(WorkbenchMessages.getString("ClosePerspectiveAction.toolTip")); //$NON-NLS-1$
		setEnabled(false);
		this.window = window;
		WorkbenchHelp.setHelp(this, IHelpContextIds.CLOSE_PAGE_ACTION);
	}
	
	/* (non-Javadoc)
	 * Method declared on IAction.
	 */
	public void run() {
		WorkbenchPage page = (WorkbenchPage) window.getActivePage();
		if (page != null) {
			Perspective persp = page.getActivePerspective();
			if (persp != null)
				page.closePerspective(persp, true,true);
		}
	}
}