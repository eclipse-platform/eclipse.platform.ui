package org.eclipse.ui.part;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.action.GroupMarker;

/**
 * A group marker used by EditorActionBars to delineate CoolItem groups.
 * Use this marker when contributing to the ToolBar for the EditorActionBar.  
 * 
 * Note that this class was introduced to address [Bug 17477], is experimental, 
 * and may change.
 * 
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class CoolItemGroupMarker extends GroupMarker {
/**
 * Create a new group marker with the given name.
 * The group name must not be <code>null</code> or the empty string.
 * The group name is also used as the item id.
 * 
 * Note that CoolItemGroupMarkers must have a group name and the name must
 * be unique.
 * 
 * @param groupName the name of the group
 */
public CoolItemGroupMarker(String groupName) {
	super(groupName);
}
}
