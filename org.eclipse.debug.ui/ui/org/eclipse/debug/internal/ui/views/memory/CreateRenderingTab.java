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
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabItem;

public class CreateRenderingTab implements IMemoryViewTab, IDebugEventSetListener{

	private IMemoryBlock fMemoryBlock;
	private TabItem fTabItem;
	private ListViewer fViewer;
	private Label fMemoryBlockLabel;
	private CreateRendering fRendering = new CreateRendering();
	private DisposeListener fDisposeListener;

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
				String label = ((IMemoryRenderingType)element).getName();
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
			IMemoryRenderingType[] renderings = MemoryRenderingManager.getMemoryRenderingManager().getRenderingTypes(inputElement, IInternalDebugUIConstants.ID_RENDERING_VIEW_PANE);
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
	
	private class CreateRendering implements IMemoryRendering 
	{
		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.core.memory.IMemoryRendering#getBlock()
		 */
		public IMemoryBlock getBlock() {
			return fMemoryBlock;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.core.memory.IMemoryRendering#getRenderingId()
		 */
		public String getRenderingId() {
			return DebugUIPlugin.getUniqueIdentifier() + ".createrendering"; //$NON-NLS-1$
		}
	}
	
	public CreateRenderingTab(IMemoryBlock memBlock, TabItem tabItem)
	{
		fMemoryBlock = memBlock;
		fTabItem = tabItem;
		Control control = createPartControl(tabItem.getParent());
		fTabItem.setControl(control);
		fTabItem.setData(this);
		fTabItem.setText(getLabel());
		
		DebugPlugin.getDefault().addDebugEventListener(this);
		
		fTabItem.addDisposeListener(fDisposeListener = new DisposeListener() {

			public void widgetDisposed(DisposeEvent e) {
				fTabItem.removeDisposeListener(fDisposeListener);
				dispose();
			}});
	
	}
	
	private Control createPartControl(Composite parent)
	{
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout compositeLayout = new GridLayout();
		compositeLayout.numColumns = 2;
		compositeLayout.makeColumnsEqualWidth = false;
		composite.setLayout(compositeLayout);
		
		GridData comositeSpec= new GridData();
		comositeSpec.grabExcessVerticalSpace= true;
		comositeSpec.grabExcessHorizontalSpace= true;
		comositeSpec.horizontalAlignment= GridData.FILL;
		comositeSpec.verticalAlignment= GridData.CENTER;
		composite.setLayoutData(comositeSpec);
		
		fMemoryBlockLabel = new Label(composite, SWT.BORDER);
		
		String memoryBlockLabel = " "; //$NON-NLS-1$
		memoryBlockLabel = getLabel();
		
		fMemoryBlockLabel.setText("  " + DebugUIMessages.getString("CreateRenderingTab.Memory_monitor") + memoryBlockLabel + "  "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		GridData textLayout = new GridData();
		textLayout.verticalAlignment=GridData.CENTER;
		textLayout.horizontalAlignment=GridData.BEGINNING;
		fMemoryBlockLabel.setLayoutData(textLayout);

		Label renderingLabel = new Label(composite, SWT.NONE);
		renderingLabel.setText(DebugUIMessages.getString("CreateRenderingTab.Select_renderings_to_create")); //$NON-NLS-1$
		GridData renderingLayout = new GridData();
		renderingLayout.horizontalAlignment = GridData.BEGINNING;
		renderingLayout.verticalAlignment = GridData.CENTER;
		renderingLayout.horizontalSpan = 2;
		renderingLabel.setLayoutData(renderingLayout);
		
		fViewer = new ListViewer(composite);
		fViewer.setContentProvider(new MemoryRenderingContentProvider());
		fViewer.setLabelProvider(new MemoryRenderingLabelProvider());
		fViewer.setInput(fMemoryBlock);
		
		if (fViewer.getElementAt(0) != null)
		{
			fViewer.getList().select(0);
		}
		
		GridData listLayout = new GridData(GridData.FILL_BOTH);
		listLayout.horizontalSpan = 1;
		fViewer.getControl().setLayoutData(listLayout);
		
		fViewer.addDoubleClickListener(new IDoubleClickListener (){

			public void doubleClick(DoubleClickEvent event) {
				addRenderings();
			}});
		
		Button addButton = new Button(composite, SWT.NONE);
		addButton.setText(DebugUIMessages.getString("CreateRenderingTab.Add_renderings")); //$NON-NLS-1$
		GridData buttonLayout = new GridData();
		buttonLayout.horizontalAlignment = GridData.BEGINNING;
		buttonLayout.verticalAlignment = GridData.BEGINNING;
		addButton.setLayoutData(buttonLayout);
		
		addButton.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				addRenderings();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				addRenderings();
			}});
		
