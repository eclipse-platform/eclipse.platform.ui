package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationHistoryElement;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IActionDelegateWithEvent;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate;

/**
 * Superclass of run & debug pulldown actions.
 */
public abstract class LaunchDropDownAction implements IWorkbenchWindowPulldownDelegate,
														  IActionDelegateWithEvent {
	
	private ExecutionAction fLaunchAction;
	
	public LaunchDropDownAction(ExecutionAction launchAction) {
		setLaunchAction(launchAction);		
	}

	private void createMenuForAction(Menu parent, IAction action, int count) {
		if (count > 0) {
			StringBuffer label= new StringBuffer();
			if (count < 10) {
				//add the numerical accelerator
				label.append('&');
				label.append(count);
				label.append(' ');
			}
			label.append(action.getText());
			action.setText(label.toString());
		}
		ActionContributionItem item= new ActionContributionItem(action);
		item.fill(parent, -1);
	}

	/**
	 * @see IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
	}

	/**
	 * @see IWorkbenchWindowPulldownDelegate#getMenu(Control)
	 */
	public Menu getMenu(Control parent) {
		Menu menu= new Menu(parent);
		return createMenu(menu);
	}
	
	/**
	 * @see IMenuCreator#getMenu(Menu)
	 */
	public Menu getMenu(Menu parent) {
		Menu menu= new Menu(parent);
		return createMenu(menu);
	}

	protected Menu createMenu(Menu menu) {
		
		LaunchConfigurationHistoryElement[] favoriteList = getFavorites();
		int total = 0;
		for (int i = 0; i < favoriteList.length; i++) {
			LaunchConfigurationHistoryElement launch= favoriteList[i];
			RelaunchHistoryLaunchAction newAction= new RelaunchHistoryLaunchAction(launch);
			createMenuForAction(menu, newAction, total + 1);
			total++;
		}		
		
		//used in the tool bar drop down for the cascade launch with menu
		if (favoriteList.length > 0) {
			new MenuItem(menu, SWT.SEPARATOR);
		}		
		
		LaunchConfigurationHistoryElement[] historyList= getHistory();
		for (int i = 0; i < historyList.length; i++) {
			LaunchConfigurationHistoryElement launch= historyList[i];
			RelaunchHistoryLaunchAction newAction= new RelaunchHistoryLaunchAction(launch);
			createMenuForAction(menu, newAction, total+1);
			total++;;
		}
		
		if (getLaunchAction() != null) {
			//used in the tool bar drop down for the cascade launch with menu
			if (historyList.length > 0) {
				new MenuItem(menu, SWT.SEPARATOR);
			}
		
			createMenuForAction(menu, new LaunchWithAction(getMode()), -1);
		}

		return menu;
	}
	
	/**
	 * @see runWithEvent(IAction, Event)
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
	}
	
	/**
	 * @see IActionDelegateWithEvent#runWithEvent(IAction, Event)
	 */
	public void runWithEvent(IAction action, Event event) {
		getLaunchAction().runWithEvent(action, event);
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection){
	}
	
	/**
	 * @see IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window){
	}
	
	/**
	 * Returns an array of previous launches applicable to this drop down.
	 */
	public abstract LaunchConfigurationHistoryElement[] getHistory();
	
	/**
	 * Returns an array of favorites applicable to this drop down.
	 */
	public abstract LaunchConfigurationHistoryElement[] getFavorites();	
	
	/**
	 * Returns the mode (e.g., 'run' or 'debug') of this drop down.
	 */
	public abstract String getMode();
	
	protected ExecutionAction getLaunchAction() {
		return fLaunchAction;
	}

	protected void setLaunchAction(ExecutionAction launchAction) {
		fLaunchAction = launchAction;
	}
}

