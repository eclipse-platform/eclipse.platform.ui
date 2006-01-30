/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.elements.adapters;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILogicalStructureType;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IIndexedValue;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.internal.ui.viewers.provisional.AsynchronousContentAdapter;
import org.eclipse.debug.internal.ui.viewers.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.views.variables.IndexedVariablePartition;
import org.eclipse.debug.internal.ui.views.variables.VariablesView;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ui.IWorkbenchPart;

public class VariableContentAdapter extends AsynchronousContentAdapter {

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.viewers.AsynchronousTreeContentAdapter#getChildren(java.lang.Object, org.eclipse.debug.ui.viewers.IPresentationContext)
     */
    protected Object[] getChildren(Object parent, IPresentationContext context) throws CoreException {
        IVariable variable = (IVariable) parent;
        IValue value = variable.getValue();
        if (value != null) {
            return getValueChildren(variable, value, context);
        }
        return EMPTY;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.viewers.AsynchronousTreeContentAdapter#hasChildren(java.lang.Object, org.eclipse.debug.ui.viewers.IPresentationContext)
     */
    protected boolean hasChildren(Object element, IPresentationContext context) throws CoreException {
        IValue value = ((IVariable)element).getValue();
        return value.hasVariables();
    }
    
    /**
     * Returns children for the given value, creating array paritions if
     * required
     * 
     * @param parent expression or variable containing the given value
     * @param value the value to retrieve children for
     * @param context the context in which children have been requested
     * @return children for the given value, creating array paritions if
     *         required
     * @throws CoreException
     */
    protected Object[] getValueChildren(IDebugElement parent, IValue value, IPresentationContext context) throws CoreException {
        if (value == null) {
            return EMPTY;
        }
        IValue logicalValue = getLogicalValue(value, context);
        if (logicalValue instanceof IIndexedValue) {
            IIndexedValue indexedValue = (IIndexedValue) logicalValue;
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
     * Returns the partition size to use for the given indexed value. The
     * partition size is computed by determining the number of levels that an
     * indexed collection must be nested in order to partition the collection
     * sub-collections of the preferred partition size.
     * 
     * @param value
     *            indexed value
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
     * Returns any logical value for the raw value in the specified context
     * 
     * @param value
     * @param context
     * @return
     */
    protected IValue getLogicalValue(IValue value, IPresentationContext context) {
        return getLogicalValue(value, new ArrayList(), context);
    }

    /**
     * Returns any logical value for the raw value. This method will recurse
     * over the returned value until the same structure is encountered again (to
     * avoid infinite recursion).
     * 
     * @param value
     * @param previousStructureIds
     *            the list of logical structures that have already been applied
     *            to the returned value during the recursion of this method.
     *            Callers should always pass in a new, empty list.
     * @return
     */
    protected IValue getLogicalValue(IValue value, List previousStructureIds, IPresentationContext context) {
        if (isShowLogicalStructure(context)) {
            ILogicalStructureType[] types = DebugPlugin.getLogicalStructureTypes(value);
            if (types.length > 0) {
                ILogicalStructureType type = DebugPlugin.getDefaultStructureType(types);
                if (type != null && !previousStructureIds.contains(type.getId())) {
                    try {
                        value = type.getLogicalStructure(value);
                        previousStructureIds.add(type.getId());
                        return getLogicalValue(value, previousStructureIds, context);
                    } catch (CoreException e) {
                        // unable to display logical structure
                    }
                }
            }
        }
        return value;
    }

    /**
     * Return wether to show compute a logical structure or a raw structure
     * in the specified context
     * 
     * @return wether to show compute a logical structure or a raw structure
     * in the specified context
     */
    protected boolean isShowLogicalStructure(IPresentationContext context) {
    	IWorkbenchPart part = context.getPart();
    	if (part instanceof VariablesView) {
			return ((VariablesView) part).isShowLogicalStructure();
		}
        return false;
    }

    /**
     * Returns the number of entries that should be displayed in each partition
     * of an indexed collection.
     * 
     * @return the number of entries that should be displayed in each partition
     *         of an indexed collection
     */
    protected int getArrayPartitionSize() {
        // TODO: should fix this with a user pref
        return 100;
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.viewers.AsynchronousTreeContentAdapter#supportsPartId(java.lang.String)
	 */
	protected boolean supportsPartId(String id) {
        return id.equals(IDebugUIConstants.ID_EXPRESSION_VIEW) || id.equals(IDebugUIConstants.ID_VARIABLE_VIEW) || id.equals(IDebugUIConstants.ID_REGISTER_VIEW);
	}   	    
}
