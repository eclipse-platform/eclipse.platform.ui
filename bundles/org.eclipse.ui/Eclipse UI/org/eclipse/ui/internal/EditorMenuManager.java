package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.action.*;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.RetargetAction;

import java.util.*;

/**
 * An <code>EditorMenuManager</code> is used to sort the contributions
 * made by an editor so that they always appear after the action sets.  
 */
public class EditorMenuManager extends SubMenuManager {
	private IMenuManager parentMgr;
	private boolean enabledAllowed = true;
	private ArrayList wrappers;
/**
 * Constructs a new editor manager.
 */
public EditorMenuManager(IMenuManager mgr) {
	super(mgr);
	parentMgr = mgr;
}
/* (non-Javadoc)
 * Method declared on IContributionManager.
 */
public IContributionItem[] getItems() {
	return parentMgr.getItems();
}
/* (non-Javadoc)
 * Method declared on IContributionManager.
 * Inserts the new item after any action set contributions which may
 * exist within the toolbar to ensure a consistent order for actions.
 */
public void insertAfter(String id, IContributionItem item) {
	IContributionItem refItem = PluginActionSetBuilder.findInsertionPoint(id,
		null, parentMgr, false);
	if (refItem != null) {
		super.insertAfter(refItem.getId(), item);
	} else {
		WorkbenchPlugin.log("Reference action not found: " + id); //$NON-NLS-1$
	}
}
/* (non-Javadoc)
 * Method declared on IContributionManager.
 * Inserts the new item after any action set contributions which may
 * exist within the toolbar to ensure a consistent order for actions.
 */
public void prependToGroup(String groupName, IContributionItem item) {
	insertAfter(groupName, item);
}
/**
 * Sets the visibility of the manager. If the visibility is <code>true</code>
 * then each item within the manager appears within the parent manager.
 * Otherwise, the items are not visible if force visibility is
 * <code>true</code>, or grayed out if force visibility is <code>false</code>
 * <p>
 * This is a workaround for the layout flashing when editors contribute
 * large amounts of items.</p>
 *
 * @param visible the new visibility
 * @param forceVisibility whether to change the visibility or just the
 * 		enablement state. This parameter is ignored if visible is 
 * 		<code>true</code>.
 */
public void setVisible(boolean visible, boolean forceVisibility) {
	if (visible) {
		// Make the items visible 
		if (!enabledAllowed) 
			setEnabledAllowed(true);
		if (!isVisible())
			setVisible(true);
	}
	else {
		if (forceVisibility)
			// Remove the editor menu items
			setVisible(false);
		else
			// Disable the editor menu items.
			setEnabledAllowed(false);
	}
}
/**
 * Sets the enablement ability of all the items contributed by the editor.
 */
public void setEnabledAllowed(boolean enabledAllowed) {
	this.enabledAllowed = enabledAllowed;
	IContributionItem[] items = super.getItems();
	for (int i = 0; i < items.length; i++) {
		IContributionItem item = items[i];
		if (!(item instanceof ActionContributionItem &&
			((ActionContributionItem)item).getAction() instanceof RetargetAction)) {
			// skip retarget actions, they stay enabled if they have a handler	
			item.setEnabledAllowed(enabledAllowed);
		}
	}
	// pass it on to the created wrappers
	if (wrappers != null) {
		for (int i = 0; i < wrappers.size(); i++) {
			((EditorMenuManager)wrappers.get(i)).setEnabledAllowed(enabledAllowed);
		}
	}
}

/* (non-Javadoc)
 * Method declared on SubMenuManager.
 */
protected SubMenuManager wrapMenu(IMenuManager menu) {
	if (wrappers == null)
		wrappers = new ArrayList();
	EditorMenuManager manager = new EditorMenuManager(menu);
	wrappers.add(manager);	
	return manager;
}
}
