/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;


import java.util.ArrayList;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.*;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.registry.*;

/**
 * This class contains shared functionality for reading action contributions
 * from plugins into workbench parts (both editors and views).
 */
public abstract class PluginActionBuilder extends RegistryReader {
	public static final String TAG_MENU = "menu"; //$NON-NLS-1$
	public static final String TAG_ACTION = "action"; //$NON-NLS-1$
	public static final String TAG_SEPARATOR = "separator"; //$NON-NLS-1$
	public static final String TAG_GROUP_MARKER = "groupMarker"; //$NON-NLS-1$
	public static final String TAG_FILTER = "filter"; //$NON-NLS-1$
	public static final String TAG_VISIBILITY = "visibility"; //$NON-NLS-1$
	public static final String TAG_ENABLEMENT = "enablement"; //$NON-NLS-1$
	public static final String TAG_SELECTION = "selection"; //$NON-NLS-1$

	public static final String ATT_TARGET_ID = "targetID"; //$NON-NLS-1$
	public static final String ATT_ID = "id"; //$NON-NLS-1$
	public static final String ATT_LABEL = "label"; //$NON-NLS-1$
	public static final String ATT_ENABLES_FOR = "enablesFor"; //$NON-NLS-1$
	public static final String ATT_NAME = "name"; //$NON-NLS-1$
	public static final String ATT_PATH = "path"; //$NON-NLS-1$

	protected String targetID;
	protected String targetContributionTag;
	protected BasicContribution currentContribution;
	protected ArrayList cache;

	/**
	 * The default constructor.
	 */
	public PluginActionBuilder() {
	}

	/**
	 * Contributes submenus and/or actions into the provided menu and tool bar
	 * managers.
	 */
	public final void contribute(IMenuManager menu, IToolBarManager toolbar, boolean appendIfMissing) {
		if (cache == null)
			return;

		for (int i = 0; i < cache.size(); i++) {
			BasicContribution contribution = (BasicContribution)cache.get(i);
			contribution.contribute(menu, appendIfMissing, toolbar, appendIfMissing);
		}
	}
	
	/**
	 * This factory method returns a new ActionDescriptor for the
	 * configuration element.  It should be implemented by subclasses.
	 */
	protected abstract ActionDescriptor createActionDescriptor(IConfigurationElement element);
	
	/**
	 * Factory method to create the helper contribution class that will hold
	 * onto the menus and actions contributed.
	 */
	protected BasicContribution createContribution() {
		return new BasicContribution();
	}
	
	/**
	 * Returns the name of the part ID attribute that is expected
	 * in the target extension.
	 */
	protected String getTargetID(IConfigurationElement element) {
		String value = element.getAttribute(ATT_TARGET_ID);
		return value != null ? value : "???"; //$NON-NLS-1$
	}
	
	/**
	 * Reads the contributions from the registry for the provided workbench
	 * part and the provided extension point ID.
	 */
	protected void readContributions(String id, String tag, String extensionPoint) {
		cache = null;
		currentContribution = null;
		targetID = id;
		targetContributionTag = tag;
		readRegistry(Platform.getPluginRegistry(), PlatformUI.PLUGIN_ID, extensionPoint);
	}
	
	/**
	 * Implements abstract method to handle the provided XML element
	 * in the registry.
	 */
	protected boolean readElement(IConfigurationElement element) {
		String tag = element.getName();

		// Ignore all object contributions element as these
		// are handled by the ObjectActionContributorReader.
		if (tag.equals(ObjectActionContributorReader.TAG_OBJECT_CONTRIBUTION)) {
			return true;
		}

		// Found top level contribution element		
		if (tag.equals(targetContributionTag)) {
			if (targetID != null) {
				// Ignore contributions not matching target id
				String id = getTargetID(element);
				if (id == null || !id.equals(targetID))
					return true;
			}
			
			// Read it's sub-elements
			currentContribution = createContribution();
			readElementChildren(element);
			if (cache == null)
				cache = new ArrayList(4);
			cache.add(currentContribution);
			currentContribution = null;
			return true;
		}
		

		// Found menu contribution sub-element		
		if (tag.equals(TAG_MENU)) {
			currentContribution.addMenu(element);
			return true;
		}
		
		// Found action contribution sub-element
		if (tag.equals(TAG_ACTION)) {
			currentContribution.addAction(createActionDescriptor(element));
			return true;
		}
		
		return false;
	}

	
	/**
	 * Helper class to collect the menus and actions defined within a
	 * contribution element.
	 */
	protected static class BasicContribution {
		protected ArrayList menus;
		protected ArrayList actions;
		
