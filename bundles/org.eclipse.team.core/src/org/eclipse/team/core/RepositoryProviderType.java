package org.eclipse.team.core;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.core.internal.Policy;

/**
 * Describes a type of repository provider snf provides
 * 
 * @see RepositoryProvider
 */
abstract public class RepositoryProviderType {
	
	// contains a list of registered provider types keyed by provider type id.
	// {keys = String (ids) values = RepositoryProviderType
	private static final Map providerTypes =  new HashMap();
	
	/**
	 * Default constructor required for the team plugin to instantiate this class from a
	 * extension definition.
	 */
	public RepositoryProviderType() {
	}
	
	/**
	 * Registers a provider type. This method is not intended to be called by clients and should only be
	 * called by the team plugin.
	 * 
	 * @throws TeamException if the provider type is already registered.
	 */
	/*package*/ final static void addProviderType(RepositoryProviderType providerType) throws TeamException {
		if(providerTypes.containsKey(providerType.getID())) {
			throw new TeamException(new Status(IStatus.ERROR, TeamPlugin.ID, 0, Policy.bind("RepositoryProviderTypeduplicate_provider_found_in_plugin.xml___1") + providerType.getID(), null)); //$NON-NLS-1$
		} else {
			providerTypes.put(providerType.getID(), providerType);			
		}
	}

	/**
	 * Returns all known (registered) <code>RepositoryProviderType</code>.
	 * 
	 * @return an array of registered <code>RepositoryProviderType</code> instances.
	 */
	final public static RepositoryProviderType[] getAllProviderTypes() {
		return (RepositoryProviderType[]) providerTypes.values().toArray(new RepositoryProviderType[providerTypes.size()]);
	}
	
	/**
	 * Returns the provider for a given IProject or <code>null</code> if a provider is not associated with 
	 * the project. This assumes that only one repository provider can be associated with a project at a
	 * time.
	 * 
	 * @return a repository provider for the project or <code>null</code> if the project is not 
	 * associated with a provider.
	 */
	final public static RepositoryProvider getProvider(IProject project) {
		RepositoryProviderType[] allTypes = getAllProviderTypes();
		for (int i = 0; i < allTypes.length; i++) {
			RepositoryProvider provider = allTypes[i].getInstance(project);
			if(provider!=null) {
				return provider;
			}
		}
		return null;
	}
	
	/**
	 * Returns the provider type instance with the given id or <code>null</code> if a provider type
	 * with that id is not registered.
	 * 
	 * @return a provider type with the given id or <code>null</code> if a provider with that id
	 * is not registered.
	 */
	final public static RepositoryProviderType getProviderType(String id) {
		return (RepositoryProviderType) providerTypes.get(id);
	}
	
	/**
	 * Returns a provider of type the receiver if associated with the given project or <code>null</code>
	 * if the project is not associated with a provider of that type.
	 * 
	 * @return the repository provider
	 */
	final public RepositoryProvider getInstance(IProject project) {
		String id = getID();
		try {
			if(project.exists() && project.isOpen()) {
				return (RepositoryProvider)project.getNature(id);
			}
		} catch(ClassCastException e) {
			TeamPlugin.log(new Status(IStatus.ERROR, TeamPlugin.ID, 0, Policy.bind("RepositoryProviderTypeRepositoryProvider_assigned_to_the_project_must_be_a_subclass_of_RepositoryProvider___2") + id, e)); //$NON-NLS-1$
		} catch(CoreException ex) {
			// would happen if provider nature id is not registered with the resources plugin
			TeamPlugin.log(new Status(IStatus.WARNING, TeamPlugin.ID, 0, Policy.bind("RepositoryProviderTypeRepositoryProvider_not_registered_as_a_nature_id___3") + id, ex)); //$NON-NLS-1$
		}
		return null;
	}
	
	/**
	 * Returns the unique identifier for this provider type. 
	 * For example, org.eclipse.team.repotype.provider.
	 * This identifier must match the <code>IProjectNature</code> ID of
	 * the corresponding <code>RepositoryProvider</code>.
	 * 
	 * @return the id
	 */
	abstract public String getID();
	
	/**
	 * Returns all instances of the providers of this type.
	 * 
	 * @return an array of repository providers
	 */
	public RepositoryProvider[] getAllInstances() {
		// traverse projects in workspace and return the list of project that have our id as the nature id.
		List projectsWithMyId = new ArrayList();
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (int i = 0; i < projects.length; i++) {
			RepositoryProvider provider = getInstance(projects[i]);
			if(provider!=null) {
				projectsWithMyId.add(provider);
			}
		}
		return (RepositoryProvider[]) projectsWithMyId.toArray(new RepositoryProvider[projectsWithMyId.size()]);
	}
	
	/**
	 * Returns a description of this provider type. The exact details of the
	 * representation are unspecified and subject to change, but the following
	 * may be regarded as typical:
	 * 
	 * "org.eclipse.team.cvs.provider"
	 * 
	 * @return a string description of this provider type
	 */
	public String toString() {
		return getID();
	}
}