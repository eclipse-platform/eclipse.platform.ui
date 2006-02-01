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
import java.util.Vector;

import org.eclipse.debug.core.model.MemoryByte;
import org.eclipse.debug.internal.ui.viewers.AsynchronousTableViewer;
import org.eclipse.debug.internal.ui.viewers.AsynchronousTableViewerContentManager;

public class TableRenderingContentManager extends
		AsynchronousTableViewerContentManager implements IVirtualContentManager, IContentChangeComputer {

	private Hashtable fCache;
	private Vector fOrderedCache;			// needed to re-organize cache
	
	public TableRenderingContentManager(AsynchronousTableViewer viewer) {
		super(viewer);
		fCache = new Hashtable();
		fOrderedCache = new Vector();
	}
	
	public int indexOf(Object key)
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
		if (viewer.getPresentationContext() instanceof TableRenderingPresentationContext)
		{
			TableRenderingPresentationContext context = (TableRenderingPresentationContext)viewer.getPresentationContext();
			if (context.getTableRendering() != null)
			{
				return context.getTableRendering().getAddressableUnitPerColumn();
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
							if (newBytes[i].getValue() != oldBytes[i].getValue())
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
		
		int bytesPerLine = -1;
		int numAddressableUnitPerLine = -1;
		 
		if (getTableViewer().getPresentationContext() instanceof TableRenderingPresentationContext)
		{
			TableRenderingPresentationContext context = (TableRenderingPresentationContext)getTableViewer().getPresentationContext();
			AbstractAsyncTableRendering rendering = context.getTableRendering();
			bytesPerLine = rendering.getBytesPerLine();
			numAddressableUnitPerLine = rendering.getAddressableUnitPerLine();
		}
		
		clearCache();
		MemorySegment[] newSegments = convertMemoryBytesToSegments(address, bytes, bytesPerLine, numAddressableUnitPerLine);
		for (int i=0; i<newSegments.length; i++)
		{
			cache(newSegments[i].getAddress(), newSegments[i]);
		}
	}
	
	private void rebuildContent()
	{
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
		
		int bytesPerLine = -1;
		int numAddressableUnitPerLine = -1;
		 
		if (getTableViewer().getPresentationContext() instanceof TableRenderingPresentationContext)
		{
			TableRenderingPresentationContext context = (TableRenderingPresentationContext)getTableViewer().getPresentationContext();
			AbstractAsyncTableRendering rendering = context.getTableRendering();
			bytesPerLine = rendering.getBytesPerLine();
			numAddressableUnitPerLine = rendering.getAddressableUnitPerLine();
		}
		
		BigInteger address = (BigInteger)getKey(0);
		
		MemorySegment[] newSegments = convertMemoryBytesToSegments(address, bytes, bytesPerLine, numAddressableUnitPerLine);
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
	
	private MemorySegment[] convertMemoryBytesToSegments(BigInteger address, MemoryByte[] bytes, int bytesPerLine, int numAddressableUnitPerLine) {
		
		ArrayList segments = new ArrayList();
		if (bytesPerLine > 0 && numAddressableUnitPerLine > 0)
		{
			int idx = 0;
			
			while (idx < bytes.length && (idx + bytesPerLine)< bytes.length)
			{
				MemoryByte[] newBytes = new MemoryByte[bytesPerLine];
				System.arraycopy(bytes, idx, newBytes, 0, bytesPerLine);
				
				MemorySegment segment = new MemorySegment(address, newBytes, numAddressableUnitPerLine);
				segments.add(segment);
				
				address = address.add(BigInteger.valueOf(bytesPerLine));
				idx += bytesPerLine;
			}
		}
		return (MemorySegment[])segments.toArray(new MemorySegment[segments.size()]);
	}

	
//	private void printSegment(MemorySegment segment)
//	{
//		StringBuffer buf = new StringBuffer();
//		buf.append(segment.getAddress().toString(16));
//		buf.append(": ");
//		MemoryByte[] bytes = segment.getBytes();
//		for (int i=0; i<bytes.length; i++)
//		{
//			buf.append(bytes[i].getValue());
//			buf.append(" ");
//		}
//		System.out.println(buf.toString());
//	}


}
