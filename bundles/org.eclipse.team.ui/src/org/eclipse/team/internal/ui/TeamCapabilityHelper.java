/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui;

import java.util.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.internal.core.TeamPlugin;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.*;

/**
 * Utility class that manages promotion of team capabilities in response to workspace changes
 * and existing repository providers.
 * 
 * @since 3.0
 */
public class TeamCapabilityHelper {
    
    /**
     * Mapping from repository provider id to IPluginContribution.  Used for proper
     * activity mapping of natures.
     */
    private Map providerIdToPluginId;

    /**
     * Singleton instance.
     */
    private static TeamCapabilityHelper singleton;
    
    /*
     * This is copied from RepositoryProviderType to provide a quick way to query if
     * a project is mapped to a provider id.
     */
    private final static QualifiedName PROVIDER_PROP_KEY = 
		new QualifiedName("org.eclipse.team.core", "repository");  //$NON-NLS-1$  //$NON-NLS-2$
    
    /**
     * Get the singleton instance of this class.
     * @return the singleton instance of this class.
     * @since 3.0
     */
    public static TeamCapabilityHelper getInstance() {
        if (singleton == null) {
            singleton = new TeamCapabilityHelper();            
        }
        return singleton;
    }
    
    /**
     * Create a new <code>IDEWorkbenchActivityHelper</code> which will listen 
     * for workspace changes and promote activities accordingly.
     */
    private TeamCapabilityHelper() {
    	providerIdToPluginId = new HashMap();
        loadRepositoryProviderIds();
 
        // crawl the initial projects
        IProject [] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        IWorkbenchActivitySupport workbenchActivitySupport = PlatformUI.getWorkbench().getActivitySupport();
        for (int i = 0; i < projects.length; i++) {
            try {
                processProject(projects[i], workbenchActivitySupport);
            } catch (CoreException e) {
                // do nothing
            }
        }
   }
    
   /**
    * Loads the list of registered provider types
    */
   public void loadRepositoryProviderIds() {
		providerIdToPluginId.clear();
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint("org.eclipse.team.core.repository"); //$NON-NLS-1$
		if (point != null) {
			IExtension[] extensions = point.getExtensions();
			for (int i = 0; i < extensions.length; i++) {
				IExtension extension = extensions[i];
				IConfigurationElement[] elements = extension.getConfigurationElements();
				for (int j = 0; j < elements.length; j++) {
					IConfigurationElement element = elements[j];
					final String pluginId = extension.getDeclaringPluginDescriptor().getUniqueIdentifier();
					if (element.getName().equals(TeamPlugin.REPOSITORY_EXTENSION)) {
						final String id = element.getAttribute("id"); //$NON-NLS-1$
						if (id == null) {
							// bad extension point
							continue;
						}
						providerIdToPluginId.put(id, new IPluginContribution() {
							public String getLocalId() {
								return id;
							}
							public String getPluginId() {
								return pluginId;
							}
						});
					}
				}
			}
		}
	}

    /**
     * Handle natures for the given project.
     * 
     * @param project the project
     * @param workbenchActivitySupport the activity support
     */
    protected void processProject(IProject project, IWorkbenchActivitySupport workbenchActivitySupport) throws CoreException {
		if (!project.isOpen())
			return;
		IActivityManager activityManager = workbenchActivitySupport
				.getActivityManager();
		String id = getProviderIdFor(project);
		if (id == null)
			return;
		Set activities = new HashSet(activityManager.getEnabledActivityIds());
		boolean changed = false;

		IPluginContribution contribution = (IPluginContribution) providerIdToPluginId.get(id);
		if (contribution == null)
			return; //bad provider ID.
		IIdentifier identifier = activityManager.getIdentifier(WorkbenchActivityHelper.createUnifiedId(contribution));
		if (activities.addAll(identifier.getActivityIds())) {
			changed = true;
		}

		if (changed)
			workbenchActivitySupport.setEnabledActivityIds(activities);
	}

    /**
     * Returns the provider id for this project or <code>null</code> if no providers are mapped
     * to this project. Note that this won't instantiate the provider, but instead will simply query
     * the persistent property
     * 
     * @param project the project to query.
     * @return the provider id for this project or <code>null</code> if no providers are mapped
     * to this project
     * @throws CoreException
     */
    public String getProviderIdFor(IProject project) throws CoreException {
    	String id = null;
    	if(project.isAccessible()) {	
			//First, look for the session property
			RepositoryProvider provider = (RepositoryProvider)project.getSessionProperty(PROVIDER_PROP_KEY);
			if(provider != null)
				id = provider.getID();
			//Next, check if it has the ID as a persistent property
			id = project.getPersistentProperty(PROVIDER_PROP_KEY);
    	}
    	return id;
    }
}
