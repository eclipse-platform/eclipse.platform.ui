package org.eclipse.team.internal.ccvs.core;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

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
import org.eclipse.core.runtime.Status;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.ccvs.core.CVSStatus;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.ccvs.core.ICVSListener;
import org.eclipse.team.ccvs.core.ICVSProvider;
import org.eclipse.team.ccvs.core.ICVSRemoteFolder;
import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.ccvs.core.IConnectionMethod;
import org.eclipse.team.core.IFileTypeRegistry;
import org.eclipse.team.core.ITeamManager;
import org.eclipse.team.core.ITeamProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.internal.ccvs.core.client.Checkout;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Import;
import org.eclipse.team.internal.ccvs.core.client.Session;
import org.eclipse.team.internal.ccvs.core.client.Command.GlobalOption;
import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
import org.eclipse.team.internal.ccvs.core.client.Command.QuietOption;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.connection.CVSServerException;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFolder;
import org.eclipse.team.internal.ccvs.core.resources.RemoteFolderTree;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.util.Util;

public class CVSProvider implements ICVSProvider {

	private static final String STATE_FILE = ".cvsProviderState"; //$NON-NLS-1$
	
	private static CVSProvider instance;
	private PrintStream printStream;
	private Map repositories;
	
	private List listeners = new ArrayList();
		
	private CVSProvider() {
		repositories = new HashMap();
	}
	
	/*
	 * Add the repository location to the cahced locations
	 */
	private void addToCache(ICVSRepositoryLocation repository) {
		repositories.put(repository.getLocation(), repository);
	}
	
	private void repositoryAdded(ICVSRepositoryLocation repository) {
		Iterator it = listeners.iterator();
		while (it.hasNext()) {
			ICVSListener listener = (ICVSListener)it.next();
			listener.repositoryAdded(repository);
		}
	}
	
	public void addRepositoryListener(ICVSListener listener) {
		listeners.add(listener);
	}
	
