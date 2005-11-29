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

package org.eclipse.debug.ui.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IMemoryBlockRetrieval;
import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.internal.ui.actions.ActionMessages;
import org.eclipse.debug.internal.ui.contexts.DebugContextManager;
import org.eclipse.debug.internal.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.memory.IMemoryRenderingType;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * A add memory rendering action that can be contributed to a an editor or view or by object contribution. 
 * The action will perform the "add memory rendering" operation for debug context the provides 
 * an appropriate <code>IMemoryBlockRetrieval</code> and <code>IAddMemoryRenderingTarget</code>.
 * <p>
 * Clients may reference/contribute this class as an action delegate
 * in plug-in XML. This class is not intended to be subclassed.
 * </p>
 * @since 3.2
 * 
 * TODO:  new api, needs review
 */
public class AddMemoryRenderingActionDelegate extends Action implements IViewActionDelegate, IEditorActionDelegate, IObjectActionDelegate, IActionDelegate2{
	
	private IAction fAction;
	private IWorkbenchPart fPart;
	private ISelection fCurrentSelection;
	private IAddMemoryRenderingsTarget fActionDelegate;
	private IMenuCreator fMenuCreator;
	private IAdaptable fDebugContext;
	private IWorkbenchWindow fWindow;
	private DebugContextListener fDebugContextListener = new DebugContextListener();
	
	private class AddMemoryRenderingAction extends Action
	{
		private IMemoryRenderingType fRenderingType;			// type of rendering to add
		AddMemoryRenderingAction(IMemoryRenderingType renderingType)
		{
			super(renderingType.getLabel());
			fRenderingType = renderingType;
		}

		public void runWithEvent(Event event) {
			if (fActionDelegate != null)
			{
				try {
					fActionDelegate.addMemoryRenderings(fPart, fCurrentSelection, new IMemoryRenderingType[]{fRenderingType});
				} catch (CoreException e) {
					DebugUIPlugin.errorDialog(DebugUIPlugin.getShell(), ActionMessages.AddMemoryRenderingActionDelegate_0, ActionMessages.AddMemoryRenderingActionDelegate_1, e);
				}
			}
		}
	}
	
	private class AddMemoryRenderingMenuCreator implements IMenuCreator
	{

		public void dispose() {
			
		}

		public Menu getMenu(Control parent) {
			return null;
		}

		public Menu getMenu(Menu parent) {
			Menu menu = new Menu(parent);
			menu.addMenuListener(new MenuAdapter() {
				public void menuShown(MenuEvent e) {
					Menu m = (Menu)e.widget;
					MenuItem[] items = m.getItems();
					for (int i=0; i < items.length; i++) {
						items[i].dispose();
					}
					fillMenu(m);
				}
			});		
			return menu;
		}
		
		private void fillMenu(Menu parent)
		{
			if (fActionDelegate != null)
			{
				IMemoryRenderingType[] types = fActionDelegate.getMemoryRenderingTypes(fPart, fCurrentSelection);
				
				for (int i=0; i<types.length; i++)
				{
					AddMemoryRenderingAction action = new AddMemoryRenderingAction(types[i]);
					ActionContributionItem item = new ActionContributionItem(action);
					item.fill(parent, -1);
				}
			}
		}
	}	
	
	private class DebugContextListener implements IDebugContextListener
	{

		public void contextActivated(ISelection selection, IWorkbenchPart part) {
			setupActionDelegate(selection);
			updateAction(fAction, fCurrentSelection);
			
		}

		public void contextChanged(ISelection selection, IWorkbenchPart part) {
			setupActionDelegate(selection);
			updateAction(fAction, fCurrentSelection);
		}
	}
	
