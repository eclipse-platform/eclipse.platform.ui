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
package org.eclipse.team.internal.ccvs.core.resources;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSStatus;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.ICVSRunnable;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.client.Checkout;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Import;
import org.eclipse.team.internal.ccvs.core.client.Request;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.internal.ccvs.core.client.Update;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.connection.CVSServerException;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;

/**
 * This class provides static methods for checking out projects from a repository
 * into the local workspace and for converting IResources into CVSRespources
 * and sync trees.
 * Instances of this class represent a local workspace root (i.e. a project).
 */
public class CVSWorkspaceRoot {

	private ICVSFolder localRoot;
	
	public CVSWorkspaceRoot(IContainer resource){
		this.localRoot = getCVSFolderFor(resource);
	}

	/**
	 * Checkout a CVS module.
	 * 
	 * The provided project represents the target project. Any existing contents
	 * may or may not get overwritten. If project is <code>null</code> then a project
	 * will be created based on the provided sourceModule. If soureModule is null, 
	 * then the project name will be used as the module to
	 * check out. If both are absent, an exception is thrown.
	 * 
	 * Resources existing in the local file system at the target project location but now 
	 * known to the workbench will be overwritten.
	 * 
	 * After the successful completion of this method, the project will exist
	 * and be open.
	 */
	public static void checkout(
		ICVSRepositoryLocation repository,
		IProject project,
		String sourceModule,
		CVSTag tag,
		IProgressMonitor monitor)
		throws TeamException {
		
		if (sourceModule == null)
			sourceModule = project.getName();
		checkout(new ICVSRemoteFolder[] { new RemoteFolder(null, repository, sourceModule, tag)},
			new IProject[] { project }, monitor);
	}

