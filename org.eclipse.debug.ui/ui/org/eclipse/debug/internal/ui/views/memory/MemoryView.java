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
	private AbstractMemoryViewPane fMemoryViewPane;
	private AbstractMemoryViewPane fRenderingPane;
	
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
				setVisible(true);
				fMemoryViewPane.restoreViewPane();
				fRenderingPane.restoreViewPane();
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
		
		// create memory view pane
		fMemoryViewPane = new MemoryViewPane(this);
		ViewForm memoryViewForm = new ViewForm(fSashForm, SWT.FLAT);
		Control memoryControl = fMemoryViewPane.createViewPane(memoryViewForm);
		memoryViewForm.setContent(memoryControl);
		
		fSelectionProvider = new MemoryViewSelectionProvider();
		fMemoryViewPane.addSelectionListener(fSelectionProvider);
		
		ToolBarManager memoryViewMgr = new ToolBarManager(SWT.FLAT);	
		IAction[] actions = fMemoryViewPane.getActions();
		for (int i=0; i<actions.length; i++)
		{
			memoryViewMgr.add(actions[i]);
		}
		ToolBar memoryToolbar = memoryViewMgr.createControl(memoryViewForm);
		memoryViewForm.setTopRight(memoryToolbar);

		Label memoryLabel = new Label(memoryViewForm, SWT.WRAP);
		memoryLabel.setText(DebugUIMessages.getString("MemoryView.Memory_monitors")); //$NON-NLS-1$
		memoryViewForm.setTopLeft(memoryLabel);
		
		// create rendering view pane
		fRenderingPane = new RenderingViewPane(this);
		ViewForm renderingViewForm = new ViewForm(fSashForm, SWT.FLAT);
		Control renderingControl = fRenderingPane.createViewPane(renderingViewForm);
		renderingViewForm.setContent(renderingControl);
		fRenderingPane.addSelectionListener(fSelectionProvider);
		
		ToolBarManager renderingViewMgr = new ToolBarManager(SWT.FLAT);
		IAction[] renderingActions = fRenderingPane.getActions();
		for (int i=0; i<renderingActions.length; i++)
		{
			renderingViewMgr.add(renderingActions[i]);
		}
		ToolBar renderingToolbar = renderingViewMgr.createControl(renderingViewForm);
		renderingViewForm.setTopRight(renderingToolbar);
		
		Label renderingLabel = new Label(renderingViewForm, SWT.NONE);
		renderingLabel.setText(DebugUIMessages.getString("MemoryView.Memory_renderings")); //$NON-NLS-1$
		renderingViewForm.setTopLeft(renderingLabel);
		
		// set up selection provider and listeners
		
		getSite().setSelectionProvider(fSelectionProvider);
		contributeToActionBars();
		
		fPartListener = new MemoryViewPartListener(this);
		getSite().getPage().addPartListener(fPartListener);
		
		setVisible(true);
	}
	
	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		bars.getMenuManager().add(new SetDefaultColumnSizePrefAction());
		bars.updateActionBars();
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	public void setFocus() {
	}
	
	public void dispose() {
		
		fMemoryViewPane.dispose();
		fRenderingPane.dispose();
		
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.IMultipaneMemoryView#getTopMemoryTab(java.lang.String)
	 */
	public IMemoryViewTab getTopMemoryTab(String paneId) {
		if (paneId.equals(fMemoryViewPane.getPaneId()))
		{
			return fMemoryViewPane.getTopMemoryTab();
		}
		else if (paneId.equals(fRenderingPane.getPaneId()))
		{
			return fRenderingPane.getTopMemoryTab();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.IMultipaneMemoryView#getAllViewTabs(java.lang.String)
	 */
	public IMemoryViewTab[] getAllViewTabs(String paneId) {
		if (paneId.equals(fMemoryViewPane.getPaneId()))
		{
			return fMemoryViewPane.getAllViewTabs();
		}
		else if (paneId.equals(fRenderingPane.getPaneId()))
		{
			return fRenderingPane.getAllViewTabs();
		}
		return new IMemoryViewTab[0];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.views.memory.IMultipaneMemoryView#moveToTop(java.lang.String, org.eclipse.debug.internal.ui.views.memory.IMemoryViewTab)
	 */
	public void moveToTop(String paneId, IMemoryViewTab viewTab) {
		if (paneId.equals(fMemoryViewPane.getPaneId()))
		{
			fMemoryViewPane.moveToTop(viewTab);
		}
		else if (paneId.equals(fRenderingPane.getPaneId()))
		{
			fRenderingPane.moveToTop(viewTab);
		}
	}
	
	private void setVisible(boolean visible)
	{
		fMemoryViewPane.setVisible(visible);
		fRenderingPane.setVisible(visible);
	}	
}
