package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.action.Action;
import org.eclipse.ui.*;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * The <code>HistoryAction</code> is move navigation history 
 * back and forward.
 */
public class NavigationHistoryAction extends PageEventAction {
	private boolean forward;
	
	/**
	 * Create a new instance of <code>NavigationHistoryAction</code>
	 * 
	 * @param window the workbench window this action applies to
	 * @param forward if this action should move history forward of backward
	 */
	public NavigationHistoryAction(IWorkbenchWindow window,boolean forward) {
		super("",window); //$NON-NLS-1$
		if(forward) {
			setText(WorkbenchMessages.getString("NavigationHistoryAction.forward.text"));
			setToolTipText(WorkbenchMessages.getString("NavigationHistoryAction.forward.toolTip")); //$NON-NLS-1$
		} else {
			setText(WorkbenchMessages.getString("NavigationHistoryAction.backward.text"));
			setToolTipText(WorkbenchMessages.getString("NavigationHistoryAction.backward.toolTip")); //$NON-NLS-1$
		}
		// WorkbenchHelp.setHelp(this, IHelpContextIds.CLOSE_ALL_PAGES_ACTION);
		setEnabled(false);
		this.forward = forward;
	}
	/* (non-Javadoc)
	 * Method declared on PageEventAction.
	 */	
	public void pageActivated(IWorkbenchPage page) {
		super.pageActivated(page);
		NavigationHistory nh = ((WorkbenchPage)page).getNavigationHistory();
		if(forward)
			nh.setForwardAction(this);
		else
			nh.setBackwardAction(this);
	}
	/* (non-Javadoc)
	 * Method declared on IAction.
	 */
	public void run() {
		WorkbenchPage page = (WorkbenchPage)getActivePage();
		if (page != null)
			if(forward)
				page.getNavigationHistory().forward();
			else
				page.getNavigationHistory().backward();
	}
}