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
package org.eclipse.team.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFileModificationValidator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IProjectNatureDescriptor;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.team.IMoveDeleteHook;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.internal.core.Policy;
import org.eclipse.team.internal.core.TeamPlugin;
import org.eclipse.team.internal.core.simpleAccess.SimpleAccessOperations;

/**
 * A concrete subclass of <code>RepositoryProvider</code> is created for each
 * project that is associated with a repository provider. The lifecycle of these
 * instances is is similar to that of the platform's 'nature' mechanism.
 * <p>
 * To create a repository provider and have it registered with the platform, a client
 * must minimally:
 * <ol>
 * 	<li>extend <code>RepositoryProvider</code>
 * 	<li>define a repository extension in <code>plugin.xml</code>. 
 *     Here is an example extension point definition:
 * 
 *  <code>
 *	<br>&lt;extension point="org.eclipse.team.core.repository"&gt;
 *  <br>&nbsp;&lt;repository
 *  <br>&nbsp;&nbsp;class="org.eclipse.myprovider.MyRepositoryProvider"
 *  <br>&nbsp;&nbsp;id="org.eclipse.myprovider.myProviderID"&gt;
 *  <br>&nbsp;&lt;/repository&gt;
 *	<br>&lt;/extension&gt;
 *  </code>
 * </ol></p>
 * <p>
 * Once a repository provider is registered with Team, then you
 * can associate a repository provider with a project by invoking <code>RepositoryProvider.map()</code>.
 * </p>
 * @see RepositoryProvider.map(IProject, String)
 *
 * @since 2.0
 */
public abstract class RepositoryProvider implements IProjectNature {
	
	private final static String TEAM_SETID = "org.eclipse.team.repository-provider"; //$NON-NLS-1$
	
	private final static QualifiedName PROVIDER_PROP_KEY = 
		new QualifiedName("org.eclipse.team.core", "repository");  //$NON-NLS-1$  //$NON-NLS-2$

	private final static List AllProviderTypeIds = initializeAllProviderTypes();
	
	// the project instance that this nature is assigned to
	private IProject project;	
	
	/**
	 * Instantiate a new RepositoryProvider with concrete class by given providerID
	 * and associate it with project.
	 * 
	 * @param project the project to be mapped
	 * @param id the ID of the provider to be mapped to the project
	 * @throws TeamException if
	 * <ul>
	 * <li>There is no provider by that ID.</li>
	 * <li>The project is already associated with a repository provider and that provider
	 * prevented its unmapping.</li>
	 * </ul>
	 * @see RepositoryProvider#unmap(IProject)
	 */
	public static void map(IProject project, String id) throws TeamException {
		try {
			RepositoryProvider existingProvider = null;

			if(project.getPersistentProperty(PROVIDER_PROP_KEY) != null)
				existingProvider = getProvider(project);	// get the real one, not the nature one
			
			//if we already have a provider, and its the same ID, we're ok
			//if the ID's differ, unmap the existing.
			if(existingProvider != null) {
				if(existingProvider.getID().equals(id))
					return;	//nothing to do
				else
					unmap(project);
			}
			
			// Create the provider as a session property before adding the persistant
			// property to ensure that the provider can be instantiated
			RepositoryProvider provider = mapNewProvider(project, id);

			//mark it with the persistent ID for filtering
			try {
				project.setPersistentProperty(PROVIDER_PROP_KEY, id);
			} catch (CoreException outer) {
				// couldn't set the persistant property so clear the session property
				try {
					project.setSessionProperty(PROVIDER_PROP_KEY, null);
				} catch (CoreException inner) {
					// something is seriously wrong
					TeamPlugin.log(IStatus.ERROR, Policy.bind("RepositoryProvider.couldNotClearAfterError", project.getName(), id), inner);//$NON-NLS-1$
				}
				throw outer;
			}	
			
			provider.configure();	//xxx not sure if needed since they control with wiz page and can configure all they want

			//adding the nature would've caused project description delta, so trigger one
			project.touch(null);
		} catch (CoreException e) {
			throw TeamPlugin.wrapException(e);
		}
	}	

