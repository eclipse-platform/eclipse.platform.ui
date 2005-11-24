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

package org.eclipse.debug.internal.ui.views.memory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
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
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.core.model.IMemoryBlockRetrievalExtension;
import org.eclipse.debug.core.model.ITerminate;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.memory.IMemoryRendering;
import org.eclipse.debug.ui.memory.IMemoryRenderingContainer;
import org.eclipse.debug.ui.memory.IMemoryRenderingSite;
import org.eclipse.debug.ui.memory.IMemoryRenderingType;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;


/**
 * Action for adding memory block.
 * 
 * @since 3.0
 */
public class AddMemoryBlockAction extends Action implements ISelectionListener, IDebugEventSetListener{
	
	protected ISelection fCurrentSelection = null;
	protected IMemoryBlock fLastMemoryBlock;
	private boolean fAddDefaultRenderings = true;
	protected IMemoryRenderingSite fSite;
	
	public AddMemoryBlockAction(IMemoryRenderingSite site)
	{
		initialize(site);
	}
	
	/**
	 * @param addDefaultRenderings - specify if the action should add
	 * default renderings for the new memory block when it is run
	 */
	AddMemoryBlockAction(IMemoryRenderingSite site, boolean addDefaultRenderings)
	{
		initialize(site);
		fAddDefaultRenderings = addDefaultRenderings;
	}
	
