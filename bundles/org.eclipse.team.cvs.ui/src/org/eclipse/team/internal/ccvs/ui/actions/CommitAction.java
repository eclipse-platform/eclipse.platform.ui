package org.eclipse.team.internal.ccvs.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.core.ITeamManager;
import org.eclipse.team.core.ITeamProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.ReleaseCommentDialog;
import org.eclipse.team.ui.actions.TeamAction;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * Action for checking in files to a CVS provider.
 * Prompts the user for a release comment.
 */
public class CommitAction extends TeamAction {

	// The previously remembered comment
	private static String previousComment = "";

	/*
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		run(new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
				try {
					CVSUIPlugin.getPlugin().getRepositoryManager().commit(getSelectedResources(), getShell(), monitor);
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				}
			}
		}, Policy.bind("CommitAction.commit"), this.PROGRESS_DIALOG);
	}
	
	/*
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		IResource[] resources = getSelectedResources();
		try {
			if (resources.length == 0) return false;
			ITeamManager manager = TeamPlugin.getManager();
			for (int i = 0; i < resources.length; i++) {
				ITeamProvider provider = manager.getProvider(resources[i].getProject());
				if (provider == null) return false;
				CVSTeamProvider cvsProvider = (CVSTeamProvider)provider;
				if (!cvsProvider.isManaged(resources[i])) return false;
				if (!cvsProvider.isCheckedOut(resources[i])) return false;
			}
			return true;
		} catch (TeamException e) {
			return false;
		}
	}
}
