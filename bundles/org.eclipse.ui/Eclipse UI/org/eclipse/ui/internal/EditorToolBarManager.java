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
		setEnabled(visible);
	}
	else {
		// Make the editor tool bar items inactive for the user
		if (forceVisibility)
			setVisible(false);
		// Disabled the tool items that are already enabled.
		setEnabled(visible);
	}
}

/**
 * Enable / disable all of the items contributed by the editor.
 * <p>
 * Note: This relies upon unspecified behavior for IContributionItem.update.
 * We assume that the update method will always update the underlying
 * SWT item.  In the spec for update it says: "Updates any SWT controls 
 * cached by this contribution item with any changes which have been made to 
 * this contribution item since the last update."  All existing implementations
 * always update the item.
 * </p><p>
 * See also 1GJNB52: ITPUI:ALL - ToolItems in EditorToolBarManager can get out of synch with the state of the IAction
 * </p>
 */
private void setEnabled(boolean enable) {
	ToolBar toolbar = getToolBar();
	ToolItem[] toolItems = toolbar.getItems();
	for (int i = 0; i < toolItems.length; i++) {
		ToolItem item = toolItems[i];
		Object data = item.getData();
		if (data instanceof SubContributionItem) {
			// This is an item contributed by a sub tool manager
			if (!(data instanceof ActionSetContributionItem)) {
				// But not from the action set sub tool manager
				if (enable) {
					SubContributionItem contr = (SubContributionItem)data;
					contr.update();
				} else {
					item.setEnabled(false);
				}
			}
		}
	}
}
}