	/**
	 * Checkout the remote resources into the local workspace. Each resource will 
	 * be checked out into the corresponding project. If the corresponding project is
	 * null or if projects is null, the name of the remote resource is used as the name of the project.
	 * 
	 * Resources existing in the local file system at the target project location but now 
	 * known to the workbench will be overwritten.
	 */
	public static void checkout(final ICVSRemoteFolder[] resources, final IProject[] projects, final IProgressMonitor monitor) throws TeamException {
		final TeamException[] eHolder = new TeamException[1];
		try {
			IWorkspaceRunnable workspaceRunnable = new IWorkspaceRunnable() {
				public void run(IProgressMonitor pm) throws CoreException {
					try {
						pm.beginTask(null, 1000 * resources.length);
						
						// Get the location of the workspace root
						ICVSFolder root = CVSWorkspaceRoot.getCVSFolderFor(ResourcesPlugin.getWorkspace().getRoot());
						
						for (int i=0;i<resources.length;i++) {
							IProject project = null;
							RemoteFolder resource = (RemoteFolder)resources[i];
							
							// Determine the provided target project if there is one
							if (projects != null) 
								project = projects[i];
							
							// Determine the remote module to be checked out
							String moduleName;
							if (resource instanceof RemoteModule) {
								moduleName = ((RemoteModule)resource).getName();
							} else {
								moduleName = resource.getRepositoryRelativePath();
							}
							
							// Open a connection session to the repository
							ICVSRepositoryLocation repository = resource.getRepository();
							Session session = new Session(repository, root);
							try {
								session.open(Policy.subMonitorFor(pm, 50));
								
								// Determine the local target projects (either the project provider or the module expansions) 
								final Set targetProjects = new HashSet();
								if (project == null) {
									
									// Fetch the module expansions
									IStatus status = Request.EXPAND_MODULES.execute(session, new String[] {moduleName}, Policy.subMonitorFor(pm, 50));
									if (status.getCode() == CVSStatus.SERVER_ERROR) {
										throw new CVSServerException(status);
									}
									
									// Convert the module expansions to local projects
									String[] expansions = session.getModuleExpansions();
									for (int j = 0; j < expansions.length; j++) {
										targetProjects.add(ResourcesPlugin.getWorkspace().getRoot().getProject(new Path(expansions[j]).segment(0)));
									}
									
								} else {
									targetProjects.add(project);
								}
								
								// Prepare the target projects to receive resources
								root.run(new ICVSRunnable() {
									public void run(IProgressMonitor monitor) throws CVSException {
										scrubProjects((IProject[]) targetProjects.toArray(new IProject[targetProjects.size()]), monitor);
									}
								}, Policy.subMonitorFor(pm, 100));
							
								// Build the local options
								List localOptions = new ArrayList();
								// Add the option to load into the target project if one was supplied
								if (project != null) {
									localOptions.add(Checkout.makeDirectoryNameOption(project.getName()));
								}
								// Prune empty directories if pruning enabled
								if (CVSProviderPlugin.getPlugin().getPruneEmptyDirectories()) 
									localOptions.add(Checkout.PRUNE_EMPTY_DIRECTORIES);
								// Add the options related to the CVSTag
								CVSTag tag = resource.getTag();
								if (tag == null) {
									// A null tag in a remote resource indicates HEAD
									tag = CVSTag.DEFAULT;
								}
								localOptions.add(Update.makeTagOption(tag));
		
								// Perform the checkout
								IStatus status = Command.CHECKOUT.execute(session,
									Command.NO_GLOBAL_OPTIONS,
									(LocalOption[])localOptions.toArray(new LocalOption[localOptions.size()]),
									new String[]{moduleName},
									null,
									Policy.subMonitorFor(pm, 800));
								if (status.getCode() == CVSStatus.SERVER_ERROR) {
									// XXX Should we cleanup any partially checked out projects?
									throw new CVSServerException(status);
								}
								
								// Bring the project into the workspace
								refreshProjects((IProject[])targetProjects.toArray(new IProject[targetProjects.size()]), Policy.subMonitorFor(pm, 100));

							} finally {
								session.close();
							}
						}
					}
					catch (TeamException e) {
						// Pass it outside the workspace runnable
						eHolder[0] = e;
					} finally {
						pm.done();
					}
					// CoreException and OperationCanceledException are propagated
				}
			};
			ResourcesPlugin.getWorkspace().run(workspaceRunnable, monitor);
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		} finally {
			monitor.done();
		}		
		// Re-throw the TeamException, if one occurred
		if (eHolder[0] != null) {
			throw eHolder[0];
		}
	}

