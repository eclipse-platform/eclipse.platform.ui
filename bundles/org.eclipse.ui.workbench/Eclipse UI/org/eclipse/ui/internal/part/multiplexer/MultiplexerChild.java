/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.part.multiplexer;

import org.eclipse.ui.internal.part.Part;

/**
 * 
 * 
 * @since 3.1
 */
public class MultiplexerChild {
    private Part part;
    private NestedContext context;
    
    public MultiplexerChild(NestedContext context, Part part) {
        this.part = part;
        this.context = context;
    }
    
    public Part getPart() {
        return part;
    }
    
    public NestedContext getContext() {
        return context;
    }
    
}
