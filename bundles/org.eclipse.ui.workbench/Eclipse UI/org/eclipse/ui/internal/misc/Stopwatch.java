/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.misc;

import java.util.Map;
import java.util.HashMap;

/**
 * A Stopwatch is used to measure the time elapsed during 
 * an operation.  To do this create a stopwatch before the
 * operation is executed and call stop() when the operation 
 * afterwards.  The elapsed time will be printed.
 *
 * A Stopwatch can also be used to measure an interval time.  To
 * do this create a stopwatch and call printInterval after
 * every important interval.  The resetInterval method can also
 * be used to start a new interval.
 *
 * A Stopwatch can also be registered for global access.  To 
 * do this create a stopwatch and call register.  From this point 
 * on a handle to the stopwatch can be retrieved by calling
 * Stopwatch.getStopwatch("name").  The stopwatch should be
 * unregistered when no longer needed.
 */
public class Stopwatch {
	private long startTime;
	private long lastTime;
	private String name;
	private static Map registry;
/**
 * Construct a new Stopwatch and start it.
 * To reset the watch at a later time just call start() again.
 */
public Stopwatch(String name) {
	this.name = name;
	start();
}
/**
 * Get a stopwatch from the registry.
 */
static public Stopwatch getStopwatch(String name) {
	if (registry != null)
		return (Stopwatch)registry.get(name);
	else
		return null;
}
/**
 * Print the elapsed time since start(), printInterval(), or 
 * resetInterval() was last called.
 */
public void printInterval(String hint) {
	long time = System.currentTimeMillis() - lastTime;
	System.out.println(name + " '" + hint + "' took " + time + " ms");//$NON-NLS-3$//$NON-NLS-2$//$NON-NLS-1$
	lastTime = System.currentTimeMillis();
}
/**
 * Print the current elapsed time.
 */
public void printTime() {
	long time = System.currentTimeMillis() - startTime;
	System.out.print(name + " is now " + time + " ms");//$NON-NLS-2$//$NON-NLS-1$
}
/**
 * Add this stopwatch to the registry.
 */
public void register() {
	if (registry == null)
		registry = new HashMap(2);
	registry.put(name, this);
}
/**
 * Reset the interval timer.
 */
public void resetInterval() {
	lastTime = System.currentTimeMillis();
}
/**
 * Start the watch.
 */
public void start() {
	startTime = lastTime = System.currentTimeMillis();
	System.out.println(name + " started");//$NON-NLS-1$
}
/**
 * Stop the watch and print the elapsed time.
 */
public void stop() {
	long time = System.currentTimeMillis() - startTime;
	System.out.println(name + " finished in " + time + " ms");//$NON-NLS-2$//$NON-NLS-1$
}
/**
 * Remove this stopwatch from the registry.
 */
public void unregister() {
	if (registry != null)
		registry.remove(name);
}
}
