/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
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
import java.util.ListIterator;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IBreakpointsListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.DelegatingModelPresentation;
import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.debug.internal.ui.actions.OpenBreakpointMarkerAction;
import org.eclipse.debug.internal.ui.actions.ShowSupportedBreakpointsAction;
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
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchActionConstants;

/**
 * This view shows the breakpoints registered with the breakpoint manager
 */
public class BreakpointsView extends AbstractDebugView {

	private BreakpointsViewEventHandler fEventHandler;
	private ICheckStateListener fCheckListener= new ICheckStateListener() {
		public void checkStateChanged(CheckStateChangedEvent event) {
			handleCheckStateChanged(event);
		}
	};
	private IBreakpointsListener fBreakpointListener= new IBreakpointsListener() {
		public void breakpointsAdded(IBreakpoint[] breakpoints) {
		}
		public void breakpointsRemoved(IBreakpoint[] breakpoints, IMarkerDelta[] deltas) {
		}
		public void breakpointsChanged(final IBreakpoint[] breakpoints, IMarkerDelta[] deltas) {
			DebugUIPlugin.getStandardDisplay().asyncExec(new Runnable() {
				public void run() {
					CheckboxTableViewer viewer= getCheckboxViewer();
					for (int i = 0; i < breakpoints.length; i++) {
						IBreakpoint breakpoint= breakpoints[i];
						try {
							boolean enabled= breakpoint.isEnabled();
							if (viewer.getChecked(breakpoint) != enabled) {
								viewer.setChecked(breakpoint, breakpoint.isEnabled());								
							}
						} catch (CoreException e) {
							DebugUIPlugin.log(e);
						}
					}
				}
			});
		}
	};
	
	/**
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		if (getViewer() != null) {
			initializeCheckedState();
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
		setEventHandler(new BreakpointsViewEventHandler(this));
		return viewer;
	}	
	
	/**
	 * Sets the initial checked state of the items in the viewer and hooks
	 * up the listener to maintain future state.
	 */
	private void initializeCheckedState() {
		IBreakpointManager manager= DebugPlugin.getDefault().getBreakpointManager();
		final CheckboxTableViewer viewer= getCheckboxViewer();
		Object[] elements= ((IStructuredContentProvider) viewer.getContentProvider()).getElements(manager);
		ArrayList breakpoints= new ArrayList(elements.length);
		for (int i = 0; i < elements.length; i++) {
			breakpoints.add(elements[i]);
		}
		ListIterator iterator= breakpoints.listIterator();
		UnsupportedOperationException iteratorException= null;
		while (iterator.hasNext()) {
			try {
				if (!((IBreakpoint) iterator.next()).isEnabled()) {
					try {
						iterator.remove();
					} catch (UnsupportedOperationException exception) {
						iteratorException= exception;
						break;
					}
				}
			} catch (CoreException e) {
				DebugUIPlugin.log(e);
			}
		}
		if (iteratorException != null) {
			DebugUIPlugin.errorDialog(getSite().getShell(), DebugUIViewsMessages.getString("BreakpointsView.12"), DebugUIViewsMessages.getString("BreakpointsView.13"), iteratorException); //$NON-NLS-1$ //$NON-NLS-2$
			DebugUIPlugin.log(iteratorException);
		}
		viewer.setCheckedElements(breakpoints.toArray());
		manager.addBreakpointListener(fBreakpointListener);
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
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(fBreakpointListener);
		getCheckboxViewer().removeCheckStateListener(fCheckListener);
		IAction action= getAction("ShowBreakpointsForModel"); //$NON-NLS-1$
		if (action != null) {
			((ShowSupportedBreakpointsAction)action).dispose(); 
		}
		
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
	}

}