		public void addMenu(IConfigurationElement element) {
			if (menus == null)
				menus = new ArrayList(1);
			menus.add(element);
		}
		
		public void addAction(ActionDescriptor desc) {
			if (actions == null)
				actions = new ArrayList(3);
			actions.add(desc);
		}

		/**
		 * Contributes submenus and/or actions into the provided menu and tool bar
		 * managers.
		 */
		public void contribute(IMenuManager menu, boolean menuAppendIfMissing, IToolBarManager toolbar, boolean toolAppendIfMissing) {
			if (menus != null && menu != null) {
				for (int i = 0; i < menus.size(); i++) {
					IConfigurationElement menuElement = (IConfigurationElement) menus.get(i);
					contributeMenu(menuElement, menu, menuAppendIfMissing);
				}
			}
			
			if (actions != null) {
				for (int i = 0; i < actions.size(); i++) {
					ActionDescriptor ad = (ActionDescriptor) actions.get(i);
					if (menu != null)
						contributeMenuAction(ad, menu, menuAppendIfMissing);
					if (toolbar != null)
						contributeToolbarAction(ad, toolbar, toolAppendIfMissing);
				}
			}
		}
	
		/**
		 * Creates a menu from the information in the menu configuration element and
		 * adds it into the provided menu manager. If 'appendIfMissing' is true, and
		 * menu path slot is not found, it will be created and menu will be added
		 * into it. Otherwise, add operation will fail.
		 */
		protected void contributeMenu(IConfigurationElement menuElement, IMenuManager mng, boolean appendIfMissing) {
			// Get config data.
			String id = menuElement.getAttribute(ATT_ID);
			String label = menuElement.getAttribute(ATT_LABEL);
			String path = menuElement.getAttribute(ATT_PATH);
			if (label == null) {
				WorkbenchPlugin.log("Invalid Menu Extension (label == null): " + id); //$NON-NLS-1$
				return;
			}

			// Calculate menu path and group.
			String group = null;
			if (path != null) {
				int loc = path.lastIndexOf('/');
				if (loc != -1) {
					group = path.substring(loc + 1);
					path = path.substring(0, loc);
				} else {
					// assume that path represents a slot
					// so actual path portion should be null
					group = path;
					path = null;
				}
			}

			// Find parent menu.
			IMenuManager parent = mng;
			if (path != null) {
				parent = mng.findMenuUsingPath(path);
				if (parent == null) {
					WorkbenchPlugin.log("Invalid Menu Extension (Path is invalid): " + id); //$NON-NLS-1$
					return;
				}
			}

			// Find reference group.
			if (group == null)
				group = IWorkbenchActionConstants.MB_ADDITIONS;
			IContributionItem sep = parent.find(group);
			if (sep == null) {
				if (appendIfMissing)
					addGroup(parent, group);
				else {
					WorkbenchPlugin.log("Invalid Menu Extension (Group is invalid): " + id); //$NON-NLS-1$
					return;
				}
			}

			// If the menu does not exist create it.
			IMenuManager newMenu = parent.findMenuUsingPath(id);
			if (newMenu == null)
				newMenu = new MenuManager(label, id);

			// Add the menu
			try {
				insertAfter(parent, group, newMenu);
			} catch (IllegalArgumentException e) {
				WorkbenchPlugin.log("Invalid Menu Extension (Group is missing): " + id); //$NON-NLS-1$
			}

			// Get the menu again as it may be wrapped, otherwise adding
			// the separators and group markers below will not be wrapped
			// properly if the menu was just created.
			newMenu = parent.findMenuUsingPath(id);
			if (newMenu == null)
				WorkbenchPlugin.log("Could not find new menu: " + id); //$NON-NLS-1$

			// Create separators.
			IConfigurationElement[] children = menuElement.getChildren();
			for (int i = 0; i < children.length; i++) {
				String childName = children[i].getName();
				if (childName.equals(TAG_SEPARATOR)) {
					contributeSeparator(newMenu, children[i]);
				} else if (childName.equals(TAG_GROUP_MARKER)) {
					contributeGroupMarker(newMenu, children[i]);
				}
			}
		}
	
