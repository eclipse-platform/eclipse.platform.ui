package org.eclipse.team.internal.ccvs.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.ccvs.core.ICVSProvider;
import org.eclipse.team.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.RepositoryManager;
import org.eclipse.team.ui.actions.TeamAction;

/**
 * RemoveRootAction removes a repository
 */
public class RemoveRootAction extends TeamAction {
	/**
	 * Returns the selected remote files
	 */
	protected ICVSRepositoryLocation[] getSelectedRemoteRoots() {
		ArrayList resources = null;
		if (!selection.isEmpty()) {
			resources = new ArrayList();
			Iterator elements = ((IStructuredSelection) selection).iterator();
			while (elements.hasNext()) {
				Object next = elements.next();
				if (next instanceof ICVSRepositoryLocation) {
					resources.add(next);
					continue;
				}
				if (next instanceof IAdaptable) {
					IAdaptable a = (IAdaptable) next;
					Object adapter = a.getAdapter(ICVSRepositoryLocation.class);
					if (adapter instanceof ICVSRemoteFile) {
						resources.add(adapter);
						continue;
					}
				}
			}
		}
		if (resources != null && !resources.isEmpty()) {
			ICVSRepositoryLocation[] result = new ICVSRepositoryLocation[resources.size()];
			resources.toArray(result);
			return result;
		}
		return new ICVSRepositoryLocation[0];
	}
	/*
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				ICVSRepositoryLocation[] roots = getSelectedRemoteRoots();
				if (roots.length == 0) return;
				ICVSProvider provider = CVSProviderPlugin.getProvider();
				for (int i = 0; i < roots.length; i++) {
					try {
						// This will notify the RepositoryManager of the removal
						provider.disposeRepository(roots[i]);
					} catch (CVSException e) {
						throw new InvocationTargetException(e);
					}
				}
			}
		}, Policy.bind("RemoveRootAction.removeRoot"), this.PROGRESS_DIALOG);

	}
	/*
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		ICVSRepositoryLocation[] roots = getSelectedRemoteRoots();
		return roots.length > 0;
	}
}

