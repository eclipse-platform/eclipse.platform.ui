/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.console;

/**
 * The console manager manages registered consoles.
 * <p>
 * Clients are not intended to implement this interface.
 * </p>
 * @since 3.0 
 */
public interface IConsoleManager {
	
	/**
	 * Registers the given listener for console notifications. Has
	 * no effect if an identical listener is already registered.
	 * 
	 * @param listener listener to register
	 */
	public void addConsoleListener(IConsoleListener listener);
	
	/**
	 * Deregisters the given listener for console notifications. Has
	 * no effect if an identical listener is not already registered.
	 * 
	 * @param listener listener to deregister
	 */
	public void removeConsoleListener(IConsoleListener listener);

	/**
	 * Adds the given consoles to the console manager. Has no effect for
	 * equivalent consoles already registered. The consoles will be added
	 * to any existing console views.
	 * 
	 * @param consoles consoles to add
	 */
	public void addConsoles(IConsole[] consoles);
	
	/**
	 * Removes the given consoles from the console manager. If the consoles are
	 * being displayed in any console views, the associated pages will be removed
	 * and disposed.
	 * 
	 * @param consoles consoles to remove
	 */
	public void removeConsoles(IConsole[] consoles);
	
	/**
	 * Returns a collection of consoles registered with the console manager.
	 * 
	 * @return a collection of consoles registered with the console manager
	 */
	public IConsole[] getConsoles();
	
	/**
	 * Opens the console view and displays given the console.
	 * If the view is already open, it is brought to the front unless
	 * the view is pinned on a console other than the given console.
	 * Has no effect if the given console is not currently registered.
	 * 
	 * @param console console to display
	 */
	public void showConsoleView(IConsole console);
	
	/**
	 * Warns that the content of the given console has changed in
	 * all console views. Has no effect if the given console is not
	 * currently registered.
	 * 
	 * @param console the console that has changed
	 */
	public void warnOfContentChange(IConsole console);
	
	/**
	 * Returns a collection of pattern match listeners which are enabled for
	 * the given console. The pattern match listeners are new instances, intended
	 * to be used in a new console. No methods on the participants have been
	 * called. Clients are responsible for connecting to and disconnecting from
	 * the pattern match listeners.
	 * 
	 * @param console the console for which pattern match listeners are requested
	 * @return a collection of new pattern match listeners
	 * @since 3.1
	 */
	public IPatternMatchListener[] getPatternMatchListeners(IConsole console);
	
}
