package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.*;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.*;

/**
 * Used to copy the values of variables to the clipboard from
 * the Variables and Inspector views.
 */
public class CopyVariablesToClipboardActionDelegate extends CopyToClipboardActionDelegate {
	
	private static final String PREFIX= "copy_variables_to_clipboard_action.";

	protected String getHelpContextId() {
		return IDebugHelpContextIds.COPY_VARIABLES_TO_CLIPBOARD_ACTION;
	}
	
	/**
	 * Only append children that are visible in the tree viewer
	 */
	protected boolean shouldAppendChildren(Object e) {
		return((TreeViewer)fViewer).getExpandedState(e);
	}
	
	/**
	 * @see ControlActionDelegate
	 */
	public void initializeForOwner(ControlAction controlAction) {		
		controlAction.setEnabled(!controlAction.getStructuredSelection().isEmpty());
		fViewer = (ContentViewer)controlAction.getSelectionProvider();		
	}
	
	/**
	 * @see ControlActionDelegate
	 */
	protected String getPrefix() {
		return PREFIX;
	}
	
	/**
	 * @see ControlActionDelegate
	 */
	public boolean isEnabledFor(Object element) {
		return element instanceof IDebugElement || element instanceof InspectItem;
	}
	
	/**
	 * @see IActionDelegate
	 */
	public void selectionChanged(IAction action, ISelection s) {
		action.setEnabled(!s.isEmpty());
	}
}