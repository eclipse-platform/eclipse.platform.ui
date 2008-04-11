/*******************************************************************************
 * Copyright (c) 2003, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.activities.ITriggerPoint;
import org.eclipse.ui.activities.IWorkbenchActivitySupport;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * Utility class that manages promotion of activites in response to workspace changes.
 * 
 * @since 3.0
 */
public class IDEWorkbenchActivityHelper {

    private static final String NATURE_POINT = "org.eclipse.ui.ide.natures"; //$NON-NLS-1$
    
    /**
     * Resource listener that reacts to new projects (and associated natures) 
     * coming into the workspace.
     */
    private IResourceChangeListener listener;

    /**
     * Mapping from composite nature ID to IPluginContribution.  Used for proper
     * activity mapping of natures.
     */
    private Map natureMap;

    /**
     * Lock for the list of nature ids to be processed.
     */
	private final IDEWorkbenchActivityHelper lock;
	
	/**
	 * The update job.
	 */
	private WorkbenchJob fUpdateJob;
	
	/**
	 * The collection of natures to process.
	 */
	private HashSet fPendingNatureUpdates= new HashSet();

    /**
     * Singleton instance.
     */
    private static IDEWorkbenchActivityHelper singleton;

    /**
     * Get the singleton instance of this class.
     * @return the singleton instance of this class.
     * @since 3.0
     */
    public static IDEWorkbenchActivityHelper getInstance() {
        if (singleton == null) {
            singleton = new IDEWorkbenchActivityHelper();
        }
        return singleton;
    }

    /**
     * Create a new <code>IDEWorkbenchActivityHelper</code> which will listen 
     * for workspace changes and promote activities accordingly.
     */
    private IDEWorkbenchActivityHelper() {
    	lock = this;
        natureMap = new HashMap();
        // for dynamic UI
        Platform.getExtensionRegistry().addRegistryChangeListener(
                new IRegistryChangeListener() {
                    public void registryChanged(IRegistryChangeEvent event) {
                        if (event.getExtensionDeltas(
                                "org.eclipse.core.resources", "natures").length > 0) { //$NON-NLS-1$ //$NON-NLS-2$
							loadNatures();
						}
                    }
                }, "org.eclipse.core.resources"); //$NON-NLS-1$
        loadNatures();
        listener = getChangeListener();
        ResourcesPlugin.getWorkspace().addResourceChangeListener(listener);
        // crawl the initial projects to set up nature bindings
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
                .getProjects();
        processProjects(new HashSet(Arrays.asList(projects)));
    }

    /**
     * For dynamic UI.  Clears the cache of known natures and recreates it.
     */
    public void loadNatures() {
        natureMap.clear();
        IExtensionPoint point = Platform.getExtensionRegistry()
                .getExtensionPoint("org.eclipse.core.resources.natures"); //$NON-NLS-1$
        IExtension[] extensions = point.getExtensions();
        for (int i = 0; i < extensions.length; i++) {
            IExtension extension = extensions[i];
            final String localId = extension.getSimpleIdentifier();
            final String pluginId = extension.getNamespaceIdentifier();
            String natureId = extension.getUniqueIdentifier();
            natureMap.put(natureId, new IPluginContribution() {
                public String getLocalId() {
                    return localId;
                }

                public String getPluginId() {
                    return pluginId;
                }
            });
        }
    }

    /**
     * Get a change listener for listening to resource changes.
     * 
     * @return the resource change listeners
     */
    private IResourceChangeListener getChangeListener() {
        return new IResourceChangeListener() {
            /*
             * (non-Javadoc) @see
             * org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
             */
            public void resourceChanged(IResourceChangeEvent event) {
                if (!WorkbenchActivityHelper.isFiltering()) {
					return;
				}
                IResourceDelta mainDelta = event.getDelta();

                if (mainDelta == null) {
					return;
				}
                //Has the root changed?
                if (mainDelta.getKind() == IResourceDelta.CHANGED
						&& mainDelta.getResource().getType() == IResource.ROOT) {

					IResourceDelta[] children = mainDelta.getAffectedChildren();
					Set projectsToUpdate = new HashSet();
					for (int i = 0; i < children.length; i++) {
						IResourceDelta delta = children[i];
						if (delta.getResource().getType() == IResource.PROJECT) {
							IProject project = (IProject) delta.getResource();

							if (project.isOpen()) {
								projectsToUpdate.add(project);
							}
						}
					}

					processProjects(projectsToUpdate);
				}
            }
        };
    }


    /**
	 * Drain the queue and consult the helper.
	 */
	protected void runPendingUpdates() {
		String[] ids = null;
		synchronized (lock) {
			ids = (String[]) fPendingNatureUpdates
					.toArray(new String[fPendingNatureUpdates.size()]);
			fPendingNatureUpdates.clear();
		}
		IWorkbenchActivitySupport workbenchActivitySupport = PlatformUI
				.getWorkbench().getActivitySupport();
		for (int j = 0; j < ids.length; j++) {
			final IPluginContribution contribution = (IPluginContribution) natureMap
					.get(ids[j]);
			if (contribution == null) {
				continue; // bad nature ID.
			}

			final ITriggerPoint triggerPoint = workbenchActivitySupport
					.getTriggerPointManager().getTriggerPoint(NATURE_POINT);
			// consult the advisor - if the activities need enabling, they will
			// be
			WorkbenchActivityHelper.allowUseOf(triggerPoint, contribution);
		}

	}

	/**
	 * Unhooks the <code>IResourceChangeListener</code>.
	 */
    public void shutdown() {
        if (listener != null) {
            ResourcesPlugin.getWorkspace().removeResourceChangeListener(
                    listener);
        }
    }

	/**
	 * @param projectsToUpdate
	 */
	private void processProjects(Set projectsToUpdate) {
		boolean needsUpdate = false;
		for (Iterator i = projectsToUpdate.iterator(); i.hasNext();) {
			try {
				IProject project = (IProject) i.next();
				String[] ids = project.getDescription().getNatureIds();
				if (ids.length == 0) {
					continue;
				}

				synchronized (lock) {
					needsUpdate = fPendingNatureUpdates.addAll(Arrays
							.asList(ids)) | needsUpdate;
				}

			} catch (CoreException e) {
				// Do nothing if there is a CoreException
			}
		}
		if (needsUpdate) {
			if (fUpdateJob == null) {
				fUpdateJob = new WorkbenchJob(IDEWorkbenchMessages.IDEWorkbenchActivityHelper_jobName) { 
					public IStatus runInUIThread(
							IProgressMonitor monitor) {
						runPendingUpdates();
						return Status.OK_STATUS;
					}
				};
				fUpdateJob.setSystem(true);
			}
			fUpdateJob.schedule();
		}
	}
}
