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
package org.eclipse.team.internal.ccvs.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.IProjectSetSerializer;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSProvider;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

public class CVSProjectSetSerializer implements IProjectSetSerializer {

	/**
	 * @see IProjectSetSerializer#asReference(IProject[])
	 * 
	 * "1.0,repoLocation,module,projectName[,tag]"
	 */
	public String[] asReference(IProject[] providerProjects, Object context, IProgressMonitor monitor) throws TeamException {
		String[] result = new String[providerProjects.length];
		for (int i = 0; i < providerProjects.length; i++) {
			StringBuffer buffer = new StringBuffer();
			buffer.append("1.0,"); //$NON-NLS-1$
			
			IProject project = providerProjects[i];
			CVSTeamProvider provider = (CVSTeamProvider)RepositoryProvider.getProvider(project);
			CVSWorkspaceRoot root = provider.getCVSWorkspaceRoot();
			CVSRepositoryLocation location = CVSRepositoryLocation.fromString(root.getRemoteLocation().getLocation());
			location.setUserMuteable(true);
			String repoLocation = location.getLocation();
			buffer.append(repoLocation);
			buffer.append(","); //$NON-NLS-1$
			
			ICVSFolder folder = root.getLocalRoot();
			FolderSyncInfo syncInfo = folder.getFolderSyncInfo();
			String module = syncInfo.getRepository();
			buffer.append(module);
			buffer.append(","); //$NON-NLS-1$
			
			String projectName = folder.getName();
			buffer.append(projectName);
			CVSTag tag = syncInfo.getTag();
			if (tag != null) {
				if (tag.getType() != CVSTag.DATE) {
					buffer.append(","); //$NON-NLS-1$
					String tagName = tag.getName();
					buffer.append(tagName);
				}
			}
			result[i] = buffer.toString();
		}
		return result;
	}

	/**
	 * @see IProjectSetSerializer#addToWorkspace(String[])
	 */
	public IProject[] addToWorkspace(String[] referenceStrings, String filename, Object context, IProgressMonitor monitor) throws TeamException {
		final int size = referenceStrings.length;
		final CVSProvider provider = CVSProvider.getInstance();
		final IProject[] projects = new IProject[size];
		final ICVSRepositoryLocation[] locations = new ICVSRepositoryLocation[size];
		final String[] modules = new String[size];
		final CVSTag[] tags = new CVSTag[size];
		
		for (int i = 0; i < size; i++) {
			StringTokenizer tokenizer = new StringTokenizer(referenceStrings[i], ","); //$NON-NLS-1$
			String version = tokenizer.nextToken();
			if (!version.equals("1.0")) { //$NON-NLS-1$
				// Bail out, this is a newer version
				return null;
			}
			String repo = tokenizer.nextToken();
			locations[i] = CVSRepositoryLocation.fromString(repo);
			modules[i] = tokenizer.nextToken();
			String projectName = tokenizer.nextToken();
			projects[i] = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			if (tokenizer.hasMoreTokens()) {
				String tagName = tokenizer.nextToken();
				tags[i] = new CVSTag(tagName, CVSTag.BRANCH);
			}
		}
		// Check if any projects will be overwritten, and warn the user.
		boolean yesToAll = false;
		int action;
		final int[] num = new int[] {size};
		for (int i = 0; i < size; i++) {
			Shell shell = null;
			IProject project = projects[i];
			if (project.exists()) {
				if (shell == null) {
					if (context instanceof Shell) {
						shell = (Shell)context;
					} else {
						return null;
					}
				}
				action = confirmOverwrite(project, yesToAll, shell);
				yesToAll = action == 2;
				
				// message dialog
					switch (action) {
						// no
						case 1:
							// Remove it from the set
							locations[i] = null;
							num[0]--;
							break;
						// yes to all
						case 2:
						// yes
						case 0:
							break;
						// cancel
						case 3:
						default:
							return null;
					}
			}
		}
		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
				monitor.beginTask("", 1000 * num[0]); //$NON-NLS-1$
				try {
					for (int i = 0; i < size; i++) {
						if (locations[i] != null) {
							provider.checkout(locations[i], projects[i], modules[i], tags[i], new SubProgressMonitor(monitor, 1000));
						}
					}
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			op.run(monitor);
		} catch (InterruptedException e) {
		} catch (InvocationTargetException e) {
			Throwable t = e.getTargetException();
			if (t instanceof TeamException) {
				throw (TeamException)t;
			}
		}
		List result = new ArrayList();
		for (int i = 0; i < projects.length; i++) {
			if (projects[i] != null) result.add(projects[i]);
		}
		return (IProject[])result.toArray(new IProject[result.size()]);
	}
	private int confirmOverwrite(IProject project, boolean yesToAll, Shell shell) {
		if (yesToAll) return 2;
		if (!project.exists()) return 0;
		final MessageDialog dialog = 
			new MessageDialog(shell, Policy.bind("CVSProjectSetSerializer.Confirm_Overwrite_Project_8"), null, Policy.bind("CVSProjectSetSerializer.The_project_{0}_already_exists._Do_you_wish_to_overwrite_it__9", project.getName()), MessageDialog.QUESTION, //$NON-NLS-1$ //$NON-NLS-2$
				new String[] {
					IDialogConstants.YES_LABEL,
					IDialogConstants.NO_LABEL,
					IDialogConstants.YES_TO_ALL_LABEL, 
					IDialogConstants.CANCEL_LABEL}, 
				0);
		final int[] result = new int[1];
		shell.getDisplay().syncExec(new Runnable() {
			public void run() {
				result[0] = dialog.open();
			}
		});
		return result[0];
	}
}
