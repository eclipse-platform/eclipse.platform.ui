package org.eclipse.ui.tests.adaptable;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.AddBookmarkAction;
import org.eclipse.ui.views.navigator.ActionFactory;

/**
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */

public class TestNavigatorActionFactory
	extends ActionFactory {
		AddBookmarkAction addBookmarkAction;
		Shell shell;
		

	public TestNavigatorActionFactory(Shell aShell) {
		shell = aShell;
	}

	/*
	 * @see ActionFactory#makeActions()
	 */
	public void makeActions() {
		addBookmarkAction = new AddBookmarkAction(shell);
	}

	/*
	 * @see ActionFactory#fillToolBar(IToolBarManager)
	 */
	public void fillToolBar(IToolBarManager toolBar) {
	}

	/*
	 * @see ActionFactory#fillPopUpMenu(IMenuManager, IStructuredSelection)
	 */
	public void fillPopUpMenu(IMenuManager menu, IStructuredSelection selection) {
		
		//Update the selections of those who need a refresh before filling
		addBookmarkAction.selectionChanged(selection);
		menu.add(addBookmarkAction);
	}

	/*
	 * @see ActionFactory#fillActionBarMenu(IMenuManager, IStructuredSelection)
	 */
	public void fillActionBarMenu(
		IMenuManager menu,
		IStructuredSelection selection) {
	}

	/**
	 * Updates the global actions with the given selection.
	 * Be sure to invoke after actions objects have updated, since can* methods delegate to action objects.
	 */
	public void updateGlobalActions(IStructuredSelection selection) {

	}

	/**
	* Contributes actions to the local tool bar and local pulldown menu.
	* @since 2.0
	*/
	public void fillActionBars(IStructuredSelection selection) {
	}

	/**
	* Update the selection for new selection.
	*/
	public void selectionChanged(IStructuredSelection selection) {}

}