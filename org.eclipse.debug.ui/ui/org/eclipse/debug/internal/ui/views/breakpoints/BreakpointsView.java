/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.breakpoints;

 
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IBreakpointManagerListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.DelegatingModelPresentation;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.actions.LinkBreakpointsWithDebugViewAction;
import org.eclipse.debug.internal.ui.actions.OpenBreakpointMarkerAction;
import org.eclipse.debug.internal.ui.actions.ShowSupportedBreakpointsAction;
import org.eclipse.debug.internal.ui.actions.SkipAllBreakpointsAction;
import org.eclipse.debug.internal.ui.views.DebugUIViewsMessages;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;

/**
 * This view shows the breakpoints registered with the breakpoint manager
 */
public class BreakpointsView extends AbstractDebugView implements ISelectionListener, IBreakpointManagerListener {

	private BreakpointsViewEventHandler fEventHandler;
	private ICheckStateListener fCheckListener= new ICheckStateListener() {
		public void checkStateChanged(CheckStateChangedEvent event) {
			handleCheckStateChanged(event);
		}
	};
	private boolean fIsTrackingSelection= false;
	// Persistance constants
	private static String KEY_IS_TRACKING_SELECTION= "isTrackingSelection"; //$NON-NLS-1$
	private static String KEY_VALUE="value"; //$NON-NLS-1$
	
	/**
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		if (getViewer() != null) {
			initializeCheckedState();
			updateViewerBackground();
			DebugPlugin.getDefault().getBreakpointManager().addBreakpointManagerListener(this);
		}
	}

	/**
	 * @see AbstractDebugView#createViewer(Composite)
	 */
	protected Viewer createViewer(Composite parent) {
		CheckboxTableViewer viewer = CheckboxTableViewer.newCheckList(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new BreakpointsViewContentProvider());
		viewer.setLabelProvider(new DelegatingModelPresentation());
		viewer.setSorter(new BreakpointsSorter());
		viewer.setInput(DebugPlugin.getDefault().getBreakpointManager());
		viewer.addCheckStateListener(fCheckListener);
		// Necessary so that the PropertySheetView hears about selections in this view
		getSite().setSelectionProvider(viewer);
		initIsTrackingSelection();
		setEventHandler(new BreakpointsViewEventHandler(this));
		return viewer;
	}
	
	/**
	 * Initializes whether this view tracks selection in the
	 * debug view from the persisted state.
	 */
	private void initIsTrackingSelection() {
		IMemento memento= getMemento();
		if (memento != null) {
			IMemento node= memento.getChild(KEY_IS_TRACKING_SELECTION);
			if (node != null) {
				setTrackSelection(Boolean.valueOf(node.getString(KEY_VALUE)).booleanValue());
				return;
			}
		}
		setTrackSelection(false);
	}

	/**
	 * Sets the initial checked state of the items in the viewer.
	 */
	public void initializeCheckedState() {
		IBreakpointManager manager= DebugPlugin.getDefault().getBreakpointManager();
		final CheckboxTableViewer viewer= getCheckboxViewer();
		Object[] elements= ((IStructuredContentProvider) viewer.getContentProvider()).getElements(manager);
		ArrayList breakpoints= new ArrayList(elements.length);
		for (int i = 0; i < elements.length; i++) {
			breakpoints.add(elements[i]);
		}
		ListIterator iterator= breakpoints.listIterator();
		while (iterator.hasNext()) {
			try {
				if (!((IBreakpoint) iterator.next()).isEnabled()) {
					iterator.remove();
				}
			} catch (CoreException e) {
				DebugUIPlugin.log(e);
			}
		}
		viewer.setCheckedElements(breakpoints.toArray());
	}
		
	/**
	 * Returns this view's viewer as a checkbox table viewer.
	 * 
	 * @return
	 */
	private CheckboxTableViewer getCheckboxViewer() {
		return (CheckboxTableViewer) getViewer();
	}

