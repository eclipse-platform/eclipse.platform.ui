/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
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

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.MemoryByte;
import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.debug.internal.ui.views.memory.MemoryViewUtil;
import org.eclipse.debug.ui.memory.AbstractTableRendering;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.swt.widgets.TableItem;

/**
 * @since 3.1
 */
public class TableRenderingCellModifier implements ICellModifier
{
	private static final String PREFIX = "MemoryViewCellModifier."; //$NON-NLS-1$
	public static final String TITLE = PREFIX + "failure_title"; //$NON-NLS-1$
	public static final String FAILED = PREFIX + "failed";  //$NON-NLS-1$
	public static final String DATA_IS_INVALID = PREFIX + "data_is_invalid";  //$NON-NLS-1$
	public static final String DATA_IS_TOO_LONG = PREFIX + "data_is_too_long";  //$NON-NLS-1$

	private boolean editActionInvoked = false;
	private AbstractTableRendering fRendering;
	
	public TableRenderingCellModifier(AbstractTableRendering rendering)
	{
		fRendering = rendering;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
	 */
	public boolean canModify(Object element, String property)
	{	
		boolean canModify = true;	
		try
		{	
			if (!(element instanceof TableRenderingLine))
				return false;
				
			if (!editActionInvoked)
				return false;	
				
			if (fRendering == null)
				return false;	
				
			if (fRendering.getMemoryBlock().supportsValueModification() == false)
			{
				return false;	
			}
			
			TableRenderingLine line = (TableRenderingLine)element;
			if (TableRenderingLine.P_ADDRESS.equals(property)) {
			   return false;
			}
			
			// property is stored as number of addressable unit away from the line address
			// to calculate offset to the memory line array, offset = numberofAddressableUnit * addressableSize
			int addressableSize = getAddressableSize();
			
			int offset = Integer.valueOf(property, 16).intValue()*addressableSize;
			int end = offset + fRendering.getBytesPerColumn();
			
			for (int i=offset; i<end; i++)
			{
				MemoryByte oneByte = line.getByte(i);

				if (oneByte.isReadonly())
				{
					canModify = false;
				}
			}
			return canModify;
		}
		catch (NumberFormatException e)
		{
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

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object, java.lang.String)
	 */
	public Object getValue(Object element, String property)
	{
		// give back the value of the column
		
		if (!(element instanceof TableRenderingLine))
			return null;
			
		TableRenderingLine line = (TableRenderingLine)element;
		try
		{
			if (TableRenderingLine.P_ADDRESS.equals(property))
			   return line.getAddress();
			
			int offset = Integer.valueOf(property, 16).intValue() * getAddressableSize();
			int end = offset + fRendering.getBytesPerColumn();
			

			//Ask for label provider
			MemoryByte[] memory = line.getBytes(offset, end);

			if (line.isAvailable(offset, end))
			{
				// ask the renderer for a string representation of the bytes
				offset  = Integer.valueOf(property, 16).intValue();
				
				BigInteger address = new BigInteger(((TableRenderingLine)element).getAddress(), 16);
				address = address.add(BigInteger.valueOf(offset)); 
				
				return fRendering.getString(fRendering.getRenderingId(), address, memory);
			}
			// if the range is not available, just return padded string
			return getPaddedString(offset, end);
		}
		catch (NumberFormatException e)
		{
			return "00"; //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object, java.lang.String, java.lang.Object)
	 */
	public void modify(Object element, String property, Object value)
	{
		TableRenderingLine line = null;
		if (!(element instanceof TableRenderingLine))
		{
			line = (TableRenderingLine)((TableItem)element).getData();
		}
		else
		{
			line = (TableRenderingLine)element;
		}
			
		// calculate offset to update	
		IMemoryBlock memory = fRendering.getMemoryBlock();

		int lineOffset = Integer.valueOf(property, 16).intValue();
		
		// this offset is number of addressable unit from the line address
		long offset = getOffset(memory, line.getAddress(), lineOffset);
		
		// validate data
		if (!(value instanceof String))
			return;
		
		try
		{
			byte[] bytes = null;
			
			String oldValue = (String)getValue(line, property);		
			
			if (!oldValue.equals(value))
			{	

				// property is number of addressable unit from line address
				// to calculate proper offset in the memoryViewLine's array
				// offset = numberOfAddressableUnit * addressableSize
				int offsetToLine = Integer.valueOf(property, 16).intValue() * getAddressableSize();
				int end = offsetToLine + fRendering.getBytesPerColumn();
				
				MemoryByte[] oldArray= line.getBytes(offsetToLine, end);
				
				BigInteger address = new BigInteger(line.getAddress(), 16);
				address = address.add(BigInteger.valueOf(offsetToLine)); 
				
				bytes = fRendering.getBytes(fRendering.getRenderingId(), address, oldArray, (String)value);
				
				if (bytes == null)
					return;
				
				if (bytes.length == 0)
					return;
				
				if (bytes.length <= oldArray.length)
				{
					boolean changed = false;
					// check that the bytes returned has actually changed
					for (int i=0; i<bytes.length; i++)
					{
						if (bytes[i] != oldArray[i].getValue())
						{
							changed = true;
							break;
						}
					}
					if (!changed)
						return;
				}
			}
			else
			{	
				// return if value has not changed
				return;
			}
			memory.setValue(offset, bytes);
		}
		catch (DebugException e)
		{
			MemoryViewUtil.openError(DebugUIMessages.getString(TITLE), DebugUIMessages.getString(FAILED), e);
		}
		catch(NumberFormatException e)
		{
			MemoryViewUtil.openError(DebugUIMessages.getString(TITLE), 
					DebugUIMessages.getString(FAILED) + "\n" + DebugUIMessages.getString(DATA_IS_INVALID), null); //$NON-NLS-1$
		}
		
	}

	private long getOffset(IMemoryBlock memory, String lineAddress, int lineOffset) {
		
		BigInteger lineAddr = new BigInteger(lineAddress, 16);
		BigInteger memoryAddr;
		
		if (memory instanceof IMemoryBlockExtension)
		{
			memoryAddr = ((IMemoryBlockExtension)memory).getBigBaseAddress();
		}
		else
		{
			memoryAddr = BigInteger.valueOf(memory.getStartAddress());
		}
		
		if (memoryAddr == null)
			memoryAddr = new BigInteger("0"); //$NON-NLS-1$
		
		long offset = lineAddr.subtract(memoryAddr).longValue();
		
		return offset + lineOffset;
	}
	


	/**
	 * @param editActionInvoked The editActionInvoked to set.
	 */
	public void setEditActionInvoked(boolean editActionInvoked) {
		this.editActionInvoked = editActionInvoked;
	}
	
	/**
	 * @param start
	 * @param end
	 * @return padded string
	 */
	public String getPaddedString(int start, int end) {
		StringBuffer buf = new StringBuffer();
		String paddedStr = DebugUIPlugin.getDefault().getPreferenceStore().getString(IDebugPreferenceConstants.PREF_PADDED_STR);
		for (int i=start; i<end; i++)
		{	
			buf.append(paddedStr);
		}
		return buf.toString();
	}

}
