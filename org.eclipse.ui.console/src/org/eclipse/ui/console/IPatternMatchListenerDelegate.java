/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.console;

/**
 * A pattern match listener delegate is notified of regular expression matches
 * in a text console. A delegate is contributed via the
 * <code>consolePatternMatcherListeners</code> extension point.
 * <p>
 * Clients contributing a console pattern match listener extension are intended
 * to implement this interface.
 * </p>
 * @see org.eclipse.ui.console.IPatternMatchListener
 * @see org.eclipse.ui.console.TextConsole
 * @since 3.1
 */
public interface IPatternMatchListenerDelegate {
    /**
     * Notification that pattern matching will begin in the specified console.
     * A pattern matcher is connected to only one console at a time.
     * 
     * @param console the console in which pattern matching will be performed
     */
    public void connect(TextConsole console);
    
    /**
     * Notification that pattern matching has been completed in the console
     * this delegate was last connected to.
     */
    public void disconnect();
    
    /**
     * Notification that a match has been found.
     * 
     * @param event event describing where the match was found
     */
    public void matchFound(PatternMatchEvent event);
}
