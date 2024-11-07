/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.jface.action.AbstractGroupMarker;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.internal.provisional.action.IToolBarContributionItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.registry.ActionSetRegistry;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.services.IDisposable;

/**
 * This builder reads the actions for an action set from the registry.
 */
public class PluginActionSetBuilder extends PluginActionBuilder {

	private PluginActionSet actionSet;

	private IWorkbenchWindow window;

	private ArrayList<ActionSetContribution> adjunctContributions = new ArrayList<>(0);

	/**
	 * Used by the workbench window extension handler to unhook action sets from
	 * their associated window.
	 *
	 * @since 3.1
	 */
	public static class Binding implements IDisposable {
		PluginActionSetBuilder builder;
		PluginActionSet set;
		IWorkbenchWindow window;
		IExtensionTracker tracker;

		@Override
		public void dispose() {
			if (tracker != null) {
				tracker.unregisterObject(set.getConfigElement().getDeclaringExtension(), this);
				tracker = null;
			}
		}
	}

	/**
	 * Constructs a new builder.
	 */
	public PluginActionSetBuilder() {
	}

	/**
	 * Read the actions within a config element. Called by customize perspective
	 *
	 * @param set    the action set
	 * @param window the window to contribute to
	 */
	public void buildMenuAndToolBarStructure(PluginActionSet set, IWorkbenchWindow window) {
		this.actionSet = set;
		this.window = window;
		cache = null;
		currentContribution = null;
		targetID = null;
		targetContributionTag = IWorkbenchRegistryConstants.TAG_ACTION_SET;

		readElements(new IConfigurationElement[] { set.getConfigElement() });

		if (cache != null) {
			for (Object element : cache) {
				ActionSetContribution contribution = (ActionSetContribution) element;
				contribution.contribute(actionSet.getBars(), true, true);
				if (contribution.isAdjunctContributor()) {
					adjunctContributions.add(contribution);
				}
			}
		}
		for (ActionSetContribution contribution : adjunctContributions) {
			ActionSetActionBars bars = actionSet.getBars();
			for (ActionDescriptor adjunctAction : contribution.adjunctActions) {
				contribution.contributeAdjunctCoolbarAction(adjunctAction, bars);
			}
		}

		registerBinding(set);
	}

	@Override
	protected ActionDescriptor createActionDescriptor(IConfigurationElement element) {
		// As of 2.1, the "pulldown" attribute was deprecated and replaced by
		// the attribute "style". See doc for more details.
		boolean pullDownStyle = false;
		String style = element.getAttribute(IWorkbenchRegistryConstants.ATT_STYLE);
		if (style != null) {
			pullDownStyle = style.equals(ActionDescriptor.STYLE_PULLDOWN);
		} else {
			String pulldown = element.getAttribute(ActionDescriptor.STYLE_PULLDOWN);
			pullDownStyle = pulldown != null && pulldown.equals("true"); //$NON-NLS-1$
		}

		ActionDescriptor desc = null;
		if (pullDownStyle) {
			desc = new ActionDescriptor(element, ActionDescriptor.T_WORKBENCH_PULLDOWN, window);
		} else {
			desc = new ActionDescriptor(element, ActionDescriptor.T_WORKBENCH, window);
		}
		WWinPluginAction action = (WWinPluginAction) desc.getAction();
		action.setActionSetId(actionSet.getDesc().getId());
		actionSet.addPluginAction(action);
		return desc;
	}

	@Override
	protected BasicContribution createContribution() {
		return new ActionSetContribution(actionSet.getDesc().getId(), window);
	}

