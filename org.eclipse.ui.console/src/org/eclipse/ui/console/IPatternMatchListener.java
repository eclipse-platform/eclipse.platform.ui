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
 * Handles notification that a pattern has been matched to the contents in a console.
 */
public interface IPatternMatchListener {
    /**
     * Returns the pattern to be used for matching.
     * @return The pattern to be used for matching.
     */
    public String getPattern();
    
    /**
     * Handles notification that a match has been found.
     * @param text The text of the match.
     * @param offset The offset with the console's document at which the match 
     * was found.
     */
    public void matchFound(PatternMatchEvent event);
    
}
