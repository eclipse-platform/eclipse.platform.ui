package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.action.*;
import org.eclipse.ui.*;
import java.util.*;

/**
 * An <code>EditorMenuManager</code> is used to sort the contributions
 * made by an editor so that they always appear after the action sets.  
 */
public class ActionSetMenuManager extends SubMenuManager {
	private IMenuManager parentMgr;
	private String actionSetId;
/**
 * Constructs a new editor manager.
 */
public ActionSetMenuManager(IMenuManager mgr, String actionSetId) {
	super(mgr);
	parentMgr = mgr;
	this.actionSetId = actionSetId;
}
/* (non-Javadoc)
 * Method declared on IMenuManager.
 */
public IContributionItem findUsingPath(String path) {
	IContributionItem item = parentMgr.findUsingPath(path);
	// Skip any wrappers around the item contribution
	while (true) {
		if (item instanceof SubContributionItem) {
			item = ((SubContributionItem)item).getInnerItem();
		}
		else if (item instanceof ActionSetMenuManager) {
			item = ((ActionSetMenuManager)item).parentMgr;
		}
		else {
			break;
		}
	}
	return item;
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
/* (non-Javadoc)
 * Method declared on SubMenuManager.
 */
protected SubMenuManager wrapMenu(IMenuManager menu) {
	return new ActionSetMenuManager(menu, actionSetId);
}
}
