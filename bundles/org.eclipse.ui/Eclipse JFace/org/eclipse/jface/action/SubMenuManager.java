package org.eclipse.jface.action;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.*;

/**
 * A <code>SubMenuManager</code> is used to define a set of contribution
 * items within a parent manager.  Once defined, the visibility of the entire set can 
 * be changed as a unit.
 * <p>
 * A client may ask for and make additions to a submenu.  The visibility of these items
 * is also controlled by the visibility of the <code>SubMenuManager</code>.
 * </p>
 */
public class SubMenuManager extends SubContributionManager implements IMenuManager {
	/**
	 * The parent menu manager.
	 */
	private IMenuManager parentMgr;

	/**
	 * Maps each submenu in the manager to a wrapper.  The wrapper is used to
	 * monitor additions and removals.  If the visibility of the manager is modified
	 * the visibility of the submenus is also modified.
	 */
	private Map mapMenuToWrapper;

/**
 * Constructs a new manager.
 *
 * @param mgr the parent manager.  All contributions made to the 
 *      <code>SubMenuManager</code> are forwarded and appear in the
 *      parent manager.
 */
public SubMenuManager(IMenuManager mgr) {
	super(mgr);
	parentMgr = mgr;
}
/* (non-Javadoc)
 * Method declared on IMenuManager.
 */
public void addMenuListener(IMenuListener listener) {
	parentMgr.addMenuListener(listener);
}
/* (non-Javadoc)
 * Method declared on IMenuManager.
 */
public void fill(Composite parent) {
	if (isVisible())
		parentMgr.fill(parent);
}
/* (non-Javadoc)
 * Method declared on IMenuManager.
 */
public void fill(Menu parent, int index) {
	if (isVisible())
		parentMgr.fill(parent, index);
}
/* (non-Javadoc)
 * Method declared on IMenuManager.
 */
public void fill(ToolBar parent, int index) {
	if (isVisible())
		parentMgr.fill(parent, index);
}
/* (non-Javadoc)
 * Method declared on IMenuManager.
 */
/**
 * <p>
 * The menu returned is wrapped within a <code>SubMenuManager</code> to
 * monitor additions and removals.  If the visibility of this menu is modified
 * the visibility of the submenus is also modified.
 * </p>
 */
public IMenuManager findMenuUsingPath(String path) {
	IContributionItem item = findUsingPath(path);
	if (item instanceof IMenuManager) {
		IMenuManager menu = (IMenuManager)item;
		IMenuManager wrapper = getWrapper(menu);
		if (wrapper == null) {
			wrapper = wrapMenu(menu);
			putWrapper(menu, wrapper);
		}
		return wrapper;
	}
	return null;
}
/* (non-Javadoc)
 * Method declared on IMenuManager.
 */
public IContributionItem findUsingPath(String path) {
	IContributionItem item = parentMgr.findUsingPath(path);
	if (item instanceof SubContributionItem) {
		return ((SubContributionItem)item).getInnerItem();
	} else {
		return item;
	}
}
/* (non-Javadoc)
 * Method declared on IMenuManager.
 */
public String getId() {
	return parentMgr.getId();
}
/* (non-Javadoc)
 * Method declared on IMenuManager.
 */
public boolean getRemoveAllWhenShown() {
	return parentMgr.getRemoveAllWhenShown();
}
/**
 * Returns the menu wrapper for a menu manager.
 * <p>
 * The sub menus within this menu are wrapped within a <code>SubMenuManager</code> to
 * monitor additions and removals.  If the visibility of this menu is modified
 * the visibility of the sub menus is also modified.
 * <p>
 *
 * @return the menu wrapper
 */
private IMenuManager getWrapper(IMenuManager mgr) {
	if (mapMenuToWrapper == null)
		return null;
	return (IMenuManager)mapMenuToWrapper.get(mgr);
}
/* (non-Javadoc)
 * Method declared on IMenuManager.
 */
public boolean isDynamic() {
	return parentMgr.isDynamic();
}
/* (non-Javadoc)
 * Method declared on IMenuManager.
 */
public boolean isEnabled() {
	return isVisible() && parentMgr.isEnabled();
}
/* (non-Javadoc)
 * Method declared on IMenuManager.
 */
public boolean isGroupMarker() {
	return parentMgr.isGroupMarker();
}
/* (non-Javadoc)
 * Method declared on IMenuManager.
 */
public boolean isSeparator() {
	return parentMgr.isSeparator();
}
/**
 * Stores a menu wrapper for a menu manager.
 * <p>
 * The sub menus within this menu are wrapped within a <code>SubMenuManager</code> to
 * monitor additions and removals.  If the visibility of this menu is modified
 * the visibility of the sub menus is also modified.
 * <p>
 *
 * @param mgr a submenu of this menu
 * @param wrap a wrapper for the submenu of this menu
 */
private void putWrapper(IMenuManager mgr, IMenuManager wrap) {
	if (mapMenuToWrapper == null)
		mapMenuToWrapper = new HashMap(4);
	mapMenuToWrapper.put(mgr, wrap);
}
/**
 * Remove all contribution items.
 */
public void removeAll() {
	super.removeAll();
	if (mapMenuToWrapper != null) {
		Iterator iter = mapMenuToWrapper.values().iterator();
		while (iter.hasNext()) {
			SubMenuManager wrapper = (SubMenuManager)iter.next();
			wrapper.removeAll();
		}
		mapMenuToWrapper.clear();
		mapMenuToWrapper = null;
	}
}
/* (non-Javadoc)
 * Method declared on IMenuManager.
 */
public void removeMenuListener(IMenuListener listener) {
	parentMgr.removeMenuListener(listener);
}
/* (non-Javadoc)
 * Method declared on IMenuManager.
 */
public void setRemoveAllWhenShown(boolean removeAll) {
	parentMgr.setRemoveAllWhenShown(removeAll);
}
/* (non-Javadoc)
 * Method declared on SubContributionManager.
 */
public void setVisible(boolean visible) {
	super.setVisible(visible);
	if (mapMenuToWrapper != null) {
		Iterator iter = mapMenuToWrapper.values().iterator();
		while (iter.hasNext()) {
			SubMenuManager wrapper = (SubMenuManager)iter.next();
			wrapper.setVisible(visible);
		}
	}
}
/* (non-Javadoc)
 * Method declared on IMenuManager.
 */
public void update() {
	// This method is not governed by visibility.  The client may
	// call <code>setVisible</code> and then force an update.  At that
	// point we need to update the parent.
	parentMgr.update();
}
/* (non-Javadoc)
 * Method declared on IMenuManager.
 */
public void update(boolean force) {
	// This method is not governed by visibility.  The client may
	// call <code>setVisible</code> and then force an update.  At that
	// point we need to update the parent.
	parentMgr.update(force);
}
/* (non-Javadoc)
 * Method declared on IMenuManager.
 */
public void updateAll(boolean force) {
	// This method is not governed by visibility.  The client may
	// call <code>setVisible</code> and then force an update.  At that
	// point we need to update the parent.
	parentMgr.updateAll(force);
}
/**
 * Wraps a menu manager in a sub menu manager, and returns the new wrapper.
 */
protected SubMenuManager wrapMenu(IMenuManager menu) {
	return new SubMenuManager(menu);
}
}
