/**********************************************************************
 Copyright (c) 2004 Dan Rubel and others.
 All rights reserved.   This program and the accompanying materials
 are made available under the terms of the Common Public License v1.0
 which accompanies this distribution, and is available at
 http://www.eclipse.org/legal/cpl-v10.html

 Contributors:

 Dan Rubel - initial API and implementation

 **********************************************************************/

package org.eclipse.team.internal.ccvs.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.team.core.ProjectSetCapability;
import org.eclipse.team.core.ProjectSetSerializationContext;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.client.Checkout;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Request;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.internal.ccvs.core.client.Update;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.connection.CVSServerException;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFolder;
import org.eclipse.team.internal.ccvs.core.resources.RemoteModule;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.util.KnownRepositories;

/**
 * An object for serializing and deserializing
 * of references to CVS based projects.  Given a project, it can produce a
 * UTF-8 encoded String which can be stored in a file.
 * Given this String, it can load a project into the workspace.
 * 
 * @since 3.0
 */
public class CVSProjectSetCapability extends ProjectSetCapability {

	/**
	 * Override superclass implementation to return an array of project references.
	 * 
	 * @see ProjectSetSerializer#asReference(IProject[], ProjectSetSerializationContext, IProgressMonitor)
	 */
	public String[] asReference(
		IProject[] projects,
		ProjectSetSerializationContext context,
		IProgressMonitor monitor)
		throws TeamException {
		
		String[] result = new String[projects.length];
		for (int i = 0; i < projects.length; i++)
			result[i] = asReference(projects[i]);
		return result;
	}

	/**
	 * Answer a string representing the specified project
	 * 
	 * @param project the project (not <code>null</code>)
	 * @return the project reference (not <code>null</code>)
	 * @throws CVSException
	 */
	private String asReference(IProject project) throws TeamException {
		StringBuffer buffer = new StringBuffer();
		buffer.append("1.0,"); //$NON-NLS-1$
				
		CVSTeamProvider provider = (CVSTeamProvider)RepositoryProvider.getProvider(project);
		CVSWorkspaceRoot root = provider.getCVSWorkspaceRoot();
		CVSRepositoryLocation location = CVSRepositoryLocation.fromString(root.getRemoteLocation().getLocation(false));
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
		return buffer.toString();
	}

	/**
	 * Override superclass implementation to load the referenced projects into the workspace.
	 * 
	 * @see org.eclipse.team.core.ProjectSetSerializer#addToWorkspace(java.lang.String[], org.eclipse.team.core.ProjectSetSerializationContext, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IProject[] addToWorkspace(
		String[] referenceStrings,
		ProjectSetSerializationContext context,
		IProgressMonitor monitor)
		throws TeamException {
		
		monitor = Policy.monitorFor(monitor);
		Policy.checkCanceled(monitor);

		// Confirm the projects to be loaded
		Map infoMap = new HashMap(referenceStrings.length);
		IProject[] projects = asProjects(referenceStrings, infoMap);
		projects = confirmOverwrite(context, projects);
		if (projects == null)
			return new IProject[0];

		// Load the projects
		return checkout(projects, infoMap, monitor);
	}

	/**
	 * Translate the reference strings into projects to be loaded
	 * and build a mapping of project to project load information.
	 * 
	 * @param referenceStrings project references
	 * @param infoMap a mapping of project to project load information
	 * @return the projects to be loaded
	 */
	private IProject[] asProjects(String[] referenceStrings, Map infoMap) throws CVSException {
		Collection result = new ArrayList();
		for (int i = 0; i < referenceStrings.length; i++) {
			StringTokenizer tokenizer = new StringTokenizer(referenceStrings[i], ","); //$NON-NLS-1$
			String version = tokenizer.nextToken();
			// If this is a newer version, then ignore it
			if (!version.equals("1.0")) //$NON-NLS-1$
				continue;
			LoadInfo info = new LoadInfo(tokenizer);
			IProject proj = info.getProject();
			result.add(proj);
			infoMap.put(proj, info);
		}
		return (IProject[]) result.toArray(new IProject[result.size()]);
	}

