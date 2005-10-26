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

import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IMemoryBlockListener;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockExtension;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.views.memory.renderings.BasicDebugViewContentProvider;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.memory.IMemoryRendering;
import org.eclipse.debug.ui.memory.IMemoryRenderingSite;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;


/**
 * Tree viewer for memory blocks
 */
public class MemoryBlocksTreeViewPane implements ISelectionListener, IMemoryViewPane{
	
	public static final String PANE_ID = DebugUIPlugin.getUniqueIdentifier() + ".MemoryView.MemoryBlocksTreeViewPane"; //$NON-NLS-1$
	
	private IViewPart fParent;
	private TreeViewer fTreeViewer;
	private MemoryBlocksViewerContentProvider fContentProvider;
	protected IDebugTarget fDebugTarget;
	private ViewPaneSelectionProvider fSelectionProvider;
	private AddMemoryBlockAction fAddMemoryBlockAction;
	private IAction fRemoveMemoryBlockAction;
	private IAction fRemoveAllMemoryBlocksAction;
	private Hashtable fTargetMemoryBlockMap = new Hashtable();
	private String fPaneId;
	private boolean fVisible = true;
	private ArrayList fMemoryBlocks = new ArrayList();

	private AddMemoryBlocksComposite fAddMemoryBlocksComposite;
	