	/**
	 * Checkout the remote resources into the provided local folder.
	 */
	public static void checkout(final ICVSRemoteFolder resource, final IContainer target, final boolean configure, final boolean recurse, final IProgressMonitor monitor) throws TeamException {
		final TeamException[] eHolder = new TeamException[1];
		try {
			IWorkspaceRunnable workspaceRunnable = new IWorkspaceRunnable() {
				public void run(IProgressMonitor pm) throws CoreException {
					try {
						pm.beginTask(null, 1000);
						
						final ICVSFolder cvsFolder = CVSWorkspaceRoot.getCVSFolderFor(target);
						
						// Delete the target folder if it exists.
						String folderName = cvsFolder.getName();
						if (cvsFolder.exists()) {
							// Unmanage first so we don't get outgoing deletions
							cvsFolder.unmanage(Policy.subMonitorFor(pm, 50));
							cvsFolder.delete();
						}
						
						// Prepare the target to receive the contents of the directory
						cvsFolder.mkdir();
						cvsFolder.setFolderSyncInfo(resource.getFolderSyncInfo());
						
						// Open a connection session to the repository
						ICVSRepositoryLocation repository = resource.getRepository();
						Session.run(repository, cvsFolder, true, new ICVSRunnable() {
							public void run(IProgressMonitor monitor) throws CVSException {
								List localOptions = new ArrayList();
								// Add recurse option
								if (!recurse)
									localOptions.add(Update.DO_NOT_RECURSE);
								// Perform the checkout using the update command
								// We use update to avoid communicating info about the parent
								// directory which may or may not be shared with CVS
								IStatus status = Command.UPDATE.execute(
									Command.NO_GLOBAL_OPTIONS,
									(LocalOption[])localOptions.toArray(new LocalOption[localOptions.size()]),
									new ICVSResource[]{ cvsFolder },
									null,
									monitor);
								if (status.getCode() == CVSStatus.SERVER_ERROR) {
									throw new CVSServerException(status);
								}
							}
						}, Policy.subMonitorFor(pm, 800));
						if (configure) {
							manageFolder(cvsFolder, repository.getLocation());
							refreshProjects(new IProject[] { target.getProject() }, Policy.subMonitorFor(pm, 50));
						} else {
							cvsFolder.unmanage(Policy.subMonitorFor(pm, 50));
						}
					}
					catch (TeamException e) {
						// Pass it outside the workspace runnable
						eHolder[0] = e;
					} finally {
						pm.done();
					}
					// CoreException and OperationCanceledException are propagated
				}
			};
			ResourcesPlugin.getWorkspace().run(workspaceRunnable, monitor);
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		} finally {
			monitor.done();
		}		
		// Re-throw the TeamException, if one occurred
		if (eHolder[0] != null) {
			throw eHolder[0];
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
					
	/**
	 * Create a remote module in the CVS repository and link the project directory to this remote module.
	 * The contents of the project are not imported.
	 */
	public static void createModule(final ICVSRepositoryLocation location, final IProject project, String moduleName, IProgressMonitor monitor) throws TeamException {
		
		// Determine if the repository is known
		boolean alreadyExists = CVSProviderPlugin.getPlugin().isKnownRepository(location.getLocation());
		// Set the folder sync info of the project to point to the remote module
		final ICVSFolder folder = (ICVSFolder)CVSWorkspaceRoot.getCVSResourceFor(project);
			
		try {
			// Get the import properties
			String message = Policy.bind("CVSProvider.initialImport"); //$NON-NLS-1$
			String vendor = "vendor"; //$NON-NLS-1$
			String tag = "start"; //$NON-NLS-1$
			String projectName = project.getName();
			if (moduleName == null)
				moduleName = projectName;

			// Perform the import using a dummy root so the local project is not traversed
			Session s = new Session(location, new RemoteFolderTree(null, location, Path.EMPTY.toString(), null));
			s.open(monitor);
			try {
				IStatus status = Command.IMPORT.execute(s,
					Command.NO_GLOBAL_OPTIONS,
					new LocalOption[] {Import.makeArgumentOption(Command.MESSAGE_OPTION, message)},
					new String[] { moduleName, vendor, tag },
					null,
					monitor);
				// If we get a warning, the operation most likely failed so check that the status is OK
				if (status.getCode() == CVSStatus.SERVER_ERROR  || ! status.isOK()) {
					throw new CVSServerException(status);
				}
			} finally {
				s.close();
			}
			
			// perform the workspace modifications in a runnable
			try {
				final TeamException[] exception = new TeamException[] {null};
				final String modName = moduleName;
				ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
					public void run(IProgressMonitor monitor) throws CoreException {
						try {
							// Link the project to the newly created module
							folder.setFolderSyncInfo(new FolderSyncInfo(modName, location.getLocation(), null, false));
							//Register it with Team.  If it already is, no harm done.
							RepositoryProvider.map(project, CVSProviderPlugin.getTypeId());
						} catch (TeamException e) {
							exception[0] = e;
						}
					}
				}, monitor);
				if (exception[0] != null)
					throw exception[0];
			} catch (CoreException e) {
				throw CVSException.wrapException(e);
			}
		} catch (TeamException e) {
			// The checkout may have triggered password caching
			// Therefore, if this is a newly created location, we want to clear its cache
			if ( ! alreadyExists)
				CVSProviderPlugin.getPlugin().disposeRepository(location);
			throw e;
		}
		// Add the repository if it didn't exist already
		if ( ! alreadyExists)
			CVSProviderPlugin.getPlugin().addRepository(location);
	}
	
