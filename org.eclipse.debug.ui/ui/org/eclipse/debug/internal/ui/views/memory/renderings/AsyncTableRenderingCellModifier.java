/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.views.memory.renderings;

import java.math.BigInteger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.MemoryByte;
import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.views.memory.MemoryViewUtil;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.swt.widgets.TableItem;

/**
 * @since 3.1
 *
 */
// TODO:  if we want "true" flexible hierarchy, also need to allow clients
// to plug cell modifier in case the element is not MemorySegment
public class AsyncTableRenderingCellModifier implements ICellModifier {

    private boolean editActionInvoked = false;

    private AbstractAsyncTableRendering fRendering;

    public AsyncTableRenderingCellModifier(AbstractAsyncTableRendering rendering) {
        fRendering = rendering;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object,
     *      java.lang.String)
     */
    public boolean canModify(Object element, String property) {
    	// TODO:  should we make sure asking properties from MemoryByte is done
    	// on non-UI thread?
    	
        boolean canModify = true;
        try {
            if (!(element instanceof MemorySegment))
                return false;

            if (!editActionInvoked)
                return false;

            // 	TODO:  this needs to go on non-UI thread
            if (fRendering.getMemoryBlock().supportsValueModification() == false) {
                return false;
            }

            MemorySegment line = (MemorySegment) element;
            if (TableRenderingLine.P_ADDRESS.equals(property)) {
                return false;
            }

            // property is stored as number of addressable unit away from the
            // line address
            // to calculate offset to the memory line array, offset =
            // numberofAddressableUnit * addressableSize
            int addressableSize = getAddressableSize();

            int offset = Integer.valueOf(property, 16).intValue() * addressableSize;

            MemoryByte[] bytes = line.getBytes(offset, fRendering.getBytesPerColumn());
            for (int i = 0; i < bytes.length; i++) {
                if (!bytes[i].isWritable()) {
                    canModify = false;
                }
            }
            return canModify;
        } catch (NumberFormatException e) {
            canModify = false;
            return canModify;
        }
    }

    /**
     * @return
     */
    private int getAddressableSize() {
        int addressableSize = fRendering.getAddressableSize();
        if (addressableSize < 1)
            addressableSize = 1;
        return addressableSize;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object,
     *      java.lang.String)
     */
    public Object getValue(Object element, String property) {
        // give back the value of the column

        if (!(element instanceof MemorySegment))
            return null;

        MemorySegment line = (MemorySegment) element;
        try {
            if (TableRenderingLine.P_ADDRESS.equals(property))
                return line.getAddress();

            int offset = Integer.valueOf(property, 16).intValue() * getAddressableSize();
            
            MemoryByte[] memory = line.getBytes(offset, fRendering.getBytesPerColumn());

            offset = Integer.valueOf(property, 16).intValue();
            BigInteger address = line.getAddress().add(BigInteger.valueOf(offset));

            // ask the rendering for a string representation of the bytes
            return fRendering.getString(fRendering.getRenderingId(), address, memory);

        } catch (NumberFormatException e) {
            return "00"; //$NON-NLS-1$
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object,
     *      java.lang.String, java.lang.Object)
     */
    public void modify(Object element, final String property, final Object value) {
    	
        MemorySegment segment = null;
        if (element instanceof TableItem) {
        	Object data = ((TableItem)element).getData();
        	if (data != null && data instanceof MemorySegment)
        		segment = (MemorySegment)data;
        	
        } else if (element instanceof MemorySegment){
        	segment = (MemorySegment) element;
        }
        
        if (segment == null)
        	return;

        // validate data
        if (!(value instanceof String))
            return;
        
        final MemorySegment line = segment;
        
        Job job = new Job("Set Values"){ //$NON-NLS-1$

			protected IStatus run(IProgressMonitor monitor) {
				try {
		            // calculate offset to update
		            final IMemoryBlock memoryBlk = fRendering.getMemoryBlock();

		            int lineOffset = Integer.valueOf(property, 16).intValue();

		            // this offset is number of addressable unit from the line address
		            final BigInteger offset = getOffset(memoryBlk, line.getAddress(), lineOffset);

		            byte[] bytes = null;

		            String oldValue = (String) getValue(line, property);

		            if (!oldValue.equals(value)) {

		                // property is number of addressable unit from line address
		                // to calculate proper offset in the memoryViewLine's array
		                // offset = numberOfAddressableUnit * addressableSize
		                int offsetToLine = Integer.valueOf(property, 16).intValue() * getAddressableSize();

		                MemoryByte[] oldArray = line.getBytes(offsetToLine, fRendering.getBytesPerColumn());

		                BigInteger address = line.getAddress();
		                address = address.add(BigInteger.valueOf(offsetToLine));

		                bytes = fRendering.getBytes(fRendering.getRenderingId(), address, oldArray, (String) value);

		                if (bytes == null)
		                    return Status.OK_STATUS;

		                if (bytes.length == 0)
		                	 return Status.OK_STATUS;

		                if (bytes.length <= oldArray.length) {
		                    boolean changed = false;
		                    // check that the bytes returned has actually changed
		                    for (int i = 0; i < bytes.length; i++) {
		                        if (bytes[i] != oldArray[i].getValue()) {
		                            changed = true;
		                            break;
		                        }
		                    }
		                    if (!changed)
		                    	 return Status.OK_STATUS;
		                }
		            } else {
		                // return if value has not changed
		            	 return Status.OK_STATUS;
		            }
		            
		            final byte[] newByteValues = bytes;
		            
		            if (memoryBlk instanceof IMemoryBlockExtension)
		                ((IMemoryBlockExtension) memoryBlk).setValue(offset, newByteValues);
		            else
		                memoryBlk.setValue(offset.longValue(), newByteValues);				
		        } catch (DebugException e) {
		            MemoryViewUtil.openError(DebugUIMessages.MemoryViewCellModifier_failure_title, DebugUIMessages.MemoryViewCellModifier_failed, e);
		        } catch (NumberFormatException e) {
		            MemoryViewUtil.openError(DebugUIMessages.MemoryViewCellModifier_failure_title, DebugUIMessages.MemoryViewCellModifier_failed + "\n" + DebugUIMessages.MemoryViewCellModifier_data_is_invalid, null); //$NON-NLS-1$
		        }
		        return Status.OK_STATUS;
			}};

       job.setSystem(true);
       job.schedule();
    }

    private BigInteger getOffset(IMemoryBlock memory, BigInteger lineAddress, int lineOffset) throws DebugException {

        BigInteger memoryAddr;

        if (memory instanceof IMemoryBlockExtension) {
            memoryAddr = ((IMemoryBlockExtension) memory).getBigBaseAddress();
        } else {
            memoryAddr = BigInteger.valueOf(memory.getStartAddress());
        }

        if (memoryAddr == null)
            memoryAddr = new BigInteger("0"); //$NON-NLS-1$

        return lineAddress.subtract(memoryAddr).add(BigInteger.valueOf(lineOffset));
    }

    /**
     * @param editActionInvoked
     *            The editActionInvoked to set.
     */
    public void setEditActionInvoked(boolean editActionInvoked) {
        this.editActionInvoked = editActionInvoked;
    }

}
