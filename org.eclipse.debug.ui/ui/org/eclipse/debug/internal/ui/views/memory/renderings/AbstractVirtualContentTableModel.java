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

import java.util.ArrayList;

import org.eclipse.debug.internal.ui.viewers.AsynchronousTableModel;
import org.eclipse.debug.internal.ui.viewers.AsynchronousViewer;
import org.eclipse.debug.internal.ui.viewers.ModelNode;

abstract public class AbstractVirtualContentTableModel extends AsynchronousTableModel{

	public AbstractVirtualContentTableModel(AsynchronousViewer viewer) {
		super(viewer);
	}

	public Object[] getElements()
	{
		ModelNode[] nodes = getNodes(getRootNode().getElement());
		ArrayList result = new ArrayList();
		if (nodes != null)
		{
			for (int i=0; i<nodes.length; i++)
			{
				ModelNode[] children = nodes[i].getChildrenNodes();
				if (children != null)
				{
					for (int j=0; j<children.length; j++)
					{
						result.add(children[j].getElement());
					}
				}
			}
			
			return result.toArray();
		}
		return new Object[0];
	}
	
	public Object getElement(int idx)
	{
		Object[] elements = getElements();
		if (idx >=0 && idx < elements.length)
			return elements[idx];

		return null;
	}

	
	public int indexOfElement(Object element)
	{
		Object[] elements = getElements();
		
		for (int i=0; i<elements.length; i++)
		{
			if (elements[i] == element)
				return i;
		}
		return -1;
	}
	
	abstract public int indexOfKey(Object key);
	
	abstract public int columnOf(Object element, Object key);
	
	abstract public Object getKey(int idx);
	
	abstract public Object getKey(Object element);
	
	abstract public Object getKey(int idx, int col);
	
	public void handleViewerChanged() 
	{
		
	}

}
