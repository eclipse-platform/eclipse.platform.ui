/*
 * Licensed Materials - Property of IBM
 * (c) Copyright IBM Corporation 2000, 2003.
 * All Rights Reserved. 
 * Note to U.S. Government Users Restricted Rights:  Use, duplication or disclosure restricted by GSA ADP  schedule Contract with IBM Corp. 
*/

package org.eclipse.ui.cheatsheets.events;

/**
 * ICheatSheetViewEvent is an interface for view events.  View events are fired
 * by the cheat sheets view when certain item actions occur in the view.
 * For example, events are fired whenever the cheat sheets view is opened or closed.
 * Listeners implementing ICheatSheetViewListener are notified of these events.
 */
public interface ICheatSheetViewEvent extends ICheatSheetEvent {
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
}
