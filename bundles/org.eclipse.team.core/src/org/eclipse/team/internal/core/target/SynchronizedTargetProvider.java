package org.eclipse.team.internal.core.target;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.ISynchronizer;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.core.*;
import org.eclipse.team.core.target.*;
import org.eclipse.team.internal.core.Policy;
import org.eclipse.team.internal.core.Assert;

public abstract class SynchronizedTargetProvider extends TargetProvider {

	/*
	 * Configuration serialization identifier.
	 */

	private static final int CONFIG_FORMAT_VERSION = 2;

	private final int depth = IResource.DEPTH_INFINITE;

	/**
	 * These interfaces are to operations that can be performed on the array of resources,
	 * and on all resources identified by the depth parameter.
	 * @see execute(IOperation, IResource[], int, IProgressMonitor)
	 */
	protected static interface IOperation {
	}
	protected static interface IIterativeOperation extends IOperation {
		public IStatus visit(IResource resource, int depth, IProgressMonitor progress);
	}
	protected static interface IRecursiveOperation extends IOperation {
		public IStatus visit(IResource resource, IProgressMonitor progress);
	}

	/**
	 * Create a target provider.
	 */
	public SynchronizedTargetProvider() {
		super();
	}
	 
	/*************** State Factory ***************/

	public void registerWithSynchronizer() {
		getSynchronizer().add(getIdentifier());
	}

	/**
	 * Answers the synchronizer.
	 */		
	final protected static ISynchronizer getSynchronizer() {
		return ResourcesPlugin.getWorkspace().getSynchronizer();
	}

	/**
	 * Get the state descriptor for a given resource.
	 */
	public ResourceState getState(IResource resource) {
		// Create a new resource state with default values.
		ResourceState state = newState(resource);
		state.loadState();
		return state;
	}

	/**
	 * Answers a new state based on an existing local resource.
	 */
	abstract public ResourceState newState(IResource resource);

	abstract public String getLocatorString(IResource resource);

	/**
	 * Providers must override this method to configure instances based on the given
	 * properties map.  Different providers will require different types of configuration,
	 * and therefore they will look for different keys in the properties table.  If the provider
	 * cannot be configured with the values given a <code>TeamException</code> is
	 * thrown.  Subclasses should override this method (and call <code>
	 * super.configureProvider(Properties)</code>).
	 */
//	abstract public void configure(Properties properties) throws TeamException;



	/*************** Inherited Methods ***************/

	/**
	 * Get the resource from the provider to the workspace, and remember the fetched
	 * state as the base state of the resource.
	 * 
	 * @see ITeamProvider.get(IResource[], int, IProgressMonitor)
	 */
	public void get(IResource[] resources, IProgressMonitor progress)
		throws TeamException {
		execute(new IIterativeOperation() {
			public IStatus visit(IResource resource, int depth, IProgressMonitor progress) {
				ResourceState state = getState(resource);
				return new Symmetria().get(state, depth, progress);
			}
		}, resources, depth, progress);
	}


	/**
	 * Put the resources to the remote.
	 */
	public void put(
		IResource[] resources,
		IProgressMonitor progress)
		throws TeamException {
			
		execute(new IRecursiveOperation() {
			public IStatus visit(IResource resource, IProgressMonitor progress) {
				// The resource state must be checked-out.
				ResourceState state = getState(resource);
				return state.checkin(progress);
			}
		}, resources, depth, progress);
	}

	/**
	 * Delete the corresponding remote resource.
	 * Note that deletes are always deep.
	 */
	public void delete(IResource[] resources, IProgressMonitor progress)
		throws TeamException {
		execute(new IIterativeOperation() {
			public IStatus visit(IResource resource, int depth, IProgressMonitor progress) {
				ResourceState state = getState(resource);
				return state.delete(progress);
			}
		}, resources, IResource.DEPTH_INFINITE, progress);
	}

	/**
	 * Answer if the local resource currently has a different timestamp to the
	 * base timestamp for this resource.
	 * 
	 * @param resource the resource to test.
	 * @return <code>true</code> if the resource has a different modification
	 * timestamp, and <code>false</code> otherwise.
	 * @see ITeamSynch#isDirty(IResource)
	 */
	public boolean isDirty(IResource resource) {
		ResourceState state = getState(resource);
		return state.isDirty(resource);
	}

