package org.eclipse.team.internal.ccvs.core;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.ccvs.core.ICVSProvider;
import org.eclipse.team.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.ccvs.core.ICVSRepositoryLocation;
import org.eclipse.team.ccvs.core.IConnectionMethod;
import org.eclipse.team.core.IFileTypeRegistry;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.internal.ccvs.core.connection.CVSRepositoryLocation;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.resources.RemoteResource;
import org.eclipse.team.internal.ccvs.core.util.ProjectDescriptionManager;

public class CVSProvider implements ICVSProvider {

	private static CVSProvider instance;
	private PrintStream printStream;
	private Map repositories;
	
	private CVSProvider() {
		repositories = new HashMap();
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
	private CVSRepositoryLocation buildRepository(Properties configuration, boolean cachePassword) throws CVSException {
		// We build a string to allow validation of the components that are provided to us
		// NOTE: This is a bit strange. We should call the constrructor directly
		StringBuffer repository = new StringBuffer(":");
		String connection = configuration.getProperty("connection");
		if (connection == null)
			repository.append("pserver");
		else 
			repository.append(connection);
		repository.append(":");
		String user = configuration.getProperty("user");
		if (user == null)
			throw new CVSException(new Status(IStatus.ERROR, CVSProviderPlugin.ID, TeamException.UNABLE, Policy.bind("CVSTeamProvider.noUser"), null));
		else 
			repository.append(user);
		repository.append("@");
		String host = configuration.getProperty("host");
		if (host == null)
			throw new CVSException(new Status(IStatus.ERROR, CVSProviderPlugin.ID, TeamException.UNABLE, Policy.bind("CVSTeamProvider.noHost"), null));
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
			throw new CVSException(new Status(IStatus.ERROR, CVSProviderPlugin.ID, TeamException.UNABLE, Policy.bind("CVSTeamProvider.noRoot"), null));
		else 
			repository.append(root);
		
		// NOTE: Check the cache to see if the instance already exists
		
		CVSRepositoryLocation location  = CVSRepositoryLocation.fromString(repository.toString());
		
		String password = configuration.getProperty("password");
		if (password != null) {
			if (cachePassword)
				location.storePassword(password);
			else
				location.setPassword(password);
		}
		
		return location;
	}
	
	/*
	 * Add the repository location to the cahced locations
	 */
	private void addToCache(ICVSRepositoryLocation repository) {
		repositories.put(repository.getLocation(), repository);
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
			
		try {
			
			// Create the project if one wasn't passed.
			// NOTE: This will need to be fixed for module alias support
			if (project == null)
				project = ResourcesPlugin.getWorkspace().getRoot().getProject(new Path(sourceModule).lastSegment());
				
			// Get the location of the workspace root
			ICVSFolder root = Client.getManagedFolder(ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile());
			
			// Build the local options
			List localOptions = new ArrayList();
			String module = project.getName();
			if (sourceModule != null) {
				localOptions.add(Client.DEEP_OPTION);
				localOptions.add(module);
				module = sourceModule;
			}
			// XXX Needs to be updated for date tags
			if (tag != null) {
				localOptions.add(Client.TAG_OPTION );
				localOptions.add(tag.getName());
			}
				
			// Perform a checkout
			Client.execute(
					Client.CHECKOUT,
					new String[0],
					(String[])localOptions.toArray(new String[localOptions.size()]),
					new String[]{module},
					root,
					monitor,
					getPrintStream(),
					(CVSRepositoryLocation)repository,
					null);
					
			// Create, open and/or refresh the project
			if (!project.exists())
				project.create(monitor);
			if (!project.isOpen())
				project.open(monitor);
			else
				project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
			
			// Get the meta file
			// NOTE: This is related to project meta-information and is subject to change
			ProjectDescriptionManager.updateProjectIfNecessary(project, monitor);
			
			// Register the project with Team
			TeamPlugin.getManager().setProvider(project, CVSProviderPlugin.NATURE_ID, null, monitor);
			
		} catch (CoreException e) {
			throw wrapException(e);
		}
	}

	/**
	 * @see ICVSProvider#checkout(IProject, Properties, IProgressMonitor)
	 */
	public void checkout(
		IProject project,
		Properties configuration,
		IProgressMonitor monitor)
		throws TeamException {
			
		CVSRepositoryLocation location = buildRepository(configuration, false);
		try {
			checkout(location, project, configuration.getProperty("module"), getTagFromProperties(configuration), monitor);
		} catch (TeamException e) {
			// The checkout may have triggered password caching
			// Therefore, if this is a newly created location, we want to clear its cache
			if (!isCached(location))
				location.dispose();
			throw e;
		}
		// We succeeded so we should cache the password and the location
		location.updateCache();
		addToCache(location);
	}

	/**
	 * @see ICVSProvider#checkout(ICVSRemoteResource[], IProject[], IProgressMonitor)
	 */
	public void checkout(
		final ICVSRemoteResource[] resources,
		final IProject[] projects,
		final IProgressMonitor monitor)
		throws TeamException {
			
		final TeamException[] eHolder = new TeamException[1];
		try {
			IWorkspaceRunnable workspaceRunnable = new IWorkspaceRunnable() {
				public void run(IProgressMonitor pm) throws CoreException {
					try {
						for (int i=0;i<resources.length;i++) {
							IProject project = null;
							RemoteResource resource = (RemoteResource)resources[i];
							if (projects != null) 
								project = projects[i];
							checkout(resource.getRepository(), project, resource.getRemotePath(), null, monitor);
						}
					}
					catch (TeamException e) {
						// Pass it outside the workspace runnable
						eHolder[0] = e;
					}
					// CoreException and OperationCanceledException are propagated
				}
			};
			ResourcesPlugin.getWorkspace().run(workspaceRunnable, monitor);
		} catch (CoreException e) {
			throw wrapException(e);
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
		ICVSRepositoryLocation repository = buildRepository(configuration, true);
		addToCache(repository);
		return repository;
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
						} else if (!("true".equals(registry.getValue(extension, "isAscii")))) {
							result.add("*." + extension);
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
		return (ICVSRepositoryLocation[])repositories.entrySet().toArray(new ICVSRepositoryLocation[repositories.size()]);
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
	 * @see ICVSProvider#getSupportedConnectionMethods()
	 */
	public String[] getSupportedConnectionMethods() {
		IConnectionMethod[] methods = CVSRepositoryLocation.getPluggedInConnectionMethods();
		String[] result = new String[methods.length];
		for (int i=0;i<methods.length;i++)
			result[i] = methods[i].getName();
		return result;
	}

	/*
	 * @see ICVSProvider#importAndCheckout(IProject, Properties, IProgressMonitor)
	 */
	public void importAndCheckout(
		IProject project,
		Properties configuration,
		IProgressMonitor monitor)
		throws TeamException {
			
		CVSRepositoryLocation location = buildRepository(configuration, false);
		try {
			importProject(location, project, configuration, monitor);
			checkout(location, project, configuration.getProperty("module"), getTagFromProperties(configuration), monitor);
		} catch (TeamException e) {
			// The checkout may have triggered password caching
			// Therefore, if this is a newly created location, we want to clear its cache
			if (!isCached(location))
				location.dispose();
			throw e;
		}
		// We succeeded so we should cache the password and the location
		location.updateCache();
		addToCache(location);
	}
		
	private CVSTag getTagFromProperties(Properties configuration) {
		String date = configuration.getProperty("date");
		String tagName = configuration.getProperty("tag");
		if (tagName == null)
			tagName = configuration.getProperty("branch");
		if (tagName == null)
			return CVSTag.DEFAULT;
		return new CVSTag(tagName, CVSTag.BRANCH);
	}
	public void importProject(
		ICVSRepositoryLocation location,
		IProject project,
		Properties configuration,
		IProgressMonitor monitor)
		throws TeamException {
			
			// Get the location of the workspace root
			ICVSFolder root = Client.getManagedFolder(project.getLocation().toFile());
		
			// Create the meta-file
			ProjectDescriptionManager.writeProjectDescription(project, monitor);
	
			// Get the message
			String message = configuration.getProperty("message");
			if (message == null)
				message = Policy.bind("CVSProvider.initialImport");
				
			// Get the vendor
			String vendor = configuration.getProperty("vendor");
			if (vendor == null)
				vendor = location.getUsername();
				
			// Get the vendor
			String tag = configuration.getProperty("tag");
			if (tag == null)
				tag = "start";
				
			// Get the module name
			String module = configuration.getProperty("module");
			if (module == null)
				module = project.getName();
				
			// Build the local options
			List localOptions = new ArrayList();
			localOptions.add(Client.MESSAGE_OPTION);
			localOptions.add(message);
			// Create filters for all known text files
			String[] patterns = getBinaryFilePatterns(project);
			for (int i=0;i<patterns.length;i++) {
				localOptions.add(Client.WRAPPER_OPTION);
				localOptions.add(patterns[i] + " -k 'b'");
			}
	
			// Perform a import
			Client.execute(
					Client.IMPORT,
					new String[] {},
					(String[])localOptions.toArray(new String[localOptions.size()]),
					new String[]{module, vendor, tag},
					root,
					monitor,
					getPrintStream(),
					(CVSRepositoryLocation)location,
					null);
			
			// NOTE: we should check to see the results of the import
		}

	public static void initialize() {
		if (instance == null)
			instance = new CVSProvider();
	}
	
	private boolean isCached(ICVSRepositoryLocation repository) {
		return repositories.containsKey(repository.getLocation());
	}
	
	private void removeFromCache(ICVSRepositoryLocation repository) {
		repositories.remove(repository.getLocation());
	}
	
	/**
	 * Set the stream to which CVS command output is sent
	 */
	public void setPrintStream(PrintStream out) {
		printStream = out;
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

}

