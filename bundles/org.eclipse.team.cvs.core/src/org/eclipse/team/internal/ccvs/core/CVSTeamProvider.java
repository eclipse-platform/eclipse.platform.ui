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
package org.eclipse.team.internal.ccvs.core;
 
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.team.IMoveDeleteHook;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.Team;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Commit;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.internal.ccvs.core.client.Tag;
import org.eclipse.team.internal.ccvs.core.client.Update;
import org.eclipse.team.internal.ccvs.core.client.Command.KSubstOption;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.core.client.listeners.AdminKSubstListener;
import org.eclipse.team.internal.ccvs.core.client.listeners.DiffListener;
import org.eclipse.team.internal.ccvs.core.client.listeners.ICommandOutputListener;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.connection.CVSServerException;
import org.eclipse.team.internal.ccvs.core.resources.CVSRemoteSyncElement;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.resources.EclipseSynchronizer;
import org.eclipse.team.internal.ccvs.core.streams.CRLFtoLFInputStream;
import org.eclipse.team.internal.ccvs.core.streams.LFtoCRLFInputStream;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.MutableResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.util.Assert;
import org.eclipse.team.internal.ccvs.core.util.PrepareForReplaceVisitor;
import org.eclipse.team.internal.ccvs.core.util.ReplaceWithBaseVisitor;

/**
 * This class acts as both the ITeamNature and the ITeamProvider instances
 * required by the Team core.
 * 
 * The current stat of this class and it's plugin is EXPERIMENTAL.
 * As such, it is subject to change except in it's conformance to the
 * TEAM API which it implements.
 * 
 * Questions:
 * 
 * How should a project/reource rename/move effect the provider?
 * 
 * Currently we always update with -P. Is this OK?
 *  - A way to allow customizable options would be nice
 * 
 * Is the -l option valid for commit and does it work properly for update and commit?
 * 
 * Do we need an IUserInteractionProvider in the CVS core
 * 	- prompt for user info (caching could be separate)
 * 	- get release comments
 * 	- prompt for overwrite of unmanaged files
 * 
 * Need a mechanism for communicating meta-information (provided by Team?)
 * 
 * Should pass null when there are no options for a cvs command
 * 
 * We currently write the files to disk and do a refreshLocal to
 * have them appear in Eclipse. This may be changed in the future.
 */
public class CVSTeamProvider extends RepositoryProvider {
	private static final boolean IS_CRLF_PLATFORM = Arrays.equals(
		System.getProperty("line.separator").getBytes(), new byte[] { '\r', '\n' }); //$NON-NLS-1$

	private CVSWorkspaceRoot workspaceRoot;
	private IProject project;
	private String comment = "";  //$NON-NLS-1$
	
	private static IMoveDeleteHook moveDeleteHook;
		
	/**
	 * No-arg Constructor for IProjectNature conformance
	 */
	public CVSTeamProvider() {
	}
	


	/**
	 * @see IProjectNature#deconfigure()
	 */
	public void deconfigure() throws CoreException {
		// when a nature is removed from the project, notify the synchronizer that
		// we no longer need the sync info cached. This does not affect the actual CVS
		// meta directories on disk, and will remain unless a client calls unmanage().
		try {
			EclipseSynchronizer.getInstance().flush(getProject(), true, true /*flush deep*/, null);
		} catch(CVSException e) {
			throw new CoreException(e.getStatus());
		} finally {
			CVSProviderPlugin.broadcastProjectDeconfigured(getProject());
		}
	}
	
	/**
	 * @see IProjectNature#getProject()
	 */
	public IProject getProject() {
		return project;
	}
	


	/**
	 * @see IProjectNature#setProject(IProject)
	 */
	public void setProject(IProject project) {
		this.project = project;
		try {
			this.workspaceRoot = new CVSWorkspaceRoot(project);
			// Ensure that the project has CVS info
			if (workspaceRoot.getLocalRoot().getFolderSyncInfo() == null) {
				throw new CVSException(new CVSStatus(CVSStatus.ERROR, Policy.bind("CVSTeamProvider.noFolderInfo", project.getName()))); //$NON-NLS-1$
			}
		} catch (CVSException e) {
			// Log any problems creating the CVS managed resource
			CVSProviderPlugin.log(e);
		}
	}



