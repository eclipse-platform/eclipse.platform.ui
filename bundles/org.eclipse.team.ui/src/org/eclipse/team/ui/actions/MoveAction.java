package org.eclipse.team.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.internal.ui.Policy;

/**
 * Action for moving the selected resources on the provider
 */
public class MoveAction extends TeamAction {
	/** (Non-javadoc)
	 * Method declared on IActionDelegate.
	 */
	public void run(IAction action) {
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
//				try {
//					IResource resource = getSelectedResources()[0];
//					ITeamProvider provider = TeamPlugin.getPlugin().getProvider(resource.getProject());
//					// add move here
//				} catch (TeamProviderException e) {
//					throw new InvocationTargetException(e);
//				}
			}
		}, Policy.bind("MoveAction.move"), this.PROGRESS_DIALOG);
	}
	/**
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() {
		return false;
	}
}