	/**
	 * Set the sharing for a project to enable it to be used with the CVSTeamProvider.
	 * This method ensure that the repository in the FolderSyncInfo is known and that
	 * the project is mapped to a CVS repository provider. It does not modify the sync
	 * info associated with the project's resources in any way.
	 */
	public static void setSharing(IProject project, FolderSyncInfo info, IProgressMonitor monitor) throws TeamException {
		
		// Ensure provided info matches that of the project
		ICVSFolder folder = (ICVSFolder)CVSWorkspaceRoot.getCVSResourceFor(project);
		FolderSyncInfo folderInfo = folder.getFolderSyncInfo();
		if ( ! info.equals(folderInfo)) {
			throw new CVSException(new CVSStatus(CVSStatus.ERROR, Policy.bind("CVSProvider.infoMismatch", project.getName())));//$NON-NLS-1$
		}
		
		// Ensure that the provided location is managed
		ICVSRepositoryLocation location = CVSProviderPlugin.getPlugin().getRepository(info.getRoot());
		
		// Register the project with Team
		RepositoryProvider.map(project, CVSProviderPlugin.getTypeId());
	}
	
	/**
	 * Answer the list of directories that a checkout of the given resources would expand to.
	 * In other words, the returned strings represent the root paths that the given resources would 
	 * be loaded into.
	 */
	public static String[] getExpansions(ICVSRemoteFolder[] resources, IProgressMonitor monitor) throws CVSException {
		
		if (resources.length == 0) return new String[0];
		
		// Get the location of the workspace root
		ICVSFolder root = CVSWorkspaceRoot.getCVSFolderFor(ResourcesPlugin.getWorkspace().getRoot());
		
		// Get the command arguments
		String[] arguments = new String[resources.length];
		for (int i = 0; i < resources.length; i++) {
			if (resources[i] instanceof RemoteModule) {
				arguments[i] = ((RemoteModule)resources[i]).getName();
			} else {
				arguments[i]  = resources[i].getRepositoryRelativePath();
			}
		}
		
		// Perform the Expand-Modules command
		IStatus status;
		Session s = new Session(resources[0].getRepository(), root);
		s.open(monitor);
		try {
			status = Request.EXPAND_MODULES.execute(s, arguments, monitor);
		} finally {
			s.close();
		}
		if (status.getCode() == CVSStatus.SERVER_ERROR) {
			throw new CVSServerException(status);
		}
		
		return s.getModuleExpansions();
	}

	/*
	 * Delete the target projects before checking out
	 */
	private static void scrubProjects(IProject[] projects, IProgressMonitor monitor) throws CVSException {
		if (projects == null) {
			monitor.done();
			return;
		}
		monitor.beginTask(Policy.bind("CVSProvider.Scrubbing_projects_1"), projects.length * 100); //$NON-NLS-1$
		try {	
			for (int i=0;i<projects.length;i++) {
				IProject project = projects[i];
				if (project != null && project.exists()) {
					if(!project.isOpen()) {
						project.open(Policy.subMonitorFor(monitor, 10));
					}
					// We do not want to delete the project to avoid a project deletion delta
					// We do not want to delete the .project to avoid core exceptions
					monitor.subTask(Policy.bind("CVSProvider.Scrubbing_local_project_1")); //$NON-NLS-1$
					// unmap the project from any previous repository provider
					if (RepositoryProvider.getProvider(project) != null)
						RepositoryProvider.unmap(project);
					IResource[] children = project.members(IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS);
					IProgressMonitor subMonitor = Policy.subMonitorFor(monitor, 80);
					subMonitor.beginTask(null, children.length * 100);
					try {
						for (int j = 0; j < children.length; j++) {
							if ( ! children[j].getName().equals(".project")) {//$NON-NLS-1$
								children[j].delete(true /*force*/, Policy.subMonitorFor(subMonitor, 100));
							}
						}
					} finally {
						subMonitor.done();
					}
				} else if (project != null) {
					// Make sure there is no directory in the local file system.
					File location = new File(project.getParent().getLocation().toFile(), project.getName());
					if (location.exists()) {
						deepDelete(location);
					}
				}
			}
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		} catch (TeamException e) {
			throw CVSException.wrapException(e);
		} finally {
			monitor.done();
		}
	}
	
