package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;

/**
 * This class extends a single popup menu
 */
public class PopupMenuExtender implements IMenuListener
{
	private String menuID;
	private MenuManager menu;
	private SubMenuManager menuWrapper;
	private ISelectionProvider selProvider;
	private IWorkbenchPart part;
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
	if (!menu.getRemoveAllWhenShown()) {
		menuWrapper = new SubMenuManager(menu);
		menuWrapper.setVisible(true);
	}
	readStaticActions();
}
/**
 * Contributes items registered for the object type(s) in
 * the current selection.
 */
private void addObjectActions(IMenuManager mgr) {
	if (selProvider != null) {
		if (ObjectActionContributorManager.getManager()
			.contributeObjectActions(part, mgr, selProvider))
			mgr.add(new Separator());
	}
}
/**
 * Adds static items to the context menu.
 */
private void addStaticActions(IMenuManager mgr) {
	if (staticActionBuilder != null)
		staticActionBuilder.contribute(mgr, null, true);
}
/**
 * Notifies the listener that the menu is about to be shown.
 */
public void menuAboutToShow(IMenuManager mgr) {
	testForAdditions();
	if (menuWrapper != null) {
		mgr = menuWrapper;
		menuWrapper.removeAll();
	}
	addObjectActions(mgr);
	addStaticActions(mgr);
}
/**
 * Read static items for the context menu.
 */
private void readStaticActions() {
	// If no menu id provided, then there is no contributions
	// to add. Fix for bug #33140.
	if (menuID != null && menuID.length() > 0) {
		staticActionBuilder = new ViewerActionBuilder();
		if (!staticActionBuilder.readViewerContributions(menuID, selProvider, part))
			staticActionBuilder = null;
	}
}
/**
 * Checks for the existance of an MB_ADDITIONS group.
 */
private void testForAdditions() {
	IContributionItem item = menu.find(IWorkbenchActionConstants.MB_ADDITIONS);
	if (item == null) {
		WorkbenchPlugin.log(
			"Context menu missing standard group 'org.eclipse.ui.IWorkbenchActionConstants.MB_ADDITIONS'. (menu id = " //$NON-NLS-1$
			+ (menuID == null ? "???" : menuID) //$NON-NLS-1$
			+ ")  part id = " //$NON-NLS-1$
			+ (part == null ? "???" : part.getSite().getId()) //$NON-NLS-1$
			+ ")"); //$NON-NLS-1$
	}
}
}
