package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.AbstractGroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.*;

/**
 * This builder reads the actions for an action set from the registry.
 */
public class PluginActionSetBuilder extends PluginActionBuilder {
	public static final String TAG_ACTION_SET="actionSet";//$NON-NLS-1$
	// As of 2.1, the "pulldown" attribute is deprecated, use "style" attribute instead.
	public static final String ATT_PULLDOWN="pulldown";//$NON-NLS-1$
	
	private PluginActionSet actionSet;
	private IWorkbenchWindow window;
/**
 * Constructs a new builder.
 */
public PluginActionSetBuilder() {}
/* (non-Javadoc)
 * Method defined in PluginActionBuilder.
 */
/*
 * This implementation inserts the group into the action set additions group.  
 */
protected void addGroup(IContributionManager mgr, String name) {
	// Find the insertion point for this group.
	String actionSetId = actionSet.getDesc().getId();
	
	if (mgr instanceof CoolItemToolBarManager) {
		// In the coolbar case we need to create a CoolBarContributionItem
		// for the group if one does not already exist.
		CoolItemToolBarManager tBarMgr = (CoolItemToolBarManager)mgr;
		CoolBarManager cBarMgr = tBarMgr.getParentManager();
		IContributionItem cbItem = cBarMgr.find(actionSetId);
		if (cbItem == null) {
			IContributionItem refItem = findSubInsertionPoint(
				IWorkbenchActionConstants.MB_ADDITIONS, actionSetId, cBarMgr, true);
			// Add the CoolBarContributionItem to the CoolBarManager for this group.
			if (refItem == null) {
				cBarMgr.add(tBarMgr.getCoolBarItem());
			} else {
				cBarMgr.insertAfter(refItem.getId(), tBarMgr.getCoolBarItem());
			}
		}
		// Insert the group marker into the group, not the CoolBarmanager.
		ActionSetSeparator group = new ActionSetSeparator(name, actionSetId);
		tBarMgr.add(group);
	} else {
		IContributionItem refItem = findInsertionPoint(
			IWorkbenchActionConstants.MB_ADDITIONS,
			actionSetId, mgr, true);
		// Insert the new group marker.
		ActionSetSeparator group = new ActionSetSeparator(name, actionSetId);
		if (refItem == null) {
			mgr.add(group);
		} else {
			mgr.insertAfter(refItem.getId(), group);
		}
	}
}
/**
 * This factory method returns a new ActionDescriptor for the
 * configuration element.  
 */
protected ActionDescriptor createActionDescriptor(IConfigurationElement element) {
	// As of 2.1, the "pulldown" attribute was deprecated and replaced by
	// the attribute "style". See doc for more details.
	boolean pullDownStyle = false;
	String style = element.getAttribute(ActionDescriptor.ATT_STYLE);
	if (style != null) {
		pullDownStyle = style.equals(ActionDescriptor.STYLE_PULLDOWN);
	} else {
		String pulldown = element.getAttribute(ATT_PULLDOWN);
		pullDownStyle = pulldown != null && pulldown.equals("true"); //$NON-NLS-1$
	}
	
	ActionDescriptor desc = null;
	if (pullDownStyle)
		desc = new ActionDescriptor(element, ActionDescriptor.T_WORKBENCH_PULLDOWN, window);
	else
		desc = new ActionDescriptor(element, ActionDescriptor.T_WORKBENCH, window);
	WWinPluginAction action = (WWinPluginAction)desc.getAction();
	action.setActionSetId(actionSet.getDesc().getId());
	actionSet.addPluginAction(action);
	return desc;
}
/**
 * Returns the insertion point for a new contribution item.  Clients should
 * use this item as a reference point for insertAfter.
 *
 * @param startId the reference id for insertion
 * @param sortId the sorting id for the insertion.  If null then the item
 *		will be inserted at the end of all action sets.
 * @param mgr the target menu manager.
 * @param startVsEnd if <code>true</code> the items are added at the start of
 *		action with the same id; else they are added to the end
 * @return the insertion point, or null if not found.
 */
public static IContributionItem findInsertionPoint(String startId,
	String sortId, IContributionManager mgr, boolean startVsEnd) 
{
	// Get items.
	IContributionItem [] items = mgr.getItems();
	
	// Find the reference item.
	int insertIndex = 0;
	while (insertIndex < items.length) {
		if (startId.equals(items[insertIndex].getId()))
			break;
		++ insertIndex;
	}
	if (insertIndex >= items.length) 
		return null;

	// Calculate startVsEnd comparison value.
	int compareMetric = 0;
	if (startVsEnd)
		compareMetric = 1;
		
	// Find the insertion point for the new item.
	// We do this by iterating through all of the previous
	// action set contributions define within the current group.
	for (int nX = insertIndex + 1; nX < items.length; nX ++) {
		IContributionItem item = items[nX];
		if (item.isSeparator() || item.isGroupMarker()) {
			// Fix for bug report 18357
			break;
		}
		if (item instanceof IActionSetContributionItem) {
			if (sortId != null) {
				String testId = ((IActionSetContributionItem)item).getActionSetId();
				if (sortId.compareTo(testId) < compareMetric)
					break;
			}
			insertIndex = nX;
		} else {
			break;
		}
	}
	// Return item.
	return items[insertIndex];
}
public static IContributionItem findSubInsertionPoint(String startId, String sortId, CoolBarManager mgr, boolean startVsEnd) {
	// Get items.
	IContributionItem [] items = mgr.getItems();
	
	// Find the reference item.
	int insertIndex = 0;
	while (insertIndex < items.length) {
		if (startId.equals(items[insertIndex].getId()))
			break;
		++ insertIndex;
	}
	// look at each the items in each of the CoolBarContribution items
	if (insertIndex >= items.length) {
		insertIndex = 0;
		while (insertIndex < items.length) {
			CoolBarContributionItem item = (CoolBarContributionItem)items[insertIndex];
			IContributionItem foundItem = item.getToolBarManager().find(startId);
			if (foundItem != null)
				break;
			++ insertIndex;
		}
	}
	if (insertIndex >= items.length) 
		return null;

	// Calculate startVsEnd comparison value.
	int compareMetric = 0;
	if (startVsEnd)
		compareMetric = 1;
		
	// Find the insertion point for the new item.  We do this by iterating 
	// through all of the previous action set contributions.  This code 
	// assumes action set contributions are done in alphabetical order.
	for (int nX = insertIndex + 1; nX < items.length; nX ++) {
		CoolBarContributionItem item = (CoolBarContributionItem)items[nX];
		if (item.getItems().length == 0) break;
		IContributionItem subItem = item.getItems()[0];
		if (subItem instanceof IActionSetContributionItem) {
			if (sortId != null) {
				String testId = ((IActionSetContributionItem)subItem).getActionSetId();
				if (sortId.compareTo(testId) < compareMetric)
					break;
			}
			insertIndex = nX;
		} else {
			break;
		}
	}
	// Return item.
	return items[insertIndex];
}
/*
 * Insert a menu separator or group marker. Order is dependent on
 * action set id.
 */
protected void insertMenuGroup(IMenuManager menu, AbstractGroupMarker marker) {
	String actionSetId = actionSet.getDesc().getId();
	if (actionSetId != null) {
		IContributionItem[] items = menu.getItems();
		// Loop thru all the current groups looking for the first
		// group whose id > than the current action set id. Insert
		// current marker just before this item then.
		for (int i = 0; i < items.length; i++) {
			IContributionItem item = items[i];
			if (item.isSeparator() || item.isGroupMarker()) {
				if (item instanceof IActionSetContributionItem) {
					String testId = ((IActionSetContributionItem)item).getActionSetId();
					if (actionSetId.compareTo(testId) < 0) {
						menu.insertBefore(items[i].getId(), marker);
						return;
					}
				}
			}
		}
	}
	
	menu.add(marker);
}
/*
 * Inserts one action after another.  If there are any other extensions
 * for the same action the new action will be inserted into the group in
 * order of action set id.
 */
protected void insertAfter(IContributionManager mgr, String refId, 
	IContributionItem item) 
{
	String actionSetId = actionSet.getDesc().getId();
	IContributionItem refItem = findInsertionPoint(refId, actionSetId, mgr, true);
	if (refItem != null) {
		mgr.insertAfter(refItem.getId(), item);
	} else {
		WorkbenchPlugin.log("Reference item " + refId + " not found for action " + item.getId()); //$NON-NLS-1$
	}
}
/**
 * Read the actions within a config element.
 */
public void readActionExtensions(PluginActionSet set, IWorkbenchWindow window, 
	IActionBars bars) 
{
	this.actionSet = set;
	this.window = window;
	readElements(new IConfigurationElement[] {set.getConfigElement()});
	if (cache != null) {
		contribute(bars.getMenuManager(), bars.getToolBarManager(), true);
	} else {
		WorkbenchPlugin.log("Action Set is empty: " + set.getDesc().getId());//$NON-NLS-1$
	}
}
/**
 * Implements abstract method to handle the provided XML element
 * in the registry.
 */
protected boolean readElement(IConfigurationElement element) {
	String tag = element.getName();
	if (tag.equals(TAG_ACTION_SET)) {
		readElementChildren(element);
		return true;
	}
	return super.readElement(element);
}
}
