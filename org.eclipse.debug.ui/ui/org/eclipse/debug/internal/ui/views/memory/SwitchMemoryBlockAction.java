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

package org.eclipse.debug.internal.ui.views.memory;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IMemoryBlockListener;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.memory.IMemoryRendering;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.progress.WorkbenchJob;

public class SwitchMemoryBlockAction extends Action implements IViewActionDelegate, IActionDelegate2 {

	private IViewPart fView;
	private MenuCreator fMenuCreator;
	private IAction fAction;
	
	private IMemoryBlockListener fListener = new IMemoryBlockListener() {
		public void memoryBlocksAdded(IMemoryBlock[] memory) {
			updateActionEnablement();	
		}

		public void memoryBlocksRemoved(IMemoryBlock[] memory) {
			updateActionEnablement();
		}
	};
	
	private IDebugContextListener fDebugContextListener = new IDebugContextListener() {

		public void debugContextChanged(DebugContextEvent event) {
			updateActionEnablement();
		}
		
	};
	
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

		public SwitchToAction(final IMemoryBlock memBlk, boolean buildLabel)
		{
			super();
			
			if (buildLabel)
			{
				setText(DebugUIMessages.SwitchMemoryBlockAction_4);
				Job job = new Job("SwtichToAction"){ //$NON-NLS-1$
	
					protected IStatus run(IProgressMonitor monitor) {
						getLabels(memBlk);
						return Status.OK_STATUS;
					}}; 
				job.setSystem(true);
				job.schedule();
			}
			
			fMemoryblock = memBlk;
		}
		
		public SwitchToAction(final IMemoryBlock memBlk, String label)
		{
			super(label);
			fMemoryblock = memBlk;
		}
		
		
		private void getLabels(final IMemoryBlock memBlk)
		{
			StringBuffer text = new StringBuffer(""); //$NON-NLS-1$
			String label = new String(""); //$NON-NLS-1$
			if (memBlk instanceof IMemoryBlockExtension)
			{
				String expression = ((IMemoryBlockExtension)memBlk).getExpression();
				if (expression == null)
					expression = DebugUIMessages.SwitchMemoryBlockAction_0;
				
				text.append(expression);
			}
			else
			{
				long address = memBlk.getStartAddress();
				text.append(Long.toHexString(address));
			}
			
			label = text.toString();
			label = decorateLabel(memBlk, label);

			final String finalLabel = label;
			WorkbenchJob job = new WorkbenchJob("SwtichToAction Update Label") { //$NON-NLS-1$
				public IStatus runInUIThread(IProgressMonitor monitor) {
					SwitchToAction.super.setText(finalLabel);
					return Status.OK_STATUS;
				}};
			job.setSystem(true);
			job.schedule();
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

				// get all memory blocks from tree viewer
				IMemoryBlock[] allMemoryBlocks = null;
				
				// get selection from memory view
				IMemoryBlock memoryBlock = getCurrentMemoryBlock();
			
				Object context = DebugUITools.getDebugContext();
				IMemoryBlockRetrieval retrieval =  MemoryViewUtil.getMemoryBlockRetrieval(context);
				if (retrieval != null)
				{
					allMemoryBlocks = DebugPlugin.getDefault().getMemoryBlockManager().getMemoryBlocks(retrieval);
				}
				
				if (allMemoryBlocks != null)
				{
					for (int i=0; i<allMemoryBlocks.length; i++)
					{	
						SwitchToAction action = new SwitchToAction(allMemoryBlocks[i], true);
						
						if (allMemoryBlocks[i] == memoryBlock)
							action.setChecked(true);
						
						ActionContributionItem item = new ActionContributionItem(action);
						item.fill(dropdown, -1);
						
						item.getAction().setChecked(true);
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
		DebugUITools.getDebugContextManager().getContextService(fView.getViewSite().getWorkbenchWindow()).addDebugContextListener(fDebugContextListener);
		DebugPlugin.getDefault().getMemoryBlockManager().addListener(fListener);
		updateActionEnablement();
	}
	
	private StructuredViewer getViewer()
	{
		if (fView == null)
			return null;
		
		if (fView instanceof MemoryView) 
		{
			MemoryView memView = (MemoryView)fView;
			IMemoryViewPane pane = memView.getViewPane(MemoryBlocksTreeViewPane.PANE_ID);
			if (pane instanceof MemoryBlocksTreeViewPane)
			{
				 StructuredViewer viewer = ((MemoryBlocksTreeViewPane)pane).getViewer();
				return viewer;
			}
		}
		return null;
	}
	
	private void updateActionEnablement()
	{
		if (fAction != null)
		{
			IAdaptable context = DebugUITools.getDebugContext();
			if (context != null)
			{
				IMemoryBlockRetrieval retrieval = null;
				
				if (context.getAdapter(IMemoryBlockRetrieval.class) != null)
					retrieval = (IMemoryBlockRetrieval)context.getAdapter(IMemoryBlockRetrieval.class);
				
				if (retrieval == null && context instanceof IMemoryBlockRetrieval)
					retrieval = (IMemoryBlockRetrieval)context;
				
				if (retrieval == null && context instanceof IDebugElement)
					retrieval = ((IDebugElement)context).getDebugTarget();
				
				if (retrieval != null)
				{
					IMemoryBlock[] memoryBlocks = DebugPlugin.getDefault().getMemoryBlockManager().getMemoryBlocks(retrieval);
					fAction.setEnabled(memoryBlocks.length > 0);
					return;
				}
				else if (getViewer() != null)
				{
					StructuredViewer viewer = getViewer();
					if (viewer.getInput() != null && viewer.getInput() instanceof IMemoryBlockRetrieval)
					{
						retrieval = (IMemoryBlockRetrieval)viewer.getInput();
						IMemoryBlock[] memoryBlocks = DebugPlugin.getDefault().getMemoryBlockManager().getMemoryBlocks(retrieval);
						fAction.setEnabled(memoryBlocks.length > 0);
						return;
					}
				}
			}
			fAction.setEnabled(false);
		}
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
				doSwitchToNext(memoryBlocks);
			}
		}
	}

	/**
	 * @param memoryBlocks
	 */
	private void doSwitchToNext(IMemoryBlock[] memoryBlocks) {
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
			
			SwitchToAction switchAction = new SwitchToAction(memoryBlocks[next], false);
			switchAction.run();
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

	public void init(IAction action) {
		fAction = action;
		updateActionEnablement();
		fMenuCreator = new MenuCreator();
		action.setMenuCreator(fMenuCreator);
	}

	public void dispose() {
		fAction = null;
		DebugPlugin.getDefault().getMemoryBlockManager().removeListener(fListener);
		DebugUITools.getDebugContextManager().getContextService(fView.getViewSite().getWorkbenchWindow()).removeDebugContextListener(fDebugContextListener);
		
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

	/**
	 * @param memBlk
	 * @param label
	 */
	private String decorateLabel(final IMemoryBlock memBlk, String label) {
		ILabelDecorator decorator = (ILabelDecorator)memBlk.getAdapter(ILabelDecorator.class);
		if (decorator != null)
			label = decorator.decorateText(label, memBlk);
		return label;
	}

}
