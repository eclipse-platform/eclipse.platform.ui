package org.eclipse.team.ccvs.core;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.core.IFileTypeRegistry;
import org.eclipse.team.core.ITeamNature;
import org.eclipse.team.core.ITeamProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProvider;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Commit;
import org.eclipse.team.internal.ccvs.core.client.Diff;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.internal.ccvs.core.client.Tag;
import org.eclipse.team.internal.ccvs.core.client.Update;
import org.eclipse.team.internal.ccvs.core.client.Command.GlobalOption;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.core.client.listeners.DiffListener;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.connection.CVSServerException;
import org.eclipse.team.internal.ccvs.core.resources.CVSRemoteSyncElement;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFile;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.resources.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.ICVSResourceVisitor;
import org.eclipse.team.internal.ccvs.core.resources.LocalFile;
import org.eclipse.team.internal.ccvs.core.resources.LocalFolder;
import org.eclipse.team.internal.ccvs.core.resources.LocalResource;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFile;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFolder;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.util.Assert;
import org.eclipse.team.internal.ccvs.core.util.RemoteFolderTreeBuilder;

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
public class CVSTeamProvider implements ITeamNature, ITeamProvider {

	// Instance variables
	private ICVSFolder managedProject;
	private IProject project;
	private String comment = "";
	
	private static PrintStream printStream;
	
	private static String[] DEFAULT_GLOBAL_OPTIONS = new String[] {"-q"};
	
	private static final CoreException CORE_EXCEPTION = new CoreException(new Status(IStatus.OK, CVSProviderPlugin.ID, TeamException.UNABLE, "", null));
	
	/**
	 * No-arg Constructor for IProjectNature conformance
	 */
	public CVSTeamProvider() {
	}
	
	/**
	 * @see IProjectNature#configure()
	 */
	public void configure() throws CoreException {
		// Do nothing
	}