	public void removeRepositoryListener(ICVSListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * @see ICVSProvider#checkout(ICVSRepositoryLocation, IProject, String, String, IProgressMonitor)
	 */
	public void checkout(
		ICVSRepositoryLocation repository,
		IProject project,
		String sourceModule,
		CVSTag tag,
		IProgressMonitor monitor)
		throws TeamException {
			
		// Create the project if one wasn't passed.
		// NOTE: This will need to be fixed for module alias support
		if (project == null)
			project = ResourcesPlugin.getWorkspace().getRoot().getProject(new Path(sourceModule).lastSegment());
			
		// Get the location of the workspace root
		ICVSFolder root = Session.getManagedFolder(ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile());
		
		// Build the local options
		List localOptions = new ArrayList();
		// Add the option to load into a directory of a different name
		String module = project.getName();
		if (sourceModule != null) {
			localOptions.add(Checkout.makeDirectoryNameOption(module));
			module = sourceModule;
		}
		// Prune empty directories if pruning enabled
		if (CVSProviderPlugin.getPlugin().getPruneEmptyDirectories()) 
			localOptions.add(Checkout.PRUNE_EMPTY_DIRECTORIES);
		// Add the options related to the CVSTag
		if (tag != null && tag.getType() != CVSTag.HEAD) {
			localOptions.add(Checkout.makeTagOption(tag));
		}
		
		// Perform a checkout
		IStatus status;
		Session s = new Session(repository, root);
		s.open(monitor);
		try {
			status = Command.CHECKOUT.execute(s,
				getDefaultGlobalOptions(),
				(LocalOption[])localOptions.toArray(new LocalOption[localOptions.size()]),
				new String[]{module},
				null,
				monitor);
		} finally {
			s.close();
		}
		if (status.getCode() == CVSStatus.SERVER_ERROR) {
			throw new CVSServerException(status);
		}
		
		try {
			// Create, open and/or refresh the project
			if (!project.exists())
				project.create(Policy.subMonitorFor(monitor, 1));
			if (!project.isOpen())
				project.open(Policy.subMonitorFor(monitor, 5));
			else
				project.refreshLocal(IResource.DEPTH_INFINITE, Policy.subMonitorFor(monitor, 5));
						
			// Register the project with Team
			// (unless the project already has the proper nature from the project meta-information)
			if (!project.getDescription().hasNature(CVSProviderPlugin.NATURE_ID)) {
				TeamPlugin.getManager().setProvider(project, CVSProviderPlugin.NATURE_ID, null, Policy.subMonitorFor(monitor, 1));
			}
			
		} catch (CoreException e) {
			throw wrapException(e);
		} finally {
			monitor.done();
		}
	}

	/**
	 * @see ICVSProvider#checkout(ICVSRemoteResource[], IProject[], IProgressMonitor)
	 */
	public void checkout(final ICVSRemoteFolder[] resources, final IProject[] projects, IProgressMonitor monitor) throws TeamException {
		final TeamException[] eHolder = new TeamException[1];
		try {
			IWorkspaceRunnable workspaceRunnable = new IWorkspaceRunnable() {
				public void run(IProgressMonitor pm) throws CoreException {
					try {
						pm.setTaskName(Policy.bind("Checking_out_from_CVS..._5")); //$NON-NLS-1$
						pm.beginTask(null, 1000 * resources.length);
						for (int i=0;i<resources.length;i++) {
							IProject project = null;
							RemoteFolder resource = (RemoteFolder)resources[i];
							if (projects != null) 
								project = projects[i];
							checkout(resource.getRepository(), project, resource.getRepositoryRelativePath(), resource.getTag(), Policy.subMonitorFor(pm, 1000));
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
			throw wrapException(e);
		} finally {
			monitor.done();
		}		
		// Re-throw the TeamException, if one occurred
		if (eHolder[0] != null) {
			throw eHolder[0];
		}
	}

	/**
	 * @see ICVSProvider#createRepository(Properties)
	 */
	public ICVSRepositoryLocation createRepository(Properties configuration) throws CVSException {
		// Create a new repository location
		CVSRepositoryLocation location = CVSRepositoryLocation.fromProperties(configuration);
		
		// Check the cache for an equivalent instance and if there is one, throw an exception
		CVSRepositoryLocation existingLocation = (CVSRepositoryLocation)repositories.get(location.getLocation());
		if (existingLocation != null) {
			throw new CVSException(new CVSStatus(CVSStatus.ERROR, Policy.bind("CVSProvider.alreadyExists")));
		}
		
		// Set or cache the password
		String password = configuration.getProperty("password"); //$NON-NLS-1$
		if (password != null) {
			location.storePassword(password);
		}
		
		addToCache(location);
		repositoryAdded(location);

		return location;
	}

	/**
	 * @see ICVSProvider#disposeRepository(ICVSRepositoryLocation)
	 */
	public void disposeRepository(ICVSRepositoryLocation repository) throws CVSException {
		((CVSRepositoryLocation)repository).dispose();
		removeFromCache(repository);
	}

	/*
	 * Returns all patterns in the given project that should be treated as binary
	 */
	private String[] getBinaryFilePatterns(IProject project) throws TeamException {
		final IFileTypeRegistry registry = TeamPlugin.getFileTypeRegistry();
		final Set result = new HashSet();
		try {
			project.accept(new IResourceVisitor() {
				public boolean visit(IResource resource) {
					if (resource.getType() == IResource.FILE) {
						String extension = resource.getFileExtension();
						if (extension == null) {
							result.add(resource.getName());
						} else if (!("true".equals(registry.getValue(extension, "isAscii")))) { //$NON-NLS-1$ //$NON-NLS-2$
							result.add("*." + extension); //$NON-NLS-1$
						}
					}
					// Always return true and let the depth determine if children are visited
					return true;
				}
			}, IResource.DEPTH_INFINITE, false);
		} catch (CoreException e) {
			throw wrapException(e);
		}
		return (String[])result.toArray(new String[result.size()]);
	}
	
	public static GlobalOption[] getDefaultGlobalOptions() {
		QuietOption option = CVSProviderPlugin.getPlugin().getQuietness();
		if (option == null)
			return Command.NO_GLOBAL_OPTIONS;
		else
			return new GlobalOption[] {option};
	}
	
	/**
	 * Return the singleton instance of CVSProvider
	 */
	public static CVSProvider getInstance() {
		return instance;
	}
	
	/**
	 * @see ICVSProvider#getKnownRepositories()
	 */
	public ICVSRepositoryLocation[] getKnownRepositories() {
		return (ICVSRepositoryLocation[])repositories.values().toArray(new ICVSRepositoryLocation[repositories.size()]);
	}


	/**
	 * Get the print stream to which information from CVS commands
	 * is sent.
	 */
	public PrintStream getPrintStream() {
		if (printStream == null)
			return System.out;
		else
			return printStream;
	}
	
	/**
	 * @see ICVSProvider#getRepository(String)
	 */
	public ICVSRepositoryLocation getRepository(String location) throws CVSException {
		ICVSRepositoryLocation repository = (ICVSRepositoryLocation)repositories.get(location);
		if (repository == null) {
			repository = CVSRepositoryLocation.fromString(location);
			addToCache(repository);
			repositoryAdded(repository);
		}
		return repository;
	}
	
	/**
	 * @see ICVSProvider#getSupportedConnectionMethods()
	 */
	public String[] getSupportedConnectionMethods() {
		IConnectionMethod[] methods = CVSRepositoryLocation.getPluggedInConnectionMethods();
		String[] result = new String[methods.length];
		for (int i=0;i<methods.length;i++)
			result[i] = methods[i].getName();
		return result;
	}

	/**
	 * @see ICVSProvider#createModule()
	 */
	public void createModule(ICVSRepositoryLocation location, IProject project, String moduleName, IProgressMonitor monitor) throws TeamException {
		boolean alreadyExists = isCached(location);
		addToCache(location);
		try {
			// Get the import properties
			String message = Policy.bind("CVSProvider.initialImport");
			String vendor = location.getUsername();
			String tag = "start"; //$NON-NLS-1$
			String projectName = project.getName();
			if (moduleName == null)
				moduleName = projectName;

			// Perform the import using a dummy root so the local project is not traversed
			Session s = new Session(location, new RemoteFolderTree(null, location, Path.EMPTY, null));
			s.open(monitor);
			try {
				IStatus status = Command.IMPORT.execute(s,
					getDefaultGlobalOptions(),
					new LocalOption[] {Import.makeMessageOption(message)},
					new String[] { moduleName, vendor, tag },
					null,
					monitor);
				if (status.getCode() == CVSStatus.SERVER_ERROR) {
					throw new CVSServerException(status);
				}
			} finally {
				s.close();
			}
			
			// Set the folder sync info of the project to point to the remote module
			ICVSFolder folder = (ICVSFolder)Session.getManagedResource(project);
			folder.setFolderSyncInfo(new FolderSyncInfo(moduleName, location.getLocation(), null, false));

			// Register the project with Team
			// (unless the project already has the proper nature from the project meta-information)
			try {
				if (!project.getDescription().hasNature(CVSProviderPlugin.NATURE_ID)) {
					TeamPlugin.getManager().setProvider(project, CVSProviderPlugin.NATURE_ID, null, Policy.subMonitorFor(monitor, 1));
				}
			} catch (CoreException e) {
				throw wrapException(e);
			} 
		} catch (TeamException e) {
			// The checkout may have triggered password caching
			// Therefore, if this is a newly created location, we want to clear its cache
			if ( ! alreadyExists)
				disposeRepository(location);
			throw e;
		} finally {
			CVSProviderPlugin.getSynchronizer().save(project.getLocation().toFile(), Policy.subMonitorFor(monitor, 5));
		}
		// We succeeded so we should cache the password ...
		((CVSRepositoryLocation)location).updateCache();
		// and notify listeners if the location wasn't cached before
		if (! alreadyExists)
			repositoryAdded(location);
	}
		
	private CVSTag getTagFromProperties(Properties configuration) {
		String date = configuration.getProperty("date"); //$NON-NLS-1$
		String tagName = configuration.getProperty("tag"); //$NON-NLS-1$
		if (tagName == null)
			tagName = configuration.getProperty("branch"); //$NON-NLS-1$
		if (tagName == null)
			return CVSTag.DEFAULT;
		return new CVSTag(tagName, CVSTag.BRANCH);
	}
	
	private boolean isCached(ICVSRepositoryLocation repository) {
		return repositories.containsKey(repository.getLocation());
	}
	
	public static boolean isText(String filename) {
		IFileTypeRegistry registry = TeamPlugin.getFileTypeRegistry();
		int lastDot = filename.lastIndexOf('.');
		// Assume files with no extension are binary
		if (lastDot == -1) return false;
		String extension = filename.substring(lastDot + 1);
		return ((extension != null) && ("true".equals(registry.getValue(extension, "isAscii")))); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	private void removeFromCache(ICVSRepositoryLocation repository) {
		repositories.remove(repository.getLocation());
		Iterator it = listeners.iterator();
		while (it.hasNext()) {
			ICVSListener listener = (ICVSListener)it.next();
			listener.repositoryRemoved(repository);
		}
	}
	
	/**
	 * Set the stream to which CVS command output is sent
	 */
	public void setPrintStream(PrintStream out) {
		printStream = out;
	}
	
	public void setSharing(IProject project, FolderSyncInfo info, IProgressMonitor monitor) throws TeamException {
		
		// Ensure provided info matches that of the project
		ICVSFolder folder = (ICVSFolder)Session.getManagedResource(project);
		FolderSyncInfo folderInfo = folder.getFolderSyncInfo();
		if ( ! info.equals(folderInfo)) {
			throw new CVSException(new CVSStatus(CVSStatus.ERROR, "Provided CVS information does not match that on disk"));
		}
		
		// Ensure that the provided location is managed
		ICVSRepositoryLocation location = getRepository(info.getRoot());
		if (! isCached(location)) {
			addToCache(location);
			repositoryAdded(location);
		}
		
		// Register the project with Team
		// (unless the project already has the proper nature from the project meta-information)
		try {
			if (!project.getDescription().hasNature(CVSProviderPlugin.NATURE_ID))
				TeamPlugin.getManager().setProvider(project, CVSProviderPlugin.NATURE_ID, null, monitor);
		} catch (CoreException e) {
			throw wrapException(e);
		} finally {
			CVSProviderPlugin.getSynchronizer().save(project.getLocation().toFile(), Policy.subMonitorFor(monitor, 5));
		}
	}
	
	private IStatus statusFor(CoreException e) {
		return new Status(IStatus.ERROR, CVSProviderPlugin.ID, CVSException.UNABLE, getMessageFor(e), e);
	}
	
	private CVSException wrapException(CoreException e) {
		return new CVSException(statusFor(e));
	}
	
	private String getMessageFor(Exception e) {
		String message = Policy.bind(e.getClass().getName(), new Object[] {e.getMessage()});
		if (message.equals(e.getClass().getName()))
			message = Policy.bind("CVSProvider.exception", new Object[] {e.toString()}); 
		return message;
	}

	public static void startup() {
		if (instance == null) {
			instance = new CVSProvider();
		}
		try {
			getInstance().loadState();
		} catch (TeamException e) {
			Util.logError("Error loading state", e);
		}
	}
	
	public static void shutdown() {
		try {
			getInstance().saveState();
		} catch (TeamException e) {
			Util.logError("Error saving state", e);
		}
	}
	
	private void loadState() throws TeamException {
		IPath pluginStateLocation = CVSProviderPlugin.getPlugin().getStateLocation().append(STATE_FILE);
		File file = pluginStateLocation.toFile();
		if (file.exists()) {
			try {
				DataInputStream dis = new DataInputStream(new FileInputStream(file));
				readState(dis);
				dis.close();
			} catch (IOException e) {
				throw new TeamException(new Status(Status.ERROR, CVSProviderPlugin.ID, TeamException.UNABLE, Policy.bind("CVSProvider.ioException"), e));
			}
		}  else {
			// If the file did not exist, then prime the list of repositories with
			// the providers with which the projects in the workspace are shared.
			IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
			ITeamManager manager = TeamPlugin.getManager();
			for (int i = 0; i < projects.length; i++) {
				ITeamProvider provider = manager.getProvider(projects[i]);
				if (provider instanceof CVSTeamProvider) {
					CVSTeamProvider cvsProvider = (CVSTeamProvider)provider;
					ICVSFolder folder = (ICVSFolder)Session.getManagedResource(projects[i]);
					FolderSyncInfo info = folder.getFolderSyncInfo();
					if (info != null) {
						ICVSRepositoryLocation result = getRepository(info.getRoot());
						addToCache(result);
						repositoryAdded(result);
					}
				}
			}
		}
	}
	
	private void saveState() throws TeamException {
		IPath pluginStateLocation = CVSProviderPlugin.getPlugin().getStateLocation();
		File tempFile = pluginStateLocation.append(STATE_FILE + ".tmp").toFile(); //$NON-NLS-1$
		File stateFile = pluginStateLocation.append(STATE_FILE).toFile();
		try {
			DataOutputStream dos = new DataOutputStream(new FileOutputStream(tempFile));
			writeState(dos);
			dos.close();
			if (stateFile.exists()) {
				stateFile.delete();
			}
			boolean renamed = tempFile.renameTo(stateFile);
			if (!renamed) {
				throw new TeamException(new Status(Status.ERROR, CVSProviderPlugin.ID, TeamException.UNABLE, Policy.bind("RepositoryManager.rename", tempFile.getAbsolutePath()), null));
			}
		} catch (IOException e) {
			throw new TeamException(new Status(Status.ERROR, CVSProviderPlugin.ID, TeamException.UNABLE, Policy.bind("RepositoryManager.save",stateFile.getAbsolutePath()), e));
		}
	}
	private void readState(DataInputStream dis) throws IOException, CVSException {
		int count = dis.readInt();
		for (int i = 0; i < count; i++) {
			getRepository(dis.readUTF());
		}
	}
	
	private void writeState(DataOutputStream dos) throws IOException {
		// Write the repositories
		Collection repos = repositories.values();
		dos.writeInt(repos.size());
		Iterator it = repos.iterator();
		while (it.hasNext()) {
			ICVSRepositoryLocation root = (ICVSRepositoryLocation)it.next();
			dos.writeUTF(root.getLocation());
		}
	}
}

