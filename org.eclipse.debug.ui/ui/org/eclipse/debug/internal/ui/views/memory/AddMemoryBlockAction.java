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

import java.math.BigInteger;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.IMemoryBlockExtensionRetrieval;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.core.model.ITerminate;
import org.eclipse.debug.internal.core.MemoryBlockManager;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;


/**
 * Action for adding memory block.
 * 
 * @since 3.0
 */
public class AddMemoryBlockAction extends Action implements ISelectionListener, IDebugEventSetListener{
	
	private static String PREFIX = "AddMemoryBlockAction."; //$NON-NLS-1$
	private static String TITLE = PREFIX + "title"; //$NON-NLS-1$
	private static String TOOLTIP = PREFIX + "tooltip"; //$NON-NLS-1$
	private static String FAILED = PREFIX + "failed"; //$NON-NLS-1$
	private static String EXPR_EVAL_FAILED = PREFIX + "expressionEvalFailed";	 //$NON-NLS-1$
	private static String NO_MEMORY_BLOCK = PREFIX + "noMemoryBlock"; //$NON-NLS-1$
	
	protected ISelection currentSelection = null;
	protected IMemoryBlock fLastMemoryBlock;
	private boolean fAddDefaultRenderings = true;


	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled) {
		 
		super.setEnabled(enabled);
	}

	public AddMemoryBlockAction()
	{
		initialize();
	}
	
	/**
	 * @param addDefaultRenderings - specify if the action should add
	 * default renderings for the new memory block when it is run
	 */
	AddMemoryBlockAction(boolean addDefaultRenderings)
	{
		initialize();
		fAddDefaultRenderings = addDefaultRenderings;
	}
	
	/**
	 * 
	 */
	private void initialize() {
		setText(DebugUIMessages.getString(TITLE));
		
		setToolTipText(DebugUIMessages.getString(TOOLTIP));
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_MONITOR_EXPRESSION));
		setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_LCL_MONITOR_EXPRESSION));
		setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_MONITOR_EXPRESSION));
		
		// get selection from Debug View
		DebugUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getSelectionService().addSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
		
		// check to see if something is selected in the debug view since a selection event won't be generated for something selected prior to creating this action
		ISelection selection = DebugUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection(IDebugUIConstants.ID_DEBUG_VIEW);
		currentSelection = selection;
		
		// set up enablement based on current selection
		setEnabled(MemoryViewUtil.isValidSelection(selection));
		
		DebugPlugin.getDefault().addDebugEventListener(this);
	}
	
	public AddMemoryBlockAction(String text, int style)
	{
		super(text, style);
		
		setToolTipText(DebugUIMessages.getString(TOOLTIP));
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_MONITOR_EXPRESSION));
		setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_LCL_MONITOR_EXPRESSION));
		setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_MONITOR_EXPRESSION));
		
		DebugUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getSelectionService().addSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
		
		// check to see if something is selected in the debug view since a selection event won't be generated for something selected prior to creating this action
		ISelection selection = DebugUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection(IDebugUIConstants.ID_DEBUG_VIEW);
		currentSelection = selection;
		setEnabled(MemoryViewUtil.isValidSelection(selection));
		
		DebugPlugin.getDefault().addDebugEventListener(this);
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {

		//	get current selection from Debug View
		 ISelection selection = DebugUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection(IDebugUIConstants.ID_DEBUG_VIEW);
		 Object elem = ((IStructuredSelection)selection).getFirstElement();
	
		 if (!(elem instanceof IDebugElement))
			 return;
	

		// ask debug element about memeory retrieval
		IDebugTarget debugTarget = ((IDebugElement)elem).getDebugTarget();
		IMemoryBlockRetrieval standardMemRetrieval = (IMemoryBlockRetrieval)((IDebugElement)elem).getAdapter(IMemoryBlockRetrieval.class);
		
		if (standardMemRetrieval == null)
		{
			// if getAdapter returns null, assume debug target as memory block retrieval
			standardMemRetrieval = debugTarget;
		}
		
		Shell shell= DebugUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
		
		// create dialog to ask for expression/address to block
		MonitorMemoryBlockDialog dialog = new MonitorMemoryBlockDialog(shell, standardMemRetrieval);
		dialog.open();
		
		int returnCode = dialog.getReturnCode();
		
		if (returnCode == Window.CANCEL)
		{
			return;
		}
		
		// get expression entered in dialog
		String expression = dialog.getExpression();

		try {
			if (standardMemRetrieval instanceof IMemoryBlockExtensionRetrieval)
			{
				// if the debug session supports IMemoryBlockExtensionRetrieval
				IMemoryBlockExtensionRetrieval memRetrieval = (IMemoryBlockExtensionRetrieval)standardMemRetrieval;
				
				// get extended memory block with the expression entered
				IMemoryBlockExtension memBlock = memRetrieval.getExtendedMemoryBlock(expression, ((IDebugElement)elem));
				
				// add block to memory block manager
				if (memBlock != null)
				{
					fLastMemoryBlock = memBlock;
					
					MemoryViewUtil.getMemoryBlockManager().addMemoryBlocks(new IMemoryBlock[]{memBlock});
					if (fAddDefaultRenderings)
						addDefaultRenderings(memBlock);
					// move the tab with that memory block to the top
					switchMemoryBlockToTop(fLastMemoryBlock);
				}
				else
				{
					// open error if it failed to retrieve a memory block
					MemoryViewUtil.openError(DebugUIMessages.getString(TITLE), DebugUIMessages.getString(NO_MEMORY_BLOCK), null);					
				}
			}
			else
			{
				// if the debug session does not support IMemoryBlockExtensionRetrieval
				expression = expression.toUpperCase();
				String hexPrefix = "0X"; //$NON-NLS-1$
				if (expression.startsWith(hexPrefix))
				{
					expression = expression.substring(hexPrefix.length());
				}
				
				// convert the expression to an address
				BigInteger address = new BigInteger(expression, 16);

				long longAddress = address.longValue();

				// get the length of memory to block
				String strLength = dialog.getLength();
				
				long length = Long.parseLong(strLength);
				
				// must block at least one line
				if (length == 0)
				{
					length = IInternalDebugUIConstants.BYTES_PER_LINE;
				}
				
				// get standard memory block
				IMemoryBlock memBlock = standardMemRetrieval.getMemoryBlock(longAddress, length);
				
				// make sure the memory block returned is not an instance of IMemoryBlockExtension
				if (memBlock instanceof IMemoryBlockExtension)
				{
					Status status = new Status(IStatus.WARNING, DebugUIPlugin.getUniqueIdentifier(),	0, 
						"IMemoryBlockRetrieval returns IMemoryBlockExtension.  This may result in unexpected behavior.", null); //$NON-NLS-1$
					DebugUIPlugin.log(status);
				}
				
				if (memBlock != null)
				{
					// add memory block to memory block manager
					fLastMemoryBlock = memBlock;
					MemoryViewUtil.getMemoryBlockManager().addMemoryBlocks(new IMemoryBlock[]{memBlock});
					if (fAddDefaultRenderings)
						addDefaultRenderings(memBlock);
					
					// move the tab with that memory block to the top
					switchMemoryBlockToTop(fLastMemoryBlock);
				}
				else
				{
					// otherwise open up an error doalog
					MemoryViewUtil.openError(DebugUIMessages.getString(TITLE), DebugUIMessages.getString(NO_MEMORY_BLOCK), null);
				}
			}
		} catch (DebugException e1) {
			MemoryViewUtil.openError(DebugUIMessages.getString(TITLE), DebugUIMessages.getString(FAILED), e1);
		}
		catch(NumberFormatException e2)
		{
			String message = DebugUIMessages.getString(FAILED) + "\n" + DebugUIMessages.getString(EXPR_EVAL_FAILED); //$NON-NLS-1$
			MemoryViewUtil.openError(DebugUIMessages.getString(TITLE), message, null);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		
		// update enablement state based on selection from Debug View
		setEnabled(MemoryViewUtil.isValidSelection(selection));
		currentSelection = selection;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse.debug.core.DebugEvent[])
	 */
	public void handleDebugEvents(DebugEvent[] events) {
		for (int i=0; i < events.length; i++)
				handleDebugEvent(events[i]);
	} 
	
	private void handleDebugEvent(DebugEvent event)
	{
		// update action enablement based on debug event
		Object src = event.getSource();
		IDebugTarget srcDT = null;
		IDebugTarget selectionDT = null;
		
		if (event.getKind() == DebugEvent.TERMINATE)
		{
			if (src instanceof ITerminate && src instanceof IDebugElement)
			{
				srcDT = ((IDebugElement)src).getDebugTarget();
			}
			
			if (currentSelection instanceof IStructuredSelection)
			{
				Object elem = ((IStructuredSelection)currentSelection).getFirstElement();
				if (elem instanceof IDebugElement)
				{
					selectionDT = ((IDebugElement)elem).getDebugTarget();
				}
			}

			// disable action if the debug target is terminated.
			if (srcDT == selectionDT)
			{
				setEnabled(false);
			}
		}
	}
	
	/**
	 * Return the last memory block added to memory block manager via this action.
	 * @return Returns the fLastMemoryBlock.
	 */
	public IMemoryBlock getLastMemoryBlock() {
		return fLastMemoryBlock;
	}
	
	private void switchMemoryBlockToTop(IMemoryBlock memoryBlock)
	{
		//	open a new view if necessary
		IWorkbenchPage p= DebugUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
		if (p == null) {
			return;
		}
		IViewPart view = null;
		view= p.findView(IInternalDebugUIConstants.ID_MEMORY_VIEW);
		
		if (view == null) {
			try {
				IWorkbenchPart activePart= p.getActivePart();
				view= (MemoryView) p.showView(IInternalDebugUIConstants.ID_MEMORY_VIEW);
				p.activate(activePart);
			} catch (PartInitException e) {
				return;
			}		
			
		}
		
		if (view instanceof IMultipaneMemoryView)
		{
			IMemoryViewTab topTap = ((IMultipaneMemoryView)view).getTopMemoryTab(IInternalDebugUIConstants.ID_MEMORY_VIEW_PANE);
			
			if (topTap.getMemoryBlock() != memoryBlock)
			{
				IMemoryViewTab[] allTabs = ((IMultipaneMemoryView)view).getAllViewTabs(IInternalDebugUIConstants.ID_MEMORY_VIEW_PANE);
				IMemoryViewTab moveToTop = null;
			
				for (int i=0; i<allTabs.length; i++)
				{
					if (allTabs[i].getMemoryBlock() == memoryBlock)
					{
						moveToTop = allTabs[i];
						break;
					}
				}
				
				if (moveToTop != null)
				{
					((IMultipaneMemoryView)view).moveToTop(IInternalDebugUIConstants.ID_MEMORY_VIEW_PANE, moveToTop);
				}
			}
		}
	}
	
	protected void dispose() {
		DebugPlugin.getDefault().removeDebugEventListener(this);
	}
	
	private void addDefaultRenderings(IMemoryBlock memoryBlock)
	{
		// get default renderings
		String renderingIds[] = MemoryBlockManager.getMemoryRenderingManager().getDefaultRenderings(memoryBlock);
		
		// add renderings
		for (int i=0; i<renderingIds.length; i++)
		{
			try {
				MemoryBlockManager.getMemoryRenderingManager().addMemoryBlockRendering(memoryBlock, renderingIds[i]);
			} catch (DebugException e) {
				// catch error silently
				// log error
				DebugUIPlugin.logErrorMessage("Cannot create default rendering: " + renderingIds[i]); //$NON-NLS-1$
			}
		}
	}
}
