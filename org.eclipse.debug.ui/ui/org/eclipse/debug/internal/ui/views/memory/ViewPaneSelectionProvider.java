/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
 
package org.eclipse.debug.internal.ui.views.memory;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;


public class ViewPaneSelectionProvider implements ISelectionProvider
{
	ArrayList fListeners = new ArrayList();
	ISelection fSelection;

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener)
	{
		if (!fListeners.contains(listener))
			fListeners.add(listener);
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
	 */
	public ISelection getSelection()
	{
		return fSelection;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	public void removeSelectionChangedListener(ISelectionChangedListener listener)
	{
		if (fListeners.contains(listener))
			fListeners.remove(listener);
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
	 */
	public void setSelection(ISelection selection)
	{				
		if (fSelection instanceof IStructuredSelection && selection instanceof IStructuredSelection)
		{
			Object currentSel = ((IStructuredSelection)fSelection).getFirstElement();
			Object newSel = ((IStructuredSelection)selection).getFirstElement();
			
			if (currentSel != newSel)
			{
				fSelection = selection;
				fireChanged();
			}
		}
		else if (fSelection != selection)
		{
			fSelection = selection;
			fireChanged();
		}
	}
	
	public void fireChanged()
	{
		SelectionChangedEvent evt = new SelectionChangedEvent(this, getSelection());
		for (int i=0; i<fListeners.size(); i++)
		{
			((ISelectionChangedListener)fListeners.get(i)).selectionChanged(evt);
		}
	}
}