	/**
	 * Add the given resources to the project. 
	 * <p>
	 * The sematics follow that of CVS in the sense that any folders 
	 * being added are created remotely as a result of this operation 
	 * while files are created remotely on the next commit. 
	 * </p>
	 * <p>
	 * This method uses the team file type registry to determine the type
	 * of added files. If the extension of the file is not in the registry,
	 * the file is assumed to be binary.
	 * </p>
	 * <p>
	 * NOTE: for now we do three operations: one each for folders, text files and binary files.
	 * We should optimize this when time permits to either use one operations or defer server
	 * contact until the next commit.
	 * </p>
	 * 
	 * <p>
	 * There are special semantics for adding the project itself to the repo. In this case, the project 
	 * must be included in the resources array.
	 * </p>
	 */
	public void add(IResource[] resources, int depth, IProgressMonitor progress) throws TeamException {	
		
		// Visit the children of the resources using the depth in order to
		// determine which folders, text files and binary files need to be added
		// A TreeSet is needed for the folders so they are in the right order (i.e. parents created before children)
		final SortedSet folders = new TreeSet();
		// Sets are required for the files to ensure that files will not appear twice if there parent was added as well
		// and the depth isn't zero
		final Map /* from KSubstOption to Set */ files = new HashMap();
		final TeamException[] eHolder = new TeamException[1];
		for (int i=0; i<resources.length; i++) {
			
			final IResource currentResource = resources[i];
			
			// Throw an exception if the resource is not a child of the receiver
			checkIsChild(currentResource);
			
			try {		
				// Auto-add parents if they are not already managed
				IContainer parent = currentResource.getParent();
				ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(currentResource);
				while (parent.getType() != IResource.ROOT && parent.getType() != IResource.PROJECT && ! cvsResource.isManaged()) {
					folders.add(parent.getProjectRelativePath().toString());
					parent = parent.getParent();
				}
					
				// Auto-add children
				currentResource.accept(new IResourceVisitor() {
					public boolean visit(IResource resource) {
						ICVSResource mResource = CVSWorkspaceRoot.getCVSResourceFor(resource);
						// Add the resource is its not already managed and it was either
						// added explicitly (is equal currentResource) or is not ignored
						if (! mResource.isManaged() && (currentResource.equals(resource) || ! mResource.isIgnored())) {
							String name = resource.getProjectRelativePath().toString();
							if (resource.getType() == IResource.FILE) {
								KSubstOption ksubst = KSubstOption.fromFile((IFile) resource);
								Set set = (Set) files.get(ksubst);
								if (set == null) {
									set = new HashSet();
									files.put(ksubst, set);
								}
								set.add(name);
							} else {
								folders.add(name);
							}
						}
						// Always return true and let the depth determine if children are visited
						return true;
					}
				}, depth, false);
			} catch (CoreException e) {
				throw new CVSException(new Status(IStatus.ERROR, CVSProviderPlugin.ID, TeamException.UNABLE, Policy.bind("CVSTeamProvider.visitError", new Object[] {resources[i].getFullPath()}), e)); //$NON-NLS-1$
			}
		}
		// If an exception occured during the visit, throw it here
		if (eHolder[0] != null)
			throw eHolder[0];
	
		// XXX Do we need to add the project 
		
		// Add the folders, followed by files!
		IStatus status;
		Session s = new Session(workspaceRoot.getRemoteLocation(), workspaceRoot.getLocalRoot());
		progress.beginTask(null, 10 + files.size() * 10 + (folders.isEmpty() ? 0 : 10));
		try {
			// Opening the session takes 10 units of time
			s.open(Policy.subMonitorFor(progress, 10));
			if (!folders.isEmpty()) {
				status = Command.ADD.execute(s,
					Command.NO_GLOBAL_OPTIONS,
					Command.NO_LOCAL_OPTIONS,
					(String[])folders.toArray(new String[folders.size()]),
					null,
					Policy.subMonitorFor(progress, 10));
				if (status.getCode() == CVSStatus.SERVER_ERROR) {
					throw new CVSServerException(status);
				}
			}
			for (Iterator it = files.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				KSubstOption ksubst = (KSubstOption) entry.getKey();
				Set set = (Set) entry.getValue();
				status = Command.ADD.execute(s,
					Command.NO_GLOBAL_OPTIONS,
					new LocalOption[] { ksubst },
					(String[])set.toArray(new String[set.size()]),
					null,
					Policy.subMonitorFor(progress, 10));
				if (status.getCode() == CVSStatus.SERVER_ERROR) {
					throw new CVSServerException(status);
				}
			}
		} finally {
			s.close();
			progress.done();
		}
	}
	
	/**
	 * Checkin any local changes using "cvs commit ...".
	 * 
	 * @see ITeamProvider#checkin(IResource[], int, IProgressMonitor)
	 */
	public void checkin(IResource[] resources, int depth, IProgressMonitor progress) throws TeamException {
			
		// Build the local options
		List localOptions = new ArrayList();
		localOptions.add(Commit.makeArgumentOption(Command.MESSAGE_OPTION, comment));

		// If the depth is not infinite, we want the -l option
		if (depth != IResource.DEPTH_INFINITE) {
			localOptions.add(Commit.DO_NOT_RECURSE);
		}
		LocalOption[] commandOptions = (LocalOption[])localOptions.toArray(new LocalOption[localOptions.size()]);

		// Build the arguments list
		String[] arguments = getValidArguments(resources, commandOptions);
					
		// Commit the resources
		IStatus status;
		Session s = new Session(workspaceRoot.getRemoteLocation(), workspaceRoot.getLocalRoot());
		progress.beginTask(null, 100);
		try {
			// Opening the session takes 20% of the time
			s.open(Policy.subMonitorFor(progress, 20));
			status = Command.COMMIT.execute(s,
			Command.NO_GLOBAL_OPTIONS,
			commandOptions,
			arguments, null,
			Policy.subMonitorFor(progress, 80));
		} finally {
			s.close();
			progress.done();
		}
		if (status.getCode() == CVSStatus.SERVER_ERROR) {
			throw new CVSServerException(status);
		}
	}

