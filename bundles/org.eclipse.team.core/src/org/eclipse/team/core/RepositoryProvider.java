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
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFileModificationValidator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IProjectNatureDescriptor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.team.IMoveDeleteHook;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.core.internal.Policy;
import org.eclipse.team.core.internal.simpleAccess.SimpleAccessOperations;

/**
 * A concrete subclass of <code>RepositoryProvider</code> is created for each
 * project that is associated with a repository provider. The lifecycle of these
 * instances is managed by the platform's 'nature' mechanism.
 * <p>
 * To create a repository provider and have it registered with the platform a client
 * must minimally:
 * <ol>
 * 	<li>extend <code>RepositoryProvider<code>
 * 	<li>define a nature extension in <code>plugin.xml</code> that is
 * 	part of the "org.eclipse.team.repository-provider" set. Having the repository nature
 * 	assigned to the team set allows cardinality restrictions to be inforced by the platform 
 * 	(e.g. one repository provider can assigned to a project at a time). Here is an
 * 	example extension point definition:
 * 		<code>
 * 		<extension point="org.eclipse.core.resources.natures" id="myprovidernature" name="MyRepositoryType">
 *		 	<runtime>
 *		 		<run class="org.eclipse.myprovider.MyRepositoryProvider"/>
 *		 	</runtime>
 *		 	<one-of-nature id="org.eclipse.team.repository-provider"/>
 *		    </extension>
 *		    </p>
 *		    </code>
 * </ol></p>
 * <p>
 * Once a repository provider is registered as a nature and is in the team set, then you
 * can associate a repository provider with a project by assigning to the project the 
 * nature id of the repository provider.
 * </p>
 * @see IProjectNature
 * @see RepositoryProviderType
 *
 * @since 2.0
 */
public abstract class RepositoryProvider implements IProjectNature {
	
	private final static String TEAM_SETID = "org.eclipse.team.repository-provider"; //$NON-NLS-1$
	
	// the project instance that this nature is assigned to
	private IProject project;	
	
	/**
	 * Default constructor required for the resources plugin to instantiate this class from
	 * the nature extension definition.
	 */
	public RepositoryProvider() {
	}
	
	/**
	 * Configures the nature for the given project. This method is called after <code>setProject</code>
	 * and before the nature is added to the project. If an exception is generated during configuration
	 * of the project, the nature will not be assigned to the project.
	 * 
	 * @throws CoreException if the configuration fails. 
	 */
	abstract public void configureProject() throws CoreException;
	
	/**
	 * Configures the nature for the given project. This is called by the platform when a nature is assigned
	 * to a project. It is not intended to be called by clients.
	 * 
	 * @throws CoreException if this method fails. If the configuration fails the nature will not be added 
	 * to the project.
	 * 
	 * @see IProjectNature#configure
	 */
	final public void configure() throws CoreException {
		try {
			configureProject();
		} catch(CoreException e) {
			try {
				removeNatureFromProject(getProject(), getID(), null);
			} catch(TeamException e2) {
				throw new CoreException(new Status(IStatus.ERROR, TeamPlugin.ID, 0, Policy.bind("RepositoryProvider_Error_removing_nature_from_project___1") + getID(), e2)); //$NON-NLS-1$
			}
			throw e;
		}
	}