	private static void deepDelete(File resource) {
		if (resource.isDirectory()) {
			File[] fileList = resource.listFiles();
			for (int i = 0; i < fileList.length; i++) {
				deepDelete(fileList[i]);
			}
		}
		resource.delete();
	}
	
	/*
	 * Bring the provied projects into the workspace
	 */
	private static void refreshProjects(IProject[] projects, IProgressMonitor monitor) throws CoreException, TeamException {
		monitor.beginTask(Policy.bind("CVSProvider.Creating_projects_2"), projects.length * 100); //$NON-NLS-1$
		try {
			for (int i = 0; i < projects.length; i++) {
				IProject project = projects[i];
				// Register the project with Team
				RepositoryProvider.map(project, CVSProviderPlugin.getTypeId());
				CVSTeamProvider provider = (CVSTeamProvider)RepositoryProvider.getProvider(project, CVSProviderPlugin.getTypeId());
				provider.setWatchEditEnabled(CVSProviderPlugin.getPlugin().isWatchEditEnabled());
			}
			
		} finally {
			monitor.done();
		}
	}
					
	public static ICVSFolder getCVSFolderFor(IContainer resource) {
		return new EclipseFolder(resource);
	}


	public static ICVSFile getCVSFileFor(IFile resource) {
		return new EclipseFile(resource);
	}


	public static ICVSResource getCVSResourceFor(IResource resource) {
		if (resource.getType() == IResource.FILE)
			return getCVSFileFor((IFile) resource);
		else
			return getCVSFolderFor((IContainer) resource);
	}
	
	public static ICVSRemoteResource getRemoteResourceFor(IResource resource) throws CVSException {
		ICVSResource managed = getCVSResourceFor(resource);
		return getRemoteResourceFor(managed);
	}
	
	public static ICVSRemoteResource getRemoteResourceFor(ICVSResource resource) throws CVSException {
		if (resource.isFolder()) {
			ICVSFolder folder = (ICVSFolder)resource;
			FolderSyncInfo syncInfo = folder.getFolderSyncInfo();
			if (syncInfo != null) {
				return new RemoteFolder(null, CVSProviderPlugin.getPlugin().getRepository(syncInfo.getRoot()), syncInfo.getRepository(), syncInfo.getTag());
			}
		} else {
			if (resource.isManaged())
				return RemoteFile.getBase((RemoteFolder)getRemoteResourceFor(resource.getParent()), (ICVSFile)resource);
		}
		return null;
	}
	
	/*
	 * Helper method that uses the parent of a local resource that has no base to ensure that the resource
	 * wasn't added remotely by a third party
	 */
	private static ICVSRemoteResource getRemoteTreeFromParent(IResource resource, ICVSResource managed, CVSTag tag, IProgressMonitor progress) throws TeamException {
		// If the parent isn't mapped to CVS, there's nothing we can do
		ICVSFolder parent = managed.getParent();
		FolderSyncInfo syncInfo = parent.getFolderSyncInfo();
		if (syncInfo == null) {
			throw new CVSException(new CVSStatus(CVSStatus.ERROR, Policy.bind("CVSTeamProvider.unmanagedParent", resource.getFullPath().toString()), null)); //$NON-NLS-1$
		}
		ICVSRepositoryLocation location = CVSProviderPlugin.getPlugin().getRepository(parent.getFolderSyncInfo().getRoot());
		RemoteFolder remoteParent = RemoteFolderTreeBuilder.buildRemoteTree((CVSRepositoryLocation)location, parent, tag, progress);
		ICVSRemoteResource remote = null;
		if (remoteParent != null) {
			try {
				remote = (ICVSRemoteResource)remoteParent.getChild(resource.getName());
			} catch (CVSException e) {
				remote = null;
			}
			// The types need to match or we're in trouble
			if (remote != null && !(remote.isContainer() == managed.isFolder()))
				throw new CVSException(new CVSStatus(CVSStatus.ERROR, Policy.bind("CVSTeamProvider.typesDiffer", resource.getFullPath().toString()), null)); //$NON-NLS-1$
		}
		return remote;
	}
	
