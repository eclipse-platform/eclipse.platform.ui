/*
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2000, 2003.
 * All Rights Reserved. 
 * Note to U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP  schedule Contract with IBM Corp. 
*/

package org.eclipse.ui.cheatsheets.events;

/**
 * ICheatSheetItemEvent is the type of object that is received by a registered ICheatSheetItemListener.
 * The information in this type of object can be used to determine what item or sub item an event was 
 * fired from.
 */
public interface ICheatSheetItemEvent extends ICheatSheetEvent {
	/**
	 * an event type notifying of an item having been activated.
	 */
	public static final int ITEM_ACTIVATED = 0;
	/**
	 * an event type notifying of an item or sub item that had an action class run.
	 */
	public static final int ITEM_PERFORMED = 1;
	/**
	 * an event type notifying of an item or sub item having been skipped.
	 */
	public static final int ITEM_SKIPPED = 2;
	/**
	 * an event type notifying of an item or sub item having been completed.
	 */
	public static final int ITEM_COMPLETED = 3;
	/**
	 * an event type notifying of an item having been deactivated.
	 */

	public static final int ITEM_DEACTIVATED = 4;

	/**
	 * This method returns the id of the sub item associated with this event.
	 * If there is no sub item associated with this item event, then this method will return null.
	 * @return the id of the sub item associated with this event.  Null if no sub item is associated and it is just an item
	 * event.
	 */
	public String getCheatSheetSubItemID();
	/**
	 * This method returns the id of the item that is associated with this item event.  If it is a sub item event,
	 * it will return the id of the item that contains the sub item that had an event fired.
	 * @return the id of the item associated with this item event.
	 */
	public String getCheatSheetItemID();

}