	/*
	 * Instantiate the provider denoted by ID and store it in the session property.
	 * Return the new provider instance. If a TeamException is thrown, it is
	 * guaranteed that the session property will not be set.
	 * 
	 * @param project
	 * @param id
	 * @return RepositoryProvider
	 * @throws TeamException we can't instantiate the provider, or if the set
	 * session property fails from core
	 */
	private static RepositoryProvider mapNewProvider(IProject project, String id) throws TeamException {
		RepositoryProvider provider = newProvider(id); 	// instantiate via extension point

		if(provider == null)
			throw new TeamException(Policy.bind("RepositoryProvider.couldNotInstantiateProvider", project.getName(), id)); //$NON-NLS-1$
		
		// validate that either the provider supports linked resources or the project has no linked resources
		if (!provider.canHandleLinkedResources()) {
			try {
				IResource[] members = project.members();
				for (int i = 0; i < members.length; i++) {
					IResource resource = members[i];
					if (resource.isLinked()) {
						throw new TeamException(new Status(IStatus.ERROR, TeamPlugin.ID, IResourceStatus.LINKING_NOT_ALLOWED, Policy.bind("RepositoryProvider.linkedResourcesExist", project.getName(), id), null)); //$NON-NLS-1$
					}
				}
			} catch (CoreException e) {
				throw TeamPlugin.wrapException(e);
			}
		}
		
		//store provider instance as session property
		try {
			project.setSessionProperty(PROVIDER_PROP_KEY, provider);
			provider.setProject(project);
		} catch (CoreException e) {
			throw TeamPlugin.wrapException(e);
		}
		return provider;
	}	

	/**
	 * Disassoociates project with the repository provider its currently mapped to.
	 * @param project
	 * @throws TeamException The project isn't associated with any repository provider.
	 */
	public static void unmap(IProject project) throws TeamException {
		try{
			String id = project.getPersistentProperty(PROVIDER_PROP_KEY);
			
			//If you tried to remove a non-existant nature it would fail, so we need to as well with the persistent prop
			if(id == null) {
				throw new TeamException(Policy.bind("RepositoryProvider.No_Provider_Registered", project.getName())); //$NON-NLS-1$
			}
			
			//This will instantiate one if it didn't already exist,
			//which is ok since we need to call deconfigure() on it for proper lifecycle
			RepositoryProvider provider = getProvider(project);
			if (provider == null) {
				// There is a persistant property but the provider cannot be obtained.
				// The reason could be that the provider's plugin is no longer available.
				// Better log it just in case this is unexpected.
				TeamPlugin.log(new Status(IStatus.ERROR, TeamPlugin.ID, 0, 
					Policy.bind("RepositoryProvider.couldNotInstantiateProvider", project.getName(), id), null));  //$NON-NLS-1$
			}

			if (provider != null) provider.deconfigure();
							
			project.setSessionProperty(PROVIDER_PROP_KEY, null);
			project.setPersistentProperty(PROVIDER_PROP_KEY, null);
			
			if (provider != null) provider.deconfigured();
			
			//removing the nature would've caused project description delta, so trigger one
			project.touch(null);	
		} catch (CoreException e) {
			throw TeamPlugin.wrapException(e);
		}
	}	
	
	/*
	 * Return the provider mapped to project, or null if none;
	 */
	private static RepositoryProvider lookupProviderProp(IProject project) throws CoreException {
		return (RepositoryProvider) project.getSessionProperty(PROVIDER_PROP_KEY);
	}	


	/**
	 * Default constructor required for the resources plugin to instantiate this class from
	 * the nature extension definition.
	 */
	public RepositoryProvider() {
	}

	/**
	 * Configures the provider for the given project. This method is called after <code>setProject</code>. 
	 * If an exception is generated during configuration
	 * of the project, the provider will not be assigned to the project.
	 * 
	 * @throws CoreException if the configuration fails. 
	 */
	abstract public void configureProject() throws CoreException;
	
	/**
	 * Configures the nature for the given project. This is called by <code>RepositoryProvider.map()</code>
	 * the first time a provider is mapped to a project. It is not intended to be called by clients.
	 * 
	 * @throws CoreException if this method fails. If the configuration fails the provider will not be
	 * associated with the project.
	 * 
	 * @see RepositoryProvider#configureProject()
	 */
	final public void configure() throws CoreException {
		try {
			configureProject();
		} catch(CoreException e) {
			try {
				RepositoryProvider.unmap(getProject());
			} catch(TeamException e2) {
				throw new CoreException(new Status(IStatus.ERROR, TeamPlugin.ID, 0, Policy.bind("RepositoryProvider_Error_removing_nature_from_project___1") + getID(), e2)); //$NON-NLS-1$
			}
			throw e;
		}
	}

