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
 * A pattern match listener is registered with an <code>IOConsole</code>,
 * and is notified when its pattern has been matched to the contents in
 * that console.
 * 
 * @see org.eclipse.ui.console.IOConsole
 * @since 3.1
 */
public interface IPatternMatchListener {
    /**
     * Returns the pattern to be used for matching. The pattern is
     * a string representing a regular expression. 
     * 
     * @return the regular expression to be used for matching
     */
    public String getPattern();
    
    /**
     * Returns an int to be used by <code>Pattern.compile(String regex, int flags)</code>
     * @return
     */
    public int getCompilerFlags();
    
    /**
     * Returns a simple regular expression used to identify lines that may
     * match this pattern matcher's complete pattern, or <code>null</code>.
     * Use of this attribute can improve performance by disqualifying lines
     * from the search. When a line is found containing a match for this expression,
     * the line is searched from the beginning for this pattern matcher's
     * complete pattern.
     * 
     * @return a simple regular expression used to identify lines that may
     * match this pattern matcher's complete pattern, or <code>null</code>
     */
    public String getLineQualifier();
        
    /**
     * Notification that a match has been found.
     * 
     * @param event event describing where the match was found
     */

    public void matchFound(PatternMatchEvent event);

    /**
     * Connects this PatternMatchListener to the console
     * @param console The console this Listener is attached to
     */
    public void connect(IConsole console);
    
    /**
     * Disconnects this PatternMatchListener from the console
     */
    public void disconnect();
    
}
