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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.IMemoryBlockListener;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.core.model.IMemoryBlockRetrievalExtension;
import org.eclipse.debug.core.model.ITerminate;
import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.memory.IMemoryRendering;
import org.eclipse.debug.ui.memory.IMemoryRenderingContainer;
import org.eclipse.debug.ui.memory.IMemoryRenderingSite;
import org.eclipse.debug.ui.memory.IMemoryRenderingType;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.progress.UIJob;

public class AddMemoryBlocksComposite implements ISelectionListener, IDebugEventSetListener{
	
	private IViewPart fViewPart;
	private PageBook fComposite;
	private AddMemoryBlockPage fExtendedPage;
	private AddMemoryBlockPage fSimplePage;
	
	private class AddMemoryBlockPage implements IMemoryBlockListener
	{
		private Composite fPageComposite;
		private Combo fExpression;
		private Text fLengthTxt;
		private Button fAddButton;
		private boolean fShowLength;

		private AddMemoryBlockPage(boolean createLengthFields)
		{
			fShowLength= createLengthFields;
			createPage(fComposite, createLengthFields);
		}
		
		private Control createPage(Composite parent, boolean createLengthFields) {
			Composite composite = new Composite(parent, SWT.FILL);
			fPageComposite = composite;
			GridLayout addLayout = new GridLayout();
			
			if (createLengthFields)
				addLayout.numColumns = 3;
			else
				addLayout.numColumns = 2;
			
			addLayout.makeColumnsEqualWidth = false;
			composite.setLayout(addLayout);
			
			Label expr = new Label(composite, SWT.NONE);
			expr.setText(DebugUIMessages.AddMemoryBlocksComposite_0);
			GridData data = new GridData();
			data.grabExcessHorizontalSpace = false;
			data.grabExcessVerticalSpace = false;
			data.verticalAlignment = SWT.CENTER;
			data.horizontalAlignment = SWT.LEFT;
			expr.setLayoutData(data);
			
			if (createLengthFields)
			{
				Label length = new Label(composite, SWT.NONE);
				length.setText(DebugUIMessages.AddMemoryBlocksComposite_1);
				data = new GridData();
				data.grabExcessHorizontalSpace = false;
				data.grabExcessVerticalSpace = false;
				data.verticalAlignment = SWT.CENTER;
				data.horizontalAlignment = SWT.LEFT;
				length.setLayoutData(data);
			}		

			new Label(composite, SWT.NONE);
			
			fExpression = new Combo(composite, SWT.NONE);
			data = new GridData();
			data.grabExcessHorizontalSpace = true;
			data.grabExcessVerticalSpace = false;
			data.horizontalAlignment = SWT.FILL;
			data.verticalAlignment = SWT.BOTTOM;
			fExpression.setLayoutData(data);
			
			fExpression.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetDefaultSelected(SelectionEvent e)
				{
					if (validate())
						addMemoryBlocks();
				}
			});
			
			// fill history
			fillHistory();
			
			if (createLengthFields)
			{
				fLengthTxt = new Text(composite, SWT.BORDER | SWT.SINGLE);
				data = new GridData();
				data.grabExcessHorizontalSpace = true;
				data.grabExcessVerticalSpace = false;
				data.horizontalAlignment = SWT.FILL;
				data.verticalAlignment = SWT.BOTTOM;
				fLengthTxt.setLayoutData(data);
				
				fLengthTxt.addSelectionListener(new SelectionAdapter() 
				{
					public void widgetDefaultSelected(SelectionEvent e)
					{
						if (validate())
							addMemoryBlocks();
					}
				});
			}
			
			fAddButton = new Button(composite, SWT.NONE);
			fAddButton.setImage(DebugUITools.getImage(IInternalDebugUIConstants.IMG_ELCL_MONITOR_EXPRESSION));
			fAddButton.setToolTipText(DebugUIMessages.AddMemoryBlocksComposite_2);
			data = new GridData();
			data.grabExcessHorizontalSpace = false;
			data.grabExcessVerticalSpace = false;
			data.verticalAlignment = SWT.TOP;
			fAddButton.setLayoutData(data);
			
			fAddButton.addSelectionListener(new SelectionAdapter() 
			{
				public void widgetSelected(SelectionEvent e)
				{
					addMemoryBlocks();
				}
			});

