package org.eclipse.team.internal.ccvs.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.TagConfigurationDialog;
import org.eclipse.team.internal.ccvs.ui.model.RemoteModule;
import org.eclipse.team.ui.actions.TeamAction;

/**
 * DefineTagAction remembers a tag by name
 */
public class ConfigureTagsFromRepoViewOnFolder extends TeamAction {
	/**
	 * Returns the selected remote folders
	 */
	protected ICVSRemoteFolder[] getSelectedRemoteFolders() {
		ArrayList resources = null;
		if (!selection.isEmpty()) {
			resources = new ArrayList();
			Iterator elements = ((IStructuredSelection) selection).iterator();
			while (elements.hasNext()) {
				Object next = elements.next();
				if (next instanceof ICVSRemoteFolder) {
					if(new Path(((ICVSRemoteFolder)next).getRepositoryRelativePath()).segmentCount()==1) {
						resources.add(next);
					}
					continue;
				}
				if(next instanceof RemoteModule) {
					resources.add((ICVSRemoteFolder)((RemoteModule)next).getCVSResource());
				}
			}
		}
		if (resources != null && !resources.isEmpty()) {
			return (ICVSRemoteFolder[])resources.toArray(new ICVSRemoteFolder[resources.size()]);
		}
		return new ICVSRemoteFolder[0];
	}

	/*
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				final ICVSRemoteFolder[] roots = getSelectedRemoteFolders();
				final Shell shell = getShell();
				shell.getDisplay().syncExec(new Runnable() {
					public void run() {
						ICVSFolder[] cvsFolders = new ICVSFolder[roots.length];
						for (int i = 0; i < roots.length; i++) {
							cvsFolders[i] = (ICVSFolder)roots[i];
						}
						TagConfigurationDialog d = new TagConfigurationDialog(shell, cvsFolders);
						d.open();
					}
				});
			}
		}, Policy.bind("ConfigureTagsFromRepoViewOnFolderConfiguring_branch_tags_1"), this.PROGRESS_BUSYCURSOR); //$NON-NLS-1$
	}

	/*
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		ICVSRemoteFolder[] roots = getSelectedRemoteFolders();
		if (roots.length == 0) return false;
		return true;
	}
}