	/**
	 * Responds to the user checking and unchecking breakpoints by enabling
	 * and disabling them.
	 * 
	 * @param event the check state change event
	 */
	private void handleCheckStateChanged(CheckStateChangedEvent event) {
		Object source= event.getElement();
		if (!(source instanceof IBreakpoint)) {
			return;
		}
		IBreakpoint breakpoint= (IBreakpoint) source;
		boolean enable= event.getChecked();
		try {
			breakpoint.setEnabled(enable);
			getCheckboxViewer().refresh(breakpoint);
		} catch (CoreException e) {
			String titleState= enable ? DebugUIViewsMessages.getString("BreakpointsView.6") : DebugUIViewsMessages.getString("BreakpointsView.7"); //$NON-NLS-1$ //$NON-NLS-2$
			String messageState= enable ? DebugUIViewsMessages.getString("BreakpointsView.8") : DebugUIViewsMessages.getString("BreakpointsView.9");  //$NON-NLS-1$ //$NON-NLS-2$
			DebugUIPlugin.errorDialog(DebugUIPlugin.getShell(), MessageFormat.format(DebugUIViewsMessages.getString("BreakpointsView.10"), new String[] { titleState }), MessageFormat.format(DebugUIViewsMessages.getString("BreakpointsView.11"), new String[] { messageState }), e); //$NON-NLS-1$ //$NON-NLS-2$
			// If the breakpoint fails to update, reset its check state.
			getCheckboxViewer().removeCheckStateListener(fCheckListener);
			event.getCheckable().setChecked(source, !event.getChecked());
			getCheckboxViewer().addCheckStateListener(fCheckListener);
		}
	}
	/**
	 * @see AbstractDebugView#getHelpContextId()
	 */
	protected String getHelpContextId() {
		return IDebugHelpContextIds.BREAKPOINT_VIEW;
	}

	/**
	 * @see IWorkbenchPart#dispose()
	 */
	public void dispose() {
	    if (getCheckboxViewer() != null) {
	        getCheckboxViewer().removeCheckStateListener(fCheckListener);
	    }
		IAction action= getAction("ShowBreakpointsForModel"); //$NON-NLS-1$
		if (action != null) {
			((ShowSupportedBreakpointsAction)action).dispose(); 
		}
		getSite().getPage().removeSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointManagerListener(this);
		super.dispose();
		
		if (getEventHandler() != null) {
			getEventHandler().dispose();
		}
	}

	/**
	 * @see AbstractDebugView#createActions()
	 */
	protected void createActions() {
		IAction action = new OpenBreakpointMarkerAction(getViewer());
		setAction("GotoMarker", action); //$NON-NLS-1$
		setAction(DOUBLE_CLICK_ACTION, action);
		setAction("ShowBreakpointsForModel", new ShowSupportedBreakpointsAction(getStructuredViewer(),this)); //$NON-NLS-1$
		setAction("LinkWithDebugView", new LinkBreakpointsWithDebugViewAction(this)); //$NON-NLS-1$
		setAction("SkipBreakpoints", new SkipAllBreakpointsAction()); //$NON-NLS-1$
	}

