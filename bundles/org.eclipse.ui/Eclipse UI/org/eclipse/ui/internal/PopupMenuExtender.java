package org.eclipse.ui.internal;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.runtime.*;
import org.eclipse.ui.internal.registry.*;
import org.eclipse.ui.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import java.util.*;

/**
 * This class extends a single popup menu
 */
public class PopupMenuExtender implements IMenuListener
{
	private String menuID;
	private MenuManager menu;
	private ISelectionProvider selProvider;
	private IWorkbenchPart part;
	private List staticItems;
/**
 * Construct a new menu extender.
 */
public PopupMenuExtender(String id, MenuManager menu, ISelectionProvider prov, IWorkbenchPart part) {
	this.menuID = id;
	this.menu = menu;
	this.selProvider = prov;
	this.part = part;
	menu.addMenuListener(this);
	readStaticActions();
}
/**
 * Contributes items registered for the object type(s) in
 * the current selection.
 */
private void addObjectActions() {
	if (selProvider != null) {
		if (ObjectActionContributorManager.getManager()
			.contributeObjectActions(part, menu, selProvider))
			menu.add(new Separator());
	}
}
/**
 * Adds static items to the context menu.
 */
private void addStaticActions() {
	if (staticItems == null)
		return;
	for (int i = 0; i < staticItems.size(); i++) {
		Object obj = staticItems.get(i);
		if (obj instanceof IConfigurationElement) {
			IConfigurationElement menuElement = (IConfigurationElement) obj;
			PluginActionBuilder.processMenu(menuElement, menu, true);
		} else
			if (obj instanceof ActionDescriptor) {
				ActionDescriptor ad = (ActionDescriptor) obj;
				IMenuManager parent = menu;
				String mpath = ad.getMenuPath();
				String mgroup = ad.getMenuGroup();
				if (mgroup != null) {
					if (mpath != null)
						parent = parent.findMenuUsingPath(mpath);
					if (parent != null) {
						IContributionItem sep = parent.find(mgroup);
						if (sep == null || !(sep instanceof Separator)) {
							parent.add(new Separator(mgroup));
						}
						parent.insertAfter(mgroup, ad.getAction());
					}
				}
			}
	}
}
/**
 * Notifies the listener that the menu is about to be shown.
 */
public void menuAboutToShow(IMenuManager mgr) {
	testForAdditions();
	addObjectActions();
	addStaticActions();
}
/**
 * Read static items for the context menu.
 */
private void readStaticActions() {
	ViewerActionBuilder builder = new ViewerActionBuilder();
	staticItems = builder.readViewerContributions(menuID, selProvider, part);
}
/**
 * Checks for the existance of an MB_ADDITIONS group.
 */
private void testForAdditions() {
	IContributionItem item = menu.find(IWorkbenchActionConstants.MB_ADDITIONS);
	if (item == null) {
		WorkbenchPlugin.log("Context menu does not contain standard group for "
			+ "additions ("
			+ IWorkbenchActionConstants.MB_ADDITIONS 
			+ ")");
	}
}
}