	/**
	 * Method deconfigured is invoked after a provider has been unmaped. The
	 * project will no longer have the provider associated with it when this
	 * method is invoked. It is a last chance for the provider to clean up.
	 */
	protected void deconfigured() {
	}
	
	/**
	 * Answer the id of this provider instance. The id should be the repository provider's 
	 * id as defined in the provider plugin's plugin.xml.
	 * 
	 * @return the nature id of this provider
	 */
	abstract public String getID();

	/**
	 * Returns an <code>IFileModificationValidator</code> for pre-checking operations 
 	 * that modify the contents of files.
 	 * Returns <code>null</code> if the provider does not wish to participate in
 	 * file modification validation.
 	 * 
	 * @see org.eclipse.core.resources.IFileModificationValidator
	 */
	
	public IFileModificationValidator getFileModificationValidator() {
		return null;
	}
	
	/**
	 * Returns an <code>IMoveDeleteHook</code> for handling moves and deletes
	 * that occur withing projects managed by the provider. This allows providers 
	 * to control how moves and deletes occur and includes the ability to prevent them. 
	 * <p>
	 * Returning <code>null</code> signals that the default move and delete behavior is desired.
	 * 
	 * @see org.eclipse.core.resources.IMoveDeleteHook
	 */
	public IMoveDeleteHook getMoveDeleteHook() {
		return null;
	}
	
	/**
	 * Returns a brief description of this provider. The exact details of the
	 * representation are unspecified and subject to change, but the following
	 * may be regarded as typical:
	 * 
	 * "SampleProject:org.eclipse.team.cvs.provider"
	 * 
	 * @return a string description of this provider
	 */
	public String toString() {
		return Policy.bind("RepositoryProvider.toString", getProject().getName(), getID());   //$NON-NLS-1$
	}
	
	/**
	 * Returns all known (registered) RepositoryProvider ids.
	 * 
	 * @return an array of registered repository provider ids.
	 */
	final public static String[] getAllProviderTypeIds() {
		IProjectNatureDescriptor[] desc = ResourcesPlugin.getWorkspace().getNatureDescriptors();
		Set teamSet = new HashSet();
		
		teamSet.addAll(AllProviderTypeIds);	// add in all the ones we know via extension point
		
		//fall back to old method of nature ID to find any for backwards compatibility
		for (int i = 0; i < desc.length; i++) {
			String[] setIds = desc[i].getNatureSetIds();
			for (int j = 0; j < setIds.length; j++) {
				if(setIds[j].equals(TEAM_SETID)) {
					teamSet.add(desc[i].getNatureId());
				}
			}
		}
		return (String[]) teamSet.toArray(new String[teamSet.size()]);
	}
	
	/**
	 * Returns the provider for a given IProject or <code>null</code> if a provider is not associated with 
	 * the project or if the project is closed or does not exist. This method should be called if the caller 
	 * is looking for <b>any</b> repository provider. Otherwise call <code>getProvider(project, id)</code>
	 * to look for a specific repository provider type.
	 * </p>
	 * @param project the project to query for a provider
	 * @return the repository provider associated with the project
	 */
	final public static RepositoryProvider getProvider(IProject project) {
		try {					
			if(project.isAccessible()) {
				
				//-----------------------------
				//First, look for the session property
				RepositoryProvider provider = lookupProviderProp(project);
				if(provider != null)
					return provider;
				
				// -----------------------------
				//Next, check if it has the ID as a persistent property, if yes then instantiate provider
				String id = project.getPersistentProperty(PROVIDER_PROP_KEY);
				if(id != null)
					return mapNewProvider(project, id);
				
				//Couldn't find using new method, fall back to lookup using natures for backwards compatibility
				//-----------------------------		
				IProjectDescription projectDesc = project.getDescription();
				String[] natureIds = projectDesc.getNatureIds();
				IWorkspace workspace = ResourcesPlugin.getWorkspace();
				// for every nature id on this project, find it's natures sets and check if it is
				// in the team set.
				for (int i = 0; i < natureIds.length; i++) {
					IProjectNatureDescriptor desc = workspace.getNatureDescriptor(natureIds[i]);
					// The descriptor can be null if the nature doesn't exist
					if (desc != null) {
						String[] setIds = desc.getNatureSetIds();
						for (int j = 0; j < setIds.length; j++) {
							if(setIds[j].equals(TEAM_SETID)) {
								return getProvider(project, natureIds[i]);
							}			
						}
					}
				}
			}
		} catch(CoreException e) {
			TeamPlugin.log(e.getStatus());
		} catch(TeamException e) {
			TeamPlugin.log(e.getStatus());
		}
		return null;
	}
	
