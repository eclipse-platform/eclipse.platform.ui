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
package org.eclipse.debug.internal.ui.elements.adapters;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILogicalStructureType;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IIndexedValue;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.views.variables.IndexedVariablePartition;
import org.eclipse.debug.ui.DeferredDebugElementWorkbenchAdapter;


/**
 * Default deferred content provider for a variable
 */
public class DeferredVariable extends DeferredDebugElementWorkbenchAdapter {
	
	private static final IVariable[] EMPTY_VARS = new IVariable[0];
    
	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parent) {
        try {
        	IVariable variable = (IVariable) parent;
            IValue value = variable.getValue();
            if (value != null) {
                return getValueChildren(variable, value);
            }
        } catch (DebugException e) {
        }
        return EMPTY;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
	 */
	public Object getParent(Object element) {
		return null;
	}

	/**
	 * Returns children for the given value, creating array paritions if required
	 * 
	 * @param parent expression or variable containing the given value
	 * @param value the value to retrieve children for
	 * @return children for the given value, creating array paritions if required
	 * @throws DebugException
	 */
	protected IVariable[] getValueChildren(IDebugElement parent, IValue value) throws DebugException {
		if (value == null) {
			return EMPTY_VARS;
		}
		IValue logicalValue = getLogicalValue(value);
		if (logicalValue instanceof IIndexedValue) {
			IIndexedValue indexedValue = (IIndexedValue)logicalValue;
			int partitionSize = computeParitionSize(indexedValue);
			if (partitionSize > 1) {
				int offset = indexedValue.getInitialOffset();
				int length = indexedValue.getSize();
				int numPartitions = length / partitionSize;
				int remainder = length % partitionSize; 
				if (remainder > 0) {
					numPartitions++;
				}
				IVariable[] partitions = new IVariable[numPartitions];
				for (int i = 0; i < (numPartitions - 1); i++) {
					partitions[i] = new IndexedVariablePartition(parent, indexedValue, offset, partitionSize);
					offset = offset + partitionSize;
				}
				if (remainder == 0) {
					remainder = partitionSize;
				}
				partitions[numPartitions - 1] = new IndexedVariablePartition(parent, indexedValue, offset, remainder);
				return partitions;
			}
		}
		if (logicalValue == null) {
			// safeguard against an structure type returning null
			logicalValue = value;
		}
		return logicalValue.getVariables();
	}    
	
	/**
	 * Returns any logical value for the raw value.
	 * 
	 * @param value
	 * @return
	 */
	protected IValue getLogicalValue(IValue value) {
		if (isShowLogicalStructure()) {
			ILogicalStructureType[] types = DebugPlugin.getLogicalStructureTypes(value);
			if (types.length > 0) {
                ILogicalStructureType type = DebugPlugin.getSelectedStructureType(types);
				if (type != null) {
					try {
						return type.getLogicalStructure(value);
					} catch (CoreException e) {
						// unable to display logical structure
					}
				}
			}
		}
		return value;
	}	

	/**
	 * Return wether to show compute a logical structure or a raw structure.
	 * 
	 * @return wether to show compute a logical structure or a raw structure
	 */
	protected boolean isShowLogicalStructure() {
		return false;
	}
	
	/**
	 * Returns the partition size to use for the given indexed value.
	 * The partition size is computed by determining the number of levels
	 * that an indexed collection must be nested in order to partition
	 * the collection sub-collections of the preferred partition size.
	 * 
	 * @param value indexed value
	 * @return size of paritions the value should be subdivided into
	 */
	protected int computeParitionSize(IIndexedValue value) {
		int partitionSize = 1;
		try {
			int length = value.getSize();
			int partitionDepth = 0;
			int preferredSize = getArrayPartitionSize();
			int remainder = length % preferredSize;
			length = length / preferredSize;
			while (length > 0) {
				if (remainder == 0 && length == 1) {
					break; 
				}
				partitionDepth++;
				remainder = length % preferredSize;
				length = length / preferredSize;
			}
			for (int i = 0; i < partitionDepth; i++) {
				partitionSize = partitionSize * preferredSize;
			}
		} catch (DebugException e) {
		}
		return partitionSize;
	}
	
	/**
	 * Returns the number of entries that should be displayed in each
	 * partition of an indexed collection.
	 * 
	 * @return the number of entries that should be displayed in each
	 * partition of an indexed collection
	 */
	protected int getArrayPartitionSize() {
		// TODO: should fix this with a user pref
		return 100;
	}	
}
