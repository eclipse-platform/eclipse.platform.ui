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
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.team.IMoveDeleteHook;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
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
 * 	<li>extend <code>RepositoryProvider<code>
 * 	<li>define a repository extension in <code>plugin.xml</code>. 
 *     Here is an example extension point definition:
 * 
 *  <code>
 *	&lt;extension point="org.eclipse.team.core.repository"&gt;
 *      &lt;repository
 *            class="org.eclipse.myprovider.MyRepositoryProvider"
 *            id="org.eclipse.myprovider.myProviderID"&gt;
 *      &lt;/repository&gt;
 *	&lt;/extension&gt;
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
			
			RepositoryProvider provider = mapNewProvider(project, id);

			//mark it with the persistent ID for filtering
			project.setPersistentProperty(PROVIDER_PROP_KEY, id);		
			provider.configure();	//xxx not sure if needed since they control with wiz page and can configure all they want

			//adding the nature would've caused project description delta, so trigger one
			project.touch(null);
		} catch (CoreException e) {
			throw TeamPlugin.wrapException(e);
		}
	}	

	/*
	 * Instantiate the provider denoted by ID and store it in the session property.
	 * Return the new provider instance.
	 * @param project
	 * @param id
	 * @return RepositoryProvider
	 * @throws TeamException Tthe we can't instantiate the provider,
	 * or if the set session property fails from core
	 */
	private static RepositoryProvider mapNewProvider(IProject project, String id) throws TeamException {
		RepositoryProvider provider = newProvider(id); 	// instantiate via extension point

		if(provider == null)
			throw new TeamException(Policy.bind("RepositoryProvider.couldNotInstantiateProvider", project.getName(), id));
			
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
			
			//If you tried to remove a non-existance nature it would fail, so we need to as well with the persistent prop
			if(id == null)
				throw new TeamException(Policy.bind("RepositoryProvider.No_Provider_Registered", project.getName())); //$NON-NLS-1$

			//This will instantiate one if it didn't already exist,
			//which is ok since we need to call deconfigure() on it for proper lifecycle
			RepositoryProvider provider = getProvider(project);
			if (provider == null) {
				// There is a persistant property but the provider cannot be obtained.
				// The reason could be that the provider's plugin is no longer available.
				// Better log it just in case this is unexpected.
				TeamPlugin.log(new Status(IStatus.ERROR, TeamPlugin.ID, 0, 
					Policy.bind("RepositoryProvider.couldNotInstantiateProvider", project.getName(), id), null)); 
			} else {
				provider.deconfigure();
			}
							
			project.setSessionProperty(PROVIDER_PROP_KEY, null);
			project.setPersistentProperty(PROVIDER_PROP_KEY, null);
			
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
				//First check if we are using the persistent property to tag the project with provider

				//
				String id = project.getPersistentProperty(PROVIDER_PROP_KEY);
				RepositoryProvider provider = lookupProviderProp(project);  //throws core, we will reuse the catching already here

				//If we have the session but not the persistent, we have a problem
				//because we somehow got only halfway through mapping before
				if(id == null && provider != null) {
					TeamPlugin.log(IStatus.ERROR, Policy.bind("RepositoryProvider.propertyMismatch", project.getName()), null);
					project.setSessionProperty(PROVIDER_PROP_KEY, null); //clears it
					return null;
				}
				
				if(provider != null)
					return provider;
					
				//check if it has the ID as a persistent property, if yes then instantiate provider
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
	
	/*
	 * Provisional.
 	 * Returns an object which implements a set of provider neutral operations for this 
 	 * provider. Answers <code>null</code> if the provider does not wish to support these 
 	 * operations.
 	 * 
 	 * @return the repository operations or <code>null</code> if the provider does not
 	 * support provider neutral operations.
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
								return null;
							}
						}
					}
				}
			}		
		}
		return null;
	}	
	
	/*
	 * Convert a project that are using natures to mark them as Team projects
	 * to instead use persistent properties. Optionally remove the nature from the project.
	 * Do nothing if the project has no Team nature.
	 * Assume that the same ID is used for both the nature and the persistent property.
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
}
