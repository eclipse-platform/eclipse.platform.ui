package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.action.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import java.util.*;

/**
 * An <code>EditorToolBarManager</code> is used to sort the contributions
 * made by an editor so that they always appear after the action sets.  
 */
public class EditorToolBarManager extends SubToolBarManager 
{
	private IToolBarManager parentMgr;
	private ArrayList grayedOutItems;
/**
 * Constructs a new manager.
 *
 * @param mgr the parent manager.  All contributions made to the
 *      <code>EditorToolBarManager</code> are forwarded and appear in the
 *      parent manager.
 */
public EditorToolBarManager(IToolBarManager mgr) {
	super(mgr);
	parentMgr = mgr;
}
/* (non-Javadoc)
 * Method declared on IContributionManager.
 */
public IContributionItem[] getItems() {
	return parentMgr.getItems();
}
/**
 * Return the toolbar into which this manager will
 * contribute to.
 */
private ToolBar getToolBar() {
	if (parentMgr == null)
		return null;
		
	IContributionManager mgr = parentMgr;
	while (mgr instanceof SubToolBarManager)
		mgr = ((SubToolBarManager)mgr).getParent();
		
	if (mgr instanceof ToolBarManager)
		return ((ToolBarManager)mgr).getControl();

	return null;
}
/* (non-Javadoc)
 * Method declared on IContributionManager.
 */
/**
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
 */
/**
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
 * large amounts of items into the toolbar.</p>
 *
 * @param visible the new visibility
 * @param forceVisibility whether to change the visibility or just the
 * 		enablement state.
 */
public void setVisible(boolean visible, boolean forceVisibility) {
	if (visible) {
		// Make the editor tool bar items active for the user
		if (forceVisibility)
			setVisible(true);
		// Enable the ones disabled when the manager was deactivated
		if (grayedOutItems != null) {
			for (int i = 0; i < grayedOutItems.size(); i++)
				((ToolItem) grayedOutItems.get(i)).setEnabled(true);
			grayedOutItems = null;
		}
	}
	else {
		// Make the editor tool bar items inactive for the user
		grayedOutItems = null;
		if (forceVisibility)
			setVisible(false);
		else {
			// Disabled the tool items that are already enabled.
			ToolBar toolbar = getToolBar();
			if (toolbar == null)
				return;
			grayedOutItems = new ArrayList(20);
			ToolItem[] toolItems = toolbar.getItems();
			for (int i = 0; i < toolItems.length; i++) {
				ToolItem item = toolItems[i];
				if (item.getData() instanceof SubContributionItem) {
					// This is an item contributed by a sub tool manager
					if (!(item.getData() instanceof ActionSetContributionItem)) {
						// But not from the action set sub tool manager
						if (item.getEnabled()) {
							// Was enabled at the time
							grayedOutItems.add(item);
							item.setEnabled(false);
						}
					}
				}
			}
		}
	}
}
}