		addButton.setFocus();
		
		return composite;		
	}
	

	/**
	 * @return tab label for this memory view tab
	 */
	private String getLabel() {
		
		String memoryBlockLabel = " "; //$NON-NLS-1$
		if (fMemoryBlock instanceof IMemoryBlockExtension)
		{
			try {
				
				if (((IMemoryBlockExtension)fMemoryBlock).getExpression() != null)
				{
					String prefix = ""; //$NON-NLS-1$
					if (((IMemoryBlockExtension)fMemoryBlock).getExpression().startsWith("&")) //$NON-NLS-1$
					{
						prefix = "&"; //$NON-NLS-1$
						memoryBlockLabel += prefix;
					}
					memoryBlockLabel += ((IMemoryBlockExtension)fMemoryBlock).getExpression();
				}
				
				if (((IMemoryBlockExtension)fMemoryBlock).getBigBaseAddress() != null)
				{
					memoryBlockLabel += " <0x" + ((IMemoryBlockExtension)fMemoryBlock).getBigBaseAddress().toString(16) + ">"; //$NON-NLS-1$ //$NON-NLS-2$
				}
			} catch (DebugException e) {
				memoryBlockLabel = fMemoryBlock.toString();
			}
		}
		else
		{
			long address = fMemoryBlock.getStartAddress();
			memoryBlockLabel = Long.toHexString(address);
		}
		return memoryBlockLabel;
	}

	private void addRenderings()
	{									
		ISelection selection = fViewer.getSelection();
		Object[] renderings = null;
		
		if (selection instanceof IStructuredSelection)
		{
			IStructuredSelection strucSelection = (IStructuredSelection)selection;
			
			renderings = strucSelection.toArray();
		}
		
		if (renderings == null)
		{
			Status stat = new Status(
					IStatus.ERROR,DebugUIPlugin.getUniqueIdentifier(),
					DebugException.INTERNAL_ERROR, DebugUIMessages.getString("CreateRenderingTab.0"), null); //$NON-NLS-1$
			DebugUIPlugin.errorDialog(DebugUIPlugin.getShell(), DebugUIMessages.getString("CreateRenderingTab.1"), DebugUIMessages.getString("CreateRenderingTab.2"), stat); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
										
		// ask for debug target and memory block retrieval
		IDebugTarget debugTarget = ((IDebugElement)fMemoryBlock).getDebugTarget();
		IMemoryBlockRetrieval standardMemRetrieval = (IMemoryBlockRetrieval)((IDebugElement)fMemoryBlock).getAdapter(IMemoryBlockRetrieval.class);
		
		if (standardMemRetrieval == null)
		{	
			standardMemRetrieval = debugTarget;
		}
		
		// add memory renderings to Memory Rendering Manager
		for (int i=0; i<renderings.length; i++)
		{	
			if (renderings[i] instanceof IMemoryRenderingType)
			{
				String id = ((IMemoryRenderingType)renderings[i]).getRenderingId();
				try {
					MemoryRenderingManager.getMemoryRenderingManager().addMemoryBlockRendering(fMemoryBlock, id );
				} catch (DebugException e) {
					DebugUIPlugin.errorDialog(DebugUIPlugin.getShell(), DebugUIMessages.getString("CreateRenderingTab.3"), DebugUIMessages.getString("CreateRenderingTab.4"), e); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}					
		}		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.IMemoryViewTab#getMemoryBlock()
	 */
	public IMemoryBlock getMemoryBlock() {
		return fMemoryBlock;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.IMemoryViewTab#dispose()
	 */
	public void dispose() {
		DebugPlugin.getDefault().removeDebugEventListener(this);
		fTabItem.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.IMemoryViewTab#goToAddress(java.math.BigInteger)
	 */
	public void goToAddress(BigInteger address) throws DebugException {
				
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.IMemoryViewTab#resetAtBaseAddress()
	 */
	public void resetAtBaseAddress() throws DebugException {
				
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.IMemoryViewTab#refresh()
	 */
	public void refresh() {
		updateMemoryBlockLabels();
		fViewer.refresh();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.IMemoryViewTab#isDisplayingError()
	 */
	public boolean isDisplayingError() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.IMemoryViewTab#isEnabled()
	 */
	public boolean isEnabled() {
		// this tab is always enabled
		// the tab is removed as soon as there is at least one rendering
		// present in the rendering view
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.IMemoryViewTab#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled) {
		if (enabled)
		{
			refresh();
		}
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.IMemoryViewTab#setFont(org.eclipse.swt.graphics.Font)
	 */
	public void setFont(Font font) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.IMemoryViewTab#setTabLabel(java.lang.String)
	 */
	public void setTabLabel(String label) {
				
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.IMemoryViewTab#getTabLabel()
	 */
	public String getTabLabel() {
		return DebugUIMessages.getString("CreateRenderingTab.Create_memory_rendering"); //$NON-NLS-1$
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.IMemoryViewTab#getRenderingId()
	 */
	public String getRenderingId() {
		return fRendering.getRenderingId();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.IMemoryViewTab#getRendering()
	 */
	public IMemoryRendering getRendering() {
		return fRendering;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.IMemoryViewTab#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	public void fillContextMenu(IMenuManager menu) {
				
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.IMemoryViewTab#getSelectedAddress()
	 */
	public BigInteger getSelectedAddress() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.IMemoryViewTab#getSelectedContent()
	 */
	public String getSelectedContent() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse.debug.core.DebugEvent[])
	 */
	public void handleDebugEvents(DebugEvent[] events) {
		
		for (int i=0; i<events.length; i++)
		{
			DebugEvent event = events[i];
			// do nothing if the debug event did not come from a debug element comes from non-debug element
			if (!(event.getSource() instanceof IDebugElement))
				return;
			
			IDebugElement src = (IDebugElement)event.getSource();
			
			// if a debug event happens from the memory block
			// invoke contentChanged to get content of the memory block updated
			if (event.getKind() == DebugEvent.CHANGE && event.getSource() == fMemoryBlock)
			{
				updateMemoryBlockLabels();
			}
			
			// if the suspend evnet happens from the debug target that the blocked
			// memory block belongs to
			if (event.getKind() == DebugEvent.SUSPEND && src.getDebugTarget() == fMemoryBlock.getDebugTarget())
			{	
				updateMemoryBlockLabels();
			}
		
		}
	}

	/**
	 * Update tab name.
	 * Update the label inside the tab.
	 */
	private void updateMemoryBlockLabels() {
		
		// This function must run on the UI thread since the debug event could
		// come from a non-UI thread.  Otherwise, this would cause a swt exception
		// for accessing the swt widgets on non-UI thread.
		Display.getDefault().asyncExec(new Runnable(){
			public void run()
			{
				if (!fMemoryBlock.getDebugTarget().isDisconnected() && 
					!fMemoryBlock.getDebugTarget().isTerminated())
				{
					fTabItem.setText(getLabel());
					fMemoryBlockLabel.setText("  " + DebugUIMessages.getString("CreateRenderingTab.Memory_monitor") + getLabel() + "  "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					
					// resize the memory block label accordingly
					fMemoryBlockLabel.getParent().layout();
				}
			}
		});
	}
}