	/**
	 * Returns a provider of type with the given id if associated with the given project 
	 * or <code>null</code> if the project is not associated with a provider of that type
	 * or the nature id is that of a non-team repository provider nature.
	 * 
	 * @param project the project to query for a provider
	 * @param id the repository provider id
	 * @return the repository provider
	 */
	final public static RepositoryProvider getProvider(IProject project, String id) {
		try {
			if(project.isAccessible()) {
				String existingID = project.getPersistentProperty(PROVIDER_PROP_KEY);

				if(id.equals(existingID)) {
					//if the IDs are the same then they were previously mapped
					//see if we already instantiated one
					RepositoryProvider provider = lookupProviderProp(project);  //throws core, we will reuse the catching already here
					if(provider != null)
						return provider;
					//otherwise instantiate and map a new one					
					return mapNewProvider(project, id);
				}
					
				//couldn't find using new method, fall back to lookup using natures for backwards compatibility
				//-----------------------------

				// if the nature id given is not in the team set then return
				// null.
				IProjectNatureDescriptor desc = ResourcesPlugin.getWorkspace().getNatureDescriptor(id);
				if(desc == null) //for backwards compat., may not have any nature by that ID
					return null;
					
				String[] setIds = desc.getNatureSetIds();
				for (int i = 0; i < setIds.length; i++) {
					if(setIds[i].equals(TEAM_SETID)) {
						return (RepositoryProvider)project.getNature(id);
					}			
				}
			}
		} catch(CoreException e) {
			// would happen if provider nature id is not registered with the resources plugin
			TeamPlugin.log(new Status(IStatus.WARNING, TeamPlugin.ID, 0, Policy.bind("RepositoryProviderTypeRepositoryProvider_not_registered_as_a_nature_id___3", id), e)); //$NON-NLS-1$
		} catch(TeamException e) {
			TeamPlugin.log(e.getStatus());
		}
		return null;
	}
	
	/**
	 * Returns whether the given project is shared or not. This is a lightweight
	 * method in that it will not instantiate a provider instance (as
	 * <code>getProvider</code> would) if one is not already instantiated.
	 * 
	 * Note that IProject.touch() generates a project description delta.  This, in combination
	 * with isShared() can be used to be notified of sharing/unsharing of projects.
	 * 
	 * @param project the project being tested.
	 * @return boolean
	 * 
	 * @see getProvider(IProject)
	 */
	public static boolean isShared(IProject project) {
		if (!project.isAccessible()) return false;
		try {
			if (lookupProviderProp(project) != null) return true;
			return project.getPersistentProperty(PROVIDER_PROP_KEY) != null;
		} catch (CoreException e) {
			TeamPlugin.log(e.getStatus());
			return false;
		}
	}
	
	/**
	 * Provisional non-API method.
	 *
	 * This method is here to allow experimentation with 3rd party tools
	 * calling providers in a repository neutral manner.
     *
 	 * Returns an object which implements a set of provider neutral operations for this 
 	 * provider. Answers <code>null</code> if the provider does not wish to support these 
 	 * operations.
 	 * 
 	 * @return the repository operations or <code>null</code> if the provider does not
 	 * support provider neutral operations.
 	 * @see SimpleAccessOperations
 	 */
	public SimpleAccessOperations getSimpleAccess() {
 		return null;
 	}
 	
	/*
	 * @see IProjectNature#getProject()
	 */
	public IProject getProject() {
		return project;
	}

	/*
	 * @see IProjectNature#setProject(IProject)
	 */
	public void setProject(IProject project) {
		this.project = project;
	}
	
