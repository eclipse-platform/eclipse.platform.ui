package org.eclipse.ui.views.navigator;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;

/**
 * The ActionContributionFactory is a class that 
 * contributes actions to a menu or action bar.
 */

public abstract class ActionContributionFactory
	implements ISelectionChangedListener {

	/**
	 * Updates all actions with the given selection.
	 * Necessary when popping up a menu, because some of the enablement criteria
	 * may have changed, even if the selection in the viewer hasn't.
	 */
	public abstract void updateActions(IStructuredSelection selection);

	/**
	 *	Create the receivers action objects.
	 */
	public abstract void makeActions();

	/**
	 * Contributes actions to the local tool bar and local pulldown menu.
	 * @param toolBar IToolBarManager - the manager the actions are being added to
	 */
	public void fillToolBar(IToolBarManager toolBar) {
		//Do nothing by default as we might not contribute
	}

	/**
	 * Fill the menu with any menu actions.
	 * @param menu the context sensitive menu
	 * @param selection the current selection in the project explorer
	 */
	public void fillMenu(IMenuManager menu, IStructuredSelection selection) {
		//Do nothing by default
	}

	/*
	 * @see ISelectionChangedListener#selectionChanged(SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		//Do nothing by default
	}

}