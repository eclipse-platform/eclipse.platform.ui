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
    
    public int getCompilerFlags();
    
    /**
     * Notification that a match has been found.
     * 
     * @param event event describing where the match was found
     */
    public void matchFound(PatternMatchEvent event);
    
}
