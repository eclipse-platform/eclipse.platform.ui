package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.help.ViewContextComputer;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.texteditor.IUpdate;

public class LaunchesView extends AbstractDebugView implements ISelectionChangedListener, IDoubleClickListener {
	
	/**
	 * Updates the state of the buttons in the view
	 */
	protected void updateButtons() {
		updateSelectionActions();
		updateActions();
	}

	/**
	 * @see AbstractDebugView#createActions()
	 */
	protected void createActions() {
		LaunchesViewer viewer = (LaunchesViewer)getViewer();
		
		IAction action;
		
		action = new ControlAction(viewer, new TerminateActionDelegate());
		action.setEnabled(false);
		setAction("Terminate", action);

		action = new ControlAction(viewer, new DisconnectActionDelegate());
		action.setEnabled(false);
		setAction("Disconnect", action);

		action = new RemoveTerminatedAction();
		action.setEnabled(false);
		setAction("RemoveAll", action);

		action = new ControlAction(viewer, new RelaunchActionDelegate());
		action.setEnabled(false);
		setAction("Relaunch",action);

		setAction(REMOVE_ACTION, new ControlAction(viewer, new TerminateAndRemoveActionDelegate()));
		setAction("TerminateAll", new TerminateAllAction());
		setAction("Properties", new PropertyDialogAction(getSite().getWorkbenchWindow().getShell(), getSite().getSelectionProvider()));
	}

	/**
	 * @see AbstractDebugView#createViewer(Composite)
	 */
	protected StructuredViewer createViewer(Composite parent) {
		boolean showDebugTargets = getSite().getId().equals(IDebugUIConstants.ID_DEBUG_VIEW);
		LaunchesViewer lv = new LaunchesViewer(parent, showDebugTargets, this);
		lv.addSelectionChangedListener(this);
		lv.addDoubleClickListener(this);
		lv.setContentProvider(getContentProvider());
		lv.setLabelProvider(new DelegatingModelPresentation());
		lv.setUseHashlookup(true);
		// add my viewer as a selection provider, so selective re-launch works
		getSite().setSelectionProvider(lv);
		lv.expandToLevel(2);
		lv.setInput(DebugPlugin.getDefault().getLaunchManager());
		return lv;
	}


	/**
	 * @see AbstractDebugView#getHelpContextId()
	 */
	protected String getHelpContextId() {
		return IDebugHelpContextIds.PROCESS_VIEW;
	}
	
	/**
	 * @see AbstractDebugView#configureToolBar(IToolBarManager)
	 */
	protected void configureToolBar(IToolBarManager tbm) {
		tbm.add(getAction("Terminate"));
		tbm.add(getAction("Disconnect"));
		tbm.add(getAction("RemoveAll"));
	}
	
	/**
	 * Adds items to the context menu
	 */
	protected void fillContextMenu(IMenuManager mgr) {
		updateActions();

		mgr.add(new Separator(IDebugUIConstants.EMPTY_LAUNCH_GROUP));
		mgr.add(new Separator(IDebugUIConstants.LAUNCH_GROUP));
		
		mgr.add(getAction("Terminate"));
		mgr.add(getAction("Disconnect"));
		mgr.add(new Separator());
		mgr.add(getAction(REMOVE_ACTION));
		mgr.add(getAction("TerminateAll"));
		mgr.add(getAction("RemoveAll"));
		mgr.add(getAction("Relaunch"));
		mgr.add(new Separator(IDebugUIConstants.PROPERTY_GROUP));
		
		PropertyDialogAction action = (PropertyDialogAction)getAction("Properties");
		action.setEnabled(action.isApplicableForSelection());
		mgr.add(action);
		
		mgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	/**
	 * @see IWorkbenchPart
	 */
	public void dispose() {
		if (getViewer() != null) {
			getViewer().removeDoubleClickListener(this);
			getViewer().removeSelectionChangedListener(this);
		}
		super.dispose();
	}

	/**
	 * Auto-expand and select the given element - must be called in UI thread.
	 * This is used to implement auto-expansion-and-select on a SUSPEND event.
	 */
	public void autoExpand(Object element, boolean refreshNeeded, boolean selectNeeded) {
		autoExpand(element);
	}
	
	public void autoExpand(Object element) {
		Object selectee = element;
		if (element instanceof ILaunch) {
			IProcess[] ps= ((ILaunch)element).getProcesses();
				if (ps != null && ps.length > 0) {
					selectee= ps[0];
				}
		}
		getViewer().setSelection(new StructuredSelection(selectee), true);
	}
	
	/**
	 * Returns the content provider to use for the viewer of this view.
	 */
	protected DebugContentProvider getContentProvider() {
		return new ProcessesContentProvider();
	}
	
	/**
	 * The selection has changed in the viewer. Update
	 * the state of the buttons.
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		updateButtons();
	}
		
	/**
	 * @see IDoubleClickListener#doubleClick(DoubleClickEvent)
	 */
	public void doubleClick(DoubleClickEvent event) {
		ISelection selection= event.getSelection();
		if (!(selection instanceof IStructuredSelection)) {
			return;
		}
		IStructuredSelection ss= (IStructuredSelection)selection;
		Object o= ss.getFirstElement();
		if (o instanceof IStackFrame) {
			return;
		} 
		TreeViewer tViewer= (TreeViewer)getViewer();
		boolean expanded= tViewer.getExpandedState(o);
		tViewer.setExpandedState(o, !expanded);
	}

}