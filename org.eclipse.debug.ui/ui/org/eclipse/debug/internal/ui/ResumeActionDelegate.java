package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.jface.viewers.IStructuredSelection;

public class ResumeActionDelegate extends ControlActionDelegate {
	
	private static final String PREFIX= "resume_action.";

	/**
	 * @see ControlActionDelegate
	 */
	protected void doAction(Object object) throws DebugException {
		IDebugElement element= (IDebugElement)object;
		if (element instanceof ISuspendResume) {
			ISuspendResume suspendResume= (ISuspendResume)element;
			if (suspendResume.canResume()) {
				suspendResume.resume();
			}
		}
	}

	/**
	 * @see ControlActionDelegate
	 */
	public boolean isEnabledFor(Object element) {
		return element instanceof ISuspendResume && ((ISuspendResume) element).canResume();
	}

	/**
	 * @see ControlActionDelegate
	 */
	public boolean getEnableStateForSelection(IStructuredSelection selection) {	 		
		if (selection.size() == 1) {
			return isEnabledFor(selection.getFirstElement());
		} else {
			return false;
		}
	}
	
	/**
	 * @see ControlActionDelegate
	 */
	protected String getPrefix() {
		return PREFIX;
	}

}