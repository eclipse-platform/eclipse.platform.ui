package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.SelectionProviderAction;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * Provides the common functionality of the control actions.
 * In order to avoid duplication of code, ControlActions defer to 
 * delegates to perform all of the real work.  This is necessary so that 
 * these actions can be used as regular SelectionProviderActions,
 * but also be used as contributed IWorkbenchWindowActionDelegates,
 * which must have zero-argument constructors.
 */
public class ControlAction extends SelectionProviderAction implements IUpdate {

	/**
	 * The delegate does all of the real work.  This class only responds to
	 * run() requests and selectionChanged notifications.  In both cases,
	 * it defers to the delegate.
	 */
	protected ControlActionDelegate fDelegate;

	public ControlAction(ISelectionProvider selectionProvider, ControlActionDelegate delegate) {
		super(selectionProvider, ""); //$NON-NLS-1$
		fDelegate= delegate;
		fDelegate.initializeForOwner(this);
		setText(fDelegate.getText());
		setToolTipText(fDelegate.getToolTipText());	
		WorkbenchHelp.setHelp(
			this,
			new Object[] { fDelegate.getHelpContextId() });
	}

	/**
	 * @see IAction#run()
	 * The actual work is deferred to the delegate.
	 */
	public void run() {
		fDelegate.run();
	}	

	/**
	 * Updates the enable state based on what and how much is selected.
	 * @see SelectionProviderAction#selectionChanged(IStructuredSelection)
	 */
	public void selectionChanged(IStructuredSelection sel) {
		fDelegate.selectionChanged(this, sel);
	}

	/**
	 * @see IUpdate#update()
	 */
	public void update() {
		if (fDelegate instanceof IUpdate) {
			((IUpdate)fDelegate).update();
		} else {
			ISelection sel = getSelectionProvider().getSelection();
			if (sel instanceof IStructuredSelection) {
				selectionChanged((IStructuredSelection)sel);
			}
		}
	}
}

