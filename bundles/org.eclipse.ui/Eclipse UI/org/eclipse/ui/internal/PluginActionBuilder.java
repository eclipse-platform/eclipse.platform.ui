package org.eclipse.ui.internal;

import org.eclipse.ui.*;
/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.runtime.*;
import org.eclipse.ui.*;
import org.eclipse.ui.part.*;
import java.util.*;
import org.eclipse.ui.internal.*;
import org.eclipse.jface.action.*;
import org.eclipse.ui.internal.registry.RegistryReader;

/**
 * This class contains shared functionality for reading
 * action contributions from plugins into workbench parts (both editors and views).
 */
public abstract class PluginActionBuilder extends RegistryReader {
	protected String targetID;
	protected String targetContributionTag;
	protected List cache;
	
	public static final String TAG_MENU="menu";
	public static final String TAG_ACTION="action";
	public static final String TAG_SEPARATOR="separator";
	public static final String TAG_FILTER="filter";
	
	public static final String ATT_TARGET_ID = "targetID";
	public static final String ATT_ID="id";
	public static final String ATT_LABEL="label";
	public static final String ATT_ENABLES_FOR="enablesFor";
	public static final String ATT_NAME="name";
	public static final String ATT_PATH="path";
/**
 * The default constructor.
 */
public PluginActionBuilder() {
}
/**
 * Contributes submenus and/or actions into the provided menu and tool bar managers.
 */
protected void contribute(IMenuManager menu, IToolBarManager toolbar, boolean appendIfMissing) {
	for (int i = 0; i < cache.size(); i++) {
		Object obj = cache.get(i);
		if (obj instanceof IConfigurationElement) {
			IConfigurationElement menuElement = (IConfigurationElement) obj;
			processMenu(menuElement, menu, appendIfMissing);
		} else if (obj instanceof ActionDescriptor) {
			ActionDescriptor ad = (ActionDescriptor) obj;
			contributeMenuAction(ad, menu, appendIfMissing);
			contributeToolbarAction(ad, toolbar, appendIfMissing);
		}
	}
}
/**
 * Contributes action from action descriptor into the provided menu manager.
 */
protected static void contributeMenuAction(ActionDescriptor ad, IMenuManager menu, boolean appendIfMissing) {
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
			WorkbenchPlugin.log("Invalid Menu Extension (Path is invalid): " + ad.getId());
			return;
		}
	}

	// Find reference group.
	if (mgroup == null)
		mgroup = IWorkbenchActionConstants.MB_ADDITIONS;
	IContributionItem sep = parent.find(mgroup);
	if (sep == null) {
		if (appendIfMissing)
			parent.add(new Separator(mgroup));
		else {
			WorkbenchPlugin.log("Invalid Menu Extension (Group is invalid): " + ad.getId());
			return;
		}
	}

	// Add action.
	try {
		parent.insertAfter(mgroup, ad.getAction());
	} catch (IllegalArgumentException e) {
		WorkbenchPlugin.log("Invalid Menu Extension (Group is missing): " + ad.getId());
	}
}
/**
 * Creates a named menu separator from the information in the configuration element.
 * If the separator already exists do not create a second.
 */
protected static void contributeSeparator(IMenuManager menu, IConfigurationElement element) {
	String id = element.getAttribute(ATT_NAME);
	if (id == null || id.length() <= 0)
		return;
	IContributionItem sep = menu.find(id);
	if (sep != null)
		return;
	menu.add(new Separator(id));
}
/**
 * Contributes action from the action descriptor into the provided tool bar manager.
 */
protected static void contributeToolbarAction(ActionDescriptor ad, IToolBarManager toolbar, boolean appendIfMissing) {
	// Get config data.
	String tpath = ad.getToolbarPath();
	String tgroup = ad.getToolbarGroup();
	if (tpath == null && tgroup == null)
		return;

	// Find reference group.
	if (tgroup == null)
		tgroup = IWorkbenchActionConstants.MB_ADDITIONS;
	IContributionItem sep = toolbar.find(tgroup);
	if (sep == null) {
		if (appendIfMissing)
			toolbar.add(new Separator(tgroup));
		else {
			WorkbenchPlugin.log("Invalid Toolbar Extension (Group is invalid): " + ad.getId());
			return;
		}
	}
	
	// Add action to tool bar.
	try {
		toolbar.insertAfter(tgroup, ad.getAction());
	} catch (IllegalArgumentException e) {
		WorkbenchPlugin.log("Invalid Toolbar Extension (Group is missing): " + ad.getId());
	}
}
/**
 * This factory method returns a new ActionDescriptor for the
 * configuration element.  It should be implemented by subclasses.
 */
