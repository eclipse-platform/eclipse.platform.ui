/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Bjorn Freeman-Benson - initial API and implementation
 *     Pawel Piech (Wind River) - ported PDA Virtual Machine to Java (Bug 261400)
 *******************************************************************************/
package org.eclipse.debug.examples.core.pda.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;

/**
 * A value on the data stack
 */
public class PDAStackValue extends PDADebugElement implements IValue {

    final private PDAThread fThread;
    final private String fValue;
    final private int fIndex;
    
    /**
     * Constructs a value that appears on the data stack
     * 
     * @param target debug target
     * @param value value on the stack
     * @param index index on the stack
     */
	public PDAStackValue(PDAThread thread, String value, int index) {
		super(thread.getDebugTarget());
		fThread = thread;
		fValue = value;
		fIndex = index;
	}
	
	public PDAThread getThread() {
	    return fThread;
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.debug.core.model.IValue#getValueString()
     */
    @Override
	public String getValueString() throws DebugException {
        return fValue;
    }
    /* (non-Javadoc)
     * @see org.eclipse.debug.core.model.IValue#isAllocated()
     */
    @Override
	public boolean isAllocated() throws DebugException {
        return true;
    }
    /* (non-Javadoc)
     * @see org.eclipse.debug.core.model.IValue#getVariables()
     */
    @Override
	public IVariable[] getVariables() throws DebugException {
        return new IVariable[0];
    }
    /* (non-Javadoc)
     * @see org.eclipse.debug.core.model.IValue#hasVariables()
     */
    @Override
	public boolean hasVariables() throws DebugException {
        return false;
    }
    /* (non-Javadoc)
     * @see org.eclipse.debug.core.model.IValue#getReferenceTypeName()
     */
    @Override
	public String getReferenceTypeName() throws DebugException {
        return null;
    }
	/*
	 *  (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
    @Override
	public boolean equals(Object obj) {
        return obj instanceof PDAStackValue && 
            ((PDAStackValue)obj).fValue.equals(fValue) && 
            ((PDAStackValue)obj).fIndex == fIndex;
    }
    /*
     *  (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
	public int hashCode() {
        return super.hashCode() + fIndex;
    }
}
