package org.eclipse.team.tests.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


/**
 * Abstract listener used for the generic problem of waiting for
 * something to happen and retrieving some related information.
 * e.g. Waiting for a window with a given title to open and getting its handle.
 */
public abstract class Waiter {
	/**
	 * Called when the desired event has occurred.
	 * @param object an object related to the event, type depends on the context
	 * @return true to keep waiting, otherwise false
	 */
	public abstract boolean notify(Object object);
	
	/**
	 * Called after each unsuccessful poll for the event.
	 * @return true to keep waiting, otherwise false
	 */
	public boolean keepWaiting() {
		return true;
	}
}
