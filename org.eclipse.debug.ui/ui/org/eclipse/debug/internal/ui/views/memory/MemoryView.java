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
import java.util.Enumeration;
import java.util.Hashtable;

import org.eclipse.debug.internal.ui.DebugUIMessages;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.part.ViewPart;

/**
 * 
 * @since 3.0
 */
public class MemoryView extends ViewPart implements IMultipaneMemoryView {

	protected MemoryViewSelectionProvider fSelectionProvider;
	private MemoryViewPartListener fPartListener;
	
	private SashForm fSashForm;
	private Hashtable fViewPanes = new Hashtable();
		
	class MemoryViewSelectionProvider implements ISelectionProvider, ISelectionChangedListener
	{
		ArrayList fListeners = new ArrayList();
		
		ISelection fSelections = new StructuredSelection();

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
			return fSelections;
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
			fSelections = selection; 
			fireChanged();
		}
		
		public void fireChanged()
		{
			SelectionChangedEvent evt = new SelectionChangedEvent(this, getSelection());
			for (int i=0; i<fListeners.size(); i++)
			{
				((ISelectionChangedListener)fListeners.get(i)).selectionChanged(evt);
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
		 */
		public void selectionChanged(SelectionChangedEvent event) {
			ISelection viewPaneSelection = event.getSelection();
			
			if (viewPaneSelection instanceof IStructuredSelection)
			{
				Object selected = ((IStructuredSelection)viewPaneSelection).getFirstElement();
			
					StructuredSelection strucSelection = new StructuredSelection(new Object[]{selected});
					setSelection(strucSelection);
			}
		}
	}
	
	class MemoryViewPartListener implements IPartListener2
	{
		IMultipaneMemoryView fView = null;
		
		public MemoryViewPartListener(IMultipaneMemoryView view)
		{
			fView = view;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IPartListener2#partActivated(org.eclipse.ui.IWorkbenchPartReference)
		 */
		public void partActivated(IWorkbenchPartReference ref) {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IPartListener2#partBroughtToTop(org.eclipse.ui.IWorkbenchPartReference)
		 */
		public void partBroughtToTop(IWorkbenchPartReference ref) {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IPartListener2#partClosed(org.eclipse.ui.IWorkbenchPartReference)
		 */
		public void partClosed(IWorkbenchPartReference ref) {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IPartListener2#partDeactivated(org.eclipse.ui.IWorkbenchPartReference)
		 */
		public void partDeactivated(IWorkbenchPartReference ref) {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IPartListener2#partHidden(org.eclipse.ui.IWorkbenchPartReference)
		 */
		public void partHidden(IWorkbenchPartReference ref) {
			
			IWorkbenchPart part = ref.getPart(false);
			
			if (part == fView)
			{
				setVisible(false);
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IPartListener2#partVisible(org.eclipse.ui.IWorkbenchPartReference)
		 */
		public void partVisible(IWorkbenchPartReference ref) {
			IWorkbenchPart part = ref.getPart(false);
			
			if (part == fView)
			{
				restoreView();
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.IPartListener2#partInputChanged(org.eclipse.ui.IWorkbenchPartReference)
		 */
		public void partInputChanged(IWorkbenchPartReference ref) {
		}

		public void partOpened(IWorkbenchPartReference ref)
		{	
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {

		WorkbenchHelp.setHelp(parent, IDebugUIConstants.PLUGIN_ID + ".MemoryView_context"); //$NON-NLS-1$
		
		fSashForm = new SashForm(parent, SWT.HORIZONTAL);
		fSelectionProvider = new MemoryViewSelectionProvider();
		
		// create memory view pane
		createMemoryViewPane();
		
		createRenderingViewPane(RenderingViewPane.RENDERING_VIEW_PANE_ID);
		
		setVisible(true);
		
		fSashForm.layout();
		
		// set up selection provider and listeners
		
		getSite().setSelectionProvider(fSelectionProvider);
		contributeToActionBars();
		
		fPartListener = new MemoryViewPartListener(this);
		getSite().getPage().addPartListener(fPartListener);
		
		// restore view pane after finishing creating all the view panes
		restoreView();

	}
	
	/**
	 * 
	 */
	private void createMemoryViewPane() {
		IAction[] actions;
		
		MemoryViewPane memoryViewPane = new MemoryViewPane(this);
		fViewPanes.put(MemoryViewPane.MEMORY_VIEW_PANE_ID, memoryViewPane);
		ViewForm memoryViewForm = new ViewForm(fSashForm, SWT.FLAT);
		Control memoryControl = memoryViewPane.createViewPane(memoryViewForm, MemoryViewPane.MEMORY_VIEW_PANE_ID);
		memoryViewForm.setContent(memoryControl);
		memoryViewPane.addSelectionListener(fSelectionProvider);
		
		ToolBarManager memoryViewMgr = new ToolBarManager(SWT.FLAT);	
		actions = memoryViewPane.getActions();
		for (int i=0; i<actions.length; i++)
		{
			memoryViewMgr.add(actions[i]);
		}
		ToolBar memoryToolbar = memoryViewMgr.createControl(memoryViewForm);
		memoryViewForm.setTopRight(memoryToolbar);

		Label memoryLabel = new Label(memoryViewForm, SWT.WRAP);
		memoryLabel.setText(DebugUIMessages.getString("MemoryView.Memory_monitors")); //$NON-NLS-1$
		memoryViewForm.setTopLeft(memoryLabel);
	}


	/**
	 * Create rendering view pane with specified pane id.
	 * @param paneId
	 */
	public void createRenderingViewPane(String paneId) {
		RenderingViewPane renderingPane = new RenderingViewPane(this); //$NON-NLS-1$
		fViewPanes.put(paneId, renderingPane);
		ViewForm renderingViewForm = new ViewForm(fSashForm, SWT.FLAT);
		Control renderingControl = renderingPane.createViewPane(renderingViewForm, paneId);
		renderingViewForm.setContent(renderingControl);
		renderingPane.addSelectionListener(fSelectionProvider);
		
		ToolBarManager renderingViewMgr = new ToolBarManager(SWT.FLAT);
		IAction[] renderingActions = renderingPane.getActions();
		for (int i=0; i<renderingActions.length; i++)
		{
			renderingViewMgr.add(renderingActions[i]);
		}
		ToolBar renderingToolbar = renderingViewMgr.createControl(renderingViewForm);
		renderingViewForm.setTopRight(renderingToolbar);
		
		Label renderingLabel = new Label(renderingViewForm, SWT.NONE);
		renderingLabel.setText(DebugUIMessages.getString("MemoryView.Memory_renderings")); //$NON-NLS-1$
		renderingViewForm.setTopLeft(renderingLabel);
		

	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		bars.getMenuManager().add(new SetDefaultColumnSizePrefAction());		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	public void setFocus() {
	}
	
	public void dispose() {
		
		Enumeration enumeration = fViewPanes.elements();
		
		while (enumeration.hasMoreElements())
		{
			Object element = enumeration.nextElement();
			if (element instanceof IMemoryViewPane)
			{
				((IMemoryViewPane)element).dispose();
			}
		}
		
		super.dispose();
	}
	
	private void setVisible(boolean visible)
	{
		IMemoryViewPane[] viewPanes = getViewPanes();
		
		for (int i=0; i<viewPanes.length; i++)
		{
			if (viewPanes[i] instanceof AbstractMemoryViewPane)
			{
				((AbstractMemoryViewPane)viewPanes[i]).setVisible(visible);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.IMultipaneMemoryView#getViewPane(java.lang.String)
	 */
	public IMemoryViewPane getViewPane(String paneId) {
		return (IMemoryViewPane)fViewPanes.get(paneId);
	}	
	

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.IMultipaneMemoryView#getViewPanes()
	 */
	public IMemoryViewPane[] getViewPanes() {
		IMemoryViewPane[] viewPanes = new IMemoryViewPane[fViewPanes.size()];

		Enumeration enumeration = fViewPanes.elements();
		int i=0;
		while (enumeration.hasMoreElements())
		{
			viewPanes[i] = (IMemoryViewPane)enumeration.nextElement();
			i++;
		}
		
		return viewPanes;
	}
	
	/**
	 * Restore each view pane from the memory view based on current
	 * debug selection
	 */
	private void restoreView() {
		setVisible(true);
		IMemoryViewPane[] viewPanes = getViewPanes();
		for (int i=0; i<viewPanes.length; i++)
		{
			viewPanes[i].restoreViewPane();
		}
	}
}