	/**
	 * @see IProjectNature#deconfigure()
	 */
	public void deconfigure() throws CoreException {
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
			this.managedProject = Session.getManagedFolder(project.getLocation().toFile());
		} catch (CVSException e) {
			// Log any problems creating the CVS managed resource
			CVSProviderPlugin.log(e);
		}
	}

	/**
	 * @see ITeamNature#getProvider()
	 */
	public ITeamProvider getProvider() throws TeamException {
		if (managedProject == null) {
			// An error must have occured when we were configured
			throw new TeamException(new Status(IStatus.ERROR, CVSProviderPlugin.ID, TeamException.UNABLE, Policy.bind("CVSTeamProvider.initializationFailed", new Object[]{project.getName()}), null));
		}
		return this;
	}

	/**
	 * @see ITeamNature#configureProvider(Properties)
	 */
	public void configureProvider(Properties configuration) throws TeamException {
		// Do nothing
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
		final Set textfiles = new HashSet(resources.length);
		final Set binaryfiles = new HashSet(resources.length);
		final IFileTypeRegistry registry = TeamPlugin.getFileTypeRegistry();
		final TeamException[] eHolder = new TeamException[1];
		boolean addProject = false;
		for (int i=0; i<resources.length; i++) {
			
			// Throw an exception if the resource is not a child of the receiver
			checkIsChild(resources[i]);
			
			try {		
				// Auto-add parents if they are not already managed
				IContainer parent = resources[i].getParent();
				// XXX Need to consider workspace root
				
				while (parent.getType() != IResource.ROOT && ! isManaged(parent)) {
					folders.add(parent.getFullPath().removeFirstSegments(1).toString());
					parent = parent.getParent();
				}
					
				if (resources[i].equals(project))
					addProject = true;
					
				// Auto-add children
				resources[i].accept(new IResourceVisitor() {
					public boolean visit(IResource resource) {
						try {
							if (!isManaged(resource)) {
								String name = resource.getFullPath().removeFirstSegments(1).toString();
								if (resource.getType() == IResource.FILE) {
									String extension = resource.getFileExtension();
									if ((extension != null) && ("true".equals(registry.getValue(extension, "isAscii"))))
										textfiles.add(name);
									else
										binaryfiles.add(name);
								} else
									folders.add(name);
							}
						} catch (TeamException e) {
							// Record the exception to be thrown again later
							eHolder[0] = e;
							return false;
						}
						// Always return true and let the depth determine if children are visited
						return true;
					}
				}, depth, false);
			} catch (CoreException e) {
				throw new CVSException(new Status(IStatus.ERROR, CVSProviderPlugin.ID, TeamException.UNABLE, Policy.bind("CVSTeamProvider.visitError", new Object[] {resources[i].getFullPath()}), e));
			}
		}
		// If an exception occured during the visit, throw it here
		if (eHolder[0] != null)
			throw eHolder[0];
	
		// XXX Do we need to add the project 
		
		// Add the folders, followed by files!
		IStatus status;
		Session s = new Session(getRemoteRoot(), managedProject);
		s.open(progress);
		try {
			if (!folders.isEmpty()) {
				status = Command.ADD.execute(s,
					getDefaultGlobalOptions(),
					Command.NO_LOCAL_OPTIONS,
					(String[])folders.toArray(new String[folders.size()]),
					null,
					progress);
				if (status.getCode() == CVSStatus.SERVER_ERROR) {
					throw new CVSServerException(status);
				}
			}
			if (!textfiles.isEmpty()) {
				status = Command.ADD.execute(s,
					getDefaultGlobalOptions(),
					Command.NO_LOCAL_OPTIONS,
					(String[])textfiles.toArray(new String[textfiles.size()]),
					null,
					progress);
				if (status.getCode() == CVSStatus.SERVER_ERROR) {
					throw new CVSServerException(status);
				}
			}
			if (!binaryfiles.isEmpty()) {
				status = Command.ADD.execute(s,
					getDefaultGlobalOptions(),
					new LocalOption[] { Command.KSUBST_BINARY },
					(String[])binaryfiles.toArray(new String[binaryfiles.size()]),
					null,
					progress);
				if (status.getCode() == CVSStatus.SERVER_ERROR) {
					throw new CVSServerException(status);
				}
			}
		} finally {
			s.close();
		}
	}
	
	/**
	 * Checkin any local changes using "cvs commit ...".
	 * 
	 * @see ITeamProvider#checkin(IResource[], int, IProgressMonitor)
	 */
	public void checkin(IResource[] resources, int depth, IProgressMonitor progress) throws TeamException {
			
		// Build the arguments list
		String[] arguments = getValidArguments(resources, depth, progress);
		
		// Build the local options
		List localOptions = new ArrayList();
		localOptions.add(Commit.makeMessageOption(comment));

		// If the depth is not infinite, we want the -l option
		if (depth != IResource.DEPTH_INFINITE) {
			localOptions.add(Commit.DO_NOT_RECURSE);
		}
			
		// Commit the resources
		IStatus status;
		Session s = new Session(getRemoteRoot(), managedProject);
		s.open(progress);
		try {
			status = Command.COMMIT.execute(s,
			getDefaultGlobalOptions(),
			(LocalOption[])localOptions.toArray(new LocalOption[localOptions.size()]),
			arguments, null,
			progress);
		} finally {
			s.close();
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

	/*
	 * Generate an exception if the resource is not a child of the project
	 */
	 private void checkIsChild(IResource resource) throws CVSException {
	 	if (!isChildResource(resource))
	 		throw new CVSException(new Status(IStatus.ERROR, CVSProviderPlugin.ID, TeamException.UNABLE, Policy.bind("CVSTeamProvider.invalidResource", new Object[] {resource.getFullPath().toString(), project.getName()}), null));
	 }
	
	/**
	 * @see ITeamProvider#delete(IResource[], int, IProgressMonitor)
	 */
	public void delete(IResource[] resources, final IProgressMonitor progress) throws TeamException {
		
		// Why does the API state that the file must become unmanaged!
		// CVS requires the file to be deleted before it can be removed!
		
		// Concern: I suspect that the file must be deleted but the files parent
		// must exist for this to work. We may need to modify how Remove works.
		
		// Could implement a CVSProvider.DELETE!!!
				
		// Delete any files locally and record the names.
		// Use a resource visitor to ensure the proper depth is obtained
		final List files = new ArrayList(resources.length);
		final TeamException[] eHolder = new TeamException[1];
		for (int i=0;i<resources.length;i++) {
			checkIsChild(resources[i]);
			try {
				if (resources[i].exists()) {
					resources[i].accept(new IResourceVisitor() {
						public boolean visit(IResource resource) {
							try {
								if (isManaged(resource)) {
									String name = resource.getFullPath().removeFirstSegments(1).toString();
									if (resource.getType() == IResource.FILE) {
										files.add(name);
										((IFile)resource).delete(false, true, progress);
									}
								}
							} catch (TeamException e) {
								eHolder[0] = e;
								// If there was a problem, don't visit the children
								return false;
							} catch (CoreException e) {
								eHolder[0] = wrapException(e);
								// If there was a problem, don't visit the children
								return false;
							}
							// Always return true and let the depth determine if children are visited
							return true;
						}
					}, IResource.DEPTH_INFINITE, false);
				} else if (resources[i].getType() == IResource.FILE) {
					// If the resource doesn't exist but is a file, queue it for removal
					files.add(resources[i].getFullPath().removeFirstSegments(1).toString());
				}
			} catch (CoreException e) {
				throw wrapException(e);
			}
		}
		// If an exception occured during the visit, throw it here
		if (eHolder[0] != null)
			throw eHolder[0];
		
		// Remove the files remotely
		IStatus status;
		Session s = new Session(getRemoteRoot(), managedProject);
		s.open(progress);
		try {
			status = Command.REMOVE.execute(s,
			getDefaultGlobalOptions(),
			Command.NO_LOCAL_OPTIONS,
			(String[])files.toArray(new String[files.size()]),
			null,
			progress);
		} finally {
			s.close();
		}
		if (status.getCode() == CVSStatus.SERVER_ERROR) {
			throw new CVSServerException(status);
		}
	}
	
	/** 
	 * Diff the resources against the repository and write the
	 * output to the provided PrintStream in a form that is usable
	 * as a patch
	 */
	public void diff(IResource[] resources, LocalOption[] options, PrintStream stream,
		IProgressMonitor progress) throws TeamException {
		
		// Build the arguments list
		String[] arguments = getValidArguments(resources, Diff.DO_NOT_RECURSE.isElementOf(options) ?
			IResource.DEPTH_ONE : IResource.DEPTH_INFINITE, progress);

		IStatus status;
		Session s = new Session(getRemoteRoot(), managedProject);
		s.open(progress);
		try {
			status = Command.DIFF.execute(s,
				Command.NO_GLOBAL_OPTIONS,
				options,
				arguments,
				new DiffListener(stream),
				progress);
		} finally {
			s.close();
		}
		if (status.getCode() == CVSStatus.SERVER_ERROR) {
			// XXX diff errors??
			throw new CVSServerException(status);
		}
	}
	
	/**
	 * Replace the local version of the provided resources with the remote using "cvs update -C ..."
	 * 
	 * @see ITeamProvider#get(IResource[], int, IProgressMonitor)
	 */
	public void get(IResource[] resources, final int depth, IProgressMonitor progress) throws TeamException {
			
		// Need to correct any outgoing additions and deletions so the remote contents will be retrieved properly
		ICVSResourceVisitor visitor = new ICVSResourceVisitor() {
			public void visitFile(ICVSFile file) throws CVSException {
				ResourceSyncInfo info = file.getSyncInfo();
				if (info == null || info.isAdded()) {
					// Delete the file if it's unmanaged or doesn't exist remotely
					file.delete();
					file.unmanage();
				} else if (info.isDeleted()) {
					// If deleted, null the sync info so the file will be refetched
					file.unmanage();
				}
			}

			public void visitFolder(ICVSFolder folder) throws CVSException {
				// Visit the children of the folder as appropriate
				if (depth == IResource.DEPTH_INFINITE)
					folder.acceptChildren(this);
				else if (depth == IResource.DEPTH_ONE) {
					ICVSFile[] files = folder.getFiles();
					for (int i = 0; i < files.length; i++) {
						files[i].accept(this);
					}
				}
			}
		};
		
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			getChild(resource).accept(visitor);
			CVSProviderPlugin.getSynchronizer().save(resource.getLocation().toFile(), progress);
		}
				
		// Perform an update, ignoring any local file modifications
		update(resources, depth, null, true, progress);
	}
	
	/*
	 * Get the corresponding managed child for the given resource.
	 */
	private ICVSResource getChild(IResource resource) throws CVSException {
		if (resource.equals(project))
			return managedProject;
		return managedProject.getChild(resource.getFullPath().removeFirstSegments(1).toString());
	}
	
	/**
	 * Answer the name of the connection method for the given resource's
	 * project.
	 */
	public String getConnectionMethod(IResource resource) throws TeamException {
		checkIsChild(resource);
		return getRemoteRoot().getMethod().getName();
	}
	
	private GlobalOption[] getDefaultGlobalOptions() {
		return CVSProvider.getDefaultGlobalOptions();
	}
	
	/**
	 * Get the print stream to which information from CVS commands
	 * is sent.
	 */
	private PrintStream getPrintStream() {
		return CVSProviderPlugin.getProvider().getPrintStream();
	}
	
	/** 
	 * Get the remote resource corresponding to the base of the local resource.
	 * This method returns null if the corresponding local resource does not have a base.
	 * 
	 * Use getRemoteSyncTree() to get the current remote state of HEAD or a branch.
	 */
	public ICVSRemoteResource getRemoteResource(IResource resource) throws TeamException {
		checkIsChild(resource);
		ICVSResource managed = getChild(resource);
		if (managed.isFolder()) {
			ICVSFolder folder = (ICVSFolder)managed;
			if (folder.isCVSFolder()) {
				FolderSyncInfo syncInfo = folder.getFolderSyncInfo();
				return new RemoteFolder(null, CVSProvider.getInstance().getRepository(syncInfo.getRoot()), new Path(syncInfo.getRepository()), syncInfo.getTag());
			}
		} else {
			if (managed.isManaged())
				return RemoteFile.getBase((RemoteFolder)getRemoteResource(resource.getParent()), (ICVSFile)managed);
		}
		return null;
	}
	
	/** 
	 * Return the repository location to which the provider is connected
	 */
	public ICVSRepositoryLocation getRemoteRoot() throws CVSException {
		return CVSProvider.getInstance().getRepository(managedProject.getFolderSyncInfo().getRoot());
	}
	
	public IRemoteSyncElement getRemoteSyncTree(IResource resource, CVSTag tag, IProgressMonitor progress) throws TeamException {
		checkIsChild(resource);
		ICVSResource managed = getChild(resource);
		ICVSRemoteResource remote = getRemoteResource(resource);
		ICVSRemoteResource baseTree = null;
		if (remote == null) {
			// The resource doesn't have a remote base. 
			// However, we still need to check to see if its been created remotely by a third party.
			ICVSFolder parent = managed.getParent();
			if (!parent.isCVSFolder())
				throw new TeamException(new CVSStatus(CVSStatus.ERROR, resource.getProjectRelativePath(), "Error retrieving remote resource tree. Parent is not managed", null));
			ICVSRepositoryLocation location = CVSProvider.getInstance().getRepository(parent.getFolderSyncInfo().getRoot());
			// XXX We build and fetch the whole tree from the parent. We could restrict the search to just the desired child
			RemoteFolder remoteParent = RemoteFolderTreeBuilder.buildRemoteTree((CVSRepositoryLocation)location, parent, tag, progress);
			if (remoteParent != null) {
				try {
					remote = (ICVSRemoteResource)remoteParent.getChild(resource.getName());
					// The types need to match or we're in trouble
					if (!(remote.isContainer() == managed.isFolder()))
						throw new TeamException(new CVSStatus(CVSStatus.ERROR, resource.getProjectRelativePath(), "Error retrieving remote resource tree. Local and remote resource types differ", null));
				} catch (CVSException e) {
					// XXX Either need an exception or null to indicate child does not exist
				}
			}
		} else if(resource.getType() == IResource.FILE) {
			baseTree = remote;
			remote = RemoteFile.getLatest((RemoteFolder)getRemoteResource(resource.getParent()), (ICVSFile)managed, tag, progress);
		} else {
			ICVSRepositoryLocation location = remote.getRepository();
			baseTree = RemoteFolderTreeBuilder.buildBaseTree((CVSRepositoryLocation)location, (ICVSFolder)managed, tag, progress);
			remote = RemoteFolderTreeBuilder.buildRemoteTree((CVSRepositoryLocation)location, (ICVSFolder)managed, tag, progress);
		}
		return new CVSRemoteSyncElement(false, resource, baseTree, remote);
	}
	
	public ICVSRemoteResource getRemoteTree(IResource resource, CVSTag tag, IProgressMonitor progress) throws TeamException {
		checkIsChild(resource);
		ICVSResource managed = getChild(resource);
		ICVSRemoteResource remote = getRemoteResource(resource);
		if (remote == null) {
			// The resource doesn't have a remote base. 
			// However, we still need to check to see if its been created remotely by a third party.
			ICVSFolder parent = managed.getParent();
			if (!parent.isCVSFolder())
				throw new TeamException(new CVSStatus(CVSStatus.ERROR, resource.getProjectRelativePath(), "Error retrieving remote resource tree. Parent is not managed", null));
			ICVSRepositoryLocation location = CVSProvider.getInstance().getRepository(parent.getFolderSyncInfo().getRoot());
			// XXX We build and fetch the whole tree from the parent. We could restrict the search to just the desired child
			RemoteFolder remoteParent = RemoteFolderTreeBuilder.buildRemoteTree((CVSRepositoryLocation)location, parent, tag, progress);
			if (remoteParent != null) {
				try {
					remote = (ICVSRemoteResource)remoteParent.getChild(resource.getName());
					// The types need to match or we're in trouble
					if (!(remote.isContainer() == managed.isFolder()))
						throw new TeamException(new CVSStatus(CVSStatus.ERROR, resource.getProjectRelativePath(), "Error retrieving remote resource tree. Local and remote resource types differ", null));
				} catch (CVSException e) {
					// XXX Either need an exception or null to indicate child does not exist
				}
			}
		} else if(resource.getType() == IResource.FILE) {
			remote = RemoteFile.getLatest((RemoteFolder)getRemoteResource(resource.getParent()), (ICVSFile)managed, tag, progress);
		} else {
			try {
				ICVSRepositoryLocation location = remote.getRepository();
				remote = RemoteFolderTreeBuilder.buildRemoteTree((CVSRepositoryLocation)location, (ICVSFolder)managed, tag, progress);		
			} catch(CVSException e) {
				throw new TeamException(new CVSStatus(CVSStatus.ERROR, resource.getProjectRelativePath(), "Error retrieving remote resource tree", e));
			}
		}
		return remote;
	}
	
	/**
	 * Returns an IUserInfo instance that can be used to access and set the
	 * user name and set the password. To have changes take place, the user must
	 * invoke the setUserInfo() method.
	 */ 
	public IUserInfo getUserInfo(IResource resource) throws TeamException {
		checkIsChild(resource);
		// Get the repository location for the receiver
		CVSRepositoryLocation location = (CVSRepositoryLocation)getRemoteRoot();
		// Make a copy which is mutable
		location = CVSRepositoryLocation.fromString(location.getLocation());
		location.setUserMuteable(true);
		return location;
	}
	
	/*
	 * Get the arguments to be passed to a commit or update
	 */
	private String[] getValidArguments(IResource[] resources, int depth, IProgressMonitor progress) throws CVSException {
		List arguments = new ArrayList(resources.length);
		for (int i=0;i<resources.length;i++) {
			checkIsChild(resources[i]);
			// A depth of zero is only valid for files
			if ((depth != IResource.DEPTH_ZERO) || (resources[i].getType() == IResource.FILE)) {
				IPath cvsPath = resources[i].getFullPath().removeFirstSegments(1);
				if (cvsPath.segmentCount() == 0) {
					arguments.add(Session.CURRENT_LOCAL_FOLDER);
				}
				else
					arguments.add(cvsPath.toString());
			}
		}
		return (String[])arguments.toArray(new String[arguments.size()]);
	}
	
	/**
	 * @see ITeamProvider#hasRemote(IResource)
	 * XXX to be removed when sync methods are removed from ITeamProvider
	 */
	public boolean hasRemote(IResource resource) {
		try {
			LocalResource cvsResource;
			int type = resource.getType();
			if(type!=IResource.FILE) {
				cvsResource = new LocalFolder(resource.getLocation().toFile());
				if(type==IResource.PROJECT) {
					return ((ICVSFolder)cvsResource).isCVSFolder();
				} else {
					return cvsResource.isManaged();
				}
			} else {
				cvsResource = new LocalFile(resource.getLocation().toFile());
				ResourceSyncInfo info = cvsResource.getSyncInfo();
				if(info!=null) {
					return !info.getRevision().equals("0");
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
		// check to see if the resource exists and has an entry
		try {
			return isManaged(resource);
		} catch (TeamException e) {
			// Something went wrong. Log it and say the file is not checked out
			CVSProviderPlugin.log(e);
			return false;
		}
	}
	
	/*
	 * Helper to indicate if the resource is a child of the receiver's project
	 */
	private boolean isChildResource(IResource resource) {
		return resource.getProject().getName().equals(managedProject.getName());
	}
			
	/**
	 * Return whether the given resource is managed. 
	 * 
	 * From a CVS standpoint, this means that we have a CVS entry
	 * for the resource and that uodates and commits may effect the
	 * resource or its children.
	 */
	public boolean isManaged(IResource resource) throws TeamException {
		
		if (resource.equals(project))
			return true;
			
		// Ensure that the resource is a child of our project
		if (!isChildResource(resource))
			// Is returning false enough or should we throw an exception
			return false;
			
		// Get the ICVSResource corresponding to the resource and check if its managed
		return getChild(resource).isManaged();
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
			((CVSRemoteSyncElement)elements[i]).makeOutgoing(null);
		}
	}
	
	/**
	 * @see ITeamProvider#move(IResource, IPath, IProgressMonitor)
	 */
	public void moved(IPath source, IResource resource, IProgressMonitor progress)
		throws TeamException {
			
		// this translates to a delete and an add
		
		// How is this managed? Do we do the move or is that done after?
		// It becomes complicated if the local and remote operations
		// are independant as this is not the way CVS works!
		
		// Could implement a CVSProvider.MOVE!!!
		
		// XXX ????
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
		if (!CVSRepositoryLocation.validateConnectionMethod(methodName))
			return false;
		CVSRepositoryLocation location;
		try {
			location = ((CVSRepositoryLocation)userInfo);
		} catch (ClassCastException e) {
			throw new TeamException(new Status(IStatus.ERROR, CVSProviderPlugin.ID, TeamException.UNABLE, Policy.bind("CVSTeamProvider.invalidUserInfo"), null));
		}
		location.setUserMuteable(false);
		location.setMethod(methodName);
		location.updateCache();
		setRemoteRoot(location, monitor);
		return true;
	}
	
	private void setRemoteRoot(ICVSRepositoryLocation location, IProgressMonitor monitor) throws TeamException {

		// XXX We need to do the proper progress monitoring
		
		// Check if there is a differnece between the new and old roots	
		final String root = location.getLocation();
		if (root.equals(getRemoteRoot().getLocation())) 
			return;
		
		// Visit all the children folders in order to set the root in the folder sync info
		managedProject.accept(new ICVSResourceVisitor() {
			public void visitFile(ICVSFile file) throws CVSException {};
			public void visitFolder(ICVSFolder folder) throws CVSException {
				FolderSyncInfo info = folder.getFolderSyncInfo();
				if (info != null) {
					folder.setFolderSyncInfo(new FolderSyncInfo(info.getRepository(), root, info.getTag(), info.getIsStatic()));
					folder.acceptChildren(this);
				}
			};
		});
		CVSProviderPlugin.getSynchronizer().save(project.getLocation().toFile(), new NullProgressMonitor());
		return;
	}
	
	/** 
	 * Tag the resources in the CVS repository with the given tag.
	 */
	public void tag(IResource[] resources, int depth, CVSTag tag, IProgressMonitor progress) throws TeamException {
		
		// XXX These should generate CVSExceptions
		Assert.isNotNull(tag);
		Assert.isTrue(tag.getType() == CVSTag.VERSION || tag.getType() == CVSTag.BRANCH);
		
		// Build the arguments list
		String[] arguments = getValidArguments(resources, depth, progress);
		
		// Build the local options
		List localOptions = new ArrayList();
		// If the depth is not infinite, we want the -l option
		if (depth != IResource.DEPTH_INFINITE)
			localOptions.add(Tag.DO_NOT_RECURSE);
		if (tag.getType() == CVSTag.BRANCH)
			localOptions.add(Tag.CREATE_BRANCH);
		
		// The tag name is supposed to be the first argument
		ArrayList args = new ArrayList();
		args.add(tag.getName());
		args.addAll(Arrays.asList(arguments));
		arguments = (String[])args.toArray(new String[args.size()]);

		IStatus status;
		Session s = new Session(getRemoteRoot(), managedProject);
		s.open(progress);
		try {
			status = Command.TAG.execute(s,
			getDefaultGlobalOptions(),
			(LocalOption[])localOptions.toArray(new LocalOption[localOptions.size()]),
			// XXX We should pass the tag to the command
			arguments,
			null,
			progress);
		} finally {
			s.close();
		}
		if (status.getCode() == CVSStatus.SERVER_ERROR) {
			// XXX diff errors??
			throw new CVSServerException(status);
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
	 * Generally usefull update.
	 * 
	 * For the depth, only IResource.DEPTH_ONE and IResource.DEPTH_INFINITE are meaningfull for folders.
	 * 
	 * The tag parameter determines any stickyness after the update is run. If tag is null, any tagging on the
	 * resources being updated remain the same. If the tag is a branch, version or date tag, then the resources
	 * will be appropriatly tagged. If the tag is HEAD, then there will be no tag on the resources (same as -A
	 * clear sticky option).
	 * 
	 * The ignoreLocalChanges parameter indicates whether -C should be used. This option only ignores local file 
	 * modifications. It does not ignore local uncommitted sync changes (such as additions and removals).
	 */
	public void update(IResource[] resources, int depth, CVSTag tag, boolean ignoreLocalChanges, IProgressMonitor progress) throws TeamException {
		// Build the local options
		List localOptions = new ArrayList();
		if (ignoreLocalChanges) {
			localOptions.add(Update.IGNORE_LOCAL_CHANGES);
		}
		// Use the appropriate tag options
		if (tag != null) {
			if (tag.getType() == CVSTag.HEAD) {
				localOptions.add(Update.CLEAR_STICKY);
			} else {
				localOptions.add(Update.makeTagOption(tag));
			}
		}
		// Always look for absent directories
		localOptions.add(Update.RETRIEVE_ABSENT_DIRECTORIES);
		// Prune empty directories if pruning is enabled
		if (CVSProviderPlugin.getPlugin().getPruneEmptyDirectories()) {
			localOptions.add(Update.PRUNE_EMPTY_DIRECTORIES);
		}
		// If depth = zero or 1, use -l
		if (depth != IResource.DEPTH_INFINITE) {
			localOptions.add(Update.DO_NOT_RECURSE);
		}
			
		// Build the arguments list
		String[] arguments = getValidArguments(resources, depth, progress);

		IStatus status;
		Session s = new Session(getRemoteRoot(), managedProject);
		s.open(progress);
		try {
			status = Command.UPDATE.execute(s,
			getDefaultGlobalOptions(),
			(LocalOption[])localOptions.toArray(new LocalOption[localOptions.size()]),
			arguments,
			null,
			progress);
		} finally {
			s.close();
		}
		if (status.getCode() == CVSStatus.SERVER_ERROR) {
			// XXX diff errors??
			throw new CVSServerException(status);
		}
	}

	private static TeamException wrapException(CoreException e) {
		return new TeamException(statusFor(e));
	}
	
	public static TeamException wrapException(CVSException e, List errors) {
		// NOTE: Need to find out how to pass MultiStatus. Is it up to me to subclass?
		return e;
	}
	
	private static IStatus statusFor(CoreException e) {
		// We should be taking out any status from the CVSException
		// and creating an array of IStatus!
		return new Status(IStatus.ERROR, CVSProviderPlugin.ID, TeamException.UNABLE, getMessageFor(e), e);
	}
	
	public static String getMessageFor(Exception e) {
		String message = Policy.bind(e.getClass().getName(), new Object[] {e.getMessage()});
		if (message.equals(e.getClass().getName()))
			message = Policy.bind("CVSTeamProvider.exception", new Object[] {e.toString()}); 
		return message;
	}
	
	/*
	 * @see ITeamProvider#validateEdit(IFile[], Object)
	 */
	public IStatus validateEdit(IFile[] files, Object context) {
		return new CVSStatus(CVSStatus.OK, "OK");
	}

	/*
	 * @see ITeamProvider#validateSave(IFile)
	 */
	public IStatus validateSave(IFile file) {
		return new CVSStatus(CVSStatus.OK, "OK");
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
}