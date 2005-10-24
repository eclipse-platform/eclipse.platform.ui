/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.debug.internal.ui.views.memory;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.memory.IMemoryRendering;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;

public class SwitchMemoryBlockAction extends Action implements IViewActionDelegate, IActionDelegate2 {

	private IViewPart fView;
	private MenuCreator fMenuCreator;
	
	/**
	 * Switch tab folder for fMemoryBlock to the top in Memory Rendering View
	 */
	class SwitchToAction extends Action
	{
		private IMemoryBlock fMemoryblock;
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.action.IAction#run()
		 */
		public void run() {
			if (fView == null)
				return;
			
			// tell the view to switch memory block
			fView.getSite().getSelectionProvider().setSelection(new StructuredSelection(fMemoryblock));
		}

		public SwitchToAction(IMemoryBlock memBlk)
		{
			super();
			try {
				String text;
				if (memBlk instanceof IMemoryBlockExtension)
				{
					text = ((IMemoryBlockExtension)memBlk).getExpression();
					
					if (text == null)
					{
						text = DebugUIMessages.SwitchMemoryBlockAction_0;
					}
					
					if (((IMemoryBlockExtension)memBlk).getBigBaseAddress() != null)
					{	
						text += " : 0x"; //$NON-NLS-1$
						text += ((IMemoryBlockExtension)memBlk).getBigBaseAddress().toString(16);
					}
				}
				else
				{
					long address = memBlk.getStartAddress();
					text = Long.toHexString(address);
				}
				super.setText(text);
				
				PlatformUI.getWorkbench().getHelpSystem().setHelp(this, DebugUIPlugin.getUniqueIdentifier() + ".switchToAction_context"); //$NON-NLS-1$
				
			} catch (DebugException e) {
				
				super.setText(DebugUIMessages.SwitchMemoryBlockAction_3);
			}
			fMemoryblock = memBlk;
		}
	}
	
	class MenuCreator implements IMenuCreator
	{
		Menu dropdown;

		/* (non-Javadoc)
		 * @see org.eclipse.jface.action.IMenuCreator#dispose()
		 */
		public void dispose() {
			if (dropdown != null)
				dropdown.dispose();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Control)
		 */
		public Menu getMenu(Control parent) {
			
			if (dropdown != null)
			{	
				dropdown.dispose();
				dropdown = null;
			}
			
			if (dropdown == null)
			{	
				dropdown =  new Menu(parent);
				
				ISelection selection = DebugUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection(IDebugUIConstants.ID_DEBUG_VIEW);
				
				if (selection == null)
					return dropdown;
				
				if (selection instanceof StructuredSelection && !selection.isEmpty())
				{	
					if (((StructuredSelection)selection).getFirstElement() instanceof IDebugElement)
					{	
						IDebugElement element = (IDebugElement)((StructuredSelection)selection).getFirstElement(); 
						IMemoryBlockRetrieval retrieval = (IMemoryBlockRetrieval)element.getAdapter(IMemoryBlockRetrieval.class);
						
						if (retrieval == null)
							retrieval = element.getDebugTarget();
						
						if (retrieval != null)
						{
							// get all memory blocks associated with selected debug target
							IMemoryBlock[] allMemoryBlocks = DebugPlugin.getDefault().getMemoryBlockManager().getMemoryBlocks(retrieval);
							
							// get selection from memory view
							IMemoryBlock memoryBlock = getCurrentMemoryBlock();
						
							for (int i=0; i<allMemoryBlocks.length; i++)
							{	
								SwitchToAction action = new SwitchToAction(allMemoryBlocks[i]);
								
								if (allMemoryBlocks[i] == memoryBlock)
									action.setChecked(true);
								
								ActionContributionItem item = new ActionContributionItem(action);
								item.fill(dropdown, -1);
							}
						}
					}
				}
			}
			
			return dropdown;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets.Menu)
		 */
		public Menu getMenu(Menu parent) {
			return null;
		}
		
	}	
	
	public void init(IViewPart view) {
		fView = view;
	}

	public void run(IAction action) {
		switchToNext();
	}

	public void run() {
		switchToNext();
	}

	private void switchToNext() {		
		IAdaptable context = DebugUITools.getDebugContext();
		if (context instanceof IDebugElement)
		{
			IDebugElement debugContext = (IDebugElement)context;
			IMemoryBlockRetrieval retrieval = (IMemoryBlockRetrieval)debugContext.getAdapter(IMemoryBlockRetrieval.class);
			if (retrieval == null)
				retrieval = debugContext.getDebugTarget();
			
			if (retrieval != null)
			{
				IMemoryBlock[] memoryBlocks = DebugPlugin.getDefault().getMemoryBlockManager().getMemoryBlocks(retrieval);
				// only run if there is more than one memory block
				if (memoryBlocks.length > 1)
				{
					IMemoryBlock current = getCurrentMemoryBlock();
					
					int next = 0;
					if (current != null)
					{
						for (int i=0; i<memoryBlocks.length; i++)
						{
							if (memoryBlocks[i] == current)
								next = i+1;
						}
					}
					
					if (next > memoryBlocks.length-1)
						next = 0;
					
					SwitchToAction switchAction = new SwitchToAction(memoryBlocks[next]);
					switchAction.run();
				}
			}
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

	public void init(IAction action) {
		fMenuCreator = new MenuCreator();
		action.setMenuCreator(fMenuCreator);
	}

	public void dispose() {
		if (fMenuCreator != null)
			fMenuCreator.dispose();
	}

	public void runWithEvent(IAction action, Event event) {
		switchToNext();
	}

	private IMemoryBlock getCurrentMemoryBlock() {
		if (fView == null)
			return null;
		
		ISelection memBlkSelection = fView.getSite().getSelectionProvider().getSelection();
		IMemoryBlock memoryBlock = null;
		
		if (memBlkSelection != null)
		{	
			if (!memBlkSelection.isEmpty() && memBlkSelection instanceof IStructuredSelection)
			{	
				Object obj = ((IStructuredSelection)memBlkSelection).getFirstElement();
				
				if (obj instanceof IMemoryBlock)
				{	
					memoryBlock = (IMemoryBlock)obj;
				}
				else if (obj instanceof IMemoryRendering){
					memoryBlock = ((IMemoryRendering)obj).getMemoryBlock();
				}
			}
		}
		return memoryBlock;
	}

}
