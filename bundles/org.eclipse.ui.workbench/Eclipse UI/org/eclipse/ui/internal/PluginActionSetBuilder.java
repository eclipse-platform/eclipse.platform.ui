package org.eclipse.ui.internal;

/************************************************************************
Copyright (c) 2000, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

import java.util.ArrayList;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.*;
import org.eclipse.ui.*;

/**
 * This builder reads the actions for an action set from the registry.
 */
public class PluginActionSetBuilder extends PluginActionBuilder {
	public static final String TAG_ACTION_SET = "actionSet"; //$NON-NLS-1$
	// As of 2.1, the "pulldown" attribute is deprecated, use "style" attribute instead.
	public static final String ATT_PULLDOWN = "pulldown"; //$NON-NLS-1$

	private PluginActionSet actionSet;
	private IWorkbenchWindow window;
	private ArrayList adjunctContributions = new ArrayList(0);
	/**
	 * Constructs a new builder.
	 */
	public PluginActionSetBuilder() {
	}
	/* (non-Javadoc)
	 * Method declared on PluginActionBuilder.
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
		WWinPluginAction action = (WWinPluginAction) desc.getAction();
		action.setActionSetId(actionSet.getDesc().getId());
		actionSet.addPluginAction(action);
		return desc;
	}
	
	/* (non-Javadoc)
	 * Method declared on PluginActionBuilder.
	 */
	protected BasicContribution createContribution() {
		return new ActionSetContribution(actionSet.getDesc().getId(), window);
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
	public static IContributionItem findInsertionPoint(String startId, String sortId, IContributionManager mgr, boolean startVsEnd) {
		// Get items.
		IContributionItem[] items = mgr.getItems();

		// Find the reference item.
		int insertIndex = 0;
		while (insertIndex < items.length) {
			if (startId.equals(items[insertIndex].getId()))
				break;
			++insertIndex;
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
		for (int nX = insertIndex + 1; nX < items.length; nX++) {
			IContributionItem item = items[nX];
			if (item.isSeparator() || item.isGroupMarker()) {
				// Fix for bug report 18357
				break;
			}
			if (item instanceof IActionSetContributionItem) {
				if (sortId != null) {
					String testId = ((IActionSetContributionItem) item).getActionSetId();
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
	/**
	 */
	/* package */ static void processActionSets(ArrayList pluginActionSets, WorkbenchWindow window) {
		// Process the action sets in two passes.  On the first pass the pluginActionSetBuilder
		// will process base contributions and cache adjunct contributions.  On the second
		// pass the adjunct contributions will be processed.
		PluginActionSetBuilder[] builders = new PluginActionSetBuilder[pluginActionSets.size()];
		for (int i =0; i<pluginActionSets.size(); i++) {
			PluginActionSet set = (PluginActionSet)pluginActionSets.get(i);
			PluginActionSetBuilder builder = new PluginActionSetBuilder();
			builder.readActionExtensions(set, window);
			builders[i] = builder;
		}
		for (int i =0; i<builders.length; i++) {
			PluginActionSetBuilder builder = builders[i];
			builder.processAdjunctContributions();
		}
	}
	/**
	 */
	protected void processAdjunctContributions() {
		// Contribute the adjunct contributions.
		for (int i=0; i<adjunctContributions.size(); i++) {
			ActionSetContribution contribution = (ActionSetContribution)adjunctContributions.get(i);
			ActionSetActionBars bars = actionSet.getBars();
			for (int j=0; j< contribution.adjunctActions.size(); j++) {
				ActionDescriptor adjunctAction = (ActionDescriptor)contribution.adjunctActions.get(j);
				contribution.contributeAdjunctCoolbarAction(adjunctAction, bars);
			}
		}
	}
	/**
	 * Read the actions within a config element.
	 */
	protected void readActionExtensions(PluginActionSet set, IWorkbenchWindow window) {
		this.actionSet = set;
		this.window = window;
		cache = null;
		currentContribution = null;
		targetID = null;
		targetContributionTag = TAG_ACTION_SET;
		
		readElements(new IConfigurationElement[] {set.getConfigElement()});
		
		if (cache != null) {
			for (int i = 0; i < cache.size(); i++) {
				ActionSetContribution contribution = (ActionSetContribution)cache.get(i);
				contribution.contribute(actionSet.getBars(), true, true);
				if (contribution.isAdjunctContributor()) {
					adjunctContributions.add(contribution);
				}
			}
		} else {
			WorkbenchPlugin.log("Action Set is empty: " + set.getDesc().getId()); //$NON-NLS-1$
		}
	}
	/**
	 * Helper class to collect the menus and actions defined within a
	 * contribution element.
	 */
	private static class ActionSetContribution extends BasicContribution {
		private String actionSetId;
		private WorkbenchWindow window;
		private ArrayList adjunctActions = new ArrayList(0);
		
		public ActionSetContribution(String id, IWorkbenchWindow window) {
			super();
			actionSetId = id;
			this.window = (WorkbenchWindow)window;
		}
		
		/**
		 * This implementation inserts the group into the action set additions group.  
		 */
		protected void addGroup(IContributionManager mgr, String name) {
			IContributionItem refItem = findInsertionPoint(IWorkbenchActionConstants.MB_ADDITIONS, actionSetId, mgr, true);
			// Insert the new group marker.
			ActionSetSeparator group = new ActionSetSeparator(name, actionSetId);
			if (refItem == null) {
				mgr.add(group);
			} else {
				mgr.insertAfter(refItem.getId(), group);
			}
		}
		/**
		 * Contributes submenus and/or actions into the provided menu and tool bar
		 * managers.
		 */
		public void contribute(IActionBars bars, boolean menuAppendIfMissing, boolean toolAppendIfMissing) {
			IMenuManager menuMgr = bars.getMenuManager();
			IToolBarManager toolBarMgr = bars.getToolBarManager();
			if (menus != null && menuMgr != null) {
				for (int i = 0; i < menus.size(); i++) {
					IConfigurationElement menuElement = (IConfigurationElement) menus.get(i);
					contributeMenu(menuElement, menuMgr, menuAppendIfMissing);
				}
			}
			boolean isCoolBarContribution = (toolBarMgr != null) && (toolBarMgr instanceof CoolItemToolBarManager);
			if (actions != null) {
				for (int i = 0; i < actions.size(); i++) {
					ActionDescriptor ad = (ActionDescriptor) actions.get(i);
					if (menuMgr != null)
						contributeMenuAction(ad, menuMgr, menuAppendIfMissing);
					if (toolBarMgr != null) {
						if (isCoolBarContribution) {
							contributeCoolbarAction(ad, (ActionSetActionBars)bars);
						} else {
							contributeToolbarAction(ad, toolBarMgr, toolAppendIfMissing);
						}
					}
				}
			}
		}
		/**
		 * Contributes action from the action descriptor into the cool bar manager.
		 */
		protected void contributeAdjunctCoolbarAction(ActionDescriptor ad, ActionSetActionBars bars) {
			String toolBarId = ad.getToolbarId();
			String toolGroupId = ad.getToolbarGroupId();
			String beforeGroupId = ad.getBeforeToolbarGroupId();;

			String contributingId = bars.getActionSetId();
			CoolBarManager coolBarMgr = ((CoolItemToolBarManager)bars.getToolBarManager()).getParentManager();
			PluginAction action = ad.getAction();
			PluginActionCoolBarContributionItem actionContribution = new PluginActionCoolBarContributionItem(action);
			
			bars.addAdjunctContribution(actionContribution);

			// create a coolitem for the toolbar id if it does not yet exist				
			CoolItemToolBarManager activeManager;
			CoolBarContributionItem cbItem = (CoolBarContributionItem)coolBarMgr.find(toolBarId);
			if (cbItem == null) {
				cbItem = new CoolBarContributionItem(coolBarMgr, toolBarId); 
	 			cbItem.setVisible(true);
				IContributionItem refItem = findCoolItemInsertionPoint(toolBarId, coolBarMgr);
				if (refItem == null) {
					coolBarMgr.add(cbItem);
				} else {
					coolBarMgr.insertAfter(refItem.getId(), cbItem);
				}
			}
			
			activeManager = cbItem.getToolBarManager();		
			IContributionItem groupMarker = activeManager.find(toolGroupId);
			if (groupMarker == null) {
				activeManager.addAdjunctGroupBefore(toolGroupId, contributingId, beforeGroupId);
			}
			activeManager.addToGroup(toolGroupId, contributingId, actionContribution);		 
		}
		/**
		 * Contributes action from the action descriptor into the cool bar manager.
		 */
		protected void contributeCoolbarAction(ActionDescriptor ad, ActionSetActionBars bars) {
			String toolBarId = ad.getToolbarId();
			String toolGroupId = ad.getToolbarGroupId();
			String beforeGroupId = ad.getBeforeToolbarGroupId();;
			if (toolBarId == null && toolGroupId == null)return;

			String contributingId = bars.getActionSetId();
			CoolBarManager coolBarMgr = ((CoolItemToolBarManager)bars.getToolBarManager()).getParentManager();
			
			if (toolBarId == null || toolBarId.equals("Normal") || toolBarId.equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
				// the item is being added to the coolitem for its action set
				toolBarId = contributingId;
			} 

			if (!toolBarId.equals(contributingId)) {
				// adding to another action set, validate the id
				if (!coolBarMgr.isValidCoolItemId(toolBarId)) {
					// toolbarid not valid, add the item to the coolitem for its action set
					toolBarId = contributingId;
				} else {
					adjunctActions.add(ad);
					return;
				}	
			} 
				
			PluginAction action = ad.getAction();
			PluginActionCoolBarContributionItem actionContribution = new PluginActionCoolBarContributionItem(action);

			// create a coolitem for the toolbar id if it does not yet exist
			CoolItemToolBarManager activeManager;
			CoolBarContributionItem cbItem = (CoolBarContributionItem)coolBarMgr.find(toolBarId);
			if (cbItem == null) {
				activeManager = (CoolItemToolBarManager)bars.getToolBarManager();
				cbItem = activeManager.getCoolBarItem();
	 			cbItem.setVisible(true);
				IContributionItem refItem = findCoolItemInsertionPoint(toolBarId, coolBarMgr);
				if (refItem == null) {
					coolBarMgr.add(cbItem);
				} else {
					coolBarMgr.insertAfter(refItem.getId(), cbItem);
				}
			}
			
			activeManager = cbItem.getToolBarManager();		
			IContributionItem groupMarker = activeManager.find(toolGroupId);
			if (groupMarker == null) {
				activeManager.addBaseGroup(toolGroupId);
			}
			activeManager.addToGroup(toolGroupId, contributingId, actionContribution);		 
		}
		/* (non-Javadoc)
		 * Method declared on Basic Contribution.
		 */
		protected void insertMenuGroup(IMenuManager menu, AbstractGroupMarker marker) {
			if (actionSetId != null) {
				IContributionItem[] items = menu.getItems();
				// Loop thru all the current groups looking for the first
				// group whose id > than the current action set id. Insert
				// current marker just before this item then.
				for (int i = 0; i < items.length; i++) {
					IContributionItem item = items[i];
					if (item.isSeparator() || item.isGroupMarker()) {
						if (item instanceof IActionSetContributionItem) {
							String testId = ((IActionSetContributionItem) item).getActionSetId();
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
		public IContributionItem findCoolItemInsertionPoint(String sortId, CoolBarManager mgr) {
			// Get items.
			IContributionItem[] items = mgr.getItems();
			String startId = IWorkbenchActionConstants.MB_ADDITIONS;
	
			// Find the reference item.
			int insertIndex = 0;
			// looking for the org.eclipse.ui.workbench.file toolbar
			while (insertIndex < items.length) {
				CoolBarContributionItem item = (CoolBarContributionItem) items[insertIndex];
				IContributionItem foundItem = item.getToolBarManager().find(startId);
				if (foundItem != null)
					break;
				++insertIndex;
			}
			if (insertIndex >= items.length)
				return null;
	
			// Find the insertion point for the new item.  We do this by iterating 
			// through all of the previous action set contributions.  This code 
			// assumes action set contributions are done in alphabetical order.
			for (int i = insertIndex + 1; i < items.length; i++) {
				CoolBarContributionItem item = (CoolBarContributionItem) items[i];
				String testId = item.getId();
				if (window != null) {
					if (window.isWorkbenchCoolItemId(testId)) break;
				}
				// sort based only on existing action sets
				if (sortId != null) {
					if (sortId.compareTo(testId) < 1)
						break;
				}
				insertIndex = i;
			}
			// Return item.
			return items[insertIndex];
		}
		public boolean isAdjunctContributor() {
			return adjunctActions.size() > 0;
		}
		/* (non-Javadoc)
		 * Method declared on Basic Contribution.
		 */
		protected void insertAfter(IContributionManager mgr, String refId, IContributionItem item) {
			IContributionItem refItem = findInsertionPoint(refId, actionSetId, mgr, true);
			if (refItem != null) {
				mgr.insertAfter(refItem.getId(), item);
			} else {
				WorkbenchPlugin.log("Reference item " + refId + " not found for action " + item.getId()); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}
}
