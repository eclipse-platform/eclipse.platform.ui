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
 * @see org.eclipse.ui.console.IOConsole
 * @since 3.1
 */
public interface IPatternMatchListenerDelegate {
    /**
     * Connects the delegate to the console being monitored
     * @param console the console being monitored
     */
    public void connect(IConsole console);
    
    /**
     * Disconnects the delegate from the console
     */
    public void disconnect();
    
    /**
     * Notification that a match has been found.
     * 
     * @param event event describing where the match was found
     */
    public void matchFound(PatternMatchEvent event);
}