		/**
		 * Contributes action from action descriptor into the provided menu manager.
		 */
		protected void contributeMenuAction(ActionDescriptor ad, IMenuManager menu, boolean appendIfMissing) {
			// Get config data.
			String mpath = ad.getMenuPath();
			String mgroup = ad.getMenuGroup();
			if (mpath == null && mgroup == null)
				return;

			// Find parent menu.
			IMenuManager parent = menu;
			if (mpath != null) {
				parent = parent.findMenuUsingPath(mpath);
				if (parent == null) {
					WorkbenchPlugin.log("Invalid Menu Extension (Path is invalid): " + ad.getId()); //$NON-NLS-1$
					return;
				}
			}

			// Find reference group.
			if (mgroup == null)
				mgroup = IWorkbenchActionConstants.MB_ADDITIONS;
			IContributionItem sep = parent.find(mgroup);
			if (sep == null) {
				if (appendIfMissing)
					addGroup(parent, mgroup);
				else {
					WorkbenchPlugin.log("Invalid Menu Extension (Group is invalid): " + ad.getId()); //$NON-NLS-1$
					return;
				}
			}

			// Add action.
			try {
				insertAfter(parent, mgroup, ad.getAction());
			} catch (IllegalArgumentException e) {
				WorkbenchPlugin.log("Invalid Menu Extension (Group is missing): " + ad.getId()); //$NON-NLS-1$
			}
		}
	
		/**
		 * Creates a named menu separator from the information in the configuration element.
		 * If the separator already exists do not create a second.
		 */
		protected void contributeSeparator(IMenuManager menu, IConfigurationElement element) {
			String id = element.getAttribute(ATT_NAME);
			if (id == null || id.length() <= 0)
				return;
			IContributionItem sep = menu.find(id);
			if (sep != null)
				return;
			insertMenuGroup(menu, new Separator(id));
		}
	
		/**
		 * Creates a named menu group marker from the information in the configuration element.
		 * If the marker already exists do not create a second.
		 */
		protected void contributeGroupMarker(IMenuManager menu, IConfigurationElement element) {
			String id = element.getAttribute(ATT_NAME);
			if (id == null || id.length() <= 0)
				return;
			IContributionItem marker = menu.find(id);
			if (marker != null)
				return;
			insertMenuGroup(menu, new GroupMarker(id));
		}
	
		/**
		 * Contributes action from the action descriptor into the provided tool bar manager.
		 */
		protected void contributeToolbarAction(ActionDescriptor ad, IToolBarManager toolbar, boolean appendIfMissing) {
			// Get config data.
			String tId = ad.getToolbarId();
			String tgroup = ad.getToolbarGroupId();
			if (tId == null && tgroup == null)
				return;

			// Find reference group.
			if (tgroup == null)
				tgroup = IWorkbenchActionConstants.MB_ADDITIONS;
			IContributionItem sep = null;
			sep = toolbar.find(tgroup);
			if (sep == null) {
				if (appendIfMissing) {
					addGroup(toolbar, tgroup);
				} else {
					WorkbenchPlugin.log("Invalid Toolbar Extension (Group is invalid): " + ad.getId()); //$NON-NLS-1$
					return;
				}
			}
			// Add action to tool bar.
			try {
				insertAfter(toolbar, tgroup, ad.getAction());
			} catch (IllegalArgumentException e) {
				WorkbenchPlugin.log("Invalid Toolbar Extension (Group is missing): " + ad.getId()); //$NON-NLS-1$
			}
		}
	
		/**
		 * Inserts the separator or group marker into the menu. Subclasses may override.
		 */
		protected void insertMenuGroup(IMenuManager menu, AbstractGroupMarker marker) {
			menu.add(marker);
		}
	
		/**
		 * Inserts an action after another named contribution item.
		 * Subclasses may override.
		 */
		protected void insertAfter(IContributionManager mgr, String refId, PluginAction action) {
			insertAfter(mgr, refId, new PluginActionContributionItem(action));
		}
	
		/**
		 * Inserts a contribution item after another named contribution item.
		 * Subclasses may override.
		 */
		protected void insertAfter(IContributionManager mgr, String refId, IContributionItem item) {
			mgr.insertAfter(refId, item);
		}
	
		/**
		 * Adds a group to a contribution manager.
		 * Subclasses may override.
		 */
		protected void addGroup(IContributionManager mgr, String name) {
			mgr.add(new Separator(name));
		}
	}
}
