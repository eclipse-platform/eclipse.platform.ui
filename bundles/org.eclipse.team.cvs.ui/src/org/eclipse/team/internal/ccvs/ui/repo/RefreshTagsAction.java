package org.eclipse.team.internal.ccvs.ui.repo;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.ui.actions.CVSAction;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class RefreshTagsAction extends CVSAction {

	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.CVSAction#execute(org.eclipse.jface.action.IAction)
	 */
	protected void execute(IAction action) throws InvocationTargetException, InterruptedException {
		ICVSRepositoryLocation[] locations = getSelectedRepositoryLocations();
		RefreshRemoteProjectWizard.execute(getShell(), locations[0]);
	}

	/**
	 * @see org.eclipse.team.internal.ui.actions.TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		ICVSRepositoryLocation[] locations = getSelectedRepositoryLocations();
		if (locations.length != 1) return false;
		return true;
	}

	/**
	 * Returns the selected CVS tags
	 */
	protected ICVSRepositoryLocation[] getSelectedRepositoryLocations() {
		ArrayList tags = new ArrayList();
		if (!selection.isEmpty()) {
			Iterator elements = ((IStructuredSelection) selection).iterator();
			while (elements.hasNext()) {
				Object adapter = getAdapter(elements.next(), ICVSRepositoryLocation.class);
				if (adapter instanceof ICVSRepositoryLocation) {
					tags.add(adapter);
				}
			}
		}
		return (ICVSRepositoryLocation[])tags.toArray(new ICVSRepositoryLocation[tags.size()]);
	}

}
