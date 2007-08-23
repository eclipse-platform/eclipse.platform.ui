/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.variables;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IIndexedValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.core.IInternalDebugCoreConstants;

/**
 * A parition (subrange) of values of an indexed value 
 */
public class IndexedValuePartition implements IIndexedValue {

	// the starting offset of this parition, into the associated collection
	private int fOffset;
	
	// the length of this partition
	private int fLength;

	// the indexed value
	private IIndexedValue fValue;
	
	/**
	 * Creates a parition for an indexed value.
	 * 
	 * @param value indexed value
	 * @param offset beginning offset of this partition (into the value)
	 * @param length the length of this parition
	 */
	public IndexedValuePartition(IIndexedValue value, int offset, int length) {
		fValue = value;
		fOffset = offset;
		fLength = length;
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IIndexedValue#getSize()
	 */
	public int getSize() {
		return fLength;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IIndexedValue#getVariable(int)
	 */
	public IVariable getVariable(int offset) throws DebugException {
		return fValue.getVariable(offset);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#getReferenceTypeName()
	 */
	public String getReferenceTypeName() throws DebugException {
		return fValue.getReferenceTypeName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#getValueString()
	 */
	public String getValueString() {
		return IInternalDebugCoreConstants.EMPTY_STRING;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#getVariables()
	 */
	public IVariable[] getVariables() throws DebugException {
		return getVariables(fOffset, fLength);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#hasVariables()
	 */
	public boolean hasVariables() {
		return fLength > 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IValue#isAllocated()
	 */
	public boolean isAllocated() throws DebugException {
		return fValue.isAllocated();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugElement#getDebugTarget()
	 */
	public IDebugTarget getDebugTarget() {
		return fValue.getDebugTarget();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugElement#getLaunch()
	 */
	public ILaunch getLaunch() {
		return fValue.getLaunch();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IDebugElement#getModelIdentifier()
	 */
	public String getModelIdentifier() {
		return fValue.getModelIdentifier();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		return fValue.getAdapter(adapter);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IIndexedValue#getVariables(int, int)
	 */
	public IVariable[] getVariables(int offset, int length) throws DebugException {
		return fValue.getVariables(offset, length);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IIndexedValue#getInitialOffset()
	 */
	public int getInitialOffset() {
		return fOffset;
	}

}
