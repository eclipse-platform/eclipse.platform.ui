package org.eclipse.debug.internal.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.jface.viewers.TreeViewer;

/**
 * Used to copy the values of variables to the clipboard from
 * the Variables and Expression views.
 */
public class CopyVariablesToClipboardActionDelegate extends CopyToClipboardActionDelegate {

	/**
	 * Only append children that are visible in the tree viewer
	 */
	protected boolean shouldAppendChildren(Object e) {
		return((TreeViewer)getViewer()).getExpandedState(e);
	}
	
	/**
	 * @see ControlActionDelegate#isEnabledFor(Object)
	 */
	protected boolean isEnabledFor(Object element) {
		return element instanceof IDebugElement;
	}
	
	protected String getActionId() {
		return AbstractDebugView.COPY + ".Variables"; //$NON-NLS-1$
	}
}