			return composite;
		}

		private void fillHistory() {
			String[] history = MemoryViewUtil.getHistory();
			if (history.length > 0)
			{
				String txt = fExpression.getText();
				fExpression.removeAll();
				for(int j=0; j<history.length; j++) {
					fExpression.add(history[j]);
				}
				fExpression.setText(txt);
			}
		}
		
		private void addMemoryBlocks()
		{
			IAdaptable debugContext = DebugUITools.getDebugContext();
			if (debugContext == null)
				return;
			
			IDebugElement elm = null;
			if (debugContext instanceof IDebugElement)
				elm = (IDebugElement)debugContext;
			
			if (elm == null)
				return;
			
			IMemoryBlockRetrieval standardMemRetrieval = (IMemoryBlockRetrieval)elm.getAdapter(IMemoryBlockRetrieval.class);
			if (standardMemRetrieval == null)
				standardMemRetrieval = elm.getDebugTarget();
			
			if (standardMemRetrieval == null)
				return;
			
//			 get expression entered in combo
			String input = fExpression.getText();
			
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
			
			boolean error = false;
			String[] expressionsArray = (String[])expressions.toArray(new String[expressions.size()]);
			for (int i=0; i<expressionsArray.length; i++)
			{
				String expression = expressionsArray[i].trim();
				MemoryViewUtil.addHistory(expression);
				
				try {
					if (standardMemRetrieval instanceof IMemoryBlockRetrievalExtension)
					{
						// if the debug session supports IMemoryBlockExtensionRetrieval
						IMemoryBlockRetrievalExtension memRetrieval = (IMemoryBlockRetrievalExtension)standardMemRetrieval;
						
						// get extended memory block with the expression entered
						IMemoryBlockExtension memBlock = memRetrieval.getExtendedMemoryBlock(expression, elm);
						
						// add block to memory block manager
						if (memBlock != null)
						{	
							IMemoryBlock[] memArray = new IMemoryBlock[]{memBlock};
							MemoryView view = getMemoryView();
							if (view != null)
								view.registerMemoryBlocks(memArray);
							
							MemoryViewUtil.getMemoryBlockManager().addMemoryBlocks(memArray);
								addDefaultRenderings(memBlock);
						}
						else
						{
							error = true;
							// open error if it failed to retrieve a memory block
							MemoryViewUtil.openError(DebugUIMessages.AddMemoryBlockAction_title, DebugUIMessages.AddMemoryBlockAction_noMemoryBlock, null);
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
						String strLength = fLengthTxt.getText();
						
						long length = Long.parseLong(strLength);
						
						// must monitor at least one line
						if (length <= 0)
						{
							error = true;
							String message = DebugUIMessages.AddMemoryBlockAction_failed + "\n" + DebugUIMessages.AddMemoryBlockAction_input_invalid; //$NON-NLS-1$
							MemoryViewUtil.openError(DebugUIMessages.AddMemoryBlockAction_title, message, null); 
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
							IMemoryBlock[] memArray = new IMemoryBlock[]{memBlock};
							MemoryView view = getMemoryView();
							if (view != null)
								view.registerMemoryBlocks(memArray);
							
							MemoryViewUtil.getMemoryBlockManager().addMemoryBlocks(memArray);
							addDefaultRenderings(memBlock);
						}
						else
						{
							error = true;
							// otherwise open up an error doalog
							MemoryViewUtil.openError(DebugUIMessages.AddMemoryBlockAction_title, DebugUIMessages.AddMemoryBlockAction_noMemoryBlock, null);
						}
					}
				} catch (DebugException e1) {
					error = true;
					MemoryViewUtil.openError(DebugUIMessages.AddMemoryBlockAction_title, DebugUIMessages.AddMemoryBlockAction_failed, e1);
				}
				catch(NumberFormatException e2)
				{
					error = true;
					String message = DebugUIMessages.AddMemoryBlockAction_failed + "\n" + DebugUIMessages.AddMemoryBlockAction_input_invalid; //$NON-NLS-1$
					MemoryViewUtil.openError(DebugUIMessages.AddMemoryBlockAction_title, message, null); 
				}
			}
			
			if (!error)
			{
				fExpression.setText(""); //$NON-NLS-1$
				if (fShowLength)
					fLengthTxt.setText(""); //$NON-NLS-1$
			}
		}
		
		private void becomesVisible()
		{
		}
		
		private void becomesHidden()
		{
			
		}
		
		private Control getControl()
		{
			return fPageComposite;
		}
		
		private boolean validate()
		{	
			if (fAddButton.isEnabled())
			{
				if (fShowLength)
				{
					if (fExpression.getText() != null &&
						fLengthTxt.getText() != null)
					{
						if (fLengthTxt.getText().length() > 0)
							return true;	
					}
					return false;
				}
	
				if (fExpression.getText() != null)
					return true;
				return false;
			}
			return false;
		}
		
		private void update(ISelection selection)
		{
			if (MemoryViewUtil.isValidSelection(selection))
				fAddButton.setEnabled(true);
			else
				fAddButton.setEnabled(false);
		}

		public void memoryBlocksAdded(IMemoryBlock[] memory) {
			// history has changed if a new memory block is added
			fillHistory();
		}

		public void memoryBlocksRemoved(IMemoryBlock[] memory) {
		}
	}
	
	/**
	 * @param viewPart
	 */
	public AddMemoryBlocksComposite(IViewPart viewPart)
	{
		fViewPart = viewPart;
	}
	
	public Control createComposite(Composite parent)
	{
		fComposite = new PageBook(parent, SWT.FILL);
		GridData pagebookData = new GridData();
		pagebookData.grabExcessHorizontalSpace = true;
		pagebookData.grabExcessVerticalSpace = false;
		pagebookData.horizontalAlignment = SWT.FILL;
		pagebookData.verticalAlignment = SWT.TOP;
		fComposite.setLayoutData(pagebookData);
		
		fExtendedPage = new AddMemoryBlockPage(false);
		fSimplePage = new AddMemoryBlockPage(true);
		showPageWithLength(false);
		
		IAdaptable debugContext = DebugUITools.getDebugContext();
		if (debugContext != null)
		{
			ISelection sel = new StructuredSelection(debugContext);
			update(sel);
		}
		else
			update(StructuredSelection.EMPTY);
		
		fViewPart.getViewSite().getPage().addSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
		DebugPlugin.getDefault().addDebugEventListener(this);
		DebugPlugin.getDefault().getMemoryBlockManager().addListener(fExtendedPage);
		DebugPlugin.getDefault().getMemoryBlockManager().addListener(fSimplePage);
		
		return fComposite;
	}
	
	
	
	public void showPageWithLength(boolean showLengthFields)
	{
		if (showLengthFields)
		{
			fComposite.showPage(fSimplePage.getControl());
			fExtendedPage.becomesHidden();
			fSimplePage.becomesVisible();
		}
		else
		{
			fComposite.showPage(fExtendedPage.getControl());
			fSimplePage.becomesHidden();
			fExtendedPage.becomesVisible();
		}
		
	}
	
	public void dispose()
	{
		fViewPart.getViewSite().getPage().removeSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
		DebugPlugin.getDefault().removeDebugEventListener(this);
		DebugPlugin.getDefault().getMemoryBlockManager().removeListener(fExtendedPage);
		DebugPlugin.getDefault().getMemoryBlockManager().removeListener(fSimplePage);
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		update(selection);
	}
	
	public void update(ISelection selection) {
		if (selection instanceof IStructuredSelection)
		{
			Object obj = ((IStructuredSelection)selection).getFirstElement();
			if (obj == null)
			{
				fExtendedPage.fAddButton.setEnabled(false);
				fSimplePage.fAddButton.setEnabled(false);
				return;
			}
			
			fExtendedPage.update(selection);
			fSimplePage.update(selection);
			
			if (obj instanceof IDebugElement)
			{
				IDebugElement elm = (IDebugElement)obj;
				IMemoryBlockRetrieval retrieval = (IMemoryBlockRetrieval)elm.getAdapter(IMemoryBlockRetrieval.class);
				if (retrieval == null)
					retrieval = elm.getDebugTarget();
				
				// watch expression can have null debug target
				if (retrieval == null)
				{
					return;
				}
				
				if (!(retrieval instanceof IMemoryBlockRetrievalExtension))
					showPageWithLength(true);
				else
					showPageWithLength(false);
			}
		}
	}
	
	private MemoryView getMemoryView()
	{
		if (fViewPart instanceof MemoryView)
			return (MemoryView)fViewPart;
		return null;
	}
	
	private void addDefaultRenderings(IMemoryBlock memoryBlock)
	{
		IMemoryRenderingType primaryType = DebugUITools.getMemoryRenderingManager().getPrimaryRenderingType(memoryBlock);
		IMemoryRenderingType renderingTypes[] = DebugUITools.getMemoryRenderingManager().getDefaultRenderingTypes(memoryBlock);
		
		
		// create primary rendering
		try {
			if (primaryType != null)
			{
				createRenderingInContainer(memoryBlock, primaryType, IDebugUIConstants.ID_RENDERING_VIEW_PANE_1);
			}
			else if (renderingTypes.length > 0)
			{
				primaryType = renderingTypes[0];
				createRenderingInContainer(memoryBlock, renderingTypes[0], IDebugUIConstants.ID_RENDERING_VIEW_PANE_1);
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
					createRenderingInContainer(memoryBlock, renderingTypes[i], IDebugUIConstants.ID_RENDERING_VIEW_PANE_2);
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
		IMemoryRenderingContainer container = ((IMemoryRenderingSite)getMemoryView()).getContainer(paneId);
		rendering.init(container, memoryBlock);
		container.addMemoryRendering(rendering);
	}

	public void handleDebugEvents(DebugEvent[] events) {
		for (int i=0; i<events.length; i++)
		{
			if (events[i].getKind() == DebugEvent.TERMINATE &&
				events[i].getSource() instanceof ITerminate)
			{
				IAdaptable context = DebugUITools.getDebugContext();
				if (context != null && context instanceof IDebugElement)
				{
					IDebugElement elm = (IDebugElement)context;
					if (elm.getDebugTarget().isDisconnected() ||
						elm.getDebugTarget().isTerminated())
					{
						// causes action to disable
						UIJob job = new UIJob("Update") { //$NON-NLS-1$

							public IStatus runInUIThread(IProgressMonitor monitor) {
								// updating actions need to run on UI thread
								update(StructuredSelection.EMPTY);
								return Status.OK_STATUS;
							}};
						job.setSystem(true);
						job.schedule();
					}
				}
			}
		}
	}

}
