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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.MemoryByte;
import org.eclipse.debug.internal.ui.memory.provisional.AbstractAsyncTableRendering;
import org.eclipse.debug.internal.ui.memory.provisional.MemoryViewPresentationContext;
import org.eclipse.debug.internal.ui.viewers.AsynchronousTableViewer;
import org.eclipse.debug.internal.ui.viewers.ModelNode;
import org.eclipse.debug.internal.ui.views.memory.MemoryViewUtil;
import org.eclipse.debug.ui.memory.IMemoryRendering;


public class TableRenderingModel extends AbstractVirtualContentTableModel
		implements IContentChangeComputer {


	private Hashtable fCache;
	private Vector fOrderedCache;			// needed to re-organize cache
	
	private boolean fMBSupportsChangeManagement;
	private IMemoryBlock fMemoryBlock;

	class SupportsChangeMgmtJob extends Job {
		
		SupportsChangeMgmtJob()
		{
			super("Support Change Management"); //$NON-NLS-1$
			setSystem(true);
		}

		protected IStatus run(IProgressMonitor monitor) {
			IMemoryBlock mb = getMemoryBlock();
			if (mb instanceof IMemoryBlockExtension)
			{
				IMemoryBlockExtension mbExt = (IMemoryBlockExtension)mb;
				fMBSupportsChangeManagement = mbExt.supportsChangeManagement();
			}
			return Status.OK_STATUS;
		}
		
	}
	
	
	public TableRenderingModel(AsynchronousTableViewer viewer) {
		super(viewer);
		fCache = new Hashtable();
		fOrderedCache = new Vector();
	}
	
	public int indexOfKey(Object key)
	{
		if (key instanceof BigInteger)
		{
			BigInteger address = (BigInteger)key;
			Object items[] = getElements();
			
			for (int i=0; i<items.length; i++){
				if (items[i] != null && items[i] instanceof MemorySegment)
				{	
					MemorySegment line = (MemorySegment)items[i];
					if (line.containsAddress(address))
						return i;
				}
			}
		}
		
		return -1;
	}
	
	public int columnOf(Object element, Object key)
	{
		if (element instanceof MemorySegment && key instanceof BigInteger)
		{
			BigInteger address = (BigInteger)key;
			MemorySegment line = (MemorySegment)element;
			if (line.containsAddress(address))
			{
				if (getAddressableUnitsPerColumn() > 0)
				{
					BigInteger offset = address.subtract(line.getAddress());
					
					// locate column
					int colAddressableUnit = getAddressableUnitsPerColumn();
					int col = ((offset.intValue()/colAddressableUnit)+1);
					
					if (col == 0)
						col = 1;
					
					return col;
				}
			}
		}
		
		return -1;
	}
	
	public Object getKey(int idx)
	{
		Object elmt = getElement(idx);
		if (elmt instanceof MemorySegment)
		{
			return ((MemorySegment)elmt).getAddress();
		}
	
		return null;
	}

	public Object getKey(Object element) {
		int idx = indexOfElement(element);
		if (idx >= 0)
		{
			return getKey(idx);
		}
		return null;
	}
	
	public Object getKey(int idx, int col) {
		Object element = getElement(idx);
		if (element != null && element instanceof MemorySegment)
		{
			MemorySegment segment = (MemorySegment)element;
			BigInteger rowAddress = segment.getAddress();
			
			int offset;
			if (col > 0)
			{	
				// 	get address offset
				int addressableUnit = getAddressableUnitsPerColumn();
				offset = (col-1) * addressableUnit;
			}
			else
			{
				offset = 0;
			}
			return rowAddress.add(BigInteger.valueOf(offset));
		}
		return null;
	}
	
	private int getAddressableUnitsPerColumn()
	{
		AsynchronousTableViewer viewer = getTableViewer();
		if (viewer.getPresentationContext() instanceof MemoryViewPresentationContext)
		{
			MemoryViewPresentationContext context = (MemoryViewPresentationContext)viewer.getPresentationContext();
			if (getTableRendering(context)!= null)
			{
				return getTableRendering(context).getAddressableUnitPerColumn();
			}
		}
		return -1;
	}

	public void cache(Object[] elements) {
		for (int i=0; i<elements.length; i++)
		{
			Object obj = elements[i];
			if (obj instanceof MemorySegment)
			{
				cache(((MemorySegment)obj).getAddress(), obj);
			}
		}
		
	}
	
	private void cache(Object key, Object element)
	{
		fCache.put(key, element);
		fOrderedCache.add(element);
	}

	public Object[] compare(Object[] newElements) {
		
		if (fCache.isEmpty())
			return newElements;
		
		for (int j=0; j<newElements.length; j++)
		{
			Object obj = newElements[j];
			if (obj instanceof MemorySegment)
			{
				MemorySegment newSegment = (MemorySegment)obj;
				MemorySegment oldSegment = (MemorySegment)fCache.get(newSegment.getAddress());

				if (oldSegment != null)
				{
					if (oldSegment.getNumAddressableUnits() == newSegment.getNumAddressableUnits())
					{
						MemoryByte[] newBytes = newSegment.getBytes();
						MemoryByte[] oldBytes = oldSegment.getBytes();
						
						for (int i=0; i<newBytes.length; i++)
						{
							newBytes[i].setHistoryKnown(true);
							
							if (newBytes[i].isReadable() != oldBytes[i].isReadable())
							{
								newBytes[i].setChanged(true);
								continue;
							}			

							if (newBytes[i].isReadable() && oldBytes[i].isReadable() && 
								(newBytes[i].getValue() != oldBytes[i].getValue()))
								newBytes[i].setChanged(true);
						}
					}
				}
			}
		}		
		return newElements;
	}
	
	public void clearCache()
	{
		fCache.clear();
		fOrderedCache.clear();
	}

	public boolean isEmpty() {
		return fCache.isEmpty();
	}

	public void handleViewerChanged() {
		// viewer has changed, content manager needs to re-organize the cache
		rebuildCache();
		rebuildContent();
	}
	
	private void rebuildCache()
	{
		if (isEmpty())
			return;
		
		MemoryViewPresentationContext context = (MemoryViewPresentationContext)getTableViewer().getPresentationContext();
		AbstractAsyncTableRendering rendering = getTableRendering(context);
		
		if (rendering == null)
			return;
		
		ArrayList segments = new ArrayList(); 
		Enumeration enumeration = fOrderedCache.elements();
		
		BigInteger address = ((MemorySegment)fOrderedCache.get(0)).getAddress();
		while (enumeration.hasMoreElements())
		{
			Object element = enumeration.nextElement();
			if (element instanceof MemorySegment)
			{
				
				segments.add(element);
			}
		}
		
		MemoryByte[] bytes = convertSegmentsToBytes((MemorySegment[])segments.toArray(new MemorySegment[0]));
		
		int bytesPerLine = rendering.getBytesPerLine();
		int numAddressableUnitPerLine = rendering.getAddressableUnitPerLine();
		
		int addressableSize = rendering.getAddressableSize();
		
		clearCache();
		
		TableRenderingContentDescriptor descriptor = (TableRenderingContentDescriptor)rendering.getAdapter(TableRenderingContentDescriptor.class);
		boolean alignAddress = true;
		if (descriptor != null && !descriptor.isAlignAddressToBoundary())
		{
			alignAddress = descriptor.isAlignAddressToBoundary();
		}
		
		MemorySegment[] newSegments = convertMemoryBytesToSegments(address, bytes, bytesPerLine, numAddressableUnitPerLine, addressableSize, alignAddress);
		for (int i=0; i<newSegments.length; i++)
		{
			cache(newSegments[i].getAddress(), newSegments[i]);
		}
	}
	
	private void rebuildContent()
	{
		MemoryViewPresentationContext context = (MemoryViewPresentationContext)getTableViewer().getPresentationContext();
		AbstractAsyncTableRendering rendering = getTableRendering(context);
		
		if (rendering == null)
			return;
		
		ArrayList segments = new ArrayList();
		Object[] elements = getElements();
		for (int i=0; i<elements.length; i++)
		{
			Object element = elements[i];
			if (element instanceof MemorySegment)
			{
				segments.add(element);
			}
		}
		
		MemoryByte[] bytes = convertSegmentsToBytes((MemorySegment[])segments.toArray(new MemorySegment[segments.size()]));
		
		int bytesPerLine = rendering.getBytesPerLine();
		int numAddressableUnitPerLine = rendering.getAddressableUnitPerLine();
		BigInteger address = (BigInteger)getKey(0);

		int addressableSize = rendering.getAddressableSize();
		
		TableRenderingContentDescriptor descriptor = (TableRenderingContentDescriptor)rendering.getAdapter(TableRenderingContentDescriptor.class);
		boolean alignAddress = true;
		if (descriptor != null && !descriptor.isAlignAddressToBoundary())
		{
			alignAddress = descriptor.isAlignAddressToBoundary();
		}
		
		MemorySegment[] newSegments = convertMemoryBytesToSegments(address, bytes, bytesPerLine, numAddressableUnitPerLine, addressableSize, alignAddress);
		remove(getElements());
		add(newSegments);
	}

	
	private MemoryByte[] convertSegmentsToBytes(MemorySegment[] segments)
	{
		ArrayList toReturn = new ArrayList();
		for (int i=0; i<segments.length; i++)
		{
			MemoryByte[] temp = segments[i].getBytes();
			for (int j=0; j<temp.length; j++)
			{
				toReturn.add(temp[j]);
			}
		}
		return (MemoryByte[])toReturn.toArray(new MemoryByte[0]);
	}
	
	private MemorySegment[] convertMemoryBytesToSegments(BigInteger address, MemoryByte[] bytes, int bytesPerLine, int numAddressableUnitPerLine, int addressableSize, boolean alignAddress) {
		
		Assert.isTrue(bytesPerLine > 0);
		Assert.isTrue(numAddressableUnitPerLine > 0);
		
		ArrayList segments = new ArrayList();
		MemoryByte[] temp = bytes;
		
		if (alignAddress)
		{
			BigInteger alignedAddress = MemoryViewUtil.alignToBoundary(address, numAddressableUnitPerLine);
			
			// also check that the address is properly aligned and prepend bytes if need to
			if (!address.subtract(alignedAddress).equals(BigInteger.ZERO))
			{
				BigInteger unitsToSetBack = address.subtract(alignedAddress);
				BigInteger tempAddress = address.subtract(unitsToSetBack);
				// only do this if the resulted address >= 0
				// do not want to have negative addresses
				if (tempAddress.compareTo(BigInteger.ZERO) >= 0)
				{
					address = alignedAddress;
					int numBytesNeeded = unitsToSetBack.intValue() * addressableSize;
					temp = new MemoryByte[bytes.length + numBytesNeeded];
					
					for (int i=0; i<numBytesNeeded; i++)
					{
						temp[i] = new MemoryByte();
						temp[i].setReadable(false);
						temp[i].setWritable(false);
						temp[i].setEndianessKnown(false);	
					}	
					
					System.arraycopy(bytes, 0, temp, numBytesNeeded, bytes.length);
					bytes = temp;
				}
			}
		}
		
		if (bytes.length % bytesPerLine != 0)
		{
			int numBytesNeeded = bytesPerLine - (bytes.length % bytesPerLine);
			temp = new MemoryByte[bytes.length + numBytesNeeded];
			System.arraycopy(bytes, 0, temp, 0, bytes.length);
			
			for (int i=bytes.length; i<temp.length; i++)
			{
				temp[i] = new MemoryByte();
				temp[i].setReadable(false);
				temp[i].setWritable(false);
				temp[i].setEndianessKnown(false);	
			}	
			bytes = temp;
		}
		
		int idx = 0;
		while (idx < bytes.length && (idx + bytesPerLine)<= bytes.length)
		{
			MemoryByte[] newBytes = new MemoryByte[bytesPerLine];
			System.arraycopy(bytes, idx, newBytes, 0, bytesPerLine);
			
			MemorySegment segment = new MemorySegment(address, newBytes, numAddressableUnitPerLine);
			segments.add(segment);
			
			address = address.add(BigInteger.valueOf(numAddressableUnitPerLine));
			idx += bytesPerLine;
		}
		
		return (MemorySegment[])segments.toArray(new MemorySegment[segments.size()]);
	}
	
	private AsynchronousTableViewer getTableViewer()
	{
		return (AsynchronousTableViewer)getViewer();
	}

	protected void setChildren(ModelNode parentNode, List kids) {
		
		if (computeChanges())
		{
			Object[] newContent = compare(kids.toArray());
			ArrayList newList = new ArrayList();
			for (int i=0; i<newContent.length; i++)
			{
				newList.add(newContent[i]);
			}
			super.setChildren(parentNode, newList);
		}
		else
			super.setChildren(parentNode, kids);
	}
	
	private boolean computeChanges()
	{ 
		if (isEmpty())
			return false;
		
		if (fMBSupportsChangeManagement)
		{
			return false;
		}
		
		return true;
	}
	
	private IMemoryBlock getMemoryBlock()
	{
		return fMemoryBlock;
	}
	

	public void init(Object root) {
		if (root instanceof IMemoryBlock)
		{
			fMemoryBlock = (IMemoryBlock)root;
			new SupportsChangeMgmtJob().schedule();
		}
		super.init(root);
	}
	
	private AbstractAsyncTableRendering getTableRendering(MemoryViewPresentationContext context)
	{
		IMemoryRendering memRendering = context.getRendering();
		if (memRendering != null && memRendering instanceof AbstractAsyncTableRendering)
		{
			return (AbstractAsyncTableRendering)memRendering;
		}
		return null;
	}
}
