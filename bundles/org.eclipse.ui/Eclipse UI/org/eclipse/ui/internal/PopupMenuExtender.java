package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
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
	private ViewerActionBuilder staticActionBuilder;
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
	if (staticActionBuilder != null)
		staticActionBuilder.contribute(menu, null, true);
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
	staticActionBuilder = new ViewerActionBuilder();
	if (!staticActionBuilder.readViewerContributions(menuID, selProvider, part))
		staticActionBuilder = null;
}
/**
 * Checks for the existance of an MB_ADDITIONS group.
 */
private void testForAdditions() {
	IContributionItem item = menu.find(IWorkbenchActionConstants.MB_ADDITIONS);
	if (item == null) {
		WorkbenchPlugin.log("Context menu does not contain standard group for "//$NON-NLS-1$
			+ "additions ("//$NON-NLS-1$
			+ IWorkbenchActionConstants.MB_ADDITIONS 
			+ ")");//$NON-NLS-1$
	}
}
}