	private void setupActionDelegate(ISelection context)
	{
		IAdaptable debugContext = null;
		if (context instanceof IStructuredSelection)
		{
			if (((IStructuredSelection)context).getFirstElement() instanceof IAdaptable)
				debugContext = (IAdaptable)((IStructuredSelection)context).getFirstElement();
		}
		
		if (debugContext == fDebugContext)
			return;
		
		fDebugContext = debugContext;
		
		if (fDebugContext == null)
			return;
		
		IMemoryBlockRetrieval retrieval = (IMemoryBlockRetrieval)fDebugContext.getAdapter(IMemoryBlockRetrieval.class);
		if (retrieval == null && fDebugContext instanceof IDebugElement)
		{
			retrieval = ((IDebugElement)fDebugContext).getDebugTarget();
		}
		
		if (retrieval == null)
			return;
		
		IAddMemoryRenderingsTarget target = null;
		if (fCurrentSelection instanceof IStructuredSelection)
		{
			// get target from current selection
			IStructuredSelection strucSel = (IStructuredSelection)fCurrentSelection;
			Object obj = strucSel.getFirstElement();
			target = getAddMemoryRenderingTarget(obj);
		}
		if (target == null)
		{
			// get the target from Debug View
			target = getAddMemoryRenderingTarget(fDebugContext);
		}
		if (target == null)
		{
			// try to get target from memory block retrieval
			target = getAddMemoryRenderingTarget(retrieval);
		}
		
		fActionDelegate = target;
	}

	public void init(IViewPart view) {
		bindPart(view);	
	}

	public void run(IAction action) {
		// do nothing
	}

	public void selectionChanged(IAction action, ISelection selection) {
		bindAction(action);
		fCurrentSelection = selection;
		updateAction(action, selection);
	}
	
	private void updateAction(IAction action, ISelection selection)
	{
		if (fActionDelegate != null)
		{
			try {
				action.setEnabled(fActionDelegate.canAddMemoryRenderings(fPart, selection));
				bindAction(action);
			} catch (CoreException e) {
				DebugUIPlugin.log(e);
			}
		}
		else
		{
			action.setEnabled(false);
		}
	}

	private void bindAction(IAction action) {
		if (action == null)
			return;
		
		if (action != fAction) {
			if (fMenuCreator == null)
				fMenuCreator = new AddMemoryRenderingMenuCreator();
	        action.setMenuCreator(fMenuCreator);
	        fAction= action;
	    }
	}

	private IAddMemoryRenderingsTarget getAddMemoryRenderingTarget(Object elmt) {
		IAddMemoryRenderingsTarget target = null;
		
		if (elmt instanceof IAddMemoryRenderingsTarget)
		{
			target = (IAddMemoryRenderingsTarget)elmt;
		}
		else if (elmt instanceof IAdaptable)
		{
			target = (IAddMemoryRenderingsTarget)((IAdaptable)elmt).getAdapter(IAddMemoryRenderingsTarget.class);
		}
		return target;
	}

	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		bindPart(targetEditor);
		bindAction(action);
		updateAction(action, fCurrentSelection);
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		bindPart(targetPart);
		bindAction(action);
		updateAction(action, fCurrentSelection);
	}

	public void init(IAction action) {		
		bindAction(action);
		if (action != null)
		{
			action.setText(ActionMessages.AddMemoryRenderingActionDelegate_2);
			action.setImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_MONITOR_EXPRESSION));
			action.setHoverImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_LCL_MONITOR_EXPRESSION));
			action.setDisabledImageDescriptor(DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_MONITOR_EXPRESSION));
		}	
	}

	public void dispose() {
		// remove as debug context listener
		fAction = null;
		fPart = null;
		fCurrentSelection = null;
		fActionDelegate = null;
		
		// remove debug context listener
		bindPart(null);
	}

	public void runWithEvent(IAction action, Event event) {
		// do nothing
	}
	
	private void bindPart(IWorkbenchPart part)
	{
		IWorkbenchWindow window = null;
		if (part != null)
		{
			window = part.getSite().getWorkbenchWindow();
		}
		if (window != fWindow)
		{
				
			if (fWindow != null)
			{
				DebugContextManager.getDefault().removeDebugContextListener(fDebugContextListener, fWindow, IDebugUIConstants.ID_DEBUG_VIEW);
			}
			
			if (window != null)
			{
				DebugContextManager.getDefault().addDebugContextListener(fDebugContextListener, window, IDebugUIConstants.ID_DEBUG_VIEW);
			}
			fWindow = window;
		}
		
		if (part != fPart)
			fPart = part;
		
		if (fWindow != null)
			setupActionDelegate(DebugContextManager.getDefault().getActiveContext(fWindow, IDebugUIConstants.ID_DEBUG_VIEW));
	}

}

