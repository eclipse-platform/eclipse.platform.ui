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

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;


/**
 * Abstract action class for Memory View Action.
 * This allows action contribution to both toolbar actions
 * and context menu actions without having two seperate
 * implementations.  "getViewTab" gets called whenever the view
 * tab is required to run the action.
 * 
 * @since 3.0
 */
abstract public class AbstractMemoryAction extends Action
{
	public AbstractMemoryAction()
	{
		super();
	}
	
	public AbstractMemoryAction(String label)
	{
		super(label);
	}
	
	public AbstractMemoryAction(String label, int style){
		super(label, style);
	}
	
	/**
	 * @return the view tab for which the action should act on.
	 */
	abstract IMemoryViewTab getViewTab();
	
	/**
	 * Given a view id, return the top view tab from the view.
	 * Returns null if the view cannot be opened or a top view tab is not found.
	 * @param viewId
	 * @return
	 */
	public IMemoryViewTab getTopViewTabFromView(String viewId){
		
		if (viewId.equals(IInternalDebugUIConstants.ID_MEMORY_VIEW))
		{	
			//	open a new view if necessary
			IWorkbenchPage p= DebugUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
			if (p == null) {
				return null;
			}
			IViewPart view = null;
			view= p.findView(viewId);
			
			if (view == null) {
				try {
					IWorkbenchPart activePart= p.getActivePart();
					view= (MemoryView) p.showView(viewId);
					p.activate(activePart);
				} catch (PartInitException e) {
					return null;
				}		
				
			}
			
			if (view instanceof IMemoryView)
			{
				IMemoryViewTab topTab = ((IMemoryView)view).getTopMemoryTab();
				
				return topTab;
			}
			return null;
		}
		
		return null;
	}
}