	/**
	 * Answer the id of this provider instance. The id should be the repository provider's 
	 * nature id.
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
		return getProject().getName() + ":" + getID(); //$NON-NLS-1$
	}
	
	/**
	 * Returns all known (registered) RepositoryProvider ids.
	 * 
	 * @return an array of registered repository provider ids.
	 */
	final public static String[] getAllProviderTypeIds() {
		IProjectNatureDescriptor[] desc = ResourcesPlugin.getWorkspace().getNatureDescriptors();
		List teamSet = new ArrayList();
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
	 * yo look for a specific repository provider type.
	 * </p>
	 * @param project the project to query for a provider
	 * @return the repository provider associated with the project
	 */
	final public static RepositoryProvider getProvider(IProject project) {
		try {
			if(project.isAccessible()) {
				IProjectDescription projectDesc = project.getDescription();
				String[] natureIds = projectDesc.getNatureIds();
				IWorkspace workspace = ResourcesPlugin.getWorkspace();
				// for every nature id on this project, find it's natures sets and check if it is
				// in the team set.
				for (int i = 0; i < natureIds.length; i++) {
					IProjectNatureDescriptor desc = workspace.getNatureDescriptor(natureIds[i]);
					String[] setIds = desc.getNatureSetIds();
					for (int j = 0; j < setIds.length; j++) {
						if(setIds[j].equals(TEAM_SETID)) {
							return getProvider(project, natureIds[i]);
						}			
					}
				}
			}
		} catch(CoreException e) {
			TeamPlugin.log(new Status(IStatus.ERROR, TeamPlugin.ID, 0, Policy.bind(""), e)); //$NON-NLS-1$
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
				// if the nature id given is not in the team set then return
				// null.
				IProjectNatureDescriptor desc = ResourcesPlugin.getWorkspace().getNatureDescriptor(id);
				String[] setIds = desc.getNatureSetIds();
				for (int i = 0; i < setIds.length; i++) {
					if(setIds[i].equals(TEAM_SETID)) {
						return (RepositoryProvider)project.getNature(id);
					}			
				}
			}
		} catch(CoreException ex) {
			// would happen if provider nature id is not registered with the resources plugin
			TeamPlugin.log(new Status(IStatus.WARNING, TeamPlugin.ID, 0, Policy.bind("RepositoryProviderTypeRepositoryProvider_not_registered_as_a_nature_id___3") + id, ex)); //$NON-NLS-1$
		}
		return null;
	}
	
	/**
	 * Utility method for adding a nature to a project.
	 * 
	 * @param proj the project to add the nature
	 * @param natureId the id of the nature to assign to the project
	 * @param monitor a progress monitor to indicate the duration of the operation, or
	 * <code>null</code> if progress reporting is not required.
	 * 
	 * @exception TeamException if a problem occured setting the nature
	 */
	final public static void addNatureToProject(IProject proj, String natureId, IProgressMonitor monitor) throws TeamException {
		try {
			IProjectDescription description = proj.getDescription();
			String[] prevNatures= description.getNatureIds();
			String[] newNatures= new String[prevNatures.length + 1];
			System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
			newNatures[prevNatures.length]= natureId;
			description.setNatureIds(newNatures);
			proj.setDescription(description, monitor);
		} catch(CoreException e) {
				throw new TeamException(new Status(IStatus.ERROR, TeamPlugin.ID, 0, Policy.bind("manager.errorSettingNature",  //$NON-NLS-1$
														 proj.getName(), natureId), e));
		}
	}
	
	/**
	 * Utility method for removing a project nature from a project.
	 * 
	 * @param proj the project to remove the nature from
	 * @param natureId the nature id to remove
	 * @param monitor a progress monitor to indicate the duration of the operation, or
	 * <code>null</code> if progress reporting is not required.
	 */
	final public static void removeNatureFromProject(IProject proj, String natureId, IProgressMonitor monitor) throws TeamException {
		try {
			IProjectDescription description = proj.getDescription();
			String[] prevNatures= description.getNatureIds();
			List newNatures = new ArrayList(Arrays.asList(prevNatures));
			newNatures.remove(natureId);
			description.setNatureIds((String[])newNatures.toArray(new String[newNatures.size()]));
			proj.setDescription(description, monitor);
		} catch(CoreException e) {
				throw new TeamException(new Status(IStatus.ERROR, TeamPlugin.ID, 0, Policy.bind("manager.errorRemovingNature",  //$NON-NLS-1$
														 proj.getName(), natureId), e));
		}
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
}