	private static List initializeAllProviderTypes() {
		List allIDs = new ArrayList();
		
		TeamPlugin plugin = TeamPlugin.getPlugin();
		if (plugin != null) {
			IExtensionPoint extension = plugin.getDescriptor().getExtensionPoint(TeamPlugin.REPOSITORY_EXTENSION);
			if (extension != null) {
				IExtension[] extensions =  extension.getExtensions();
				for (int i = 0; i < extensions.length; i++) {
					IConfigurationElement [] configElements = extensions[i].getConfigurationElements();
					for (int j = 0; j < configElements.length; j++) {
						String extensionId = configElements[j].getAttribute("id"); //$NON-NLS-1$
						allIDs.add(extensionId);
					}
				}
			}
		}
		return allIDs;
	}

	private static RepositoryProvider newProvider(String id) {
		TeamPlugin plugin = TeamPlugin.getPlugin();
		if (plugin != null) {
			IExtensionPoint extension = plugin.getDescriptor().getExtensionPoint(TeamPlugin.REPOSITORY_EXTENSION);
			if (extension != null) {
				IExtension[] extensions =  extension.getExtensions();
				for (int i = 0; i < extensions.length; i++) {
					IConfigurationElement [] configElements = extensions[i].getConfigurationElements();
					for (int j = 0; j < configElements.length; j++) {
						String extensionId = configElements[j].getAttribute("id"); //$NON-NLS-1$
						if (extensionId != null && extensionId.equals(id)) {
							try {
								return (RepositoryProvider) configElements[j].createExecutableExtension("class"); //$NON-NLS-1$
							} catch (CoreException e) {
								TeamPlugin.log(e.getStatus());
							} catch (ClassCastException e) {
								String className = configElements[j].getAttribute("class"); //$NON-NLS-1$
								TeamPlugin.log(IStatus.ERROR, Policy.bind("RepositoryProvider.invalidClass", id, className), e); //$NON-NLS-1$
							}
							return null;
						}
					}
				}
			}		
		}
		return null;
	}	
	
	/**
	 * Convert a project that are using natures to mark them as Team projects
	 * to instead use persistent properties. Optionally remove the nature from the project.
	 * Do nothing if the project has no Team nature.
	 * Assume that the same ID is used for both the nature and the persistent property.
	 * 
	 * @deprecated
	 */
	public static void convertNatureToProperty(IProject project, boolean removeNature) throws TeamException {
		RepositoryProvider provider = RepositoryProvider.getProvider(project);
		if(provider == null)
			return;
			
		String providerId = provider.getID();	
		
		RepositoryProvider.map(project, providerId);
		if(removeNature) {
			Team.removeNatureFromProject(project, providerId, new NullProgressMonitor());
		}
	}
	
	/**
	 * Method validateCreateLink is invoked by the Platform Core TeamHook when a
	 * linked resource is about to be added to the provider's project. It should
	 * not be called by other clients and it should not need to be overridden by
	 * subclasses (although it is possible to do so in special cases).
	 * Subclasses can indicate that they support linked resources by overridding
	 * the <code>canHandleLinkedResources()</code> method.
	 * 
	 * @param resource see <code>org.eclipse.core.resources.team.TeamHook</code>
	 * @param updateFlags see <code>org.eclipse.core.resources.team.TeamHook</code>
	 * @param location see <code>org.eclipse.core.resources.team.TeamHook</code>
	 * @return IStatus see <code>org.eclipse.core.resources.team.TeamHook</code>
	 * 
	 * @see RepositoryProvider#canHandleLinkedResources()
	 */
	public IStatus validateCreateLink(IResource resource, int updateFlags, IPath location) {
		if (canHandleLinkedResources()) {
			return Team.OK_STATUS;
		} else {
			return new Status(IStatus.ERROR, TeamPlugin.ID, IResourceStatus.LINKING_NOT_ALLOWED, Policy.bind("RepositoryProvider.linkedResourcesNotSupported", getProject().getName(), getID()), null); //$NON-NLS-1$
		}
	}
	
	/**
	 * Method canHandleLinkedResources should be overridden by subclasses who
	 * support linked resources. At a minimum, supporting linked resources
	 * requires changes to the move/delete hook 
	 * (see org.eclipe.team.resources.team.IMoveDeleteHook). This method is
	 * called after the RepositoryProvider is instantiated but before
	 * <code>setProject()</code> is invoked so it will not have access to any
	 * state determined from the <code>setProject()</code> method.
	 * @return boolean
	 * 
	 * @see org.eclipe.team.resources.team.IMoveDeleteHook
	 */
	public boolean canHandleLinkedResources() {
		return false;
	}
}	
