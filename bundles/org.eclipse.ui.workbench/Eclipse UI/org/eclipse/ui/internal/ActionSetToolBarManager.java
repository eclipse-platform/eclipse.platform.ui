package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.action.*;

/**
 * An <code>EditorToolBarManager</code> is used to sort the contributions
 * made by an editor so that they always appear after the action sets.  
 */
public class ActionSetToolBarManager extends SubToolBarManager 
{
	private IToolBarManager parentMgr;
	private String actionSetId;
/**
 * Constructs a new manager.
 *
 * @param mgr the parent manager.  All contributions made to the
 *      <code>EditorToolBarManager</code> are forwarded and appear in the
 *      parent manager.
 */
public ActionSetToolBarManager(IToolBarManager mgr, String actionSetId) {
	super(mgr);
	parentMgr = mgr;
	this.actionSetId = actionSetId;
}
/* (non-Javadoc)
 * Method declared on IContributionManager.
 */
public IContributionItem[] getItems() {
	return parentMgr.getItems();
}
/* (non-Javadoc)
 * Method declared on SubContributionManager.
 */
protected SubContributionItem wrap(IContributionItem item) {
	return new ActionSetContributionItem(item, actionSetId);
}
}
