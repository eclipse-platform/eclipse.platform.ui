/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;

public class TagInRepositoryAction extends TagAction {

	/**
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		ICVSResource[] resources = getSelectedCVSResources();
		if (resources.length == 0) return false;
		for (int i = 0; i < resources.length; i++) {
			if (resources[i] instanceof ICVSRepositoryLocation) return false;
		}
		return true;
	}
	
	/**
	 * @see CVSAction#needsToSaveDirtyEditors()
	 */
	protected boolean needsToSaveDirtyEditors() {
		return false;
	}
	
	/**
	 * @see CVSAction#execute(IAction)
	 */
	public void execute(IAction action) throws InvocationTargetException, InterruptedException {
		
		// Prompt for the tag
		final ICVSResource[] resources = getSelectedCVSResources();
		final CVSTag[] tag = new CVSTag[] { null };
		getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				// Collect the parent folders from which to determine the tags to show
				ICVSFolder[] folders = new ICVSFolder[resources.length];
				for (int i = 0; i < resources.length; i++) {
					if (resources[i].isFolder()) {
						folders[i] = (ICVSFolder)resources[i];
					} else {
						folders[i] = resources[i].getParent();
					}
				}
				tag[0] = promptForTag(folders);
			}
		});
		if (tag[0] == null) return;
					
		CVSUIPlugin.runWithProgress(getShell(), true, new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					monitor.beginTask(null, 1000 * resources.length);
					for (int i = 0; i < resources.length; i++) {
						IStatus status = ((ICVSRemoteResource)resources[i]).tag(tag[0], getLocalOptions(), new SubProgressMonitor(monitor, 1000));
						addStatus(status);
					}
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				}
			}
		});
	}

	/**
	 * Prompt for the tag to be used by the tagging operation. The default behavior
	 * is to prompt for a name and return a version tag of that name.
	 * 
	 * Subclasses can override.
	 * 
	 * @param folders the folders from which to obtain a list of existing tags
	 * @return CVSTag the tag chosen
	 */
	protected CVSTag promptForTag(ICVSFolder[] folders) {
		String name = promptForTag(folders[0]);
		return new CVSTag(name, CVSTag.VERSION);
	}
	
	/**
	 * Return the local options that should be used with the rtag command
	 */
	protected LocalOption[] getLocalOptions() {
		return Command.NO_LOCAL_OPTIONS;
	}
	
	/**
	 * Override to dislay the number of tag operations that succeeded
	 */
	protected IStatus getStatusToDisplay(IStatus[] problems) {
		// We accumulated 1 status per resource above.
		IStatus[] status = getAccumulatedStatus();
		int resourceCount = status.length;
		
		MultiStatus combinedStatus;
		if(resourceCount == 1) {
			combinedStatus = new MultiStatus(CVSUIPlugin.ID, 0, Policy.bind("TagInRepositoryAction.tagProblemsMessage"), null); //$NON-NLS-1$
		} else {
			combinedStatus = new MultiStatus(CVSUIPlugin.ID, 0, Policy.bind("TagInRepositoryAction.tagProblemsMessageMultiple"), null); //$NON-NLS-1$
		}
		for (int i = 0; i < problems.length; i++) {
			combinedStatus.merge(problems[i]);
		}
		return combinedStatus;
	}
}
