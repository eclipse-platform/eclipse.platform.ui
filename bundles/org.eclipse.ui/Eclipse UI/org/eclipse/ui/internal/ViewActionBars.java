package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.*;
import org.eclipse.jface.action.*;
import org.eclipse.ui.internal.PartPane;

/**
 * An editor container manages the services for an editor.
 */
public class ViewActionBars extends SubActionBars
{
	private ViewPane pane;
/**
 * ViewActionBars constructor comment.
 */
public ViewActionBars(IActionBars parent, ViewPane pane) {
	super(parent);
	this.pane = pane;
}
/**
 * Returns the menu manager.  If items are added or
 * removed from the manager be sure to call <code>updateActionBars</code>.
 *
 * @return the menu manager
 */
public IMenuManager getMenuManager() {
	return pane.getMenuManager();
}
/**
 * Returns the tool bar manager.  If items are added or
 * removed from the manager be sure to call <code>updateActionBars</code>.
 *
 * @return the tool bar manager
 */
public IToolBarManager getToolBarManager() {
	return pane.getToolBarManager();
}
/**
 * Commits all UI changes.  This should be called
 * after additions or subtractions have been made to a 
 * menu, status line, or toolbar.
 */
public void updateActionBars() {
	pane.updateActionBars();
	getStatusLineManager().update(false);
	fireActionHandlersChanged();
}
}
