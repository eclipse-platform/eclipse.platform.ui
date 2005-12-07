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

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.memory.IMemoryRendering;
import org.eclipse.debug.ui.memory.IMemoryRenderingSite;
import org.eclipse.debug.ui.memory.IMemoryRenderingType;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;

/**
 * Dialog allowing user to add a memory rendering
 */
public class AddMemoryRenderingDialog extends SelectionDialog {
	
	IMemoryBlock[] fMemoryBlocks;
	Combo memoryBlock;
	ListViewer fViewer;
	IMemoryBlock fSelectedMemoryBlock;
	Button addNew;
	
	ISelectionChangedListener fSelectionChangedListener;
	MouseListener fMouseListener;
	SelectionListener fSelectionListener;
	private IMemoryRenderingSite fSite;

	class MemoryRenderingLabelProvider implements ILabelProvider
	{
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
		 */
		public Image getImage(Object element) {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
		 */
		public String getText(Object element) {
			if (element instanceof IMemoryRenderingType)
			{	
				String label = ((IMemoryRenderingType)element).getLabel();
				return label;
			}
            return element.toString();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
		 */
		public void addListener(ILabelProviderListener listener) {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
		 */
		public void dispose() {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
		 */
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
		 */
		public void removeListener(ILabelProviderListener listener) {
		}

	}
	
	class MemoryRenderingContentProvider implements IStructuredContentProvider
	{

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object inputElement) {
			IMemoryRenderingType[] renderings = DebugUITools.getMemoryRenderingManager().getRenderingTypes((IMemoryBlock)inputElement);
			return renderings;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#close()
	 */
	public boolean close() {
		
		fViewer.removeSelectionChangedListener(fSelectionChangedListener);
		memoryBlock.removeSelectionListener(fSelectionListener);
		addNew.removeMouseListener(fMouseListener);
		
		return super.close();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		getButton(IDialogConstants.OK_ID).setEnabled(false);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.SelectionDialog#getResult()
	 */
	public Object[] getResult() {
		
		Object[] results = super.getResult();
		
		if (results != null)
		{	
			Object[] renderings = ((IStructuredSelection)results[0]).toArray();
			return renderings;
		}
        return new Object[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
	 */
	protected void cancelPressed() {
		
		setResult(null);
		
		super.cancelPressed();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		
		ISelection select = fViewer.getSelection();
		setSelectionResult(new Object[]{select});
		
		super.okPressed();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout compositeLayout = new GridLayout();
		compositeLayout.numColumns = 3;
		compositeLayout.makeColumnsEqualWidth = true;
		composite.setLayout(compositeLayout);
		
		GridData comositeSpec= new GridData();
		comositeSpec.grabExcessVerticalSpace= true;
		comositeSpec.grabExcessHorizontalSpace= true;
		comositeSpec.horizontalAlignment= GridData.FILL;
		comositeSpec.verticalAlignment= GridData.CENTER;
		composite.setLayoutData(comositeSpec);
		
		Label textLabel = new Label(composite, SWT.NONE);
		textLabel.setText(DebugUIMessages.AddMemoryRenderingDialog_Memory_Monitor); 
		GridData textLayout = new GridData();
		textLayout.verticalAlignment=GridData.CENTER;
		textLayout.horizontalAlignment=GridData.BEGINNING;
		textLabel.setLayoutData(textLayout);
		
		memoryBlock = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		GridData spec= new GridData(GridData.FILL_HORIZONTAL);
		spec.grabExcessVerticalSpace= false;
		spec.grabExcessHorizontalSpace= false;
		spec.horizontalAlignment= GridData.FILL;
		spec.verticalAlignment= GridData.FILL;
		spec.horizontalSpan = 4;
		memoryBlock.setLayoutData(spec);
		
		Label filler = new Label(composite, SWT.NONE);
		filler.setText(" "); //$NON-NLS-1$
		GridData fillerData = new GridData(GridData.FILL_HORIZONTAL);
		fillerData.horizontalSpan = 2;
		filler.setLayoutData(fillerData);
		
		addNew = new Button(composite, SWT.NONE);
		addNew.setText(DebugUIMessages.AddMemoryRenderingDialog_Add_New); 
		GridData specButton= new GridData();
		specButton.horizontalAlignment= GridData.END;
		specButton.verticalAlignment= GridData.CENTER;
		addNew.setLayoutData(specButton);
		
		fMouseListener =new MouseAdapter() {
			public void mouseUp(MouseEvent e) {
				IMemoryBlock[] oldBlocks = DebugPlugin.getDefault().getMemoryBlockManager().getMemoryBlocks();
				
				RetargetAddMemoryBlockAction action = new RetargetAddMemoryBlockAction(fSite, false);
				action.run();
				
				IMemoryBlock[] newBlocks = DebugPlugin.getDefault().getMemoryBlockManager().getMemoryBlocks();
				
				IMemoryBlock mb = action.getLastMemoryBlock();
				if (mb == null)
				{
					mb = findNewMemoryBlock(oldBlocks, newBlocks);
				}
				
				populateDialog(memoryBlock, fViewer, mb);
				action.dispose();
			}
			
			private IMemoryBlock findNewMemoryBlock(IMemoryBlock[] oldBlocks, IMemoryBlock[] newBlocks)
			{
				if (oldBlocks.length == newBlocks.length)
					return newBlocks[newBlocks.length - 1];
				
				if (newBlocks.length > oldBlocks.length)
				{
					// search from the back as new memory blocks should be at the
					// end of the list
					for (int i=newBlocks.length - 1; i>=0; i++)
					{
						boolean found = false;
						for (int j=0; j<oldBlocks.length; j++)
						{
							if (newBlocks[i] == oldBlocks[j])
								found = true;
						}
						if (!found)
							return newBlocks[i];
					}
				}	
				
				return newBlocks[newBlocks.length - 1];
			}
		}; 
		
		addNew.addMouseListener(fMouseListener);
		
		fSelectionListener = new SelectionListener(){

			public void widgetSelected(SelectionEvent e) {
				
				int idx = memoryBlock.getSelectionIndex();
				
				// avoid null pointer exception
				if (fMemoryBlocks == null)
					return;
				
				fSelectedMemoryBlock = fMemoryBlocks[idx];
				
				fViewer.setInput(fSelectedMemoryBlock);			
				
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}};
		
		memoryBlock.addSelectionListener(fSelectionListener);
		
		Label renderingLabel = new Label(composite, SWT.NONE);
		renderingLabel.setText(DebugUIMessages.AddMemoryRenderingDialog_Memory_renderings); 
		GridData renderingLayout = new GridData();
		renderingLayout.horizontalAlignment = GridData.BEGINNING;
		renderingLayout.verticalAlignment = GridData.CENTER;
		renderingLayout.horizontalSpan = 3;
		renderingLabel.setLayoutData(renderingLayout);
		
		fViewer = new ListViewer(composite);
		fViewer.setContentProvider(new MemoryRenderingContentProvider());
		fViewer.setLabelProvider(new MemoryRenderingLabelProvider());
		
		GridData listLayout = new GridData(GridData.FILL_BOTH);
		listLayout.horizontalSpan = 3;
		listLayout.heightHint =140;
		fViewer.getControl().setLayoutData(listLayout);
		
		fViewer.addDoubleClickListener(new IDoubleClickListener (){

			public void doubleClick(DoubleClickEvent event) {
				okPressed();
			}});
		
		populateDialog(memoryBlock, fViewer, null);
		
		fSelectionChangedListener = new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection  = fViewer.getSelection();
				
				if (selection.isEmpty())
				{	
					getButton(IDialogConstants.OK_ID).setEnabled(false);
				}
				else
				{	
					getButton(IDialogConstants.OK_ID).setEnabled(true);
				}
			}};
		
		fViewer.addSelectionChangedListener(fSelectionChangedListener);
		return composite;
	}

	public AddMemoryRenderingDialog(Shell parent, IMemoryRenderingSite site) {
		super(parent);
		super.setTitle(DebugUIMessages.AddMemoryRenderingDialog_Add_memory_rendering); 
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, DebugUIPlugin.getUniqueIdentifier() + ".AddMemoryRenderingDialog_context"); //$NON-NLS-1$
		setShellStyle(getShellStyle() | SWT.RESIZE);
		fSite = site;
	}
	
	
	private void populateDialog(Combo combo, ListViewer viewer, IMemoryBlock lastAdded)
	{	
		// clean up
		combo.removeAll();
		
		IMemoryBlock currentBlock;
		
		if (lastAdded != null)
			currentBlock = lastAdded;
		else
		{
			// take Memory View's selection if possible
			ISelectionProvider selectionProvider = fSite.getSite().getSelectionProvider();
			ISelection selection = null;
			
			if (selectionProvider != null)
				selection = selectionProvider.getSelection();
			else // otherwise, take selection from selection service
				selection = DebugUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection(IDebugUIConstants.ID_MEMORY_VIEW); 
			
			IDebugElement element = getMemoryBlock(selection);
			
			if (!(element instanceof IMemoryBlock) ||(element == null))
			{
				
				ISelection debugSelection = DebugUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection(IDebugUIConstants.ID_DEBUG_VIEW);
				if (!(debugSelection instanceof IStructuredSelection))
					return;
	
				//only single selection is allowed for this action
				if (debugSelection == null || debugSelection.isEmpty() || ((IStructuredSelection)debugSelection).size() > 1)
				{
					return;
				}
	
				Object elem = ((IStructuredSelection)debugSelection).getFirstElement();
	
				// if not debug element
				if (!(elem instanceof IDebugElement))
					return;			
				
				IMemoryBlock[] blocks = MemoryViewUtil.getMemoryBlockManager().getMemoryBlocks(((IDebugElement)elem).getDebugTarget());
				
				if (blocks.length <= 0)
				{
					combo.add(DebugUIMessages.AddMemoryRenderingDialog_Add_New); 
					combo.select(0);
					return;
				}
				
				currentBlock = blocks[0];
			}
			else
			{	
				currentBlock = (IMemoryBlock)element;
			}
		}
		
		fMemoryBlocks = MemoryViewUtil.getMemoryBlockManager().getMemoryBlocks(currentBlock.getDebugTarget());
		int selectionIdx = -1;
		for (int i=0; i<fMemoryBlocks.length; i++)
		{	
			String text = ""; //$NON-NLS-1$
			if (fMemoryBlocks[i] instanceof IMemoryBlockExtension)
			{
				try {
					text = ((IMemoryBlockExtension)fMemoryBlocks[i]).getExpression();
					
					if (text == null)
						text = DebugUIMessages.AddMemoryRenderingDialog_Unknown; 
					
					if (((IMemoryBlockExtension)fMemoryBlocks[i]).getBigBaseAddress() != null)
					{
						text += " : 0x"; //$NON-NLS-1$
						text += ((IMemoryBlockExtension)fMemoryBlocks[i]).getBigBaseAddress().toString(16);
					}	
				} catch (DebugException e) {
					long address = fMemoryBlocks[i].getStartAddress();
					text = Long.toHexString(address);
				}
			}
			else
			{
				long address = fMemoryBlocks[i].getStartAddress();
				text = Long.toHexString(address);
			}

			if (fMemoryBlocks[i] == currentBlock)
			{
				selectionIdx = i;
			}
			
			// ask decorator to decorate to ensure consistent label
			ILabelDecorator decorator = (ILabelDecorator)fMemoryBlocks[i].getAdapter(ILabelDecorator.class);
			if (decorator != null)
				text = decorator.decorateText(text, fMemoryBlocks[i]);
			
			combo.add(text);
		}

		combo.select(selectionIdx);
		fSelectedMemoryBlock = currentBlock;
		
		viewer.setInput(currentBlock);
	}
	
	private IMemoryBlock getMemoryBlock(ISelection selection)
	{
		if (!(selection instanceof IStructuredSelection))
			return null;

		//only single selection of PICLDebugElements is allowed for this action
		if (selection == null || selection.isEmpty() || ((IStructuredSelection)selection).size() > 1)
		{
			return null;
		}

		Object elem = ((IStructuredSelection)selection).getFirstElement();
		
		if (elem instanceof IMemoryBlock)
			return (IMemoryBlock)elem;
		else if (elem instanceof IMemoryRendering)
			return ((IMemoryRendering)elem).getMemoryBlock();
		else
			return null;
	}
	
	public IMemoryBlock getMemoryBlock()
	{
		return fSelectedMemoryBlock;
	}
}