	/**
	 * Checkout projects from the CVS repository
	 * 
	 * @param projects the projects to be loaded from the repository
	 * @param infoMap a mapping of project to project load information
	 * @param monitor the progress monitor (not <code>null</code>)
	 */
	private IProject[] checkout(
		IProject[] projects,
		Map infoMap,
		IProgressMonitor monitor)
		throws TeamException {
			
		monitor.beginTask("", 1000 * projects.length); //$NON-NLS-1$
		List result = new ArrayList();
		try {
			for (int i = 0; i < projects.length; i++) {
				if (monitor.isCanceled())
					break;
				IProject project = projects[i];
				LoadInfo info = (LoadInfo) infoMap.get(project);
				if (info != null && info.checkout(new SubProgressMonitor(monitor, 1000)))
					result.add(project);
			}
		}
		finally {
			monitor.done();
		}
		return (IProject[])result.toArray(new IProject[result.size()]);
	}

	/**
	 * Internal class for adding projects to the workspace 
	 */
	class LoadInfo {
		private final ICVSRepositoryLocation repositoryLocation;
		private final String module;
		private final IProject project;
		private final CVSTag tag;

		/**
		 * Construct a new instance wrappering the specified project reference
		 * 
		 * @param projRef the project reference
		 */
		LoadInfo(StringTokenizer tokenizer) throws CVSException {
			String repo = tokenizer.nextToken();
			repositoryLocation = getRepositoryLocationFromString(repo);
			module = tokenizer.nextToken();
			String projectName = tokenizer.nextToken();
			project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			if (tokenizer.hasMoreTokens()) {
				String tagName = tokenizer.nextToken();
				tag = new CVSTag(tagName, CVSTag.BRANCH);
			}
			else {
				tag = null;
			}
		}

		/**
		 * Answer the project referenced by this object.
		 * The project may or may not already exist.
		 * 
		 * @return the project (not <code>null</code>)
		 */
		private IProject getProject() {
			return project;
		}
		
		/**
		 * Checkout the project specified by this reference.
		 * 
		 * @param monitor project monitor
		 * @return true if loaded, else false
		 * @throws TeamException
		 */
		boolean checkout(IProgressMonitor monitor) throws TeamException {
			if (repositoryLocation == null)
				return false;
			CVSProjectSetCapability.checkout(
				repositoryLocation,
				project,
				module,
				tag,
				monitor);
			return true;
		}
	}

	/**
	 * Extract the CVS repository location information from the specified string
	 * 
	 * @param repo the repository location as a string
	 * @return the CVS repository information
	 * @throws CVSException
	 */
	private static ICVSRepositoryLocation getRepositoryLocationFromString(String repo) throws CVSException {
		// create the new location
		ICVSRepositoryLocation newLocation = CVSRepositoryLocation.fromString(repo);
		if (newLocation.getUsername() == null || newLocation.getUsername().length() == 0) {
			// look for an existing location that matched
			ICVSRepositoryLocation[] locations = CVSProviderPlugin.getPlugin().getKnownRepositories();
			for (int i = 0; i < locations.length; i++) {
				ICVSRepositoryLocation location = locations[i];
				if (location.getMethod() == newLocation.getMethod()
					&& location.getHost().equals(newLocation.getHost())
					&& location.getPort() == newLocation.getPort()
					&& location.getRootDirectory().equals(newLocation.getRootDirectory()))
						return location;
			}
		}
		// No existing location was found so add this location to the list of known repositories
		KnownRepositories.getInstance().addRepository(newLocation, true);
		return newLocation;
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
								session.open(Policy.subMonitorFor(pm, 50), false /* read-only */);
								
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
										targetProjects.add(ResourcesPlugin.getWorkspace().getRoot().getProject(new Path(null, expansions[j]).segment(0)));
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
			ResourcesPlugin.getWorkspace().run(workspaceRunnable, new MultiRule(projects), 0, monitor);
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
	
	/*
	 * Bring the provied projects into the workspace
	 */
	/* internal use only */ static void refreshProjects(IProject[] projects, IProgressMonitor monitor) throws CoreException, TeamException {
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
	
	/*
	 * Delete the target projects before checking out
	 */
	/* internal use only */ static void scrubProjects(IProject[] projects, IProgressMonitor monitor) throws CVSException {
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

}
