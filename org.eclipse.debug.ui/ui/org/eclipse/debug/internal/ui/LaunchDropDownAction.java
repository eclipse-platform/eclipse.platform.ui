package org.eclipse.debug.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2001
 */
 
import org.eclipse.debug.core.ILaunch;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

/**
 * Superclass of run & debug pulldown actions.
 */
public abstract class LaunchDropDownAction implements IWorkbenchWindowPulldownDelegate {
	
	private ExecutionAction fLaunchAction;
	
	/**
	 * Create a new instance of this class
	 */
	public LaunchDropDownAction(ExecutionAction launchAction) {
		fLaunchAction= launchAction;		
	}

	private void createMenuForAction(Menu parent, Action action) {
		ActionContributionItem item= new ActionContributionItem(action);
		item.fill(parent, -1);
	}

	/**
	 * @see IWorkbenchWindowActionDelegate
	 */
	public void dispose() {
	}

	/**
	 * @see IWorkbenchWindowPulldownDelegate
	 */
	public Menu getMenu(Control parent) {
		Menu menu= new Menu(parent);
		ILaunch[] historyList= getHistory();
		int count= 0;
		for (int i = 0; i < historyList.length; i++) {
			ILaunch launch= historyList[i];
			if (launch != null) {
				RelaunchHistoryLaunchAction newAction= new RelaunchHistoryLaunchAction(launch, getMode());
				createMenuForAction(menu, newAction);
				count++;
			}
		}
		if (count > 0) {
			new MenuItem(menu, SWT.SEPARATOR);
		}
		createMenuForAction(menu, new LaunchWithAction(getMode()));

		return menu;
	}

	/**
	 * @see IActionDelegate
	 */
	public void run(IAction action) {
		fLaunchAction.run();
	}
	
	/**
	 * @see IActionDelegate
	 */
	public void selectionChanged(IAction action, ISelection selection){
	}
	
	/**
	 * @see IWorkbenchWindowActionDelegate
	 */
	public void init(IWorkbenchWindow window){
	}
	
	/**
	 * Returns an array of previous launches applicable to this drop down
	 */
	public abstract ILaunch[] getHistory();
	
	/**
	 * Returns the mode (e.g., 'run' or 'debug') of this drop down
	 */
	public abstract String getMode();
}

