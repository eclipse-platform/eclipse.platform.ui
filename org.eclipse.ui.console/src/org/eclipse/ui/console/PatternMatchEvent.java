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

    public PatternMatchEvent(IOConsole console, int matchOffset, int matchLength) {
        super(console);
        offset = matchOffset;
        length = matchLength;
    }

    /**
     * @return Returns the length of the matched string.
     */
    public int getLength() {
        return length;
    }

    /**
     * @return Returns the offset of the match within the document.
     */
    public int getOffset() {
        return offset;
    }
    
}
