package org.eclipse.team.internal.ccvs.ui.repo;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
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
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				ICVSRepositoryLocation[] locations = getSelectedRepositoryLocations();
				RepositoryManager manager = CVSUIPlugin.getPlugin().getRepositoryManager();
				try {
					monitor.beginTask(null, 100 * locations.length);
					for (int j = 0; j < locations.length; j++) {
						ICVSRepositoryLocation location = locations[j];
						// todo: This omits defined modules when there is no current working set
						ICVSRemoteResource[] resources = manager.getWorkingFoldersForTag(location, CVSTag.DEFAULT, Policy.subMonitorFor(monitor, 10));
						if (promptToRefresh(location, resources)) {
							IProgressMonitor subMonitor = Policy.subMonitorFor(monitor, 90);
							subMonitor.beginTask(null, 100 * resources.length);
							for (int i = 0; i < resources.length; i++) {
								ICVSRemoteResource resource = resources[i];
								if (resource instanceof ICVSFolder) {
									manager.refreshDefinedTags((ICVSFolder)resource, true /* replace */, true, Policy.subMonitorFor(subMonitor, 100));
								}
							}
							subMonitor.done();
						}
					}
					manager.saveState();
					monitor.done();
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				}
			}
		}, true, PROGRESS_DIALOG);
	}

	/**
	 * @see org.eclipse.team.internal.ui.actions.TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		ICVSRepositoryLocation[] locations = getSelectedRepositoryLocations();
		if (locations.length == 0) return false;
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

	private boolean promptToRefresh(final ICVSRepositoryLocation location, final ICVSRemoteResource[] resources) {
		if (resources.length == 0) return true;
		final boolean[] result = new boolean[] {false};
		getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				result[0] = MessageDialog.openQuestion(getShell(), 
					Policy.bind("RefreshTagsAction.title"), 
					Policy.bind("RefreshTagsAction.message", location.getLocation(), new Integer(resources.length).toString()));
			}
		});
		return result[0];
	}
			
}
