/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.operations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSStatus;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.ICVSResourceVisitor;
import org.eclipse.team.internal.ccvs.core.client.Checkout;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Request;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.internal.ccvs.core.client.Update;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;

/**
 * This method checks out one or more remote folders from the same repository
 * into an existing project or folder in the workspace. The target project
 * must either be shared with the same repository or it must not be shared 
 * with any repository
 */
public class CheckoutIntoOperation extends CheckoutOperation {

	private boolean recursive;
	private ICVSFolder localFolder;
	private String localFolderName;

	/**
	 * Constructor which takes a set of remote folders and the local folder into which the folders should be
	 * loaded.
	 */
	public CheckoutIntoOperation(Shell shell, ICVSRemoteFolder[] remoteFolders, IContainer localFolder, boolean recursive) {
		super(shell, remoteFolders);
		this.recursive = recursive;
		this.localFolder = CVSWorkspaceRoot.getCVSFolderFor(localFolder);
	}

	/**
	 * Constructor which takes a single remote folder and the target local folder.
	 */
	public CheckoutIntoOperation(Shell shell, ICVSRemoteFolder remoteFolder, IContainer localFolder, boolean recursive) {
		this(shell, new ICVSRemoteFolder[] { remoteFolder }, localFolder.getParent(), recursive);
		this.localFolderName = localFolder.getName();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.CheckoutOperation#getTaskName()
	 */
	protected String getTaskName() {
		ICVSRemoteFolder[] remoteFolders = getRemoteFolders();
		String localFolderName = "";
		try {
			localFolderName = getLocalFolder().getIResource().getFullPath().toString();
		} catch (CVSException e) {
			CVSUIPlugin.log(e);
		}
		return Policy.bind("CheckoutIntoOperation.taskname", new Integer(remoteFolders.length).toString(), localFolderName); 
	}

	/**
	 * @return
	 */
	public ICVSFolder getLocalFolder() {
		return localFolder;
	}
	
	/**
	 * @return
	 */
	public boolean isRecursive() {
		return recursive;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.operations.CheckoutOperation#checkout(org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void checkout(ICVSRemoteFolder[] folders, IProgressMonitor monitor) throws CVSException {
		ICVSFolder parentLocalFolder = getLocalFolder();
		boolean recurse = isRecursive();
		monitor.beginTask(null, 100 * folders.length + 100);
		for (int i = 0; i < folders.length; i++) {
			ICVSRemoteFolder folder = folders[i];
			checkout(folders[i], parentLocalFolder, recurse, Policy.subMonitorFor(monitor, 100));
		}
		refreshRoot(getLocalRoot(parentLocalFolder), Policy.subMonitorFor(monitor, 100));
		monitor.done();
	}
	
	/*
	 * Prepare the local folders to receive the remote folders. If localFolderName is not null, then
	 * if will be the only target folder of the checkout. Otherwise, the remote folder
	 * could expand to multiple local folders witinb the given parent folder.
	 */
	private ICVSFolder[] prepareLocalFolders(Session session, ICVSRemoteFolder remoteFolder, ICVSFolder parentFolder, String localFolderName, IProgressMonitor monitor) throws CVSException {
		Set targetFolderSet = new HashSet();
		monitor.beginTask(null, 30);
		if (localFolderName == null) {
			
			// Determine which local folders will be afected
			IStatus status = Request.EXPAND_MODULES.execute(session, new String[] { remoteFolder.getRepositoryRelativePath()}, Policy.subMonitorFor(monitor, 10));
			if (status.getCode() == CVSStatus.SERVER_ERROR) {
				addError(status);
				return null;
			}
			
			// Convert the module expansions to target folders
			String[] expansions = session.getModuleExpansions();
			for (int j = 0; j < expansions.length; j++) {
				String childPath = new Path(expansions[j]).segment(0);
				ICVSResource resource = parentFolder.getChild(childPath);
				if (resource != null && !resource.isFolder()) {
					// The target folder conflicts with an existing file
					addError(new CVSStatus(IStatus.ERROR, Policy.bind("CheckoutIntoOperation.targetIsFile", remoteFolder.getName(), resource.getIResource().getFullPath().toString())));
					return null;
				}
				targetFolderSet.add(parentFolder.getFolder(childPath));
			}
			
		} else {
			targetFolderSet.add(parentFolder.getFolder(localFolderName));
		}
		
		final ICVSFolder[] targetFolders = (ICVSFolder[]) targetFolderSet.toArray(new ICVSFolder[targetFolderSet.size()]);
		
		// Ensure that the checkout will not conflict with existing resources
		IStatus status = validateTargetFolders(remoteFolder, targetFolders, Policy.subMonitorFor(monitor, 10));
		if (!status.isOK()) {
			addError(status);
			return null;
		}
		
		// Prepare the target projects to receive resources
		status = scrubFolders(remoteFolder, targetFolders, Policy.subMonitorFor(monitor, 10));
		// return the target projects if the scrub succeeded
		if (status.isOK()) {
			return targetFolders;
		} else {
			addError(status);
			return null;
		}
	}
	
	/*
	 * Ensure that the new folders will not conflict with existing folders (even those that are pruned).
	 */
	private IStatus validateTargetFolders(ICVSRemoteFolder remoteFolder, ICVSFolder[] targetFolders, IProgressMonitor monitor) throws CVSException {
		for (int i = 0; i < targetFolders.length; i++) {
			ICVSFolder targetFolder = targetFolders[i];
			FolderSyncInfo localInfo = targetFolder.getFolderSyncInfo();
			FolderSyncInfo remoteInfo = remoteFolder.getFolderSyncInfo();
			
			if (!remoteInfo.isSameMapping(localInfo)) {
				if (localInfo != null ) {
					if (isRemoteChildOfParent(targetFolder)) {
						// if the local folder is child of it's parent remotely (i.e. path of child is parent/child)
						// then the remote cannot be loaded.
						String message;
						if (targetFolder.exists()) {
							message = Policy.bind("CheckoutIntoOperation.targetIsFolder", remoteFolder.getName(), targetFolder.getIResource().getFullPath().toString());
						} else {
							message = Policy.bind("CheckoutIntoOperation.targetIsPrunedFolder", remoteFolder.getName(), targetFolder.getFolderSyncInfo().getRepository());
						}
						return new CVSStatus(IStatus.ERROR, message);
					}
				}
				// Verify that no other folders in the local workspace are mapped to the remote folder
				IStatus status = validateUniqueMapping(remoteFolder, targetFolder, Policy.subMonitorFor(monitor, 10));
				if (!status.isOK()) return status;
			}
		}
		return OK;
	}

	/*
	 * Return true if the given local folder is a direct descendant of it's local parent in 
	 * the repository as well
	 */
	private boolean isRemoteChildOfParent(ICVSFolder targetFolder) throws CVSException {
		FolderSyncInfo localInfo = targetFolder.getFolderSyncInfo();
		if (localInfo == null) return false;
		FolderSyncInfo parentInfo = targetFolder.getParent().getFolderSyncInfo();
		if (parentInfo == null) return false;
		IPath childPath = new Path(localInfo.getRepository());
		IPath parentPath = new Path(parentInfo.getRepository());
		return parentPath.isPrefixOf(childPath);
	}

	/**
	 * @param targetFolder
	 * @return
	 */
	private IContainer getLocalRoot(ICVSFolder targetFolder) throws CVSException {
		return targetFolder.getIResource().getProject();
	}

	/*
	 * Ensure that there is no equivalent mapping alreay in the local workspace
	 */
	private IStatus validateUniqueMapping(final ICVSRemoteFolder remoteFolder, final ICVSFolder targetFolder, IProgressMonitor iProgressMonitor) throws CVSException {
		
		final IContainer root = getLocalRoot(targetFolder);
		final FolderSyncInfo remoteInfo = remoteFolder.getFolderSyncInfo();
		if (remoteInfo.equals(FolderSyncInfo.VIRTUAL_DIRECTORY)) {
			// We can't really check the mapping ahead of time
			// so we'll let the operation continue
			return OK;
		}
		ICVSFolder cvsFolder = CVSWorkspaceRoot.getCVSFolderFor(root);
		try {
			cvsFolder.accept(new ICVSResourceVisitor() {
				public void visitFile(ICVSFile file) throws CVSException {
					// do nothing
				}
				public void visitFolder(ICVSFolder folder) throws CVSException {
					if (!folder.isCVSFolder()) return;
					IResource resource = folder.getIResource();
					if (resource == null) return;
					FolderSyncInfo info = folder.getFolderSyncInfo();
					if (info.isSameMapping(remoteInfo)) {
						throw new CVSException(Policy.bind("CheckoutIntoOperation.mappingAlreadyExists", 
							new Object[] {
								remoteFolder.getName(), 
								targetFolder.getIResource().getFullPath().toString(),
								resource.getFullPath().toString()
							}));
					}
					folder.acceptChildren(this);
				}
			});
		} catch (CVSException e) {
			return e.getStatus();
		}
		return OK;
	}
	
	/*
	 * Purge the local contents of the given folders
	 */
	private IStatus scrubFolders(ICVSRemoteFolder remoteFolder, ICVSFolder[] targetFolders, IProgressMonitor monitor) throws CVSException {
		monitor.beginTask(null, 100 * targetFolders.length);
		
		// Prompt first before any work is done
		if (targetFolders.length > 1) {
			setInvolvesMultipleResources(true);
		}
		for (int i=0;i<targetFolders.length;i++) {
			ICVSFolder targetFolder = targetFolders[i];
			if (needsPromptForOverwrite(targetFolder, Policy.subMonitorFor(monitor, 50)) && !promptToOverwrite(targetFolder)) {
				return new CVSStatus(IStatus.INFO, Policy.bind("CheckoutIntoOperation.cancelled", remoteFolder.getName()));
			}
		}
		
		for (int i = 0; i < targetFolders.length; i++) {
			IStatus status = scrubFolder(targetFolders[i], Policy.subMonitorFor(monitor, 50));
			if (!status.isOK()) return status;
		}
		monitor.done();
		return OK;
	}

	private boolean needsPromptForOverwrite(ICVSFolder targetFolder, IProgressMonitor monitor) throws CVSException {
		return targetFolder.isModified(monitor);
	}

	private boolean promptToOverwrite(ICVSFolder folder) {
		return promptToOverwrite(
			Policy.bind("CheckoutOperation.confirmOverwrite"), 
			Policy.bind("CheckoutIntoOperation.overwriteMessage", folder.getName()));
	}
	
	private IStatus scrubFolder(ICVSFolder folder, IProgressMonitor monitor) throws CVSException {
		if (folder.exists() || folder.isCVSFolder()) {
			// Unmanage first so we don't get outgoing deletions
			folder.unmanage(Policy.subMonitorFor(monitor, 50));
			if (folder.exists()) folder.delete();
		}
		return OK;
	}

	private void checkout(final ICVSRemoteFolder remoteFolder, ICVSFolder parentFolder, boolean recurse, IProgressMonitor monitor) throws CVSException {
		// Open a connection session to the repository
		ICVSRepositoryLocation repository = remoteFolder.getRepository();
		Session session = new Session(repository, parentFolder);
		try {
			session.open(Policy.subMonitorFor(monitor, 10));
			
			// Determine which local folders will be affected
			ICVSFolder[] targetFolders = prepareLocalFolders(session, remoteFolder, parentFolder, localFolderName, Policy.subMonitorFor(monitor, 10));
			if (targetFolders == null) {
				// an error occured and has been added to the operation's error list
				return;
			}
			
			// Add recurse option
			List localOptions = new ArrayList();
			if (!recurse)
				localOptions.add(Update.DO_NOT_RECURSE);
			if (localFolderName != null) {
				localOptions.add(Checkout.makeDirectoryNameOption(localFolderName));
			}
			
			// Prune empty directories if pruning enabled
			if (CVSProviderPlugin.getPlugin().getPruneEmptyDirectories()) 
				localOptions.add(Checkout.PRUNE_EMPTY_DIRECTORIES);
			// Add the options related to the CVSTag
			CVSTag tag = remoteFolder.getTag();
			if (tag == null) {
				// A null tag in a remote resource indicates HEAD
				tag = CVSTag.DEFAULT;
			}
			localOptions.add(Update.makeTagOption(tag));
			
			// Perform the checkout
			IStatus status = Command.CHECKOUT.execute(session,
				Command.NO_GLOBAL_OPTIONS,
				(LocalOption[])localOptions.toArray(new LocalOption[localOptions.size()]),
				new String[] { remoteFolder.getRepositoryRelativePath() },
				null,
				Policy.subMonitorFor(monitor, 10));
			if (!status.isOK()) {
				addError(status);
				return;
			}
			
			manageFolders(targetFolders, repository.getLocation());
			
		} finally {
			session.close();
		}
	}

	private void manageFolders(ICVSFolder[] targetFolders, String root) throws CVSException {
		for (int i = 0; i < targetFolders.length; i++) {
			manageFolder(targetFolders[i], root);
		}
	}
	
	private static void manageFolder(ICVSFolder folder, String root) throws CVSException {
		// Ensure that the parent is a CVS folder
		ICVSFolder parent = folder.getParent();
		if (!parent.isCVSFolder()) {
			parent.setFolderSyncInfo(new FolderSyncInfo(FolderSyncInfo.VIRTUAL_DIRECTORY, root, CVSTag.DEFAULT, true));
			IResource resource = parent.getIResource();
			if (resource.getType() != IResource.PROJECT) {
				manageFolder(parent, root);
			}
		}
		// reset the folder sync info so it will be managed by it's parent
		folder.setFolderSyncInfo(folder.getFolderSyncInfo());
	}

	/*
	 * Bring the provided projects into the workspace
	 */
	private static void refreshRoot(IContainer root, IProgressMonitor monitor) throws CVSException {
			try {
				IProject project = root.getProject();
				CVSTeamProvider provider = (CVSTeamProvider)RepositoryProvider.getProvider(project, CVSProviderPlugin.getTypeId());
				if (provider == null) {
					// Register the project with Team
					RepositoryProvider.map(project, CVSProviderPlugin.getTypeId());
					
					// TODO: This should be somewhere else
					provider = (CVSTeamProvider)RepositoryProvider.getProvider(project, CVSProviderPlugin.getTypeId());
					provider.setWatchEditEnabled(CVSProviderPlugin.getPlugin().isWatchEditEnabled());
				}
			} catch (TeamException e) {
				throw CVSException.wrapException(e);
			}
	}
}
