package org.eclipse.ui.internal;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.ui.*;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.dialogs.*;
import org.eclipse.ui.internal.misc.*;
import org.eclipse.ui.internal.registry.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import java.util.*;

/**
 * The base class for all shortcut menus.  A shortcut menu changes to reflect the
 * action sets for the active perspective.
 */
public abstract class ShortcutMenu {
	private IWorkbenchWindow window;
	private IMenuManager innerMgr;
	private Listener listener = new Listener();

	private class Listener implements IPerspectiveListener, IPageListener {
		public void pageActivated(IWorkbenchPage page) {
		    updateMenu();
		}
		public void pageClosed(IWorkbenchPage page) {
		    updateMenu();
		}
		public void pageOpened(IWorkbenchPage page) {
		    // wait for activation.
		}
		public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
		    updateMenu();
		}
		public void perspectiveReset(IWorkbenchPage page, IPerspectiveDescriptor perspective){
		    updateMenu();
		}
	}
/**
 * Create a shortcut menu.
 * This menu does not listen to changes in perspective in the window.
 *
 * @param innerMgr the location for the shortcut menu contents
 * @param window the window containing the menu
 */
public ShortcutMenu(IMenuManager innerMgr, IWorkbenchWindow window) {
	this(innerMgr, window, false);
}
/**
 * Create a shortcut menu.
 *
 * @param innerMgr the location for the shortcut menu contents
 * @param window the window containing the menu
 * @param register if <code>true</code> the menu listens to perspective changes in
 * 		the window
 */
public ShortcutMenu(IMenuManager innerMgr, IWorkbenchWindow window, boolean register) {
	this.innerMgr = innerMgr;
	this.window = window;
	if (register) {
		window.addPageListener(listener);
		((WorkbenchWindow)window).getPerspectiveService().addPerspectiveListener(listener);
	}
}
/**
 * Removes all listeners from the containing workbench window.
 * <p>
 * This method should only be called if the shortcut menu is created
 * with <code>registery = true</code>.
 * </p>
 */
public void deregisterListeners() {
	window.removePageListener(listener);
	((WorkbenchWindow)window).getPerspectiveService().removePerspectiveListener(listener);
}
/**
 * Fills the menu.  This method is typically called when the active perspective
 * or page within the target window changes.
 * <p>
 * Subclasses must implement.
 * </p>
 */
protected abstract void fillMenu();
/**
 * Returns the current perspective descriptor, or null if none.
 */
protected IPerspectiveDescriptor getCurrentPerspective() {
	IWorkbenchPage page = window.getActivePage();
	if (page == null)
		return null;
	return page.getPerspective();
}
/**
 * Returns the menu manager.
 */
protected IMenuManager getMenuManager() {
	return innerMgr;
}
/**
 * Returns the window.
 */
protected IWorkbenchWindow getWindow() {
	return window;
}
/**
 * Updates the menu if the perspective has changed.
 */
protected void updateMenu() {
	// contribute the sub menu items
	fillMenu();
	
	// call this update so the actual swt
	// menu in the manager is updated with
	// the new items from the fillMenu() call.
	innerMgr.update(false);
}
}
