package org.eclipse.jface.action;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * A group marker is a special kind of contribution item denoting
 * the beginning of a group. These groups are used to structure
 * the list of items. Unlike regular contribution items and
 * separators, group markers have no visual representation.
 * The name of the group is synonymous with the contribution item id.
 * <p>
 * This class may be instantiated; it is not intended to be 
 * subclassed outside the framework.
 * </p>
 */
public class GroupMarker extends AbstractGroupMarker {
/**
 * Create a new group marker with the given name.
 * The group name must not be <code>null</code> or the empty string.
 * The group name is also used as the item id.
 * 
 * @param groupName the name of the group
 */
public GroupMarker(String groupName) {
	super(groupName);
}
/**
 * The <code>GroupMarker</code> implementation of this method
 * returns <code>false</code> since group markers are always invisible.
 */
public boolean isVisible() {
	return false;
}
}