	/**
	 * Returns the insertion point for a new contribution item. Clients should use
	 * this item as a reference point for insertAfter.
	 *
	 * @param startId    the reference id for insertion
	 * @param sortId     the sorting id for the insertion. If null then the item
	 *                   will be inserted at the end of all action sets.
	 * @param mgr        the target menu manager.
	 * @param startVsEnd if <code>true</code> the items are added at the start of
	 *                   action with the same id; else they are added to the end
	 * @return the insertion point, or null if not found.
	 */
	public static IContributionItem findInsertionPoint(String startId, String sortId, IContributionManager mgr,
			boolean startVsEnd) {
		// Get items.
		IContributionItem[] items = mgr.getItems();

		// Find the reference item.
		int insertIndex = 0;
		while (insertIndex < items.length) {
			if (startId.equals(items[insertIndex].getId())) {
				break;
			}
			++insertIndex;
		}
		if (insertIndex >= items.length) {
			return null;
		}

		// Calculate startVsEnd comparison value.
		int compareMetric = 0;
		if (startVsEnd) {
			compareMetric = 1;
		}

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
					if (sortId.compareTo(testId) < compareMetric) {
						break;
					}
				}
				insertIndex = nX;
			} else {
				break;
			}
		}
		// Return item.
		return items[insertIndex];
	}

	/* package */static void processActionSets(List<PluginActionSet> pluginActionSets, WorkbenchWindow window) {
		// Process the action sets in two passes. On the first pass the
		// pluginActionSetBuilder
		// will process base contributions and cache adjunct contributions. On the
		// second
		// pass the adjunct contributions will be processed.
		PluginActionSetBuilder[] builders = new PluginActionSetBuilder[pluginActionSets.size()];
		for (int i = 0; i < pluginActionSets.size(); i++) {
			PluginActionSet set = pluginActionSets.get(i);
			PluginActionSetBuilder builder = new PluginActionSetBuilder();
			builder.readActionExtensions(set, window);
			builders[i] = builder;
		}
		for (PluginActionSetBuilder builder : builders) {
			builder.processAdjunctContributions();
		}
	}

	protected void processAdjunctContributions() {
		// Contribute the adjunct contributions.
		for (ActionSetContribution contribution : adjunctContributions) {
			ActionSetActionBars bars = actionSet.getBars();
			for (ActionDescriptor adjunctAction : contribution.adjunctActions) {
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
		targetContributionTag = IWorkbenchRegistryConstants.TAG_ACTION_SET;

		readElements(new IConfigurationElement[] { set.getConfigElement() });

		if (cache != null) {
			// for dynamic UI - save cache for future removal lf actionset extensions
			// Don't call addCache -- it's broken, and is only used for dynamic plugin
			// removal,
			// which the workbench doesn't currently support.
			// See bug 66374 for more details.
			// WorkbenchPlugin.getDefault().getActionSetRegistry().addCache(set.getDesc().getId(),
			// cache);
			for (Object element : cache) {
				ActionSetContribution contribution = (ActionSetContribution) element;
				contribution.contribute(actionSet.getBars(), true, true);
				if (contribution.isAdjunctContributor()) {
					adjunctContributions.add(contribution);
				}
			}

			registerBinding(set);

		} else {
			WorkbenchPlugin.log("Action Set is empty: " + set.getDesc().getId()); //$NON-NLS-1$
		}
	}

	private void registerBinding(final PluginActionSet set) {
		final IExtensionTracker tracker = window.getExtensionTracker();

		// register the new binding
		final Binding binding = new Binding();
		binding.builder = this;
		binding.set = set;
		binding.window = window;
		binding.tracker = tracker;
		tracker.registerObject(set.getConfigElement().getDeclaringExtension(), binding, IExtensionTracker.REF_STRONG);
		set.setBuilder(binding);
	}

	/**
	 * Helper class to collect the menus and actions defined within a contribution
	 * element.
	 */
	private static class ActionSetContribution extends BasicContribution {
		private String actionSetId;

		private WorkbenchWindow window;

		protected ArrayList<ActionDescriptor> adjunctActions = new ArrayList<>(0);

		/**
		 * Create a new instance of <code>ActionSetContribution</code>.
		 *
		 * @param id     the id
		 * @param window the window to contribute to
		 */
		public ActionSetContribution(String id, IWorkbenchWindow window) {
			super();
			actionSetId = id;
			this.window = (WorkbenchWindow) window;
		}

		/**
		 * This implementation inserts the group into the action set additions group.
		 */
		@Override
		protected void addGroup(IContributionManager mgr, String name) {
			IContributionItem refItem = findInsertionPoint(IWorkbenchActionConstants.MB_ADDITIONS, actionSetId, mgr,
					true);
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
		 *
		 * @param bars                the action bars to contribute to
		 * @param menuAppendIfMissing append to the menubar if missing
		 * @param toolAppendIfMissing append to the toolbar if missing
		 */
		public void contribute(IActionBars bars, boolean menuAppendIfMissing, boolean toolAppendIfMissing) {

			IMenuManager menuMgr = bars.getMenuManager();
			IToolBarManager toolBarMgr = bars.getToolBarManager();
			if (menus != null && menuMgr != null) {
				for (IConfigurationElement menuElement : menus) {
					contributeMenu(menuElement, menuMgr, menuAppendIfMissing);
				}
			}

			if (actions != null) {
				for (ActionDescriptor ad : actions) {
					if (menuMgr != null) {
						contributeMenuAction(ad, menuMgr, menuAppendIfMissing);
					}
					if (toolBarMgr != null) {
						if (bars instanceof ActionSetActionBars) {
							contributeCoolbarAction(ad, (ActionSetActionBars) bars);
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

			String contributingId = bars.getActionSetId();
			ICoolBarManager coolBarMgr = bars.getCoolBarManager();
			if (coolBarMgr == null) {
				return;
			}

			PluginAction action = ad.getAction();
			ActionContributionItem actionContribution = new PluginActionCoolBarContributionItem(action);
			actionContribution.setMode(ad.getMode());

			bars.addAdjunctContribution(actionContribution);

			// create a coolitem for the toolbar id if it does not yet exist
			IToolBarManager toolBarManager = bars.getToolBarManager(toolBarId);

			// Check to see if the group already exists
			IContributionItem groupMarker = toolBarManager.find(toolGroupId);
			// Add a group marker if one does not exist
			if (groupMarker == null) {
				toolBarManager.add(new Separator(toolGroupId));
			}
			IContributionItem refItem = findAlphabeticalOrder(toolGroupId, contributingId, toolBarManager);
			if (refItem != null && refItem.getId() != null) {
				toolBarManager.insertAfter(refItem.getId(), actionContribution);
			} else {
				toolBarManager.add(actionContribution);
			}
			toolBarManager.update(false);

		}

		/**
		 * Contributes action from the action descriptor into the cool bar manager.
		 */
		protected void contributeCoolbarAction(ActionDescriptor ad, ActionSetActionBars bars) {
			String toolBarId = ad.getToolbarId();
			String toolGroupId = ad.getToolbarGroupId();
			if (toolBarId == null && toolGroupId == null) {
				return;
			}

			String contributingId = bars.getActionSetId();

			if (toolBarId == null || toolBarId.isEmpty()) {
				// the item is being added to the coolitem for its action set
				toolBarId = contributingId;
			}

			if (!toolBarId.equals(contributingId)) {
				// adding to another action set, validate the id
				if (!isValidCoolItemId(toolBarId, window)) {
					// toolbarid not valid, add the item to the coolitem for its action set
					toolBarId = contributingId;
				} else {
					adjunctActions.add(ad);
					return;
				}
			}

			// Create the action
			PluginAction action = ad.getAction();
			ActionContributionItem actionContribution = new PluginActionCoolBarContributionItem(action);
			actionContribution.setMode(ad.getMode());

			// retreive the toolbar from the action bars.
			IToolBarManager toolBar = bars.getToolBarManager(toolBarId);

			// Check to see if the group already exists
			IContributionItem groupMarker = toolBar.find(toolGroupId);
			// Add a group marker if one does not exist
			if (groupMarker == null) {
				// @issue should this be a GroupMarker?
				toolBar.add(new Separator(toolGroupId));
			}
			toolBar.prependToGroup(toolGroupId, actionContribution);
			toolBar.update(false);

		}

		/**
		 * Checks to see if the cool item id is in the given window.
		 */
		private boolean isValidCoolItemId(String id, WorkbenchWindow window) {
			ActionSetRegistry registry = WorkbenchPlugin.getDefault().getActionSetRegistry();
			if (registry.findActionSet(id) != null) {
				return true;
			}
			if (window != null) {
				return window.isWorkbenchCoolItemId(id);
			}
			return false;
		}

		@Override
		protected void insertMenuGroup(IMenuManager menu, AbstractGroupMarker marker) {
			if (actionSetId != null) {
				IContributionItem[] items = menu.getItems();
				// Loop thru all the current groups looking for the first
				// group whose id > than the current action set id. Insert
				// current marker just before this item then.
				for (IContributionItem item : items) {
					if (item.isSeparator() || item.isGroupMarker()) {
						if (item instanceof IActionSetContributionItem) {
							String testId = ((IActionSetContributionItem) item).getActionSetId();
							if (actionSetId.compareTo(testId) < 0) {
								menu.insertBefore(item.getId(), marker);
								return;
							}
						}
					}
				}
			}

			menu.add(marker);
		}

		private IContributionItem findAlphabeticalOrder(String startId, String itemId, IContributionManager mgr) {
			IContributionItem[] items = mgr.getItems();
			int insertIndex = 0;

			// look for starting point
			while (insertIndex < items.length) {
				IContributionItem item = items[insertIndex];
				if (startId != null && startId.equals(item.getId())) {
					break;
				}
				++insertIndex;
			}

			// Find the index that this item should be inserted in
			for (int i = insertIndex + 1; i < items.length; i++) {
				IContributionItem item = items[i];
				if (item.isGroupMarker()) {
					break;
				}

				String testId = null;
				if (item instanceof PluginActionCoolBarContributionItem) {
					testId = ((PluginActionCoolBarContributionItem) item).getActionSetId();
				}
				if (testId == null) {
					break;
				}

				if (itemId != null) {
					if (itemId.compareTo(testId) < 1) {
						break;
					}
				}
				insertIndex = i;
			}
			if (insertIndex >= items.length) {
				return null;
			}
			return items[insertIndex];
		}

		/**
		 * Returns whether the contributor is an adjunct contributor.
		 *
		 * @return whether the contributor is an adjunct contributor
		 */
		public boolean isAdjunctContributor() {
			return adjunctActions.size() > 0;
		}

		@Override
		protected void insertAfter(IContributionManager mgr, String refId, IContributionItem item) {
			IContributionItem refItem = findInsertionPoint(refId, actionSetId, mgr, true);
			if (refItem != null) {
				mgr.insertAfter(refItem.getId(), item);
			} else {
				WorkbenchPlugin.log("Reference item " + refId + " not found for action " + item.getId()); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		// for dynamic UI
		private void revokeContribution(WorkbenchWindow window, String id) {
			revokeActionSetFromMenu(window.getMenuManager(), id);
			revokeActionSetFromCoolbar(window.getCoolBarManager2(), id);
		}

		// for dynamic UI
		protected void revokeAdjunctCoolbarAction(ActionDescriptor ad, ActionSetActionBars bars) {
			String toolBarId = ad.getToolbarId();
//            String toolGroupId = ad.getToolbarGroupId();
//
//            String contributingId = bars.getActionSetId();
			ICoolBarManager coolBarMgr = bars.getCoolBarManager();
			// ((CoolItemToolBarManager)bars.getToolBarManager()).getParentManager();
			PluginAction action = ad.getAction();
			PluginActionCoolBarContributionItem actionContribution = new PluginActionCoolBarContributionItem(action);
			actionContribution.setMode(ad.getMode());

			bars.removeAdjunctContribution(actionContribution);

			// remove a coolitem for the toolbar id if it exists
			IContributionItem cbItem = coolBarMgr.find(toolBarId);
			if (cbItem != null) {
				coolBarMgr.remove(cbItem);
			}

			// activeManager = cbItem.getToolBarManager();
			// activeManager.remove(contributingId);
			// IContributionItem groupMarker = activeManager.find(toolGroupId);
			// if (groupMarker != null) {
			// int idx = activeManager.indexOf(toolGroupId);
			// IContributionItem[] items = activeManager.getItems();
			// if (items.length == idx+1 ||
			// ((items.length > idx && items[idx+1] instanceof Separator)))
			// if (activeManager.find(toolGroupId) != null)
			// activeManager.remove(toolGroupId);
			// }
			// activeManager.addAdjunctItemToGroup(toolGroupId, contributingId,
			// actionContribution);
		}

		// for dynamic UI
		private void revokeActionSetFromMenu(IMenuManager menuMgr, String actionsetId) {
			IContributionItem[] items = menuMgr.getItems();
			ArrayList<IContributionItem> itemsToRemove = new ArrayList<>();
			String id;
			for (IContributionItem item : items) {
				if (item instanceof IMenuManager) {
					revokeActionSetFromMenu((IMenuManager) item, actionsetId);
				} else if (item instanceof ActionSetContributionItem) {
					id = ((ActionSetContributionItem) item).getActionSetId();
					if (actionsetId.equals(id)) {
						itemsToRemove.add(item);
					}
				} else if (item instanceof Separator) {
					id = ((Separator) item).getId();
					if (actionsetId.equals(id)) {
						itemsToRemove.add(item);
					}
				} else if (item instanceof GroupMarker) {
					id = ((GroupMarker) item).getId();
					if (actionsetId.equals(id)) {
						itemsToRemove.add(item);
					}
				}
			}
			Iterator<IContributionItem> iter = itemsToRemove.iterator();
			while (iter.hasNext()) {
				IContributionItem item = iter.next();
				menuMgr.remove(item);
			}
			menuMgr.update(true);
		}

		// for dynamic UI
		private void revokeActionSetFromCoolbar(ICoolBarManager coolbarMgr, String actionsetId) {
			IContributionItem[] items = coolbarMgr.getItems();
			ArrayList<IContributionItem> itemsToRemove = new ArrayList<>();
			String id;
			for (IContributionItem item : items) {
				id = item.getId();
				if (actionsetId.equals(id)) {
					itemsToRemove.add(item);
					continue;
				}
				if (item instanceof IToolBarManager) {
					revokeActionSetFromToolbar((IToolBarManager) item, actionsetId);
				} else if (item instanceof IToolBarContributionItem) {
					id = item.getId();
					if (actionsetId.equals(id)) {
						itemsToRemove.add(item);
					}
				} else if (item instanceof GroupMarker) {
					id = ((GroupMarker) item).getId();
					if (actionsetId.equals(id)) {
						itemsToRemove.add(item);
					}
				}
			}
			Iterator<IContributionItem> iter = itemsToRemove.iterator();
			while (iter.hasNext()) {
				coolbarMgr.remove(iter.next());
			}
			coolbarMgr.update(true);
		}

		// for dynamic UI
		private void revokeActionSetFromToolbar(IToolBarManager toolbarMgr, String actionsetId) {
			IContributionItem[] items = toolbarMgr.getItems();
			ArrayList<IContributionItem> itemsToRemove = new ArrayList<>();
			String id;
			for (IContributionItem item : items) {
				id = item.getId();
				if (id.equals(actionsetId)) {
					itemsToRemove.add(item);
					continue;
				}
				if (item instanceof PluginActionCoolBarContributionItem) {
					id = ((PluginActionCoolBarContributionItem) item).getActionSetId();
					if (actionsetId.equals(id)) {
						itemsToRemove.add(item);
					}
				} else if (item instanceof ActionContributionItem) {
					id = ((ActionContributionItem) item).getId();
					if (actionsetId.equals(id)) {
						itemsToRemove.add(item);
					}
				} else if (item instanceof GroupMarker) {
					id = ((GroupMarker) item).getId();
					if (actionsetId.equals(id)) {
						itemsToRemove.add(item);
					}
				}
			}
			Iterator<IContributionItem> iter = itemsToRemove.iterator();
			while (iter.hasNext()) {
				toolbarMgr.remove(iter.next());
			}
			toolbarMgr.update(true);
		}
	}

	/**
	 * Remove the given action set from the window.
	 *
	 * @param set    the set to remove
	 * @param window the window to remove from
	 */
	protected void removeActionExtensions(PluginActionSet set, IWorkbenchWindow window) {
		this.actionSet = set;
		this.window = window;
		currentContribution = null;
		targetID = null;
		targetContributionTag = IWorkbenchRegistryConstants.TAG_ACTION_SET;
		String id = set.getDesc().getId();

		if (cache != null) {
			for (Object element : cache) {
				ActionSetContribution contribution = (ActionSetContribution) element;
				contribution.revokeContribution((WorkbenchWindow) window, id);
				if (contribution.isAdjunctContributor()) {
					for (ActionDescriptor adjunctAction : contribution.adjunctActions) {
						contribution.revokeAdjunctCoolbarAction(adjunctAction, actionSet.getBars());
					}
				}
			}
		}
	}
}
