/*
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2000, 2003.
 * All Rights Reserved. 
 * Note to U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP  schedule Contract with IBM Corp. 
*/

package org.eclipse.ui.cheatsheets;

import org.eclipse.ui.cheatsheets.ICheatSheetManager;

/**
 * ICheatSheetEvent is an interface for lifecycle events.  Lifecycle events
 * are fired by the cheat sheets when certain actions occur.
 * For example, events are fired whenever the cheat sheets is opened or closed.
 * <p>
 * Listeners subclassing CheatSheetListener are notified of these events.
 * </p>
 * <p>
 * The event type may be accessed, as well as the id of the cheat sheet that
 * was open when the event was fired.  A handle to the ICheatSheetManager is
 * available from cheat sheet events.
 * </p>
 */
public interface ICheatSheetEvent {
	
	/**
	 * an event type notifying that the cheat sheets view was opened.
	 */
	public static final int CHEATSHEET_OPENED = 0;
	/**
	 * an event type notifying that the cheat sheets view was closed.
	 */
	public static final int CHEATSHEET_CLOSED = 1;
	/**
	 * an event type notifying that the cheat sheet was started.
	 */
	public static final int CHEATSHEET_STARTED = 2;
	/**
	 * an event type notifying that the cheat sheet was restarted.
	 */
	public static final int CHEATSHEET_RESTARTED = 3;
	/**
	 * an event type notifying that the cheat sheet has reached the end and all items were either completed or skipped.
	 */
	public static final int CHEATSHEET_END_REACHED = 4;

	/**
	 * This method returns an integer that corresponds to an event type.
	 * @return the event code
	 */
	public int getEventType();
	/**
	 * This method returns the id of the cheat sheet that generated the event.
	 * @return the id of the cheat sheet that fired the event
	 */
	public String getCheatSheetID();
	/**
	 * This method returns a handle to the implementation of ICheatSheetManager.
	 * @return a reference to the ICheatSheetManager
	 */
	public ICheatSheetManager getCheatSheetManager();

}