protected abstract ActionDescriptor createActionDescriptor(IConfigurationElement element);
/**
 * Returns the name of the part ID attribute that is expected
 * in the target extension.
 */
protected String getTargetID(IConfigurationElement element) {
	String value=element.getAttribute(ATT_TARGET_ID);
	return value!=null? value : "???";
}
/**
 * Creates the menu from the information in the menu configuration element and
 * adds it into the provided menu manager. Target menu path must exist.
 */
public static void processMenu(IConfigurationElement menuElement, IMenuManager mng) {
	processMenu(menuElement, mng, false);
}
/**
 * Creates the menu from the information in the menu configuration element and
 * adds it into the provided menu manager. If 'appendIfMissing' is true, and
 * menu path slot is not found, it will be created and menu will be added
 * into it. Otherwise, add operation will fail.
 */
public static void processMenu(IConfigurationElement menuElement, IMenuManager mng, boolean appendIfMissing) {
	// Get config data.
	String id = menuElement.getAttribute(ATT_ID);
	String label = menuElement.getAttribute(ATT_LABEL);
	String path = menuElement.getAttribute(ATT_PATH);
	
	// Calculate menu path and group.
	String group = null;
	if (path != null) {
		int loc = path.lastIndexOf('/');
		if (loc != -1) {
			group = path.substring(loc + 1);
			path = path.substring(0, loc);
		}
		else {
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
			WorkbenchPlugin.log("Invalid Menu Extension (Path is invalid): " + id);
			return;
		}
	}

	// Find reference group.
	if (group == null) 
		group = IWorkbenchActionConstants.MB_ADDITIONS;
	IContributionItem sep = parent.find(group);
	if (sep==null) {
		if (appendIfMissing)
			parent.add(new Separator(group));
		else {
			WorkbenchPlugin.log("Invalid Menu Extension (Group is invalid): " + id);
			return;
		}
	}
	
	// If the menu does not exist create it.
	IMenuManager newMenu = parent.findMenuUsingPath(id);
	if (newMenu == null)
		newMenu = new MenuManager(label, id);
	
	// Create separators.
	IConfigurationElement[] children = menuElement.getChildren(TAG_SEPARATOR);
	for (int i = 0; i < children.length; i++) {
		contributeSeparator(newMenu, children[i]);
	}

	// Add new menu
	try {
		parent.insertAfter(group, newMenu);
	} catch (IllegalArgumentException e) {
		WorkbenchPlugin.log("Invalid Menu Extension (Group is missing): " + id);
	}
}
/**
 * Reads the contributions from the registry for the provided workbench
 * part and the provided extension point ID.
 */
public void readContributions(String id, String tag, String extensionPoint) {
	cache = null;
	targetID = id;
	targetContributionTag = tag;
	IPluginRegistry registry = Platform.getPluginRegistry();
	readRegistry(registry, IWorkbenchConstants.PLUGIN_ID, extensionPoint);
}
/**
 * Implements abstract method to handle the provided XML element
 * in the registry.
 */
protected boolean readElement(IConfigurationElement element) {
	String tag = element.getName();
	if (tag.equals(ObjectActionContributorReader.TAG_OBJECT_CONTRIBUTION)) {
		// This builder is sometimes used to read the popup menu
		// extension point.  Ignore all object contributions.
		return true;
	}
	if (tag.equals(targetContributionTag)) {
		String id = getTargetID(element);
		if (id == null || !id.equals(targetID)) {
			// This is not of interest to us - don't go deeper
			return true;
		}
	} else if (tag.equals(TAG_MENU)) {
		if (cache == null)
			cache = new ArrayList();
		cache.add(element);
		return true; // just cache the element - don't go into it
	} else if (tag.equals(TAG_ACTION)) {
		if (cache == null)
			cache = new ArrayList();
		cache.add(createActionDescriptor(element));
		return true; // just cache the action - don't go into
	} else {
		return false;
	}
	
	readElementChildren(element);
	return true;
}
}
