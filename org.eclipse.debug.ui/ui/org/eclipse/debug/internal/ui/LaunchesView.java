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

public class LaunchesView extends AbstractDebugView implements ISelectionChangedListener, IDoubleClickListener {

	protected SelectionProviderAction fTerminateAction;
	protected RemoveTerminatedAction fRemoveTerminatedAction;
	protected TerminateAllAction fTerminateAllAction;
	protected SelectionProviderAction fDisconnectAction;
	protected SelectionProviderAction fRelaunchAction;
	protected SelectionProviderAction fTerminateAndRemoveAction;
	
	protected PropertyDialogAction fPropertyDialogAction;
	
	/**
	 * Updates the state of the buttons in the view
	 */
	protected void updateButtons() {
		ISelection s= getViewer().getSelection();
		if (s instanceof IStructuredSelection) {
			IStructuredSelection selection= (IStructuredSelection) s;
			fTerminateAction.selectionChanged(selection);
			fDisconnectAction.selectionChanged(selection);
		}
		fRemoveTerminatedAction.update();
		fTerminateAllAction.update();
	}

	/**
	 * Initializes the actions of this view.
	 */
	protected void initializeActions(LaunchesViewer viewer) {

		fTerminateAction= new ControlAction(viewer, new TerminateActionDelegate());
		fTerminateAction.setEnabled(false);

		fDisconnectAction= new ControlAction(viewer, new DisconnectActionDelegate());
		fDisconnectAction.setEnabled(false);

		fRemoveTerminatedAction= new RemoveTerminatedAction(this instanceof DebugView);
		fRemoveTerminatedAction.setEnabled(false);

		fRelaunchAction= new ControlAction(viewer, new RelaunchActionDelegate());
		fRelaunchAction.setEnabled(false);

		fTerminateAndRemoveAction= new ControlAction(viewer, new TerminateAndRemoveActionDelegate());

		fTerminateAllAction= new TerminateAllAction();
		
		fPropertyDialogAction= new PropertyDialogAction(getSite().getWorkbenchWindow().getShell(), getSite().getSelectionProvider());
	}

