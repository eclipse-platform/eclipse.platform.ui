package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.*;
import org.eclipse.jface.action.*;

/**
 * A <code>ShortcutMenu</code> is used to populate a menu manager with
 * actions.  The visible actions are determined by the active perspective
 * within the workbench window.  
 */
public abstract class ShortcutMenu implements IInternalPerspectiveListener, IPageListener {
	private IWorkbenchWindow window;
	private IMenuManager innerMgr;

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
 * <p>
 * If the menu will appear on a semi-permanent basis, for instance within
 * a toolbar or menubar, the value passed for <code>register</code> should be true.
 * If set, the menu will listen to perspective activation and update itself
 * to suit.  In this case clients are expected to call <code>deregister</code> 
 * when the menu is no longer needed.  This will unhook any perspective
 * listeners.
 * </p>
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
		window.addPageListener(this);
		((WorkbenchWindow)window).getPerspectiveService().addPerspectiveListener(this);
	}
}
/**
 * Removes all listeners from the containing workbench window.
 * <p>
 * This method should only be called if the shortcut menu is created
 * with <code>register = true</code>.
 * </p>
 */
public void deregisterListeners() {
	window.removePageListener(this);
	((WorkbenchWindow)window).getPerspectiveService().removePerspectiveListener(this);
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
 * Returns the current perspective descriptor.
 *
 * @return the current perspective or <code>null if none
 */
protected IPerspectiveDescriptor getCurrentPerspective() {
	IWorkbenchPage page = window.getActivePage();
	if (page == null)
		return null;
	return page.getPerspective();
}
/**
 * Returns the menu manager.
 *
 * @return the menu manager
 */
protected IMenuManager getMenuManager() {
	return innerMgr;
}
/**
 * Returns the window.
 *
 * @return the window
 */
protected IWorkbenchWindow getWindow() {
	return window;
}
/**
 * Updates the menu.  This method will only be called
 * to initialize the menu, or if the active perspective within the
 * window has changed.
 */
protected void updateMenu() {
	// contribute the sub menu items
	fillMenu();
	
	// call this update so the actual swt
	// menu in the manager is updated with
	// the new items from the fillMenu() call.
	innerMgr.update(false);
}

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
public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String id){
    updateMenu();
}
public void perspectiveClosed(IWorkbenchPage page, IPerspectiveDescriptor perspective){
    updateMenu();
}
public void perspectiveOpened(IWorkbenchPage page, IPerspectiveDescriptor perspective){
    updateMenu();
}

}