	/**
	 * 
	 */
	private void initialize(IMemoryRenderingSite site) {
		fSite = site;
		setText(DebugUIMessages.AddMemoryBlockAction_title);
		
		setToolTipText(DebugUIMessages.AddMemoryBlockAction_tooltip);
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_MONITOR_EXPRESSION));
		setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_LCL_MONITOR_EXPRESSION));
		setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_MONITOR_EXPRESSION));
		
		// get selection from Debug View
		fSite.getSite().getPage().addSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
		
		// check to see if something is selected in the debug view since a selection event won't be generated for something selected prior to creating this action
		ISelection selection = DebugUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection(IDebugUIConstants.ID_DEBUG_VIEW);
		fCurrentSelection = selection;
		
		// set up enablement based on current selection
		updateAction(selection);
		
		DebugPlugin.getDefault().addDebugEventListener(this);
	}
	
	public AddMemoryBlockAction(String text, int style, IMemoryRenderingSite site)
	{
		super(text, style);
		
		fSite = site;
		setToolTipText(DebugUIMessages.AddMemoryBlockAction_tooltip);
		setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_MONITOR_EXPRESSION));
		setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_LCL_MONITOR_EXPRESSION));
		setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_MONITOR_EXPRESSION));
		
		fSite.getSite().getPage().addSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
		
		// check to see if something is selected in the debug view since a selection event won't be generated for something selected prior to creating this action
		ISelection selection = fSite.getSite().getPage().getSelection(IDebugUIConstants.ID_DEBUG_VIEW);
		fCurrentSelection = selection;
		updateAction(selection);
		
		DebugPlugin.getDefault().addDebugEventListener(this);
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	public void run() {
		boolean exit = false;
		String prefillExp = null;
		String prefillLength = null;
		while (!exit)
		{
			exit = true;
			//	get current selection from Debug View
			 ISelection selection = fSite.getSite().getPage().getSelection(IDebugUIConstants.ID_DEBUG_VIEW);
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
			if (standardMemRetrieval == null)
				return;
			
			Shell shell= DebugUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
			// create dialog to ask for expression/address to block
			MonitorMemoryBlockDialog dialog = new MonitorMemoryBlockDialog(shell, standardMemRetrieval, prefillExp, prefillLength);
			dialog.open();
			int returnCode = dialog.getReturnCode();
			if (returnCode == Window.CANCEL)
			{
				return;
			}
			// get expression entered in dialog
			String input = dialog.getExpression();
			
			// remember expression and length
			prefillExp = input;
			prefillLength = dialog.getLength();
			
			ArrayList expressions = new ArrayList();
			
			if (input.length() == 0)
			{
				expressions.add(""); //$NON-NLS-1$
			}
			else
			{
				StringTokenizer tokenizer = new StringTokenizer(input, ","); //$NON-NLS-1$
				while (tokenizer.hasMoreTokens())
				{
					expressions.add(tokenizer.nextToken());
				}
			}	
			String[] expressionsArray = (String[])expressions.toArray(new String[expressions.size()]);
			for (int i=0; i<expressionsArray.length; i++)
			{
				String expression = expressionsArray[i].trim();
				try {
					if (standardMemRetrieval instanceof IMemoryBlockRetrievalExtension)
					{
						// if the debug session supports IMemoryBlockExtensionRetrieval
						IMemoryBlockRetrievalExtension memRetrieval = (IMemoryBlockRetrievalExtension)standardMemRetrieval;
						
						// get extended memory block with the expression entered
						IMemoryBlockExtension memBlock = memRetrieval.getExtendedMemoryBlock(expression, elem);
						
						// add block to memory block manager
						if (memBlock != null)
						{
							fLastMemoryBlock = memBlock;
							
							IMemoryBlock[] memArray = new IMemoryBlock[]{memBlock};
							MemoryView view = getMemoryView();
							if (view != null)
								view.registerMemoryBlocks(memArray);
							
							MemoryViewUtil.getMemoryBlockManager().addMemoryBlocks(memArray);
							if (fAddDefaultRenderings)
								addDefaultRenderings(memBlock);
						}
						else
						{
							// open error if it failed to retrieve a memory block
							MemoryViewUtil.openError(DebugUIMessages.AddMemoryBlockAction_title, DebugUIMessages.AddMemoryBlockAction_noMemoryBlock, null);
							exit = false;
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
						
						// must monitor at least one line
						if (length <= 0)
						{
							String message = DebugUIMessages.AddMemoryBlockAction_failed + "\n" + DebugUIMessages.AddMemoryBlockAction_input_invalid; //$NON-NLS-1$
							MemoryViewUtil.openError(DebugUIMessages.AddMemoryBlockAction_title, message, null); 
							exit = false;
							continue;
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
							IMemoryBlock[] memArray = new IMemoryBlock[]{memBlock};
							MemoryView view = getMemoryView();
							if (view != null)
								view.registerMemoryBlocks(memArray);
							
							MemoryViewUtil.getMemoryBlockManager().addMemoryBlocks(memArray);
							if (fAddDefaultRenderings)
								addDefaultRenderings(memBlock);
						}
						else
						{
							// otherwise open up an error doalog
							MemoryViewUtil.openError(DebugUIMessages.AddMemoryBlockAction_title, DebugUIMessages.AddMemoryBlockAction_noMemoryBlock, null);
							exit = false;
						}
					}
				} catch (DebugException e1) {
					MemoryViewUtil.openError(DebugUIMessages.AddMemoryBlockAction_title, DebugUIMessages.AddMemoryBlockAction_failed, e1);
					exit = false;
				}
				catch(NumberFormatException e2)
				{
					String message = DebugUIMessages.AddMemoryBlockAction_failed + "\n" + DebugUIMessages.AddMemoryBlockAction_input_invalid; //$NON-NLS-1$
					MemoryViewUtil.openError(DebugUIMessages.AddMemoryBlockAction_title, message, null); 
					exit = false;
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		
		// update enablement state based on selection from Debug View
		updateAction(selection);
		fCurrentSelection = selection;
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
			
			if (fCurrentSelection instanceof IStructuredSelection)
			{
				Object elem = ((IStructuredSelection)fCurrentSelection).getFirstElement();
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
	
	protected void dispose() {
		
		// remove listeners
		DebugPlugin.getDefault().removeDebugEventListener(this);
		fSite.getSite().getPage().removeSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
	}
	
	private void addDefaultRenderings(IMemoryBlock memoryBlock)
	{
		IMemoryRenderingType primaryType = DebugUITools.getMemoryRenderingManager().getPrimaryRenderingType(memoryBlock);
		IMemoryRenderingType renderingTypes[] = DebugUITools.getMemoryRenderingManager().getDefaultRenderingTypes(memoryBlock);
		
		
		// create primary rendering
		try {
			if (primaryType != null)
			{
				createRenderingInContainer(memoryBlock, primaryType, IInternalDebugUIConstants.ID_RENDERING_VIEW_PANE_1);
			}
			else if (renderingTypes.length > 0)
			{
				primaryType = renderingTypes[0];
				createRenderingInContainer(memoryBlock, renderingTypes[0], IInternalDebugUIConstants.ID_RENDERING_VIEW_PANE_1);
			}
		} catch (CoreException e1) {
			DebugUIPlugin.log(e1);	
		}
		
		for (int i = 0; i<renderingTypes.length; i++)
		{
			try {
				boolean create = true;
				if (primaryType != null)
				{
					if (primaryType.getId().equals(renderingTypes[i].getId()))
						create = false;
				}
				if (create)
					createRenderingInContainer(memoryBlock, renderingTypes[i], IInternalDebugUIConstants.ID_RENDERING_VIEW_PANE_2);
			} catch (CoreException e) {
				DebugUIPlugin.log(e);
			}
		}
	}

	/**
	 * @param memoryBlock
	 * @param primaryType
	 * @throws CoreException
	 */
	private void createRenderingInContainer(IMemoryBlock memoryBlock, IMemoryRenderingType primaryType, String paneId) throws CoreException {
		IMemoryRendering rendering = primaryType.createRendering();
		IMemoryRenderingContainer container = fSite.getContainer(paneId);
		rendering.init(container, memoryBlock);
		container.addMemoryRendering(rendering);
	}
	
	protected MemoryView getMemoryView()
	{
		if (fSite instanceof MemoryView)
			return (MemoryView)fSite;
		return null;
	}
	
	protected void updateAction(ISelection debugContext)
	{
		setEnabled(MemoryViewUtil.isValidSelection(debugContext));
	}
}
