package org.eclipse.debug.internal.ui;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import org.eclipse.ui.actions.SelectionProviderAction;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * Provides the common functionality of the control actions.
 * In order to avoid duplication of code, ControlActions defer to 
 * delegates to perform all of the real work.  This is necessary so that 
 * these actions can be used as regular SelectionProviderActions,
 * but also be used as contributed IWorkbenchWindowActionDelegates,
 * which must have zero-argument constructors.
 */
public class ControlAction extends SelectionProviderAction {

	/**
	 * The delegate does all of the real work.  This class only responds to
	 * run() requests and selectionChanged notifications.  In both cases,
	 * it defers to the delegate.
	 */
	protected ControlActionDelegate fDelegate;

	public ControlAction(ISelectionProvider selectionProvider, ControlActionDelegate delegate) {
		super(selectionProvider, "");
		fDelegate= delegate;
		fDelegate.initializeForOwner(this);
		setText(DebugUIUtils.getResourceString(fDelegate.getPrefix() + TEXT));
		setToolTipText(DebugUIUtils.getResourceString(fDelegate.getPrefix() + TOOL_TIP_TEXT));		
	}

	/**
	 * @see Action
	 * The actual work is deferred to the delegate.
	 */
	public void run() {
		fDelegate.selectionChanged(this, getStructuredSelection());
		fDelegate.run();
	}	

	/**
	 * @see SelectionProviderAction
	 * Updates the enable state based on what and how much is selected.
	 */
	public void selectionChanged(IStructuredSelection sel) {
		setEnabled(fDelegate.getEnableStateForSelection(sel));		
	}

}