	class TreeViewerRemoveMemoryBlocksAction extends Action
	{
		TreeViewerRemoveMemoryBlocksAction()
		{
			super();
			setText(DebugUIMessages.RemoveMemoryBlockAction_title); 

			setToolTipText(DebugUIMessages.RemoveMemoryBlockAction_tooltip); 
			setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_REMOVE_MEMORY));	
			setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_LCL_REMOVE_MEMORY));
			setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_REMOVE_MEMORY));
			PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugUIConstants.PLUGIN_ID + ".RemoveMemoryBlockAction_context"); //$NON-NLS-1$
			setEnabled(true);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.action.IAction#run()
		 */
		public void run() {
			ISelection selected = fTreeViewer.getSelection();
			
			if (selected != null && selected instanceof IStructuredSelection)
			{
				Object[] selectedMemBlks = ((IStructuredSelection)selected).toArray();
				IMemoryBlock[] memBlocks = new IMemoryBlock[selectedMemBlks.length];
				System.arraycopy(selectedMemBlks, 0, memBlocks, 0, selectedMemBlks.length);
				DebugPlugin.getDefault().getMemoryBlockManager().removeMemoryBlocks(memBlocks);
			}
		}
	}
	
	class TreeViewerRemoveAllMemoryBlocksAction extends Action
	{
		TreeViewerRemoveAllMemoryBlocksAction()
		{
			super();
			setText(DebugUIMessages.MemoryBlocksTreeViewPane_2); 

			setToolTipText(DebugUIMessages.MemoryBlocksTreeViewPane_2); 
			setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_REMOVE_ALL));	
			setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_REMOVE_ALL));
			setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_REMOVE_ALL));
			PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IDebugUIConstants.PLUGIN_ID + ".RemoveAllMemoryBlocksAction_context"); //$NON-NLS-1$
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.action.IAction#run()
		 */
		public void run() {
			
			IWorkbenchWindow window= DebugUIPlugin.getActiveWorkbenchWindow();
			if (window == null) {
				return;
			}
			boolean proceed = MessageDialog.openQuestion(window.getShell(), DebugUIMessages.MemoryBlocksTreeViewPane_0, DebugUIMessages.MemoryBlocksTreeViewPane_1); // 
			if (proceed) {
				IMemoryBlock[] memBlocks;
				
				IContentProvider contentProvider = fTreeViewer.getContentProvider();
				
				if (contentProvider instanceof IStructuredContentProvider)
				{
					IStructuredContentProvider strucContentProv = (IStructuredContentProvider)contentProvider;
					Object[] elements = strucContentProv.getElements(fDebugTarget);
					memBlocks = new IMemoryBlock[elements.length];
					System.arraycopy(elements, 0, memBlocks, 0, elements.length);
					DebugPlugin.getDefault().getMemoryBlockManager().removeMemoryBlocks(memBlocks);	
				}
			}
		}		
	}
	
	class MemoryBlocksViewerContentProvider extends BasicDebugViewContentProvider implements IMemoryBlockListener, ITreeContentProvider
	{
		
		public MemoryBlocksViewerContentProvider()
		{
			DebugPlugin.getDefault().getMemoryBlockManager().addListener(this);
			DebugPlugin.getDefault().addDebugEventListener(this);
		}
		
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			
			if (newInput != fDebugTarget && newInput instanceof IDebugTarget)
			{
				fDebugTarget = (IDebugTarget)newInput;
				fMemoryBlocks.clear();
				getMemoryBlocks();
				updateActionsEnablement();
			}

			super.inputChanged(viewer, oldInput, newInput);
		}
		
		/**
		 * 
		 */
		private void getMemoryBlocks() {
			
			if (fDebugTarget == null)
				return;
			
			IMemoryBlock memoryBlocks[] = DebugPlugin.getDefault().getMemoryBlockManager().getMemoryBlocks(fDebugTarget);

			for (int i=0; i<memoryBlocks.length; i++)
			{
				if (!fMemoryBlocks.contains(memoryBlocks[i]))
				{
					fMemoryBlocks.add(memoryBlocks[i]);
				}
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof IDebugTarget)
			{	
				fDebugTarget = (IDebugTarget)inputElement;
				
				return fMemoryBlocks.toArray(new IMemoryBlock[fMemoryBlocks.size()]);
			}
			return new Object[]{inputElement};
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
			super.dispose();
			fAddMemoryBlocksComposite.dispose();
			DebugPlugin.getDefault().getMemoryBlockManager().removeListener(this);
			DebugPlugin.getDefault().removeDebugEventListener(this);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.core.memory.IMemoryBlockListener#MemoryBlockAdded(org.eclipse.debug.core.model.IMemoryBlock)
		 */
		public void memoryBlocksAdded(final IMemoryBlock[] memory) {

			DebugUIPlugin.getDefault().getWorkbench().getDisplay().asyncExec(new Runnable() {

				public void run() {
					// if the content provider is disposed, do not handle event
					if (fDisposed)
						return;
					
					boolean setSelection = false;
					if (fMemoryBlocks.size() == 0)
						setSelection = true;
					
					IMemoryBlock memoryBlocks[] = memory;
		
					for (int i=0; i<memoryBlocks.length; i++)
					{
						if (!fMemoryBlocks.contains(memoryBlocks[i]))
						{
							fMemoryBlocks.add(memoryBlocks[i]);
							
							if (fParent instanceof MemoryView)
							{
								MemoryView mv = (MemoryView)fParent;
								if (mv.isMemoryBlockRegistered(memoryBlocks[i]))
									setSelection = true;
							}
						}
					}
					
					fTreeViewer.refresh();
					
					if (fParent instanceof MemoryView)
					{
						MemoryView mv = (MemoryView)fParent;
						// force a selection if there is currently only one memory block in the view
						if (!mv.isPinMBDisplay() || setSelection)
						{
							// switch to the memory block if there is only one memory block in the view
							fTreeViewer.setSelection(new StructuredSelection(memory));
							fSelectionProvider.setSelection(new StructuredSelection(memory));
						}
					}
					else
					{
						fTreeViewer.setSelection(new StructuredSelection(memory));
						fSelectionProvider.setSelection(new StructuredSelection(memory));
					}
					updateActionsEnablement();
			}});
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.core.memory.IMemoryBlockListener#MemoryBlockRemoved(org.eclipse.debug.core.model.IMemoryBlock)
		 */
		public void memoryBlocksRemoved(final IMemoryBlock[] memory) {
			
			DebugUIPlugin.getDefault().getWorkbench().getDisplay().asyncExec(new Runnable() {

				public void run() {
					for (int i=0; i<memory.length; i++)
					{
						fMemoryBlocks.remove(memory[i]);
					}
					
					// if the content provider is disposed, do not update viewer
					if (!fDisposed)
					{
						fTreeViewer.refresh();
						
						IStructuredSelection selection = (IStructuredSelection)fSelectionProvider.getSelection();
						if (!selection.isEmpty())
						{
							IMemoryBlock currentSel = (IMemoryBlock)selection.getFirstElement();

							IMemoryBlock[] memoryBlocks = DebugPlugin.getDefault().getMemoryBlockManager().getMemoryBlocks(fDebugTarget);
							boolean selectionRemoved = true;
							
							for (int i=0; i<memoryBlocks.length; i++)
							{
								if (memoryBlocks[i] == currentSel)
									selectionRemoved = false;
							}
							if (selectionRemoved)
							{
								if (memoryBlocks != null && memoryBlocks.length > 0)
								{
									fTreeViewer.setSelection(new StructuredSelection(memoryBlocks[0]));
								}
							}
						}
						else
						{
							IMemoryBlock[] memoryBlocks = DebugPlugin.getDefault().getMemoryBlockManager().getMemoryBlocks(fDebugTarget);
							if (memoryBlocks != null && memoryBlocks.length > 0)
							{
								fTreeViewer.setSelection(new StructuredSelection(memoryBlocks[0]));
							}
						}
					}
					updateActionsEnablement();
				}});
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.internal.ui.views.memory.BasicDebugViewContentProvider#doHandleDebugEvent(org.eclipse.debug.core.DebugEvent)
		 */
		protected void doHandleDebugEvent(DebugEvent event) {
			
			// if the view is disposed, do not handle event
			if (fDisposed)
				return;
			
			if (event.getKind() == DebugEvent.TERMINATE)
			{
				if (event.getSource() == fDebugTarget)
					fTreeViewer.setInput(null);
				else if (event.getSource() instanceof IDebugTarget)
				{
					IDebugTarget target = (IDebugTarget)event.getSource();
					fTargetMemoryBlockMap.remove(target);
				}
			}			
			else if (event.getKind() == DebugEvent.SUSPEND)
			{
				if (event.getSource() instanceof IDebugElement)
				{
					IDebugElement elem = (IDebugElement)event.getSource();
					// only update if the view pane is visible
					if (elem.getDebugTarget() == fDebugTarget && fVisible)
						fTreeViewer.refresh();
				}
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
		 */
		public Object[] getChildren(Object parentElement) {
			return new Object[0];
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
		 */
		public Object getParent(Object element) {
			if (element instanceof IDebugTarget)
			{
				return null;
			}
			return fDebugTarget;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
		 */
		public boolean hasChildren(Object element) {
			if (element instanceof IDebugTarget)
			{
				return true;
			}
			return false;
		}
	}
	
	class MemoryBlocksViewerLabelProvider extends LabelProvider
	{	
		public Image getImage(Object element) {
			
			if (element instanceof IMemoryBlock)
				return DebugPluginImages.getImage(IDebugUIConstants.IMG_OBJS_VARIABLE);
			return super.getImage(element);
		}
		
		public String getText(Object element) {
			if (element instanceof IMemoryBlock)
			{
				return getLabel((IMemoryBlock)element);
			}
			return element.toString();
		}
		
		/**
		 * @param memoryBlockLabel
		 * @return
		 */
		private String getLabel(IMemoryBlock memoryBlock) {
			
			String memoryBlockLabel = " "; //$NON-NLS-1$
			if (memoryBlock instanceof IMemoryBlockExtension)
			{
				// simply return the expression without the address
				// do not want to keep track of changes in the address
				if (((IMemoryBlockExtension)memoryBlock).getExpression() != null)
				{
					memoryBlockLabel += ((IMemoryBlockExtension)memoryBlock).getExpression();
				}
			}
			else
			{
				long address = memoryBlock.getStartAddress();
				memoryBlockLabel = Long.toHexString(address);
			}
			return memoryBlockLabel;
		}
	}
	
	public MemoryBlocksTreeViewPane(IViewPart parent)
	{
		fParent = parent;
		fSelectionProvider = new ViewPaneSelectionProvider();
	}
	
	public Control createViewPane(Composite parent, String paneId, String label)
	{	
		Composite composite = new Composite(parent, SWT.FILL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.makeColumnsEqualWidth = false;
		composite.setLayout(layout);
		
		fAddMemoryBlocksComposite = new AddMemoryBlocksComposite(fParent);
		fAddMemoryBlocksComposite.createComposite(composite);
		
		fPaneId = paneId;
		fTreeViewer = new TreeViewer(composite);
		fContentProvider = new MemoryBlocksViewerContentProvider();
		fTreeViewer.setLabelProvider(new MemoryBlocksViewerLabelProvider());
		fTreeViewer.setContentProvider(fContentProvider);
		fParent.getViewSite().getPage().addSelectionListener(this);
		
		fTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				ISelection treeSelected = event.getSelection();
				fSelectionProvider.setSelection(treeSelected);
				
				if (treeSelected instanceof IStructuredSelection)
				{
					Object mem = ((IStructuredSelection)treeSelected).getFirstElement();
					if (mem != null)
						fTargetMemoryBlockMap.put(fDebugTarget, mem);
				}
				
			}});

		populateViewPane();
		
		// create context menu
		MenuManager mgr = createContextMenuManager();
		Menu menu = mgr.createContextMenu(fTreeViewer.getControl());
		fTreeViewer.getControl().setMenu(menu);
		
		// set selection to the first memory block if one exist
		IMemoryBlock[] memoryBlocks = DebugPlugin.getDefault().getMemoryBlockManager().getMemoryBlocks(fDebugTarget);
		if (memoryBlocks.length > 0)
		{
			fTreeViewer.setSelection(new StructuredSelection(memoryBlocks[0]));
		}
		
		GridData data = new GridData();
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		data.horizontalAlignment = SWT.FILL;
		data.verticalAlignment = SWT.FILL;
		fTreeViewer.getControl().setLayoutData(data);
		
		return composite;
	}
	
	
	/**
	 * 
	 */
	private void populateViewPane() {
		
		ISelection selected = fParent.getSite().getPage().getSelection(IDebugUIConstants.ID_DEBUG_VIEW); 
		if (selected instanceof IStructuredSelection)
		{
			Object obj = ((IStructuredSelection)selected).getFirstElement();
			
			if (obj instanceof IDebugElement)
			{
				fTreeViewer.setInput(((IDebugElement)obj).getDebugTarget());
				fAddMemoryBlocksComposite.update(selected);
			}
		}
		
		ISelection selection = null;
		if (fParent.getSite().getSelectionProvider() != null)
			selection = fParent.getSite().getSelectionProvider().getSelection();
		
		IMemoryBlock memoryBlock = null;
		
		if (selection == null)
		{
			return;
		}
		
		// get memory block from selection if selection is not null
		memoryBlock = getMemoryBlock(selection);
		
		if (memoryBlock != null)
			fTreeViewer.setSelection(new StructuredSelection(memoryBlock));
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

	protected MenuManager createContextMenuManager() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				manager.add(fAddMemoryBlockAction);
				manager.add(fRemoveMemoryBlockAction);
				manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
			}
		});

		// register a context menu manager, use its pane id as the menu id
		fParent.getSite().registerContextMenu(getId(), menuMgr, fSelectionProvider);
		return menuMgr;
	}
	
	public void dispose()
	{
		fMemoryBlocks.clear();
		fParent.getViewSite().getPage().removeSelectionListener(this); 
		fContentProvider.dispose();
		fAddMemoryBlockAction.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof IStructuredSelection)
		{
			Object obj = ((IStructuredSelection)selection).getFirstElement();
			
			if (obj instanceof IDebugElement)
			{
				IDebugTarget debugTarget = ((IDebugElement)obj).getDebugTarget();
				if (debugTarget != null && fTreeViewer != null && fDebugTarget != debugTarget)
				{
					fTreeViewer.setInput(debugTarget);
					Object selectedObj = fTargetMemoryBlockMap.get(debugTarget);
					
					// if no selected object is stored, pick the first memory block
					if (selectedObj == null)
					{
						IMemoryBlock[] memBlks = DebugPlugin.getDefault().getMemoryBlockManager().getMemoryBlocks(debugTarget);
						if (memBlks.length > 0)
							selectedObj = memBlks[0];
					}
					
					if (selectedObj != null)
						fTreeViewer.setSelection(new StructuredSelection(selectedObj));
				}
			}
			if (obj instanceof IMemoryBlock)
			{
				// if the selection event comes from this view
				if (part == fParent)
				{
					// do not change selection if the selection is already correct
					ISelection treeSel = fTreeViewer.getSelection();
					if (treeSel instanceof IStructuredSelection)
					{
						if (((IStructuredSelection)treeSel).getFirstElement() == obj)
							return;
					}
					fTreeViewer.setSelection(new StructuredSelection(obj));
				}
			}
		}
	}
	
	public String getId()
	{
		return fPaneId;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.IMemoryViewPane#getActions()
	 */
	public IAction[] getActions() {
		
		if (fAddMemoryBlockAction == null)
				fAddMemoryBlockAction = new AddMemoryBlockAction((IMemoryRenderingSite)fParent);
		
		if (fRemoveMemoryBlockAction == null)
		{
			fRemoveMemoryBlockAction = new TreeViewerRemoveMemoryBlocksAction();
		}
		
		if (fRemoveAllMemoryBlocksAction == null)
		{
			fRemoveAllMemoryBlocksAction = new TreeViewerRemoveAllMemoryBlocksAction();
		}
		
		updateActionsEnablement();
		
		return new IAction[]{fRemoveMemoryBlockAction, fRemoveAllMemoryBlocksAction};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.IMemoryViewPane#addSelectionListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	public void addSelectionListener(ISelectionChangedListener listener)
	{
		if (fSelectionProvider == null)
			fSelectionProvider = new ViewPaneSelectionProvider();
		
		fSelectionProvider.addSelectionChangedListener(listener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.IMemoryViewPane#removeSelctionListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	public void removeSelctionListener(ISelectionChangedListener listener)
	{
		if (fSelectionProvider == null)
			return;
		
		fSelectionProvider.removeSelectionChangedListener(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.IMemoryViewPane#getSelectionProvider()
	 */
	public ISelectionProvider getSelectionProvider() {
		return fSelectionProvider;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.IMemoryViewPane#restoreViewPane()
	 */
	public void restoreViewPane() {
		populateViewPane();
		updateActionsEnablement();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.IMemoryViewPane#getControl()
	 */
	public Control getControl() {
		return fTreeViewer.getControl();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.IMemoryViewPane#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		if (fVisible != visible)
		{
			fVisible = visible;
			
			if(fVisible)
				fTreeViewer.refresh();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.IMemoryViewPane#isVisible()
	 */
	public boolean isVisible() {
		return fVisible;
	}
	
	private void updateActionsEnablement()
	{
		if (fRemoveMemoryBlockAction == null)
			return;
		
		if (fMemoryBlocks == null)
			return;
		
		if (fRemoveAllMemoryBlocksAction == null)
			return;
		
		if (fMemoryBlocks.size() > 0)
		{
			fRemoveMemoryBlockAction.setEnabled(true);
			fRemoveAllMemoryBlocksAction.setEnabled(true);
		}
		else
		{
			fRemoveMemoryBlockAction.setEnabled(false);
			fRemoveAllMemoryBlocksAction.setEnabled(false);
		}
	}
}