	/**
	 * Checkout the provided resources so they can be modified locally and committed.
	 * 
	 * Currently, we support only the optimistic model so checkout does nothing.
	 * 
	 * @see ITeamProvider#checkout(IResource[], int, IProgressMonitor)
	 */
	public void checkout(IResource[] resources, int depth, IProgressMonitor progress) throws TeamException {
	}
		
	/**
	 * @see ITeamProvider#delete(IResource[], int, IProgressMonitor)
	 */
	public void delete(IResource[] resources, final IProgressMonitor progress) throws TeamException {
		try {
			progress.beginTask(null, 100);
			
			// Delete any files locally and record the names.
			// Use a resource visitor to ensure the proper depth is obtained
			final IProgressMonitor subProgress = Policy.infiniteSubMonitorFor(progress, 30);
			subProgress.beginTask(null, 256);
			final List files = new ArrayList(resources.length);
			final TeamException[] eHolder = new TeamException[1];
			for (int i=0;i<resources.length;i++) {
				IResource resource = resources[i];
				checkIsChild(resource);
				try {
					if (resource.exists()) {
						resource.accept(new IResourceVisitor() {
							public boolean visit(IResource resource) {
								try {
									ICVSResource cvsResource = workspaceRoot.getCVSResourceFor(resource);
									if (cvsResource.isManaged()) {
										String name = resource.getProjectRelativePath().toString();
										if (resource.getType() == IResource.FILE) {
											files.add(name);
											((IFile)resource).delete(false, true, subProgress);
										}
									}
								} catch (CoreException e) {
									eHolder[0] = wrapException(e);
									// If there was a problem, don't visit the children
									return false;
								}
								// Always return true and let the depth determine if children are visited
								return true;
							}
						}, IResource.DEPTH_INFINITE, false);
					} else if (resource.getType() == IResource.FILE) {
						// If the resource doesn't exist but is a file, queue it for removal
						files.add(resource.getProjectRelativePath().toString());
					}
				} catch (CoreException e) {
					throw wrapException(e);
				}
			}
			subProgress.done();
			// If an exception occured during the visit, throw it here
			if (eHolder[0] != null) throw eHolder[0];		
			// If there are no files to delete, we are done
			if (files.isEmpty()) return;
			
			// Remove the files remotely
			IStatus status;
			Session s = new Session(workspaceRoot.getRemoteLocation(), workspaceRoot.getLocalRoot());
			s.open(progress);
			try {
				status = Command.REMOVE.execute(s,
				Command.NO_GLOBAL_OPTIONS,
				Command.NO_LOCAL_OPTIONS,
				(String[])files.toArray(new String[files.size()]),
				null,
				Policy.subMonitorFor(progress, 70));
			} finally {
				s.close();
			}
			if (status.getCode() == CVSStatus.SERVER_ERROR) {
				throw new CVSServerException(status);
			}	
		} finally {
			progress.done();
		}
	}
	
	/** 
	 * Diff the resources against the repository and write the output to the provided 
	 * PrintStream in a form that is usable as a patch. The patch is rooted at the
	 * project.
	 */
	public void diff(IResource resource, LocalOption[] options, PrintStream stream,
		IProgressMonitor progress) throws TeamException {
		
		// Determine the command root and arguments arguments list
		ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource);
		ICVSFolder commandRoot;
		String[] arguments;
		if (cvsResource.isFolder()) {
			commandRoot = (ICVSFolder)cvsResource;
			arguments = new String[] {Session.CURRENT_LOCAL_FOLDER};
		} else {
			commandRoot = cvsResource.getParent();
			arguments = new String[] {cvsResource.getName()};
		}

