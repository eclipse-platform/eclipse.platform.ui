package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugPlugin;import org.eclipse.debug.core.ILaunch;import org.eclipse.debug.core.model.IProcess;import org.eclipse.debug.core.model.IStackFrame;import org.eclipse.debug.ui.IDebugUIConstants;import org.eclipse.jface.action.*;import org.eclipse.jface.viewers.*;import org.eclipse.swt.SWT;import org.eclipse.swt.events.KeyAdapter;import org.eclipse.swt.events.KeyEvent;import org.eclipse.swt.widgets.Composite;import org.eclipse.swt.widgets.Control;import org.eclipse.ui.IWorkbenchActionConstants;import org.eclipse.ui.actions.SelectionProviderAction;import org.eclipse.ui.dialogs.PropertyDialogAction;import org.eclipse.ui.help.ViewContextComputer;import org.eclipse.ui.help.WorkbenchHelp;

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
		ISelection s= fViewer.getSelection();
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
		fViewer= lv;
		fViewer.addSelectionChangedListener(this);
		fViewer.addDoubleClickListener(this);
		fViewer.setContentProvider(getContentProvider());
		fViewer.setLabelProvider(new DelegatingModelPresentation());
		fViewer.setUseHashlookup(true);
		// add my viewer as a selection provider, so selective re-launch works
		getSite().setSelectionProvider(fViewer);
		initializeActions(lv);
		// register this viewer as the debug UI selection provider
		DebugUIPlugin.getDefault().addSelectionProvider(fViewer, this);

		// create context menu
		createContextMenu(lv.getTree());
		initializeToolBar();
		lv.expandToLevel(2);
		fViewer.setInput(DebugPlugin.getDefault().getLaunchManager());
		fViewer.getControl().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				handleKeyPressed(e);
			}
		});
		configureView(parent);
	}

	protected void configureView(Composite parent) {
		setTitleToolTip("System Processes");
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
		Control c = fViewer.getControl();
		if (!c.isFocusControl()) {
			c.setFocus();
			//ensure that all downstream listeners
			//know the current selection..switching from another view
			DebugUIPlugin.getDefault().selectionChanged(new SelectionChangedEvent(fViewer, fViewer.getSelection()));
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
		if (fViewer != null) {
			DebugUIPlugin.getDefault().removeSelectionProvider(fViewer, this);
			fViewer.removeDoubleClickListener(this);
			fViewer.removeSelectionChangedListener(this);
		}
		super.dispose();
	}

	/**
	 * Auto-expand and select the given element - must be called in UI thread.
	 * This is used to implement auto-expansion-and-select on a SUSPEND event.
	 */
	public void autoExpand(Object element, boolean refreshNeeded) {
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
		fViewer.setSelection(new StructuredSelection(selectee), true);
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
		TreeViewer tViewer= (TreeViewer)fViewer;
		boolean expanded= tViewer.getExpandedState(o);
		tViewer.setExpandedState(o, !expanded);
	}
}


