/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.console;

import org.eclipse.ui.IViewPart;

/**
 * A view that displays consoles registered with the console manager.
 * @since 3.0
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IConsoleView extends IViewPart {
	
	/**
	 * Displays the page for the given console in this console view.
	 * Has no effect if this console view has a pinned console. 
	 *  
	 * @param console console to display, cannot be <code>null</code>
	 */
	public void display(IConsole console);
	
	/**
	 * Pins this console view. No other console page will be displayed until
     * this console view is un-pinned.
	 *  
	 * @param pin <code>true</code> to pin the current console to the 
     * top of the stack, <code>false</code> otherwise
     * @since 3.1
	 */
	public void setPinned(boolean pin);
	
    /**
     * Displays and pins the given console in this console view. No
     * other console can be displayed until this console view is
     * un-pinned. Specifying <code>null</code> un-pins this console
     *  
     * @param console console to pin, or <code>null</code> to un-pin
     * @deprecated rather than pinning a specific console, a console view is
     *  pinned - use <code>setPinned(boolean)</code>
     */
    public void pin(IConsole console);
    
	/**
	 * Returns whether this console view is currently pinned to a
	 * specific console.
	 * 
	 * @return whether this console view is currently pinned to a
	 *  specific console
	 */
	public boolean isPinned();
	
	/**
	 * Returns the console currently being displayed, or <code>null</code>
	 * if none
	 * 
	 * @return the console currently being displayed, or <code>null</code>
	 *  if none
	 */
	public IConsole getConsole();
	
	/**
	 * Warns that the content of the given console has changed.
	 * 
	 * @param console the console that has changed
	 */
	public void warnOfContentChange(IConsole console);	
    
    /**
     * Sets the scroll lock state of the currently active console.
     * 
     * @param scrollLock <code>true</code> to turn scroll lock on, otherwise <code>false</code>
     * @since 3.1
     */
    public void setScrollLock(boolean scrollLock);
    
    /**
     * Returns the scroll lock state of the currently active console.
     * 
     * @return <code>true</code> if scroll lock is on, <code>false</code> otherwise
     * @since 3.1
     */
    public boolean getScrollLock();

}
