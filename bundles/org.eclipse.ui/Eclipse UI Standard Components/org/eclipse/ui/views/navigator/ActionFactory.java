package org.eclipse.ui.views.navigator;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.views.navigator.*;

/**
 * The ActionFactory is a class that 
 * contributes actions to a menu or action bar.
 * 
 * @deprecated use ActionGroup instead
 */
public abstract class ActionFactory {

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
	 * Fill the pop up menu with any menu actions.
	 * @param menu the context sensitive menu
	 * @param selection the current selection in the project explorer
	 */
	public void fillPopUpMenu(IMenuManager menu, IStructuredSelection selection) {
		//Do nothing by default
	}
	
	/**
	 * Fill the action bar menu with any menu actions.
	 * @param menu the context sensitive menu
	 * @param selection the current selection in the project explorer
	 */
	public void fillActionBarMenu(IMenuManager menu, IStructuredSelection selection) {
		//Do nothing by default
	}
	
}