package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeViewer;

/**
 * Used to copy the values of variables to the clipboard from
 * the Variables and Expression views.
 */
public class CopyVariablesToClipboardActionDelegate extends CopyToClipboardActionDelegate {

	/**
	 * @see ControlActionDelegate#getHelpContextId()
	 */
	protected String getHelpContextId() {
		return IDebugHelpContextIds.COPY_VARIABLES_TO_CLIPBOARD_ACTION;
	}
	
	/**
	 * Only append children that are visible in the tree viewer
	 */
	protected boolean shouldAppendChildren(Object e) {
		return((TreeViewer)getViewer()).getExpandedState(e);
	}
	
	/**
	 * @see ControlActionDelegate#isEnabledFor(Object)
	 */
	public boolean isEnabledFor(Object element) {
		return element instanceof IDebugElement;
	}
	
	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection s) {
		action.setEnabled(!s.isEmpty());
	}
	
	/**
	 * @see ControlActionDelegate#getText()
	 */
	protected String getText() {
		return DebugUIMessages.getString("CopyVariablesToClipboardActionDelegate.Copy_&Variables_2"); //$NON-NLS-1$
	}
}