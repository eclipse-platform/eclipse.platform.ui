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

import java.util.EventObject;

/**
 * An event describing a pattern match in a console. The source of the event
 * is an <code>IOConsole</code>.
 * 
 * @see org.eclipse.ui.console.IPatternMatchListener
 * @see org.eclipse.ui.console.IOConsole
 * @since 3.1
 */
public class PatternMatchEvent extends EventObject {
    /*
     * required by EventObject for ObjectSerialization.
     */
    private static final long serialVersionUID = 876890383326854537L;
    
    /**
     * The offset of the match within the console's document. 
     */
    private int offset;
    
    /**
     * The length of the matched string
     */
    private int length;

    /**
     * Constructs a new pattern match event.
     * 
     * @param console the console in which the match was found
     * @param matchOffset the offset at which the match was found
     * @param matchLength the length of the text that matched
     */
    public PatternMatchEvent(TextConsole console, int matchOffset, int matchLength) {
        super(console);
        offset = matchOffset;
        length = matchLength;
    }

    /**
     * Returns the length of the matched string.
     * 
     * @return the length of the matched string
     */
    public int getLength() {
        return length;
    }

    /**
     * Returns the offset of the match within the document.
     * 
     * @return the offset of the match within the document
     */
    public int getOffset() {
        return offset;
    }
    
}
