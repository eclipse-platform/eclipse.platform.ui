package org.eclipse.jface.action;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.*;

import org.eclipse.jface.util.Assert;
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
		return (IMenuManager)item;
	}
	return null;
}
/* (non-Javadoc)
 * Method declared on IMenuManager.
 *
 * Returns the item passed to us, not the wrapper.
 *
 * We use use the same algorithm as MenuManager.findUsingPath, but unwrap
 * submenus along so that SubMenuManagers are visible.
 */
public IContributionItem findUsingPath(String path) {
	String id = path;
	String rest = null;
	int separator = path.indexOf('/');
	if (separator != -1) {
		id = path.substring(0, separator);
		rest = path.substring(separator + 1);
	}
	IContributionItem item = find(id); // unwraps item
	if (rest != null && item instanceof IMenuManager) {
		IMenuManager menu = (IMenuManager) item;
		item = menu.findUsingPath(rest);
	}
	return item;
}
/* (non-Javadoc)
 * Method declared on IContributionManager.
 *
 * Returns the item passed to us, not the wrapper.
 * In the case of menu's not added by this manager,
 * ensure that we return a wrapper for the menu.
 */
public IContributionItem find(String id) {
	IContributionItem item = parentMgr.find(id);
	if (item instanceof SubContributionItem)
		// Return the item passed to us, not the wrapper.
		item = unwrap(item);
		
	if (item instanceof IMenuManager) {
		IMenuManager menu = (IMenuManager)item;
		item = getWrapper(menu);
	}

	return item;
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
	return false;
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
	if (mapMenuToWrapper == null) {
		mapMenuToWrapper = new HashMap(4);
	}
	SubMenuManager wrapper = (SubMenuManager) mapMenuToWrapper.get(mgr);
	if (wrapper == null) {
		wrapper = wrapMenu(mgr);
		mapMenuToWrapper.put(mgr, wrapper);
	}
	return wrapper;
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
 * Method declared on IContributionItem.
 */
public void setParent(IContributionManager parent) {
	// do nothing, our "parent manager's" parent 
	// is set when it is added to a manager
}
/* (non-Javadoc)
 * Method declared on IMenuManager.
 */
public void setRemoveAllWhenShown(boolean removeAll) {
	Assert.isTrue(false, "Should not be called on submenu manager");
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
/* (non-Javadoc)
 * Method declared on IContributionItem.
 */
public void update(String id) {
	parentMgr.update(id);
}
/**
 * Wraps a menu manager in a sub menu manager, and returns the new wrapper.
 */
protected SubMenuManager wrapMenu(IMenuManager menu) {
	SubMenuManager mgr = new SubMenuManager(menu);
	mgr.setVisible(isVisible());
	return mgr;
}

}
