package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.action.*;
import org.eclipse.ui.*;

/**
 * This class represents the action bars for an action set.
 */
public class ActionSetActionBars extends SubActionBars {
	private String actionSetId;
	private CoolBarContributionItem coolBarItem;
/**
 * Constructs a new action bars object
 */
public ActionSetActionBars(IActionBars parent, String actionSetId) {
	super(parent);
	this.actionSetId = actionSetId;
}
/* (non-Javadoc)
 * Inherited from SubActionBars.
 */
protected SubMenuManager createSubMenuManager(IMenuManager parent) {
	return new ActionSetMenuManager(parent, actionSetId);
}
/* (non-Javadoc)
 * Inherited from SubActionBars.
 */
protected SubToolBarManager createSubToolBarManager(IToolBarManager parent) {
	return new ActionSetToolBarManager(parent, actionSetId);
}
/**
 * Dispose the contributions.
 */
public void dispose() {
	super.dispose();
	if (coolBarItem != null) 
		coolBarItem.removeAll();
}
/**
 * Returns the tool bar manager.  If items are added or
 * removed from the manager be sure to call <code>updateActionBars</code>.
 *
 * @return the tool bar manager
 */
public IToolBarManager getToolBarManager() {
	IToolBarManager parentMgr = parent.getToolBarManager();
	if (parentMgr instanceof ToolBarManager) {
		return super.getToolBarManager();
	} else if (parentMgr instanceof CoolBarManager) {
		if (coolBarItem == null) {
			// Create a CoolBar item for this action bar.
			CoolBarManager cBarMgr = ((CoolBarManager)parentMgr);
			coolBarItem = new CoolBarContributionItem(cBarMgr, actionSetId);
			coolBarItem.setVisible(active);
		}
		return coolBarItem;
	}
	return null;
}
/**
 * Activate / Deactivate the contributions.
 */
protected void setActive(boolean set) {
	super.setActive(set);
	if (coolBarItem != null)
		coolBarItem.setVisible(set);
}
}
