package org.eclipse.team.internal.ccvs.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareUI;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.ccvs.core.ICVSResource;
import org.eclipse.team.ccvs.core.ILogEntry;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.CVSCompareRevisionsInput;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.ui.actions.TeamAction;

public class CompareWithRevisionAction extends TeamAction {
	/**
	 * Returns the selected remote file
	 */
	protected ICVSRemoteFile getSelectedRemoteFile() {
		IResource[] resources = getSelectedResources();
		if (resources.length != 1) return null;
		if (!(resources[0] instanceof IFile)) return null;
		IFile file = (IFile)resources[0];
		CVSTeamProvider provider = (CVSTeamProvider)TeamPlugin.getManager().getProvider(file.getProject());
		try {
			return (ICVSRemoteFile)CVSWorkspaceRoot.getRemoteResourceFor(file);
		} catch (TeamException e) {
			handle(e, null, null);
			return null;
		}
	}
	/*
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				ICVSRemoteFile file = getSelectedRemoteFile();
				if (file == null) {
					// No revisions for selected file
					MessageDialog.openWarning(getShell(), Policy.bind("CompareWithRevisionAction.noRevisions"), Policy.bind("CompareWithRevisionAction.noRevisionsLong"));
					return;
				}
				ILogEntry[] entries = null;
				try {
					entries = file.getLogEntries(new NullProgressMonitor());
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				}
				CompareUI.openCompareEditor(new CVSCompareRevisionsInput((IFile)getSelectedResources()[0], entries));
			}
		}, Policy.bind("CompareWithRevisionAction.compare"), this.PROGRESS_BUSYCURSOR);
	}
	/*
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		IResource[] resources = getSelectedResources();
		if (resources.length != 1) return false;
		ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resources[0]);
		return cvsResource.isManaged();
	}
}