	/**
	 * Answers true if the base identifier of the given resource is different to the
	 * current released state of the resource.
	 * 
	 * @param resource the resource state to test.
	 * @return <code>true</code> if the resource base identifier is different to the
	 * current released state of the resource, and <code>false</code> otherwise.
	 * @see ITeamSynch#isOutOfDate(IResource)
	 */
	public boolean isOutOfDate(IResource resource) {
		ResourceState state = getState(resource);
		return state.isOutOfDate();
	}

	/**
	 * Answer whether the resource has a corresponding remote resource in the provider.
	 * 
	 * @param resource the resource state to test.
	 * @return <code>true</code> if the resource has a corresponding remote resource,
	 * and <code>false</code> otherwise.
	 * @see ITeamSynch#hasRemote(IResource)
	 */
	public boolean hasRemote(IResource resource) {
		ResourceState state = getState(resource);
		return state.hasRemote();
	}

	/*
	 * @see ITeamProvider#refreshState(IResource[], int, IProgressMonitor)
	 */
	public void refreshState(
		IResource[] resources,
		int depth,
		IProgressMonitor progress)
		throws TeamException {
	}

	public String getDecorationTextPostFix(IResource resource) {
		return "";
	}

	public String getDecorationLocation(IResource resource) {
		return getLocatorString(resource);
	}


	/**
	 * Perform the given operation on the array of resources, each to the
	 * specified depth.  Throw an exception if a problem ocurs, otherwise
	 * remain silent.
	 */
	protected void execute(
		IOperation operation,
		IResource[] resources,
		int depth,
		IProgressMonitor progress)
		throws TeamException {
			
		// Create an array to hold the status for each resource.
		IStatus[] statuses = new IStatus[resources.length];
		
		// Remember if a failure occurred in any resource, so we can throw an exception at the end.
		boolean failureOccurred = false;

		// For each resource in the local resources array.
		for (int i = 0; i < resources.length; i++) {
			if (operation instanceof IRecursiveOperation)
				statuses[i] = execute((IRecursiveOperation)operation, resources[i], depth, progress);
			else
				statuses[i] = ((IIterativeOperation)operation).visit(resources[i], depth, progress);
			failureOccurred = failureOccurred || (!statuses[i].isOK());
		}

		// Finally, if any problems occurred, throw the exeption with all the statuses,
		// but if there were no problems exit silently.
		if (failureOccurred)
			throw new TeamException(
				new MultiStatus(
					TeamPlugin.ID,
					IStatus.ERROR,
					statuses,
					Policy.bind("multiStatus.errorsOccurred"),
					null));

		// Cause all the resource changes to be broadcast to listeners.
//		TeamPlugin.getManager().broadcastResourceStateChanges(resources);
	}

	/**
	 * Perform the given operation on a resource to the given depth.
	 */
	protected IStatus execute(
		IRecursiveOperation operation,
		IResource resource,
		int depth,
		IProgressMonitor progress) {

		// Visit the given resource first.
		IStatus status = operation.visit(resource, progress);

		// If the resource is a file then the depth parameter is irrelevant.
		if (resource.getType() == IResource.FILE)
			return status;

		// If we are not considering any members of the container then we are done.
		if (depth == IResource.DEPTH_ZERO)
			return status;

		// If the operation was unsuccessful, do not attempt to go deep.
		if (!status.isOK())
			return status;

		// If the container has no children then we are done.
		IResource[] members = getMembers(resource);
		if (members.length == 0)
			return status;
		
		// There are children and we are going deep, the response will be a multi-status.
		MultiStatus multiStatus =
			new MultiStatus(
				status.getPlugin(),
				status.getCode(),
				status.getMessage(),
				status.getException());
				
		// The next level will be one less than the current level...
		int childDepth =
			(depth == IResource.DEPTH_ONE)
				? IResource.DEPTH_ZERO
				: IResource.DEPTH_INFINITE;
				
		// Collect the responses in the multistatus.
		for (int i = 0; i < members.length; i++)
			multiStatus.add(execute(operation, members[i], childDepth, progress));

		return multiStatus;
	}

	/**
	 * Answers an array of local resource members for the given resource
	 * or an empty arrray if the resource has no members.
	 * 
	 * @param resource the local resource whose members are required.
	 * @return an array of <code>IResource</code> or an empty array if
	 * the resource has no members.
	 */
	protected IResource[] getMembers(IResource resource) {
		if (resource.getType() != IResource.FILE) {
			try {
				return ((IContainer) resource).members();
			} catch (CoreException exception) {
				exception.printStackTrace();
				throw new RuntimeException();
			}
		} //end-if
		else
			return new IResource[0];
	}

