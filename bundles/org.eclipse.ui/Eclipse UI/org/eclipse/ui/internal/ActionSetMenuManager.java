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
 * Method declared on IContributionManager.
 *
 * Returns the item passed to us, not the wrapper.
 * In the case of menu's not added by this manager,
 * ensure that we return a wrapper for the menu.
 */
public IContributionItem find(String id) {
	IContributionItem item = parentMgr.find(id);
	if (item instanceof SubContributionItem)
		// Return the item passed to us, not the wrapper.
		item = unwrap(item);
		
	if (item instanceof IMenuManager) {
		// if it is a menu manager wrap it before returning
		IMenuManager menu = (IMenuManager)item;
		if (menu instanceof SubMenuManager)
			// it it is already wrapped then remover the wrapper and 
			// rewrap. We have a table of wrappers so we reuse wrappers
			// we create.
			menu = (IMenuManager) ((SubMenuManager)menu).getParent();
		item = getWrapper(menu);
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