	/**
	 * @see IWorkbenchPart
	 */
	public void createPartControl(Composite parent) {
		boolean showDebugTargets = getSite().getId().equals(IDebugUIConstants.ID_DEBUG_VIEW);
		LaunchesViewer lv = new LaunchesViewer(parent, showDebugTargets, this);

		lv.addSelectionChangedListener(this);
		lv.addDoubleClickListener(this);
		lv.setContentProvider(getContentProvider());
		lv.setLabelProvider(new DelegatingModelPresentation());
		lv.setUseHashlookup(true);
		setViewer(lv);
		// add my viewer as a selection provider, so selective re-launch works
		getSite().setSelectionProvider(lv);
		initializeActions(lv);

		// create context menu
		createContextMenu(lv.getTree());
		initializeToolBar();
		lv.expandToLevel(2);
		lv.setInput(DebugPlugin.getDefault().getLaunchManager());
		lv.getControl().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				handleKeyPressed(e);
			}
		});
		configureView(parent);
	}

	protected void configureView(Composite parent) {
		WorkbenchHelp.setHelp(
			parent,
			new ViewContextComputer(this, IDebugHelpContextIds.PROCESS_VIEW ));
	}
	
	/**
	 * Configures the toolBar
	 */
	protected void configureToolBar(IToolBarManager tbm) {
		tbm.add(fTerminateAction);
		tbm.add(fDisconnectAction);
		tbm.add(fRemoveTerminatedAction);
	}

	/**
	 * @see IWorkbenchPart
	 */
	public void setFocus() {
		StructuredViewer viewer= getViewer();
		if (viewer != null) {
			Control c = viewer.getControl();
			if (!c.isFocusControl()) {
				c.setFocus();
			}
		}
	}
	
	/**
	 * Adds items to the context menu
	 */
	protected void fillContextMenu(IMenuManager mgr) {
		fRemoveTerminatedAction.update();
		fTerminateAllAction.update();

		mgr.add(new Separator(IDebugUIConstants.EMPTY_LAUNCH_GROUP));
		mgr.add(new Separator(IDebugUIConstants.LAUNCH_GROUP));
		
		mgr.add(fTerminateAction);
		mgr.add(fDisconnectAction);
		mgr.add(new Separator());
		mgr.add(fTerminateAndRemoveAction);
		mgr.add(fTerminateAllAction);
		mgr.add(fRemoveTerminatedAction);
		mgr.add(fRelaunchAction);
		mgr.add(new Separator(IDebugUIConstants.PROPERTY_GROUP));
		fPropertyDialogAction.setEnabled(fPropertyDialogAction.isApplicableForSelection());
		mgr.add(fPropertyDialogAction);
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
	 * Handles key events in viewer.  Specifically interested in
	 * the Delete key.
	 */
	protected void handleKeyPressed(KeyEvent event) {
		if (event.character == SWT.DEL && event.stateMask == 0 
			&& fTerminateAndRemoveAction.isEnabled()) {
				fTerminateAndRemoveAction.run();
		}
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
	/**
	 * Gets the disconnectAction.
	 * @return Returns a SelectionProviderAction
	 */
	public SelectionProviderAction getDisconnectAction() {
		return fDisconnectAction;
	}

	/**
	 * Sets the disconnectAction.
	 * @param disconnectAction The disconnectAction to set
	 */
	protected void setDisconnectAction(SelectionProviderAction disconnectAction) {
		fDisconnectAction = disconnectAction;
	}

	/**
	 * Gets the propertyDialogAction.
	 * @return Returns a PropertyDialogAction
	 */
	protected PropertyDialogAction getPropertyDialogAction() {
		return fPropertyDialogAction;
	}

	/**
	 * Sets the propertyDialogAction.
	 * @param propertyDialogAction The propertyDialogAction to set
	 */
	protected void setPropertyDialogAction(PropertyDialogAction propertyDialogAction) {
		fPropertyDialogAction = propertyDialogAction;
	}

	/**
	 * Gets the relaunchAction.
	 * @return Returns a SelectionProviderAction
	 */
	protected SelectionProviderAction getRelaunchAction() {
		return fRelaunchAction;
	}

	/**
	 * Sets the relaunchAction.
	 * @param relaunchAction The relaunchAction to set
	 */
	protected void setRelaunchAction(SelectionProviderAction relaunchAction) {
		fRelaunchAction = relaunchAction;
	}

	/**
	 * Gets the removeTerminatedAction.
	 * @return Returns a RemoveTerminatedAction
	 */
	protected RemoveTerminatedAction getRemoveTerminatedAction() {
		return fRemoveTerminatedAction;
	}

	/**
	 * Sets the removeTerminatedAction.
	 * @param removeTerminatedAction The removeTerminatedAction to set
	 */
	protected void setRemoveTerminatedAction(RemoveTerminatedAction removeTerminatedAction) {
		fRemoveTerminatedAction = removeTerminatedAction;
	}

	/**
	 * Gets the terminateAction.
	 * @return Returns a SelectionProviderAction
	 */
	protected SelectionProviderAction getTerminateAction() {
		return fTerminateAction;
	}

	/**
	 * Sets the terminateAction.
	 * @param terminateAction The terminateAction to set
	 */
	protected void setTerminateAction(SelectionProviderAction terminateAction) {
		fTerminateAction = terminateAction;
	}

	/**
	 * Gets the terminateAllAction.
	 * @return Returns a TerminateAllAction
	 */
	protected TerminateAllAction getTerminateAllAction() {
		return fTerminateAllAction;
	}

	/**
	 * Sets the terminateAllAction.
	 * @param terminateAllAction The terminateAllAction to set
	 */
	protected void setTerminateAllAction(TerminateAllAction terminateAllAction) {
		fTerminateAllAction = terminateAllAction;
	}

	/**
	 * Gets the terminateAndRemoveAction.
	 * @return Returns a SelectionProviderAction
	 */
	protected SelectionProviderAction getTerminateAndRemoveAction() {
		return fTerminateAndRemoveAction;
	}

	/**
	 * Sets the terminateAndRemoveAction.
	 * @param terminateAndRemoveAction The terminateAndRemoveAction to set
	 */
	protected void setTerminateAndRemoveAction(SelectionProviderAction terminateAndRemoveAction) {
		fTerminateAndRemoveAction = terminateAndRemoveAction;
	}
	
}