		Session s = new Session(workspaceRoot.getRemoteLocation(), commandRoot);
		progress.beginTask(null, 100);
		try {
			s.open(Policy.subMonitorFor(progress, 20));
			Command.DIFF.execute(s,
				Command.NO_GLOBAL_OPTIONS,
				options,
				arguments,
				new DiffListener(stream),
				Policy.subMonitorFor(progress, 80));
		} finally {
			s.close();
			progress.done();
		}
	}
	
	/**
	 * Replace the local version of the provided resources with the remote using "cvs update -C ..."
	 * 
	 * @see ITeamProvider#get(IResource[], int, IProgressMonitor)
	 */
	public void get(IResource[] resources, final int depth, IProgressMonitor progress) throws TeamException {
		get(resources, depth, null, progress);
	}
	
	public void get(final IResource[] resources, final int depth, CVSTag tag, IProgressMonitor progress) throws TeamException {
		try {
			progress.beginTask(null, 100);
			
			// Handle the retrival of the base in a special way
			if (tag != null && tag.equals(CVSTag.BASE)) {
				new ReplaceWithBaseVisitor().replaceWithBase(getProject(), resources, depth, Policy.subMonitorFor(progress, 100)); //$NON-NLS-1$
				return;
			}
			
			// Prepare for the replace (special handling for "cvs added" and "cvs removed" resources
			new PrepareForReplaceVisitor().visitResources(getProject(), resources, "CVSTeamProvider.scrubbingResource", depth, Policy.subMonitorFor(progress, 30)); //$NON-NLS-1$
						
			// Perform an update, ignoring any local file modifications
			List options = new ArrayList();
			options.add(Update.IGNORE_LOCAL_CHANGES);
			if(depth != IResource.DEPTH_INFINITE) {
				options.add(Command.DO_NOT_RECURSE);
			}
			LocalOption[] commandOptions = (LocalOption[]) options.toArray(new LocalOption[options.size()]);
			update(resources, commandOptions, tag, true /*createBackups*/, Policy.subMonitorFor(progress, 70));
		} finally {
			progress.done();
		}
	}
	
	/**
	 * Return the remote location to which the receiver's project is mapped.
	 */
	public ICVSRepositoryLocation getRemoteLocation() throws CVSException {
		try {
			return workspaceRoot.getRemoteLocation();
		} catch (CVSException e) {
			// If we can't get the remote location, we should disconnect since nothing can be done with the provider
			try {
				Team.removeNatureFromProject(project, CVSProviderPlugin.getTypeId(), Policy.monitorFor(null));
			} catch (TeamException ex) {
				CVSProviderPlugin.log(ex);
			}
			// We need to trigger a decorator refresh					
			throw e;
		}
	}
	
	/**
	 * @see ITeamProvider#hasRemote(IResource)
	 * XXX to be removed when sync methods are removed from ITeamProvider
	 */
	public boolean hasRemote(IResource resource) {
		try {
			ICVSResource cvsResource = workspaceRoot.getCVSResourceFor(resource);
			int type = resource.getType();
			if(type!=IResource.FILE) {
				if(type==IResource.PROJECT) {
					return ((ICVSFolder)cvsResource).isCVSFolder();
				} else {
					return cvsResource.isManaged();
				}
			} else {
				ResourceSyncInfo info = cvsResource.getSyncInfo();
				if(info!=null) {
					return !info.isAdded();
				} else {
					return false;
				}
			}					
		} catch(CVSException e) {
			return false;
		}
	}
	
	/**
	 * @see ITeamProvider#isLocallyCheckedOut(IResource)
 	 * XXX to be removed when sync methods are removed from ITeamProvider
	 */
	public boolean isCheckedOut(IResource resource) {
		ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource);
		return cvsResource.isManaged();
	}
	
	/*
	 * Use specialiazed tagging to move all local changes (including additions and
	 * deletions) to the specified branch.
	 */
	public void makeBranch(IResource[] resources, CVSTag versionTag, CVSTag branchTag, boolean moveToBranch, boolean eclipseWay, IProgressMonitor monitor) throws TeamException {
		
		// Determine the total amount of work
		int totalWork = 10 + (versionTag!= null ? 60 : 40) + (moveToBranch ? 20 : 0);
		monitor.beginTask(Policy.bind("CVSTeamProvider.makeBranch"), totalWork);  //$NON-NLS-1$
		try {
			
			// Determine which tag command to used depending on whether the Eclipse specific
			// method of branching is requested
			Tag tagCommand = Command.TAG;
			if (eclipseWay) {
				tagCommand = Command.CUSTOM_TAG;
			}
			
			// Build the arguments list
			String[] arguments = getValidArguments(resources, Command.NO_LOCAL_OPTIONS);
			
			// Tag the remote resources
			Session s = new Session(workspaceRoot.getRemoteLocation(), workspaceRoot.getLocalRoot());
			try {
				s.open(Policy.subMonitorFor(monitor, 10));
				
				IStatus status;
				if (versionTag != null) {
					// Version using tag and braqnch using rtag
					status = tagCommand.execute(s,
						Command.NO_GLOBAL_OPTIONS,
						Command.NO_LOCAL_OPTIONS,
						versionTag,
						arguments,
						null,
						Policy.subMonitorFor(monitor, 40));
					if (status.getCode() != CVSStatus.SERVER_ERROR) {
						// XXX Could use RTAG here when it works
						status = tagCommand.execute(s,
							Command.NO_GLOBAL_OPTIONS,
							Command.NO_LOCAL_OPTIONS,
							branchTag,
							arguments,
							null,
							Policy.subMonitorFor(monitor, 20));
					}
				} else {
					// Just branch using tag
					status = tagCommand.execute(s,
						Command.NO_GLOBAL_OPTIONS,
						Command.NO_LOCAL_OPTIONS,
						branchTag,
						arguments,
						null,
						Policy.subMonitorFor(monitor, 40));
	
				}
				if (status.getCode() == CVSStatus.SERVER_ERROR) {
					throw new CVSServerException(status);
				}
			} finally {
				s.close();
			}
			
			// Set the tag of the local resources to the branch tag (The update command will not
			// properly update "cvs added" and "cvs removed" resources so a custom visitor is used
			if (moveToBranch) {
				if (eclipseWay) {
					setTag(resources, branchTag, Policy.subMonitorFor(monitor, 20));
				} else {
					update(resources, Command.NO_LOCAL_OPTIONS, branchTag, true /*createBackups*/, Policy.subMonitorFor(monitor, 20));
				}
			}
		} finally {
			monitor.done();
		}
	}
	
	/**
	 * Update the sync info of the local resource associated with the sync element such that
	 * the revision of the local resource matches that of the remote resource.
	 * This will allow commits on the local resource to succeed.
	 * 
	 * Only file resources can be merged.
	 */
	public void merged(IRemoteSyncElement[] elements) throws TeamException {	
		for (int i=0;i<elements.length;i++) {
			((CVSRemoteSyncElement)elements[i]).makeOutgoing(Policy.monitorFor(null));
		}
	}
	
	/**
	 * @see ITeamProvider#move(IResource, IPath, IProgressMonitor)
	 */
	public void moved(IPath source, IResource resource, IProgressMonitor progress) throws TeamException {
	}

	/**
	 * Set the comment to be used on the next checkin
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}
		
	/**
	 * Set the connection method for the given resource's
	 * project. If the conection method name is invalid (i.e.
	 * no corresponding registered connection method), false is returned.
	 */
	public boolean setConnectionInfo(IResource resource, String methodName, IUserInfo userInfo, IProgressMonitor monitor) throws TeamException {
		checkIsChild(resource);
		try {
			monitor.beginTask(Policy.bind("CVSTeamProvider.connectionInfo", project.getName()), 100); //$NON-NLS-1$
			
			if (!CVSRepositoryLocation.validateConnectionMethod(methodName))
				return false;
				
			// Get the original location
			ICVSRepositoryLocation location = workspaceRoot.getRemoteLocation();
			
			// Make a copy to work on
			CVSRepositoryLocation newLocation = CVSRepositoryLocation.fromString(location.getLocation());
			newLocation.setMethod(methodName);
			newLocation.setUserInfo(userInfo);
	
			// Validate that a connection can be made with the new location
			try {
				newLocation.validateConnection(Policy.subMonitorFor(monitor, 20));
			} catch (CVSException e) {
				// XXX We should really only do this if it didn't exist previously
				CVSProviderPlugin.getProvider().disposeRepository(newLocation);
				throw e;
			}
			
			// Add the location to the provider
			CVSProvider.getInstance().addRepository(newLocation);
			
			// Set the project to use the new Locations
			setRemoteRoot(newLocation, Policy.subMonitorFor(monitor, 80));
			return true;
		} finally {
			monitor.done();
		}
	}
	
	/*
	 * This method sets the tag for a project.
	 * It expects to be passed an InfiniteSubProgressMonitor
	 */
	private void setTag(final IResource[] resources, final CVSTag tag, IProgressMonitor monitor) throws TeamException {
	
		workspaceRoot.getLocalRoot().run(new ICVSRunnable() {
			public void run(IProgressMonitor progress) throws CVSException {
				try {
					// 512 ticks gives us a maximum of 2048 which seems reasonable for folders and files in a project
					progress.beginTask(null, 100);
					final IProgressMonitor monitor = Policy.infiniteSubMonitorFor(progress, 100);
					monitor.beginTask(Policy.bind("CVSTeamProvider.folderInfo", project.getName()), 512);  //$NON-NLS-1$
					
					// Visit all the children folders in order to set the root in the folder sync info
					for (int i = 0; i < resources.length; i++) {
						CVSWorkspaceRoot.getCVSResourceFor(resources[i]).accept(new ICVSResourceVisitor() {
							public void visitFile(ICVSFile file) throws CVSException {
								monitor.worked(1);
								ResourceSyncInfo info = file.getSyncInfo();
								if (info != null) {
									monitor.subTask(Policy.bind("CVSTeamProvider.updatingFile", info.getName())); //$NON-NLS-1$
									MutableResourceSyncInfo newInfo = info.cloneMutable();
									newInfo.setTag(tag);
									file.setSyncInfo(newInfo);
								}
							};
							public void visitFolder(ICVSFolder folder) throws CVSException {
								monitor.worked(1);
								FolderSyncInfo info = folder.getFolderSyncInfo();
								if (info != null) {
									monitor.subTask(Policy.bind("CVSTeamProvider.updatingFolder", info.getRepository())); //$NON-NLS-1$
									folder.setFolderSyncInfo(new FolderSyncInfo(info.getRepository(), info.getRoot(), tag, info.getIsStatic()));
									folder.acceptChildren(this);
								}
							};
						});
					}
				} finally {
					progress.done();
				}
			}
		}, monitor);
	}
	
	/** 
	 * Tag the resources in the CVS repository with the given tag.
	 * 
	 * The returned IStatus will be a status containing any errors or warnings.
	 * If the returned IStatus is a multi-status, the code indicates the severity.
	 * Possible codes are:
	 *    CVSStatus.OK - Nothing to report
	 *    CVSStatus.SERVER_ERROR - The server reported an error
	 *    any other code - warning messages received from the server
	 */
	public IStatus tag(IResource[] resources, int depth, CVSTag tag, IProgressMonitor progress) throws CVSException {
						
		// Build the local options
		List localOptions = new ArrayList();
		// If the depth is not infinite, we want the -l option
		if (depth != IResource.DEPTH_INFINITE)
			localOptions.add(Tag.DO_NOT_RECURSE);
		LocalOption[] commandOptions = (LocalOption[])localOptions.toArray(new LocalOption[localOptions.size()]);
				
		// Build the arguments list
		String[] arguments = getValidArguments(resources, commandOptions);

		// Execute the command
		Session s = new Session(workspaceRoot.getRemoteLocation(), workspaceRoot.getLocalRoot());
		progress.beginTask(null, 100);
		try {
			// Opening the session takes 20% of the time
			s.open(Policy.subMonitorFor(progress, 20));
			return Command.TAG.execute(s,
				Command.NO_GLOBAL_OPTIONS,
				commandOptions,
				tag,
				arguments,
				null,
				Policy.subMonitorFor(progress, 80));
		} finally {
			s.close();
			progress.done();
		}
	}
	
	/**
	 * Currently, we support only the optimistic model so uncheckout dores nothing.
	 * 
	 * @see ITeamProvider#uncheckout(IResource[], int, IProgressMonitor)
	 */
	public void uncheckout(IResource[] resources, int depth, IProgressMonitor progress) throws TeamException {
	}
	
	/**
	 * Generally useful update.
	 * 
	 * The tag parameter determines any stickyness after the update is run. If tag is null, any tagging on the
	 * resources being updated remain the same. If the tag is a branch, version or date tag, then the resources
	 * will be appropriatly tagged. If the tag is HEAD, then there will be no tag on the resources (same as -A
	 * clear sticky option).
	 * 
	 * @param createBackups if true, creates .# files for updated files
	 */
	public void update(IResource[] resources, LocalOption[] options, CVSTag tag, boolean createBackups, IProgressMonitor progress) throws TeamException {
		// Build the local options
		List localOptions = new ArrayList();
		
		// Use the appropriate tag options
		if (tag != null) {
			localOptions.add(Update.makeTagOption(tag));
		}
		
		// Build the arguments list
		localOptions.addAll(Arrays.asList(options));
		LocalOption[] commandOptions = (LocalOption[])localOptions.toArray(new LocalOption[localOptions.size()]);
		String[] arguments = getValidArguments(resources, commandOptions);

		IStatus status;
		Session s = new Session(workspaceRoot.getRemoteLocation(), workspaceRoot.getLocalRoot());
		progress.beginTask(null, 100);
		try {
			// Opening the session takes 20% of the time
			s.open(Policy.subMonitorFor(progress, 20));
			status = Command.UPDATE.execute(s, Command.NO_GLOBAL_OPTIONS, commandOptions, arguments,
				null, Policy.subMonitorFor(progress, 80), createBackups);
		} finally {
			progress.done();
			s.close();
		}
		if (status.getCode() == CVSStatus.SERVER_ERROR) {
			// XXX diff errors??
			throw new CVSServerException(status);
		}
	}
		
	public static String getMessageFor(Exception e) {
		String message = Policy.bind(e.getClass().getName(), new Object[] {e.getMessage()});
		if (message.equals(e.getClass().getName()))
			message = Policy.bind("CVSTeamProvider.exception", new Object[] {e.toString()}); //$NON-NLS-1$
		return message;
	}
	
	
	/*
	 * @see ITeamProvider#refreshState(IResource[], int, IProgressMonitor)
	 */
	public void refreshState(IResource[] resources, int depth, IProgressMonitor progress) throws TeamException {
		Assert.isTrue(false);
	}
	/*
	 * @see ITeamProvider#isOutOfDate(IResource)
	 * XXX to be removed when sync methods are removed from ITeamProvider
	 */
	public boolean isOutOfDate(IResource resource) {
		Assert.isTrue(false);
		return false;
	}
	
	/*
	 * @see ITeamProvider#isDirty(IResource)
	 */
	public boolean isDirty(IResource resource) {
		Assert.isTrue(false);
		return false;
	}
	
	public CVSWorkspaceRoot getCVSWorkspaceRoot() {
		return workspaceRoot;
	}
	
	/*
	 * Generate an exception if the resource is not a child of the project
	 */
	 private void checkIsChild(IResource resource) throws CVSException {
	 	if (!isChildResource(resource))
	 		throw new CVSException(new Status(IStatus.ERROR, CVSProviderPlugin.ID, TeamException.UNABLE, 
	 			Policy.bind("CVSTeamProvider.invalidResource", //$NON-NLS-1$
	 				new Object[] {resource.getFullPath().toString(), project.getName()}), 
	 			null));
	 }
	 
	/*
	 * Get the arguments to be passed to a commit or update
	 */
	private String[] getValidArguments(IResource[] resources, LocalOption[] options) throws CVSException {
		List arguments = new ArrayList(resources.length);
		for (int i=0;i<resources.length;i++) {
			checkIsChild(resources[i]);
			IPath cvsPath = resources[i].getFullPath().removeFirstSegments(1);
			if (cvsPath.segmentCount() == 0) {
				arguments.add(Session.CURRENT_LOCAL_FOLDER);
			} else {
				arguments.add(cvsPath.toString());
			}
		}
		return (String[])arguments.toArray(new String[arguments.size()]);
	}
	
	/*
	 * This method expects to be passed an InfiniteSubProgressMonitor
	 */
	public void setRemoteRoot(ICVSRepositoryLocation location, IProgressMonitor monitor) throws TeamException {

		// Check if there is a differnece between the new and old roots	
		final String root = location.getLocation();
		if (root.equals(workspaceRoot.getRemoteLocation())) 
			return;
	
		try {
			workspaceRoot.getLocalRoot().run(new ICVSRunnable() {
				public void run(IProgressMonitor progress) throws CVSException {
					try {
						// 256 ticks gives us a maximum of 1024 which seems reasonable for folders is a project
						progress.beginTask(null, 100);
						final IProgressMonitor monitor = Policy.infiniteSubMonitorFor(progress, 100);
						monitor.beginTask(Policy.bind("CVSTeamProvider.folderInfo", project.getName()), 256);  //$NON-NLS-1$
		
						// Visit all the children folders in order to set the root in the folder sync info
						workspaceRoot.getLocalRoot().accept(new ICVSResourceVisitor() {
							public void visitFile(ICVSFile file) throws CVSException {};
							public void visitFolder(ICVSFolder folder) throws CVSException {
								monitor.worked(1);
								FolderSyncInfo info = folder.getFolderSyncInfo();
								if (info != null) {
									monitor.subTask(Policy.bind("CVSTeamProvider.updatingFolder", info.getRepository())); //$NON-NLS-1$
									folder.setFolderSyncInfo(new FolderSyncInfo(info.getRepository(), root, info.getTag(), info.getIsStatic()));
									folder.acceptChildren(this);
								}
							};
						});
					} finally {
						progress.done();
					}
				}
			}, monitor);
		} finally {
			monitor.done();
		}
	}
	
	/*
	 * Helper to indicate if the resource is a child of the receiver's project
	 */
	private boolean isChildResource(IResource resource) {
		return resource.getProject().getName().equals(project.getName());
	}
	
	private static TeamException wrapException(CoreException e) {
		return new TeamException(statusFor(e));
	}
	
	private static IStatus statusFor(CoreException e) {
		// We should be taking out any status from the CVSException
		// and creating an array of IStatus!
		return new Status(IStatus.ERROR, CVSProviderPlugin.ID, TeamException.UNABLE, getMessageFor(e), e);
	}
	
	public void configureProject() throws CoreException {
		CVSProviderPlugin.broadcastProjectConfigured(getProject());
	}
	/**
	 * Sets the keyword substitution mode for the specified resources.
	 * <p>
	 * Applies the following rules in order:<br>
	 * <ul>
	 *   <li>If a file is not managed, skips it.</li>
	 *   <li>If a file is not changing modes, skips it.</li>
	 *   <li>If a file is being changed from binary to text, corrects line delimiters
	 *       then commits it, then admins it.</li>
	 *   <li>If a file is added, changes the resource sync information locally.</li>
	 *   <li>Otherwise commits the file (with FORCE to create a new revision), then admins it.</li>
	 * </ul>
	 * All files that are admin'd are committed with FORCE to prevent other developers from
	 * casually trying to commit pending changes to the repository without first checking out
	 * a new copy.  This is not a perfect solution, as they could just as easily do an UPDATE
	 * and not obtain the new keyword sync info.
	 * </p>
	 * 
	 * @param changeSet a map from IFile to KSubstOption
	 * @param monitor the progress monitor
	 * @return a status code indicating success or failure of the operation
	 * 
	 * @throws TeamException
	 */
	public IStatus setKeywordSubstitution(final Map /* from IFile to KSubstOption */ changeSet,
		IProgressMonitor monitor) throws TeamException {
		final IStatus[] result = new IStatus[] { ICommandOutputListener.OK };
		workspaceRoot.getLocalRoot().run(new ICVSRunnable() {
			public void run(final IProgressMonitor monitor) throws CVSException {
				final Map /* from KSubstOption to List of String */ filesToAdmin = new HashMap();
				final List /* of String */ filesToCommit = new ArrayList();
				final Collection /* of ICVSFile */ filesToCommitAsText = new HashSet(); // need fast lookup
		
				/*** determine the resources to be committed and/or admin'd ***/
				for (Iterator it = changeSet.entrySet().iterator(); it.hasNext();) {
					Map.Entry entry = (Map.Entry) it.next();
					IFile file = (IFile) entry.getKey();
					KSubstOption toKSubst = (KSubstOption) entry.getValue();

					// only set keyword substitution if resource is a managed file
					checkIsChild(file);
					ICVSFile mFile = CVSWorkspaceRoot.getCVSFileFor(file);
					if (! mFile.isManaged()) continue;
					
					// only set keyword substitution if new differs from actual
					ResourceSyncInfo info = mFile.getSyncInfo();
					KSubstOption fromKSubst = info.getKeywordMode();
					if (toKSubst.equals(fromKSubst)) continue;
					
					// change resource sync info immediately for an outgoing addition
					if (info.isAdded()) {
						MutableResourceSyncInfo newInfo = info.cloneMutable();
						newInfo.setKeywordMode(toKSubst);
						mFile.setSyncInfo(newInfo);
						continue;
					}

					// nothing do to for deletions
					if (info.isDeleted()) continue;

					// file exists remotely so we'll have to commit it
					String remotePath = mFile.getRelativePath(workspaceRoot.getLocalRoot());
					if (fromKSubst.isBinary() && ! toKSubst.isBinary()) {
						// converting from binary to text
						cleanLineDelimiters(file, IS_CRLF_PLATFORM, new NullProgressMonitor()); // XXX need better progress monitoring
						// remember to commit the cleaned resource as text before admin
						filesToCommitAsText.add(mFile);
					}
					// force a commit to bump the revision number
					makeDirty(file);
					filesToCommit.add(remotePath);
					// remember to admin the resource
					List list = (List) filesToAdmin.get(toKSubst);
					if (list == null) {
						list = new ArrayList();
						filesToAdmin.put(toKSubst, list);
					}
					list.add(remotePath);
				}
			
				/*** commit then admin the resources ***/
				// compute the total work to be performed
				int totalWork = filesToCommit.size();
				for (Iterator it = filesToAdmin.values().iterator(); it.hasNext();) {
					List list = (List) it.next();
					totalWork += list.size();
				}
				if (totalWork != 0) {
					Session s = new Session(workspaceRoot.getRemoteLocation(), workspaceRoot.getLocalRoot());
					monitor.beginTask(Policy.bind("CVSTeamProvider.settingKSubst"), 5 + totalWork); //$NON-NLS-1$
					try {
						s.open(Policy.subMonitorFor(monitor, 5));
						
						// commit files that changed from binary to text
						// NOTE: The files are committed as text with conversions even if the
						//       resource sync info still says "binary".
						if (filesToCommit.size() != 0) {
							String keywordChangeComment = Policy.bind("CVSTeamProvider.changingKeywordComment");
							s.setTextTransferOverride(filesToCommitAsText);
							result[0] = Command.COMMIT.execute(s, Command.NO_GLOBAL_OPTIONS,
								new LocalOption[] { Commit.DO_NOT_RECURSE, Commit.FORCE,
									Commit.makeArgumentOption(Command.MESSAGE_OPTION, keywordChangeComment) },
								(String[]) filesToCommit.toArray(new String[filesToCommit.size()]),
								null, Policy.subMonitorFor(monitor, filesToCommit.size()));
							s.setTextTransferOverride(null);
							// if errors were encountered, abort
							if (! result[0].isOK()) return;
						}
						
						// admin files that changed keyword substitution mode
						// NOTE: As confirmation of the completion of a command, the server replies
						//       with the RCS command output if a change took place.  Rather than
						//       assume that the command succeeded, we listen for these lines
						//       and update the local ResourceSyncInfo for the particular files that
						//       were actually changed remotely.
						for (Iterator it = filesToAdmin.entrySet().iterator(); it.hasNext();) {
							Map.Entry entry = (Map.Entry) it.next();
							KSubstOption toKSubst = (KSubstOption) entry.getKey();
							List list = (List) entry.getValue();
							// do it
							result[0] = Command.ADMIN.execute(s, Command.NO_GLOBAL_OPTIONS,
								new LocalOption[] { toKSubst },
								(String[]) list.toArray(new String[list.size()]),
								new AdminKSubstListener(toKSubst),
								Policy.subMonitorFor(monitor, list.size()));
							// if errors were encountered, abort
							if (! result[0].isOK()) return;
						}
					} finally {
						s.close();
						monitor.done();
					}
				}
			}
		}, Policy.monitorFor(monitor));
		return result[0];
	}
	
	/**
	 * Fixes the line delimiters in the local file to reflect the platform's
	 * native encoding.  Performs CR/LF -> LF or LF -> CR/LF conversion
	 * depending on the platform but does not affect delimiters that are
	 * already correctly encoded.
	 */
	public static void cleanLineDelimiters(IFile file, boolean useCRLF, IProgressMonitor progress)
		throws CVSException {
		try {
			// convert delimiters in memory
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			InputStream is = new BufferedInputStream(file.getContents());
			try {
				is = new CRLFtoLFInputStream(is);
				if (useCRLF) is = new LFtoCRLFInputStream(is);
				for (int b; (b = is.read()) != -1;) bos.write(b);
				bos.close();
			} finally {
				is.close();
			}
			// write file back to disk with corrected delimiters if changes were made
			ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
			file.setContents(bis, false /*force*/, false /*keepHistory*/, progress);
		} catch (CoreException e) {
			throw CVSException.wrapException(file, Policy.bind("CVSTeamProvider.cleanLineDelimitersException"), e); //$NON-NLS-1$
		} catch (IOException e) {
			throw CVSException.wrapException(file, Policy.bind("CVSTeamProvider.cleanLineDelimitersException"), e); //$NON-NLS-1$
		}
	}
	
	/*
	 * Marks a file as dirty.
	 */
	private static void makeDirty(IFile file) throws CVSException {
		ICVSFile mFile = CVSWorkspaceRoot.getCVSFileFor(file);
		mFile.setTimeStamp(null /*set the timestamp to current time*/);
	}
	
	/*
	 * @see RepositoryProvider#getID()
	 */
	public String getID() {
		return CVSProviderPlugin.getTypeId();
	}
	
	/*
	 * @see RepositoryProvider#getMoveDeleteHook()
	 */
	public IMoveDeleteHook getMoveDeleteHook() {
		return moveDeleteHook;
	}
	
	/*
	 * Return the currently registered Move/Delete Hook
	 */
	public static IMoveDeleteHook getRegisteredMoveDeleteHook() {
		return moveDeleteHook;
	}
	
	/*
	 * Set the Move/Delete hook of the CVS Team Provider. This is for internal use by CVS only. 
	 * It is not to be used by other clients
	 */
	 public static void setMoveDeleteHook(IMoveDeleteHook hook) {
	 	moveDeleteHook = hook;
	 }
}