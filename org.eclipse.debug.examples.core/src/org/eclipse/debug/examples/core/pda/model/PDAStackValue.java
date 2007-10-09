/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Bjorn Freeman-Benson - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.examples.core.pda.model;

/**
 * A value on the data stack
 */
public class PDAStackValue extends PDAValue {
    
    private int fIndex;
    
    /**
     * Constructs a value that appears on the data stack
     * 
     * @param target debug target
     * @param value value on the stack
     * @param index index on the stack
     */
	public PDAStackValue(PDADebugTarget target, String value, int index) {
		super(target, value);
		fIndex = index;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
    public boolean equals(Object obj) {
        return super.equals(obj) && ((PDAStackValue)obj).fIndex == fIndex;
    }
    /*
     *  (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return super.hashCode() + fIndex;
    }
}
