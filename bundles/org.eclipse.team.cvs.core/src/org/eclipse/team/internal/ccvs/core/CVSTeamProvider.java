package org.eclipse.team.ccvs.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.core.IFileTypeRegistry;
import org.eclipse.team.core.ITeamNature;
import org.eclipse.team.core.ITeamProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.core.sync.IRemoteResource;
import org.eclipse.team.core.sync.IRemoteSyncElement;
import org.eclipse.team.internal.ccvs.core.CVSDiffException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSRemoteSyncElement;
import org.eclipse.team.internal.ccvs.core.Client;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFile;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFolder;
import org.eclipse.team.internal.ccvs.core.resources.RemoteResource;
import org.eclipse.team.internal.ccvs.core.resources.api.FolderProperties;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFile;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFolder;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedResource;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedVisitor;
import org.eclipse.team.internal.ccvs.core.response.IResponseHandler;
import org.eclipse.team.internal.ccvs.core.response.custom.DiffErrorHandler;
import org.eclipse.team.internal.ccvs.core.response.custom.DiffMessageHandler;
import org.eclipse.team.internal.ccvs.core.util.Assert;
import org.eclipse.team.internal.ccvs.core.util.ProjectDescriptionManager;

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
	private IManagedFolder managedProject;
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
		// unmanage() removes any traces of CVS from the project
		try {
			managedProject.unmanage();
		} catch (CVSException e) {
			throw new CoreException(new Status(IStatus.ERROR, CVSProviderPlugin.ID, 0, Policy.bind("CVSTeamProvider.deconfigureProblem", new Object[] {project.getName()}), e));
		}
		project.refreshLocal(IResource.DEPTH_INFINITE, null);
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
			this.managedProject = Client.getManagedFolder(project.getLocation().toFile());
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
		// For now, perform an import and checkout.
		// NOTE: We'll need to revisit this once we start using the Team test framework
		CVSProviderPlugin.getProvider().importAndCheckout(project, configuration, Policy.monitorFor(null));
	}
			
	/*
	 * Build the repository instance from the given properties.
	 * The supported properties are:
	 * 
	 *   connection The connection method to be used
	 *   user The username for the connection
	 *   password The password used for the connection (optional)
	 *   host The host where the repository resides
	 *   port The port to connect to (optional)
	 *   root The server directory where the repository is located
	 */
	private static CVSRepositoryLocation buildRepository(Properties configuration) throws TeamException {
		StringBuffer repository = new StringBuffer(":");
		String connection = configuration.getProperty("connection");
		if (connection == null)
			repository.append("pserver");
		else 
			repository.append(connection);
		repository.append(":");
		String user = configuration.getProperty("user");
		if (user == null)
			throw new TeamException(new Status(IStatus.ERROR, CVSProviderPlugin.ID, TeamException.UNABLE, Policy.bind("CVSTeamProvider.noUser"), null));
		else 
			repository.append(user);
		repository.append("@");
		String host = configuration.getProperty("host");
		if (host == null)
			throw new TeamException(new Status(IStatus.ERROR, CVSProviderPlugin.ID, TeamException.UNABLE, Policy.bind("CVSTeamProvider.noHost"), null));
		else 
			repository.append(host);
		String port = configuration.getProperty("port");
		if (port != null) {
			repository.append("#");
			repository.append(port);
		}
		repository.append(":");
		String root = configuration.getProperty("root");
		if (root == null)
			throw new TeamException(new Status(IStatus.ERROR, CVSProviderPlugin.ID, TeamException.UNABLE, Policy.bind("CVSTeamProvider.noRoot"), null));
		else 
			repository.append(root);
		
		CVSRepositoryLocation location  = CVSRepositoryLocation.fromString(repository.toString());
		
		String password = configuration.getProperty("password");
		if (password != null) {
			location.setPassword(password);
		}
		
		return location;
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
	 */
	public void add(IResource[] resources, int depth, IProgressMonitor progress) throws TeamException {	
		
		// Visit the children of the resources using the depth in order to
		// determine which folders, text files and binary files need to be added
		final List folders = new ArrayList(resources.length);
		final List textfiles = new ArrayList(resources.length);
		final List binaryfiles = new ArrayList(resources.length);
		final IFileTypeRegistry registry = TeamPlugin.getFileTypeRegistry();
		final TeamException[] eHolder = new TeamException[1];
		for (int i=0;i<resources.length;i++) {
			
			// Throw an exception if the resource is not a child of the receiver
			checkIsChild(resources[i]);
			
			try {		
				// Auto-add parents if they are not already managed
				IResource parent = resources[i].getParent();
				List parentFolders = new ArrayList();
				while (!isManaged(parent)) {
					parentFolders.add(parent.getFullPath().removeFirstSegments(1).toString());
					parent = parent.getParent();
				}
				for (int j=parentFolders.size()-1;j>=0;j--)
					folders.add(parentFolders.get(j));
					
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
	
		// It looks like we need to add folders first, followed by files!
		try {
			if (!folders.isEmpty())
				Client.execute(
					Client.ADD,
					new String[0],
					new String[0],
					(String[])folders.toArray(new String[folders.size()]),
					managedProject,
					progress,
					getPrintStream());
			if (!textfiles.isEmpty())
				Client.execute(
					Client.ADD,
					new String[0],
					new String[0],
					(String[])textfiles.toArray(new String[textfiles.size()]),
					managedProject,
					progress,
					getPrintStream());
			if (!binaryfiles.isEmpty()) {
				// Build the local options
				List localOptions = new ArrayList();
				localOptions.add(Client.KB_OPTION);
				// We should check if files are text or not!
				Client.execute(
					Client.ADD,
					new String[0],
					(String[])localOptions.toArray(new String[localOptions.size()]),
					(String[])binaryfiles.toArray(new String[binaryfiles.size()]),
					managedProject,
					progress,
					getPrintStream());
			}
		} catch (CVSException e) {
			// Refresh and throw the exception again
			refreshResources(resources, depth, e, progress);
		}
		refreshResources(resources, depth, null, progress);
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
		localOptions.add(Client.MESSAGE_OPTION);
		localOptions.add(comment);
		// If the depth is not infinite, we want the -l option
		if (depth != IResource.DEPTH_INFINITE)
			localOptions.add(Client.LOCAL_OPTION);
			
		// Commit the resources
		try {
			Client.execute(
				Client.COMMIT,
				DEFAULT_GLOBAL_OPTIONS,
				(String[])localOptions.toArray(new String[localOptions.size()]),
				arguments,
				managedProject,
				progress,
				getPrintStream());
		} catch(CVSException e) {
			refreshResources(resources, depth, e, progress);
		}
		refreshResources(resources, depth, null, progress);
	}

	/**
	 * Checkout the provided resources so they can be modified locally and committed.
	 * 
	 * Currently, we support only the optimistic model so checkout dores nothing.
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
		final Set parents = new HashSet();
		final TeamException[] eHolder = new TeamException[1];
		for (int i=0;i<resources.length;i++) {
			checkIsChild(resources[i]);
			try {
				resources[i].accept(new IResourceVisitor() {
					public boolean visit(IResource resource) {
						try {
							if (isManaged(resource)) {
								String name = resource.getFullPath().removeFirstSegments(1).toString();
								if (resource.getType() == IResource.FILE) {
									parents.add(resource.getParent());
									files.add(name);
									((IFile)resource).delete(false, true, progress);
									// NOTE: Should we broadcast Team change events?
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
			} catch (CoreException e) {
				throw wrapException(e);
			}
		}
		// If an exception occured during the visit, throw it here
		if (eHolder[0] != null)
			throw eHolder[0];
		
		// Remove the files remotely
		try {
			Client.execute(
				Client.REMOVE,
				new String[0],
				new String[0],
				(String[])files.toArray(new String[files.size()]),
				managedProject,
				progress,
				getPrintStream());
		} catch(CVSException e) {
			refreshResources((IResource[])parents.toArray(new IResource[parents.size()]), IResource.DEPTH_INFINITE, e, progress);
		}
		refreshResources((IResource[])parents.toArray(new IResource[parents.size()]), IResource.DEPTH_INFINITE, null, progress);
	}
	
	/** 
	 * Diff the resources against the repository
	 */
	public void diff(IResource[] resources, int depth, IProgressMonitor progress) throws TeamException {
		// Build the arguments list
		String[] arguments = getValidArguments(resources, depth, progress);
		
		// Build the local options
		List localOptions = new ArrayList();
		// Perform a context diff
		localOptions.add("-c");
		// If the depth is not infinite, we want the -l option
		if (depth != IResource.DEPTH_INFINITE)
			localOptions.add(Client.LOCAL_OPTION);		
			
		try {
			Client.execute(
				Client.DIFF,
				DEFAULT_GLOBAL_OPTIONS,
				(String[])localOptions.toArray(new String[localOptions.size()]),
				arguments,
				managedProject,
				progress,
				getPrintStream());
		} catch(CVSDiffException e) {
			// Ignore this for now
		}
	}
	
	/** 
	 * Diff the resources against the repository and write the
	 * output to the provided PrintStream in a form that is usable
	 * as a patch
	 */
	public void diff(IResource[] resources, int depth, PrintStream stream, IProgressMonitor progress) throws TeamException {
			
		// Build the arguments list
		String[] arguments = getValidArguments(resources, depth, progress);	
		
		// Build the local options
		List localOptions = new ArrayList();
		// Perform a context diff
		localOptions.add("-N"); // include diffs for added and removed files
		localOptions.add("-u"); // use unified output format
		// If the depth is not infinite, we want the -l option
		if (depth != IResource.DEPTH_INFINITE)
			localOptions.add(Client.LOCAL_OPTION);
			
		final List errors = new ArrayList();	
		try {
			Client.execute(
				Client.DIFF,
				Client.EMPTY_ARGS_LIST,
				(String[])localOptions.toArray(new String[localOptions.size()]),
				arguments,
				managedProject,
				progress,
				stream,
				null,
				new IResponseHandler[] {new DiffMessageHandler(), new DiffErrorHandler(errors)});
		} catch(CVSDiffException e) {
			// Ignore this for now
		} catch (CVSException e) {
			if (!errors.isEmpty()) {
				PrintStream out = getPrintStream();
				for (int i=0;i<errors.size();i++)
					out.println(errors.get(i));
			}
			throw e;
		}
	}
	/**
	 * Temporary method to allow fixing a resources types
	 */
	public void fixFileType(IResource[] resources,int depth, IProgressMonitor progress) throws TeamException {

		// Build the arguments list and record any errors.
		// We need to visit children resources depending on the depth.
		final TeamException[] eHolder = new TeamException[1];		final List textfiles = new ArrayList(resources.length);
		final List binaryfiles = new ArrayList(resources.length);
		final IFileTypeRegistry registry = TeamPlugin.getFileTypeRegistry();
		for (int i=0;i<resources.length;i++) {
			checkIsChild(resources[i]);
			try {
				resources[i].accept(new IResourceVisitor() {
					public boolean visit(IResource resource) {
						try {
							if ((resource.getType() == IResource.FILE) && (isManaged(resource))) {
								String name = resource.getFullPath().removeFirstSegments(1).toString();
								String extension = resource.getFileExtension();
								if ((extension != null) && ("true".equals(registry.getValue(extension, "isAscii"))))
									textfiles.add(name);
								else
									binaryfiles.add(name);
							}
						} catch (TeamException e) {
							eHolder[0] = e;
							// If there was a problem, don't visit the children
							return false;
						}
						// Always return true and let the depth determine if children are visited
						return true;
					}
				}, depth, false);
			} catch (CoreException e) {
				throw wrapException(e);
			}
		}
		// If an exception occured during the visit, throw it here
		if (eHolder[0] != null)
			throw eHolder[0];
	
		if (!textfiles.isEmpty()) {
				List localOptions = new ArrayList();
				localOptions.add(Client.KO_OPTION); // disable keyword substitution
				Client.execute(
					Client.ADMIN,
					new String[0],
					(String[])localOptions.toArray(new String[localOptions.size()]),
					(String[])textfiles.toArray(new String[textfiles.size()]),
					managedProject,
					progress,
					getPrintStream());
		}
		if (!binaryfiles.isEmpty()) {
			// Build the local options
			List localOptions = new ArrayList();
			localOptions.add(Client.KB_OPTION); // disable keyword substitution
			Client.execute(
				Client.ADMIN,
				new String[0],
				(String[])localOptions.toArray(new String[localOptions.size()]),
				(String[])binaryfiles.toArray(new String[binaryfiles.size()]),
				managedProject,
				progress,
				getPrintStream());
		}
		
		// Update the options on the local files
		List localOptions = new ArrayList();
		if (depth != IResource.DEPTH_INFINITE)
			// If depth = zero or 1, use -l
			localOptions.add(Client.LOCAL_OPTION);
		localOptions.add("-A");
		update(resources, depth, (String[])localOptions.toArray(new String[localOptions.size()]), progress);
	}
	
	/**
	 * Replace the local version of the provided resources with the remote using "cvs update -C ..."
	 * 
	 * @see ITeamProvider#get(IResource[], int, IProgressMonitor)
	 */
	public void get(IResource[] resources, int depth, IProgressMonitor progress) throws TeamException {
			
		// Build the arguments list
		String[] arguments = getValidArguments(resources, depth, progress);
	
		// Build the local options
		List localOptions = new ArrayList();
		localOptions.add("-C"); // Ignore any local changes
		if (depth == IResource.DEPTH_INFINITE)
			// if depth = infinite, look for new directories
			localOptions.add(Client.DEEP_OPTION);
		else
			// If depth = zero or 1, use -l
			localOptions.add(Client.LOCAL_OPTION);
			
		try {
			Client.execute(
				Client.UPDATE,
				new String[0],
				(String[])localOptions.toArray(new String[localOptions.size()]),
				arguments,
				managedProject,
				progress,
				getPrintStream());
		} catch(CVSException e) {
			refreshResources(resources, depth, e, progress);
		}
		refreshResources(resources, depth, null, progress);
	}
	
	/*
	 * Get the corresponding managed child for the given resource.
	 */
	private IManagedResource getChild(IResource resource) throws CVSException {
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
		return CVSRepositoryLocation.fromString(managedProject.getFolderInfo().getRoot()).getMethod().getName();
	}
	
	/**
	 * Get the print stream to which information from CVS commands
	 * is sent.
	 */
	private PrintStream getPrintStream() {
		return CVSProviderPlugin.getProvider().getPrintStream();
	}
	
	/** 
	 * Get the remote resource corresponding to the base of the local resource
	 */
	public ICVSRemoteResource getRemoteResource(IResource resource) throws TeamException {
		checkIsChild(resource);
		IManagedResource managed = getChild(resource);
		if (managed.isFolder()) {
			IManagedFolder folder = (IManagedFolder)managed;
			return new RemoteFolder(CVSRepositoryLocation.fromString(folder.getFolderInfo().getRoot()), new Path(folder.getFolderInfo().getRepository()), folder.getFolderInfo().getTag());
		} else {
			// NOTE: This may not provide a proper parent!
			return new RemoteFile((RemoteFolder)getRemoteResource(resource.getParent()), managed.getName(), ((IManagedFile)managed).getFileInfo().getVersion());
		}
	}
	
	public IRemoteSyncElement getRemoteSyncTree(IResource resource, String tag, IProgressMonitor progress) throws TeamException {
		checkIsChild(resource);
		IManagedResource managed = getChild(resource);
		
		// XXX: there must be a quicker way of determining is a folder/file has a remote (e.g. in one round trip
		// at the time of this call).
		ICVSRemoteResource remote;
		try {
			if(resource.getType()!=IResource.PROJECT) {
				RemoteFolder remoteParent = (RemoteFolder)getRemoteResource(resource.getParent());
				remoteParent.getMembers(progress);
				// if an exception is thrown then just ignore
				remote = (ICVSRemoteResource)remoteParent.getChild(resource.getName());						
			} else {
				remote = (RemoteFolder)getRemoteResource(resource);
			}
		} catch(CVSException e) {
			remote = null;
		}
		
		IManagedFolder localParent = managed.isFolder() ? (IManagedFolder)managed : managed.getParent();			
		return new CVSRemoteSyncElement(resource, null, remote, localParent);
	}
	
	/**
	 * Returns an IUserInfo instance that can be used to access and set the
	 * user name and set the password. To have changes take place, the user must
	 * invoke the setUserInfo() method.
	 */ 
	public IUserInfo getUserInfo(IResource resource) throws TeamException {
		checkIsChild(resource);
		CVSRepositoryLocation location = CVSRepositoryLocation.fromString(managedProject.getFolderInfo().getRoot());
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
				if (cvsPath.segmentCount() == 0)
					arguments.add(".");
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
			// This assumes that all projects associated with a provider exists remotely
			// which is the case presently
			if (resource.equals(project))
				return isManaged(resource);
				
			IManagedResource child = getChild(resource);
			if (!child.showManaged())
				return false;
			if (resource.getType() == IResource.FOLDER) {
				// if it's managed and its a folder than it exists remotely
				return true;
			} else {
				// NOTE: This seems rather adhoc!
				return !((IManagedFile)child).getFileInfo().getVersion().equals("0");
			}
		} catch (TeamException e) {
			// Shouldn't have got an exception. Since we did, log it and return false
			CVSProviderPlugin.log(e);
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
	 * @see ITeamSynch#isDirty(IResource)
	 * XXX to be removed when sync methods are removed from ITeamProvider
	 */
	public boolean isDirty(IResource resource) {
		try {
			IManagedResource r = getChild(resource);
			return r.showDirty();
		} catch (CVSException e) {
			Assert.isTrue(false);
			return true;
		}
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
			
		// Get the IManagedResource corresponding to the resource and check if its managed
		return getChild(resource).showManaged();
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

		Client.execute(
			Client.REMOVE,
			new String[0],
			new String[0], 
			new String[] {source.removeFirstSegments(1).toString()},
			managedProject,
			progress,
			getPrintStream());
		Client.execute(
			Client.ADD,
			new String[0],
			new String[0], // We'll need to copy options from old entry
			new String[] {resource.getFullPath().removeFirstSegments(1).toString()},
			managedProject,
			progress,
			getPrintStream());
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
	public boolean setConnectionInfo(IResource resource, String methodName, IUserInfo userInfo) throws TeamException {
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
		location.updateCache();
		location.setMethod(methodName);
		final String root = location.getLocation();
		managedProject.accept(new IManagedVisitor() {
			public void visitFile(IManagedFile file) throws CVSException {};
			public void visitFolder(IManagedFolder folder) throws CVSException {
				FolderProperties info = folder.getFolderInfo();
				info.setRoot(root);
				folder.setFolderInfo(info);
				folder.acceptChildren(this);
			};
		});
		return true;
	}
	
	/**
	 * Sets the userinfo (username and password) for the resource's project.
	 */
	public void setUserInfo(IResource resource, IUserInfo userinfo) throws TeamException {
		checkIsChild(resource);
		try {
			((CVSRepositoryLocation)userinfo).updateCache();
		} catch (ClassCastException e) {
			throw new TeamException(new Status(IStatus.ERROR, CVSProviderPlugin.ID, TeamException.UNABLE, Policy.bind("CVSTeamProvider.invalidUserInfo"), null));
		}
	}
	
	/**
	 * @see ITeamProvider#refreshState(IResource[], int, IProgressMonitor)
	 */
	public void refreshState(
		IResource[] resources,
		int depth,
		IProgressMonitor progress)
		throws TeamException {
			
			// How does this translate to CVS?
			// NIK: maybe an simple update ?
	}
	
	/*
	 * Refresh the affected resources after a CVSException occured.
	 * Initially we'll ignore any CoreException and throw the CVSException 
	 * after the refresh
	 */
	private void refreshResources(IResource[] resources, int depth, CVSException e, IProgressMonitor progress) throws CVSException {
		CVSException newException = e;
		try {
			refreshResources(resources, depth, progress);
		} catch (CoreException coreException) {
			// Only use the new exception if there was no old one
			if (newException == null)
				newException = new CVSException(new Status(IStatus.ERROR, CVSProviderPlugin.ID, TeamException.UNABLE, Policy.bind("CVSTeamProvider.refreshError", new Object[] {project.getFullPath().toString()}), coreException));
		}
		if (newException != null)
			throw newException;
	}
	private IResource[] allChildrenOf(IResource[] resources, int depth, IProgressMonitor progress) throws CoreException {
		final List allResources = new ArrayList();
		for (int i=0;i<resources.length;i++) {
			resources[i].accept(new IResourceVisitor() {
				public boolean visit(IResource resource) {
					allResources.add(resource);
					return true;
				}
			}, depth, false);
		}
		return (IResource[])allResources.toArray(new IResource[allResources.size()]);
	}
	private void refreshResources(IResource[] resources, int depth, IProgressMonitor progress) throws CoreException {
		// NOTE: We may not catch all resources changes in this way
		for (int i = 0; i < resources.length; i++) {
			IResource r = resources[i];
			r.refreshLocal(depth, progress);
		}
		// NOTE: We need to refresh based on the depth
		// We should try to be smart by getting the results from the command								
		TeamPlugin.getManager().broadcastResourceStateChanges(resources);
	}
	
	/** 
	 * Tag the resources in the CVS repository with the given tag.
	 */
	public void tag(IResource[] resources, int depth, String tag, boolean isBranch, IProgressMonitor progress) throws TeamException {
			
		// Build the arguments list
		String[] arguments = getValidArguments(resources, depth, progress);
		
		// Build the local options
		List localOptions = new ArrayList();
		// If the depth is not infinite, we want the -l option
		if (depth != IResource.DEPTH_INFINITE)
			localOptions.add(Client.LOCAL_OPTION);
		if (isBranch)
			localOptions.add(Client.BRANCH_OPTION);
		
		// The tag name is supposed to be the first argument
		ArrayList args = new ArrayList();
		args.add(tag);
		args.addAll(Arrays.asList(arguments));
		arguments = (String[])args.toArray(new String[args.size()]);
		
		Client.execute(
			Client.TAG,
			new String[] {},
			(String[])localOptions.toArray(new String[localOptions.size()]),
			arguments,
			managedProject,
			progress,
			getPrintStream());
	}
	
	/**
	 * Currently, we support only the optimistic model so uncheckout dores nothing.
	 * 
	 * @see ITeamProvider#uncheckout(IResource[], int, IProgressMonitor)
	 */
	public void uncheckout(IResource[] resources, int depth, IProgressMonitor progress) throws TeamException {
	}
	
	/**
	 * Generally usefull update
	 */
	public void update(IResource[] resources, int depth, IProgressMonitor progress) throws TeamException {
		// Build the local options
		List localOptions = new ArrayList();
		if (depth == IResource.DEPTH_INFINITE) {
			// if depth = infinite, look for new directories
			localOptions.add(Client.DEEP_OPTION);
			// For now, prune empty directories
			// This must be done by the client! (not the server)
			localOptions.add(Client.PRUNE_OPTION);
		}
		else
			// If depth = zero or 1, use -l
			localOptions.add(Client.LOCAL_OPTION);
		update(resources, depth, (String[])localOptions.toArray(new String[localOptions.size()]), progress);
		
	}
	/*
	 * CVS specific update
	 */
	private void update(IResource[] resources, int depth, String[] localOptions, IProgressMonitor progress) throws TeamException {
			
		// Build the arguments list
		String[] arguments = getValidArguments(resources, depth, progress);
			
		try {
			Client.execute(
				Client.UPDATE,
				DEFAULT_GLOBAL_OPTIONS,
				localOptions,
				arguments,
				managedProject,
				progress,
				getPrintStream());
		} catch(CVSException e) {
			refreshResources(resources, depth, e, progress);
		}
		refreshResources(resources, depth, null, progress);
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
	
	/**
	 * Cause a snapshot (this saves the sync info to disk)
	 */
	static void snapshot(IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		monitor.subTask(Policy.bind("CVSTeamProvider.snapshot"));
		ResourcesPlugin.getWorkspace().save(false, monitor);
	}
	/*
	 * @see ITeamProvider#isOutOfDate(IResource)
	 * XXX to be removed when sync methods are removed from ITeamProvider
	 */
	public boolean isOutOfDate(IResource resource) {
		return false;
	}
	
	/*
	 * @see IFileModificationValidator#validateEdit(IFile[], Object)
	 */
	public IStatus validateEdit(IFile[] files, Object context) {
		return new Status(Status.OK, TeamPlugin.ID, Status.OK, "OK", null);
	}
	/*
	 * @see IFileModificationValidator#validateSave(IFile)
	 */
	public IStatus validateSave(IFile file) {
		return new Status(Status.OK, TeamPlugin.ID, Status.OK, "OK", null);
	}	
}

