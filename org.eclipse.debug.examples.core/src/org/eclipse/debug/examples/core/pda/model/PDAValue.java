/*******************************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation and others.
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
import org.eclipse.debug.examples.core.pda.protocol.PDAChildrenCommand;
import org.eclipse.debug.examples.core.pda.protocol.PDAListResult;

/**
 * Value of a PDA variable.
 */
public class PDAValue extends PDADebugElement implements IValue {
	
    final private PDAVariable fVariable;
	final private String fValue;
	
	public PDAValue(PDAVariable variable, String value) {
		super(variable.getStackFrame().getPDADebugTarget());
		fVariable = variable;
		fValue = value;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#getReferenceTypeName()
	 */
	@Override
	public String getReferenceTypeName() throws DebugException {
		try {
			Integer.parseInt(fValue);
		} catch (NumberFormatException e) {
			return "text"; //$NON-NLS-1$
		}
		return "integer"; //$NON-NLS-1$
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
	    PDAStackFrame frame = fVariable.getStackFrame();
	    PDAListResult result =  (PDAListResult) sendCommand(
	        new PDAChildrenCommand(frame.getThreadIdentifier(), frame.getIdentifier(), fVariable.getName()) );
	    
	    IVariable[] children = new IVariable[result.fValues.length];
	    for(int i = 0; i < result.fValues.length; i++) {
	        children[i] = new PDAVariable(frame, result.fValues[i]);
	    }
		return children;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#hasVariables()
	 */
	@Override
	public boolean hasVariables() throws DebugException {
	    if (getVariables().length != 0) {
	        return true;
	    }
	    // Value with multiple words can be show as an array using logical 
	    // structures. If the value has multiple words, it needs to indicate 
	    // that it has children even if logical structures are not turned on.
		return fValue.split("\\W+").length > 1; //$NON-NLS-1$
	}
	/*
	 *  (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
    @Override
	public boolean equals(Object obj) {
        return obj instanceof PDAValue && ((PDAValue)obj).fValue.equals(fValue);
    }
    /*
     *  (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
	public int hashCode() {
        return fValue.hashCode();
    }
    
    /**
     * Returns the variable that this value was created for.
     * 
     * @return The variable that this value was created for.
     * 
     * @since 3.5
     */
    public PDAVariable getVariable() {
        return fVariable;
    }
}