	public static IRemoteSyncElement getRemoteSyncTree(IResource resource, CVSTag tag, IProgressMonitor progress) throws TeamException {
		ICVSResource managed = CVSWorkspaceRoot.getCVSResourceFor(resource);
		ICVSRemoteResource remote = CVSWorkspaceRoot.getRemoteResourceFor(resource);
		ICVSRemoteResource baseTree = null;
		
		// The resource doesn't have a remote base. 
		// However, we still need to check to see if its been created remotely by a third party.
		if(resource.getType() == IResource.FILE) {
			baseTree = remote;
			ICVSRepositoryLocation location;
			if (remote == null) {
				// If there's no base for the file, get the repository location from the parent
				ICVSRemoteResource parent = CVSWorkspaceRoot.getRemoteResourceFor(resource.getParent());
				if (parent == null) {
					throw new CVSException(new CVSStatus(CVSStatus.ERROR, Policy.bind("CVSTeamProvider.unmanagedParent", resource.getFullPath().toString()), null)); //$NON-NLS-1$
				}
				location = parent.getRepository();
			} else {
				location = remote.getRepository();
			}
			remote = RemoteFolderTreeBuilder.buildRemoteTree((CVSRepositoryLocation)location, (ICVSFile)managed, tag, progress);
		} else {
			if (remote == null) {
				remote = getRemoteTreeFromParent(resource, managed, tag, progress);
			} else {
				ICVSRepositoryLocation location = remote.getRepository();
				baseTree = RemoteFolderTreeBuilder.buildBaseTree((CVSRepositoryLocation)location, (ICVSFolder)managed, tag, progress);
				remote = RemoteFolderTreeBuilder.buildRemoteTree((CVSRepositoryLocation)location, (ICVSFolder)managed, tag, progress);
			}
		}
		return new CVSRemoteSyncElement(true /*three way*/, resource, baseTree, remote);
	}
	
	/**
	 * Sync the given unshared project with the given repository and module.
	 */
	public static IRemoteSyncElement getRemoteSyncTree(IProject project, ICVSRepositoryLocation location, String moduleName, CVSTag tag, IProgressMonitor progress) throws TeamException {
		if (CVSWorkspaceRoot.getCVSFolderFor(project).isCVSFolder()) {
			return getRemoteSyncTree(project, tag, progress);
		} else {
			progress.beginTask(null, 100);
			RemoteFolder folder = new RemoteFolder(null, location, moduleName, tag);
			RemoteFolderTree remote = RemoteFolderTreeBuilder.buildRemoteTree((CVSRepositoryLocation)folder.getRepository(), folder, folder.getTag(), Policy.subMonitorFor(progress, 80));
			CVSRemoteSyncElement tree = new CVSRemoteSyncElement(true /*three way*/, project, null, remote);
			tree.makeFoldersInSync(Policy.subMonitorFor(progress, 10));

			RepositoryProvider.map(project, CVSProviderPlugin.getTypeId());

			progress.done();
			return tree;
		}
	}
	
	public static IRemoteSyncElement getRemoteSyncTree(IProject project, IResource[] resources, CVSTag tag, IProgressMonitor progress) throws TeamException {
		ICVSResource managed = CVSWorkspaceRoot.getCVSResourceFor(project);
		ICVSRemoteResource remote = CVSWorkspaceRoot.getRemoteResourceFor(project);
		if (remote == null) {
			return new CVSRemoteSyncElement(true /*three way*/, project, null, null);
		}
		ArrayList cvsResources = new ArrayList();
		for (int i = 0; i < resources.length; i++) {
			cvsResources.add(CVSWorkspaceRoot.getCVSResourceFor(resources[i]));
		}
		CVSRepositoryLocation location = (CVSRepositoryLocation)remote.getRepository();
		ICVSRemoteResource base = RemoteFolderTreeBuilder.buildBaseTree(location, (ICVSFolder)managed, tag, progress);
		remote = RemoteFolderTreeBuilder.buildRemoteTree(location, (ICVSFolder)managed, (ICVSResource[]) cvsResources.toArray(new ICVSResource[cvsResources.size()]), tag, progress);
		return new CVSRemoteSyncElement(true /*three way*/, project, base, remote);
	}
	
