package org.eclipse.jface.action;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.*;

/**
 * Abstract base class for all contribution managers, and standard implementation 
 * of <code>IContributionManager</code>. This class provides functionality 
 * common across the specific managers defined by this framework.
 * <p>
 * This class maintains a list of contribution items and a dirty flag, both as 
 * internal state. In addition to providing implementations of most 
 * <code>IContributionManager</code> methods, this class automatically
 * coalesces adjacent separators, hides beginning and ending separators,
 * and deals with dynamically changing sets of contributions. When the set
 * of contributions does change dynamically, the changes are propagated
 * to the control via the <code>update</code> method, which subclasses
 * must implement.
 * </p>
 * <p>
 * Note: A <code>ContributionItem</code> cannot be shared between different
 * <code>ContributionManager</code>s.
 * </p>
 */
public abstract class ContributionManager implements IContributionManager {

	//	Internal debug flag.
//	protected static final boolean DEBUG = false;

	/**
	 * The list of contribution items.
	 */
	private List contributions = new ArrayList();
	
	/** 
	 * Indicates whether the widgets are in sync with the contributions.
	 */
	private boolean isDirty = true;
	
	/** 
	 * Number of dynamic contribution items.
	 */
	private int dynamicItems = 0;
	
	/**
	 * The overrides for items of this manager
	 */
	private IContributionManagerOverrides overrides;
	
/**
 * Creates a new contribution manager.
 */
protected ContributionManager() {
}
/* (non-Javadoc)
 * Method declared on IContributionManager.
 */
public void add(IAction action) {
	add(new ActionContributionItem(action));
}
/* (non-Javadoc)
 * Method declared on IContributionManager.
 */
public void add(IContributionItem item) {
	item.setParent(this);
	contributions.add(item);
	itemAdded(item);
}
/**
 * Adds a contribution item to the start or end of the group 
 * with the given name.
 *
 * @param groupName the name of the group
 * @param item the contribution item
 * @param append <code>true</code> to add to the end of the group, 
 *   and <code>false</code> to add the beginning of the group
 * @exception IllegalArgumentException if there is no group with
 *   the given name
 */
private void addToGroup(String groupName, IContributionItem item, boolean append) 
{
	int i;
	item.setParent(this);
	Iterator items = contributions.iterator();
	for (i = 0; items.hasNext(); i++) {
		IContributionItem o = (IContributionItem) items.next();
		if (o.isGroupMarker()) {
			String id = o.getId();
			if (id != null && id.equalsIgnoreCase(groupName)) {
				i++;
				if (append) {
					for (; items.hasNext(); i++) {
						IContributionItem ci = (IContributionItem) items.next();
						if (ci.isGroupMarker())
							break;
					}
				}
				contributions.add(i,item);
				itemAdded(item);
				return;
			}
		}
	}
	throw new IllegalArgumentException("Group not found: " + groupName);//$NON-NLS-1$
}
/* (non-Javadoc)
 * Method declared on IContributionManager.
 */
public void appendToGroup(String groupName, IAction action) {
	addToGroup(groupName, new ActionContributionItem(action), true);
}
/* (non-Javadoc)
 * Method declared on IContributionManager.
 */
public void appendToGroup(String groupName, IContributionItem item) {
	addToGroup(groupName, item, true);
}
/**
 * Internal debug method for printing statistics about this manager
 * to <code>System.out</code>.
 */
protected void dumpStatistics() {
	int size= 0;
	if (contributions != null)
		size = contributions.size();
		
	System.out.println(this.toString());
	System.out.println("   Number of elements: " + size);//$NON-NLS-1$
	int sum= 0;
	for (int i= 0; i < size; i++)
		if (((IContributionItem) contributions.get(i)).isVisible())
			sum++;
	System.out.println("   Number of visible elements: " + sum);//$NON-NLS-1$
	System.out.println("   Is dirty: " + isDirty());				//$NON-NLS-1$
}
/* (non-Javadoc)
 * Method declared on IContributionManager.
 */
public IContributionItem find(String id) {
	Iterator e= contributions.iterator();
	while (e.hasNext()) {
		IContributionItem item= (IContributionItem) e.next();
		String itemId= item.getId();
		if (itemId != null && itemId.equalsIgnoreCase(id))
			return item;
	}
	return null;
}
/* (non-Javadoc)
 * Method declared on IContributionManager.
 */
public IContributionItem[] getItems() {
	IContributionItem[] items = new IContributionItem[contributions.size()];
	contributions.toArray(items);
	return items;
}
/**
 * The <code>ContributionManager</code> implemenatation of this
 * method declared on <code>IContributionManager</code> returns
 * the current overrides. If there is no overrides it lazily creates
 * one which overrides no item state.
 * 
 * @since 2.0
 */
public IContributionManagerOverrides getOverrides() {
	if (overrides == null) {
		overrides = new IContributionManagerOverrides() {
			public boolean getEnabledAllowed(IContributionItem item) {
				return true;
			}
			public Integer getAccelerator(IContributionItem item) {
				return null;
			}
			public String getAcceleratorText(IContributionItem item) {
				return null;
			}
			public String getText(IContributionItem item) {
				return null;
			}
		};
	}
	return overrides;
}
/**
 * Returns whether this contribution manager contains dynamic items. 
 * A dynamic contribution item contributes items conditionally, 
 * dependent on some internal state.
 *
 * @return <code>true</code> if this manager contains dynamic items, and
 *  <code>false</code> otherwise
 */
protected boolean hasDynamicItems() {
	return (dynamicItems > 0);
}
/* (non-Javadoc)
 * Method declared on IContributionManager.
 */
public void insertAfter(String ID, IAction action) {
	insertAfter(ID, new ActionContributionItem(action));
}
/* (non-Javadoc)
 * Method declared on IContributionManager.
 */
public void insertAfter(String ID, IContributionItem item) {
	IContributionItem ci= find(ID);
	if (ci == null)
		throw new IllegalArgumentException("can't find ID");//$NON-NLS-1$
	int ix= contributions.indexOf(ci);
	if (ix >= 0) {
		// System.out.println("insert after: " + ix);
		item.setParent(this);
		contributions.add(ix+1,item);
		itemAdded(item);
	}
}
/* (non-Javadoc)
 * Method declared on IContributionManager.
 */
public void insertBefore(String ID, IAction action) {
	insertBefore(ID, new ActionContributionItem(action));
}
/* (non-Javadoc)
 * Method declared on IContributionManager.
 */
public void insertBefore(String ID, IContributionItem item) {
	IContributionItem ci= find(ID);
	if (ci == null)
		throw new IllegalArgumentException("can't find ID");//$NON-NLS-1$
	int ix = contributions.indexOf(ci);
	if (ix >= 0) {
		// System.out.println("insert before: " + ix);
		item.setParent(this);
		contributions.add(ix,item);
		itemAdded(item);
	}
}
/* (non-Javadoc)
 * Method declared on IContributionManager.
 */
public boolean isDirty() {
	return isDirty || hasDynamicItems();
}
/* (non-Javadoc)
 * Method declared on IContributionManager.
 */
public boolean isEmpty() {
	return contributions.isEmpty();	
}
/**
 * The given item was added to the list of contributions.
 * Marks the manager as dirty and updates the number of dynamic items, and the memento.
 */
private void itemAdded(IContributionItem item) {
	markDirty();
	if (item.isDynamic())
		dynamicItems++;
}
/**
 * The given item was removed from the list of contributions.
 * Marks the manager as dirty and updates the number of dynamic items.
 */
private void itemRemoved(IContributionItem item) {
	markDirty();
	if (item.isDynamic())
		dynamicItems--;	
}
/* (non-Javadoc)
 * Method declared on IContributionManager.
 */
public void markDirty() {
	setDirty(true);
}
/* (non-Javadoc)
 * Method declared on IContributionManager.
 */
public void prependToGroup(String groupName, IAction action) {
	addToGroup(groupName, new ActionContributionItem(action), false);
}
/* (non-Javadoc)
 * Method declared on IContributionManager.
 */
public void prependToGroup(String groupName, IContributionItem item) {
	addToGroup(groupName, item, false);
}
/* (non-Javadoc)
 * Method declared on IContributionManager.
 */
public IContributionItem remove(String ID) {
	IContributionItem ci= find(ID);
	if (ci == null)
		throw new IllegalArgumentException("can't find ID");//$NON-NLS-1$
	return remove(ci);
}
/* (non-Javadoc)
 * Method declared on IContributionManager.
 */
public IContributionItem remove(IContributionItem item) {
	if (contributions.remove(item)) {
		itemRemoved(item);
		return item;
	}
	return null;
}
/* (non-Javadoc)
 * Method declared on IContributionManager.
 */
public void removeAll() {
	contributions.clear();
	dynamicItems = 0;
	markDirty();
}
/**
 * Sets whether this manager is dirty. When dirty, the list of contributions 
 * is not accurately reflected in the corresponding widgets.
 *
 * @param <code>true</code> if this manager is dirty, and <code>false</code>
 *   if it is up-to-date
 */
protected void setDirty(boolean d) {
	isDirty = d;
}
/**
 * Sets the overrides for this contribution manager
 * 
 * @param newOverrides the overrides for the items of this manager
 * @since 2.0
 */
public void setOverrides(IContributionManagerOverrides newOverrides) {
	overrides = newOverrides;
}
}