	/*
	 * @see IProjectNature#setProject()
	 */
	public void setProject(IProject project) {
		super.setProject(project);
/*		try {
			restoreConfiguration();  This will fail because root is null first time
		} catch (CoreException e) {
			TeamPlugin.log(IStatus.ERROR, "Error configuring project", e);
		} catch (IOException e) {
			TeamPlugin.log(IStatus.ERROR, "Error configuring project", e);
		}
*/	}


	private static String[] getAllTargetTypes() {
		return new String[] {"org.eclipse.team.webdav"};
	}

	
	/*************** Synchronizer/Configuration ***************/

/*	public static void restoreAllProviders() throws CoreException, IOException {
		String[] targetTypes = getAllTargetTypes();
		QualifiedName[] partners = getSynchronizer().getPartners();
		boolean registered = false;
		
		for (int i = 0; i < partners.length; i++) {
			for (int j = 0; j < targetTypes.length; j++) {
				String targetType = partners[i].getQualifier();
				if(targetType == targetTypes[j]) {
					String secondaryKey = partners[i].getLocalName();
					IProject project =...
					newProvider(project, targetType, secondaryKey);
				}
			}
		}
	}
*/
	
	/*
	 * Retrieve configuration information for the receiver
	 * if previously configure, and reinstantiate that configuration.
	 */
	 
	public void restoreConfiguration() throws CoreException, IOException {
		QualifiedName configKey = getIdentifier();
		QualifiedName[] partners = getSynchronizer().getPartners();
		boolean registered = false;
		
		for (int i = 0; i < partners.length; i++) {
			if(partners[i].equals(configKey))
				registered = true;	
		}
		
		if(registered) {	
			Properties properties = loadConfiguration();
			if(properties == null) {
				return;
			}
			storeConfiguration(properties);
		}
	}
		

	/*
	 * This method is called once to configure the receiver after creation.
	 */
	public void configure(Properties configuration, IProject project) throws IOException, CoreException, TeamException {
		Assert.isNotNull(configuration);
		
		setProject(project);
		
		// Configure the receiver with the new config info.
//		configure(configuration);

		// Store the new configuration for future.
		registerWithSynchronizer();

		storeConfiguration(configuration);
	}

	/*
	 * @see IProjectNature#deconfigure()
	 */
	public void deconfigure() throws CoreException {
	}

	/**
	 * Associates the given configuration with the given project.
	 * Stores the configuration under the <code>getIdentifier()</code> key.
	 */
	private void storeConfiguration(Properties configuration) throws IOException, CoreException {
		// Remove any old configuration first.
		QualifiedName configKey = getIdentifier();
		getSynchronizer().flushSyncInfo(configKey, getProject(), IResource.DEPTH_INFINITE);
		
		// Flatten the configuration to bytes.
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		byteStream.write(CONFIG_FORMAT_VERSION);
		configuration.store(byteStream, null);
		byteStream.close();
		byte[] bytes = byteStream.toByteArray();
		
		// Store the configuration persistently.
		getSynchronizer().setSyncInfo(configKey, getProject(), bytes);
		ResourcesPlugin.getWorkspace().save(false, null);
	}


	/**
	 * Loads a configuration serialized from a given project.
	 * Returns <code>null</code> if there is no serialized configuration or
	 * the configuration is invalid.
	 * 
	 * @param project the project with an associated configuration.
	 * @return the loaded configuration, or <code>null</code> if there is
	 * no such configuration.
	 */
	private Properties loadConfiguration() {
		// The project must exist for us to get the configuration.
		if (!getProject().exists())
			return null;

		try {
			// Get the stored byte representation, if any.
			QualifiedName configKey = getIdentifier();
			byte[] bytes = getSynchronizer().getSyncInfo(configKey, getProject());
			if ((bytes == null) || (bytes.length == 0))
				return null;

			ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);

			// Check version identifier.
			if (CONFIG_FORMAT_VERSION != inputStream.read())
				return null;
			// Read the properties from the stream.
			Properties result = new Properties();
			result.load(inputStream);
			return result;
		} catch (IOException e) {
			return null;
		} catch (CoreException e) {
			e.printStackTrace();
			throw new RuntimeException();
		} 
	}


	}