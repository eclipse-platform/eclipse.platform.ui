/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
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
     * Resource listener that reacts to new projects (and associated natures) 
     * coming into the workspace.
     */
    private IResourceChangeListener listener;
    
    /**
     * Mapping from repository provider id to IPluginContribution.  Used for proper
     * activity mapping of natures.
     */
    private Map providerIdToPluginId;

    /**
     * Singleton instance.
     */
    private static TeamCapabilityHelper singleton;
    
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
        listener = getChangeListener();
        ResourcesPlugin.getWorkspace().addResourceChangeListener(listener);
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
						final String id = element.getAttribute("id");
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
	 * Get a change listener for listening to resource changes.
	 */
    private IResourceChangeListener getChangeListener() {
        return new IResourceChangeListener() {
            public void resourceChanged(IResourceChangeEvent event) {
                if (!WorkbenchActivityHelper.isFiltering())
                    return;
                IResourceDelta mainDelta = event.getDelta();

                if (mainDelta == null)
                    return;
                
                //Has the root changed?
                if (mainDelta.getKind() == IResourceDelta.CHANGED
                    && mainDelta.getResource().getType() == IResource.ROOT) {
                    try {
                        IResourceDelta[] children = mainDelta.getAffectedChildren();
                        IWorkbenchActivitySupport workbenchActivitySupport = PlatformUI.getWorkbench().getActivitySupport();
                        for (int i = 0; i < children.length; i++) {
                            IResourceDelta delta = children[i];
                            if (delta.getResource().getType() == IResource.PROJECT) {
                                IProject project = (IProject) delta.getResource();
                                processProject(project, workbenchActivitySupport);
                            }
                        }
                    } catch (CoreException exception) {
                        //Do nothing if there is a CoreException
                    }
                }
            }
        };
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
		RepositoryProvider provider = RepositoryProvider.getProvider(project);
		if (provider == null)
			return;
		Set activities = new HashSet(activityManager.getEnabledActivityIds());
		boolean changed = false;

		IPluginContribution contribution = (IPluginContribution) providerIdToPluginId.get(provider.getID());
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
     * Unhooks the <code>IResourceChangeListener</code>.
     */ 
    public void shutdown() {
        if (listener != null) {
            ResourcesPlugin.getWorkspace().removeResourceChangeListener(listener);
        }   
        singleton = null;
    }
}
