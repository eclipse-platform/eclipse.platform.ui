package org.eclipse.team.internal.ccvs.ui.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.action.IAction;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.ui.operations.FetchAllMembersOperation;

public class FetchAllMembersAction extends CVSAction {

	protected void execute(IAction action) throws InvocationTargetException, InterruptedException {
		ICVSRemoteFolder[] folders = getSelectedRemoteFolders();
		ICVSRepositoryLocation repoLocation = getRepositoryManager().getRepositoryLocationFor(folders[0]);
		new FetchAllMembersOperation (getTargetPart(), folders, repoLocation).run();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.actions.TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		//Only enable for one selection for now
		return getSelectedRemoteFolders().length == 1;		
	}

}
