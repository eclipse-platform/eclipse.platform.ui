package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.action.*;
import org.eclipse.ui.*;
import java.util.*;

/**
 * An <code>EditorToolBarManager</code> is used to sort the contributions
 * made by an editor so that they always appear after the action sets.  
 */
public class EditorToolBarManager extends SubToolBarManager 
{
	private IToolBarManager parentMgr;
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
}
