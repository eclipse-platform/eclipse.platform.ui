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

import org.eclipse.ui.internal.console.ConsoleFactoryExtension;


/**
 * Manages consoles.
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
	 * being displayed in any console views, the associated pages will be closed.
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
	 * 
	 * @param console console to display
	 */
	public void showConsoleView(IConsole console);
	
	/**
	 * Warns that the content of the given console has changed in
	 * all console views.
	 * 
	 * @param console the console that has changed
	 */
	public void warnOfContentChange(IConsole console);
	
	/**
	 * Returns an array of pattern match listeners which should be enabled for
	 * the given console.
	 * @param console The console for which IPatternMatchListeners are required
	 * @return an array of IPatternMatchListner
	 * @since 3.1
	 */
	public IPatternMatchListener[] getPatternMatchListeners(IConsole console);
	
	/**
	 * Returns an array of Page Participants which should be active for the given 
	 * console 
	 * @param console the console for which IConsolePageParticipantDelegate are required
	 * @return an array of IConsolePageParticipantDelegate
	 * @since 3.1
	 */
	public IConsolePageParticipantDelegate[] getPageParticipants(IConsole console);

    /**
     * 
     * @since 3.1
     */
    public ConsoleFactoryExtension[] getConsoleFactoryExtensions();
}