	/**
	 * Adds items to the context menu.
	 * 
	 * @param menu The menu to contribute to
	 */
	protected void fillContextMenu(IMenuManager menu) {
		updateObjects();
		menu.add(new Separator(IDebugUIConstants.EMPTY_NAVIGATION_GROUP));
		menu.add(new Separator(IDebugUIConstants.NAVIGATION_GROUP));
		menu.add(getAction("GotoMarker")); //$NON-NLS-1$
		menu.add(new Separator(IDebugUIConstants.EMPTY_BREAKPOINT_GROUP));
		menu.add(new Separator(IDebugUIConstants.BREAKPOINT_GROUP));
		menu.add(new Separator(IDebugUIConstants.EMPTY_RENDER_GROUP));
		menu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	/**
	 * @see AbstractDebugView#configureToolBar(IToolBarManager)
	 */
	protected void configureToolBar(IToolBarManager tbm) {
		tbm.add(new Separator(IDebugUIConstants.BREAKPOINT_GROUP));
		tbm.add(getAction("ShowBreakpointsForModel")); //$NON-NLS-1$
		tbm.add(getAction("GotoMarker")); //$NON-NLS-1$
		tbm.add(getAction("LinkWithDebugView")); //$NON-NLS-1$
		tbm.add(getAction("SkipBreakpoints")); //$NON-NLS-1$
		tbm.add(new Separator(IDebugUIConstants.RENDER_GROUP));
	}
	
	/**
	 * Returns this view's event handler
	 * 
	 * @return a breakpoint view event handler
	 */
	protected BreakpointsViewEventHandler getEventHandler() {
		return fEventHandler;
	}

	/**
	 * Sets this view's event handler.
	 * 
	 * @param eventHandler a breakpoint view event handler
	 */
	private void setEventHandler(BreakpointsViewEventHandler eventHandler) {
		fEventHandler = eventHandler;
	}
	/**
	 * @see org.eclipse.debug.ui.AbstractDebugView#becomesVisible()
	 */
	protected void becomesVisible() {
		super.becomesVisible();
		getViewer().refresh();
		initializeCheckedState();
		updateViewerBackground();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection sel) {
		if (sel.isEmpty() || !isTrackingSelection()) {
			return;
		}
		IStructuredSelection selection= (IStructuredSelection) sel;
		Iterator iter= selection.iterator();
		Object firstElement= iter.next();
		if (firstElement == null || iter.hasNext()) {
			return;
		}
		IThread thread= null;
		if (firstElement instanceof IStackFrame) {
			thread= ((IStackFrame) firstElement).getThread();
		} else if (firstElement instanceof IThread) {
			thread= (IThread) firstElement;
		} else {
			return;
		}
		IBreakpoint[] breakpoints= thread.getBreakpoints();
		getViewer().setSelection(new StructuredSelection(breakpoints), true);
	}
	
	/**
	 * Returns whether this view is currently tracking the
	 * selection from the debug view.
	 * 
	 * @return whether this view is currently tracking the
	 *   debug view's selection
	 */
	public boolean isTrackingSelection() {
		return fIsTrackingSelection;
	}
	
	/**
	 * Sets whether this view should track the selection from
	 * the debug view.
	 * 
	 * @param trackSelection whether or not this view should
	 *   track the debug view's selection.
	 */
	public void setTrackSelection(boolean trackSelection) {
		fIsTrackingSelection= trackSelection;
		if (trackSelection) {
			getSite().getPage().addSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
		} else {
			getSite().getPage().removeSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewPart#saveState(org.eclipse.ui.IMemento)
	 */
	public void saveState(IMemento memento) {
		super.saveState(memento);
		IMemento node= memento.createChild(KEY_IS_TRACKING_SELECTION);
		node.putString(KEY_VALUE, String.valueOf(fIsTrackingSelection));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointManagerListener#breakpointManagerEnablementChanged(boolean)
	 */
	public void breakpointManagerEnablementChanged(boolean enabled) {
		DebugUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
			public void run() {
				IAction action = getAction("SkipBreakpoints"); //$NON-NLS-1$
				if (action != null) {
					((SkipAllBreakpointsAction) action).updateActionCheckedState();
				}
				updateViewerBackground();
			}
		});
	}

	/**
	 * Updates the background color of the viewer based
	 * on the breakpoint manager enablement.
	 */
	protected void updateViewerBackground() {
		Color color= null;
		boolean enabled = true;
		if (!DebugPlugin.getDefault().getBreakpointManager().isEnabled()) {
			color= DebugUIPlugin.getStandardDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
			enabled = false;
		}
		Table table = getCheckboxViewer().getTable();
		TableItem[] items = table.getItems();
		for (int i = 0; i < items.length; i++) {
			items[i].setBackground(color);
			items[i].setGrayed(!enabled);
		}
		table.setBackground(color);
		if (enabled) {
			setContentDescription(""); //$NON-NLS-1$
		} else {
			setContentDescription(DebugUIViewsMessages.getString("BreakpointsView.19")); //$NON-NLS-1$
		}
	}
	
}
