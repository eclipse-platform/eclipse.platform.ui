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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.TagConfigurationDialog;
import org.eclipse.team.internal.ccvs.ui.model.BranchCategory;
import org.eclipse.team.ui.actions.TeamAction;

/**
 * DefineTagAction remembers a tag by name
 */
public class ConfigureTagsFromRepoView extends TeamAction {
	IInputValidator validator = new IInputValidator() {
		public String isValid(String newText) {
			IStatus status = CVSTag.validateTagName(newText);
			if (status.isOK()) return null;
			return status.getMessage();
		}
	};

	/**
	 * Returns the selected remote roots
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
				if (next instanceof BranchCategory) {
					resources.add(((BranchCategory)next).getRepository(next));
					continue;
				}
				if (next instanceof IAdaptable) {
					IAdaptable a = (IAdaptable) next;
					Object adapter = a.getAdapter(ICVSRepositoryLocation.class);
					if (adapter instanceof ICVSRepositoryLocation) {
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
				final ICVSRepositoryLocation[] roots = getSelectedRemoteRoots();
				if (roots.length != 1) return;
				final Shell shell = getShell();
				shell.getDisplay().syncExec(new Runnable() {
					public void run() {
						try {
							ICVSRemoteResource[] folders = roots[0].members(CVSTag.DEFAULT, false, null);
							ICVSFolder[] cvsFolders = new ICVSFolder[folders.length];
							for (int i = 0; i < folders.length; i++) {
								cvsFolders[i] = (ICVSFolder)folders[i];
							}
							TagConfigurationDialog d = new TagConfigurationDialog(shell, cvsFolders);
							d.open();
						} catch(CVSException e) {
							ErrorDialog.openError(shell, Policy.bind("ConfigureTagsFromRepoViewConfigure_Tag_Error_1"), Policy.bind("ConfigureTagsFromRepoViewError_retreiving_root_folders_from_repository_2"), e.getStatus()); //$NON-NLS-1$ //$NON-NLS-2$
						}
					}
				});
			}
		}, Policy.bind("ConfigureTagsFromRepoViewConfiguring_branch_tags_3"), this.PROGRESS_BUSYCURSOR); //$NON-NLS-1$
	}

	/*
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		ICVSRepositoryLocation[] roots = getSelectedRemoteRoots();
		if (roots.length != 1) return false;
		return true;
	}
}