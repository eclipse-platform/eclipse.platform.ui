package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;

public class RunHistoryMenuAction extends RunDropDownAction implements IMenuCreator {

	private MenuListener fMenuListener= null;
	private Menu fTopMenu= null;
	
	public RunHistoryMenuAction() {
		super(null);
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (action instanceof Action) {
			if (getActionProxy() == null) {
				((Action)action).setMenuCreator(this);
				setActionProxy(action);
			}
		} else {
			action.setEnabled(false);
		}
	}
	
	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		//do nothing as the action strictly generates the history sub menu
	}
	
	/**
	 * @see IActionDelegateWithEvent#runWithEvent(IAction, Event)
	 */
	public void runWithEvent(IAction action, Event event) {
		//do nothing as the action strictly generates the history sub menu
	}
	
	public Menu getMenu(Menu parent) {
		cleanup();
		Menu menu= new Menu(parent);
		parent.addMenuListener(getDebugActionSetMenuListener());
		fTopMenu= parent;	
		setCreatedMenu(menu);
		Menu submenu= createMenu(menu);
		return submenu;
	}
	
	/**
	 * @see LaunchDropDownAction#getDebugActionSetMenuListener()
	 */
	protected MenuListener getDebugActionSetMenuListener() {
		if (fMenuListener == null) {
			fMenuListener= super.getDebugActionSetMenuListener();
		} 
		return fMenuListener;
	}
	
	public void dispose() {
		cleanup();
		super.dispose();
	}
	
	protected void cleanup() {
		if (fTopMenu != null && !fTopMenu.isDisposed()) {
			fTopMenu.removeMenuListener(getDebugActionSetMenuListener());
		}
	}

	/**
	 * @see org.eclipse.debug.internal.ui.actions.LaunchDropDownAction#createTopSeparator(Menu)
	 */
	protected void createTopSeparator(Menu menu) {
	}
	
}