	public static ICVSRemoteResource getRemoteTree(IResource resource, CVSTag tag, IProgressMonitor progress) throws TeamException {
		ICVSResource managed = CVSWorkspaceRoot.getCVSResourceFor(resource);
		ICVSRemoteResource remote = CVSWorkspaceRoot.getRemoteResourceFor(resource);
		if (remote == null) {
			remote = getRemoteTreeFromParent(resource, managed, tag, progress);
		} else if(resource.getType() == IResource.FILE) {
			ICVSRepositoryLocation location = remote.getRepository();
			remote = RemoteFolderTreeBuilder.buildRemoteTree((CVSRepositoryLocation)location, (ICVSFile)managed, tag, progress);
		} else {
			ICVSRepositoryLocation location = remote.getRepository();
			remote = RemoteFolderTreeBuilder.buildRemoteTree((CVSRepositoryLocation)location, (ICVSFolder)managed, tag, progress);		
		}
		return remote;
	}

	public static boolean hasRemote(IResource resource) {
		try {
			ICVSResource cvsResource = getCVSResourceFor(resource);
			int type = resource.getType();
			if(type!=IResource.FILE) {
				if(type==IResource.PROJECT) {
					return ((ICVSFolder)cvsResource).isCVSFolder();
				} else {
					return cvsResource.isManaged();
				}
			} else {
				byte[] syncBytes = ((ICVSFile)cvsResource).getSyncBytes();
				if(syncBytes!=null) {
					return !ResourceSyncInfo.isAddition(syncBytes);
				} else {
					return false;
				}
			}					
		} catch(CVSException e) {
			return false;
		}
	}
	
	public ICVSRepositoryLocation getRemoteLocation() throws CVSException {
		FolderSyncInfo info = localRoot.getFolderSyncInfo();
		if (info == null) {
			throw new CVSException(Policy.bind("CVSWorkspaceRoot.notCVSFolder", localRoot.getName()));  //$NON-NLS-1$
		}
		return CVSProviderPlugin.getPlugin().getRepository(info.getRoot());
	}

	public ICVSFolder getLocalRoot() {
		return localRoot;
	}
	
	
	/**
	 * Return true if the resource is part of a link (i.e. a linked resource or
	 * one of it's children.
	 * 
	 * @param container
	 * @return boolean
	 */
	public static boolean isLinkedResource(IResource resource) {
		// check the resource directly first
		if (resource.isLinked()) return true;
		// projects and root cannot be links
		if (resource.getType() == IResource.PROJECT || resource.getType() == IResource.ROOT) {
			return false;
		}
		// look one level under the project to see if the resource is part of a link
		String linkedParentName = resource.getProjectRelativePath().segment(0);
		IFolder linkedParent = resource.getProject().getFolder(linkedParentName);
		return linkedParent.isLinked();
	}
	
	/**
	 * A resource is considered shared 
	 * @param resource
	 * @return boolean
	 */
	public static boolean isSharedWithCVS(IResource resource) throws CVSException {
		if (!resource.isAccessible()) return false;
		if(isLinkedResource(resource)) return false;
	
		if(RepositoryProvider.getProvider(resource.getProject(), CVSProviderPlugin.getTypeId()) == null) {
			return false;
		}
	
		ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource);
		if (cvsResource.isManaged()) return true;
		if (!cvsResource.exists()) return false;
		if (cvsResource.isFolder() && ((ICVSFolder) cvsResource).isCVSFolder()) return true;
		if (cvsResource.isIgnored()) return false;
		return cvsResource.getParent().isCVSFolder();
	}
}
