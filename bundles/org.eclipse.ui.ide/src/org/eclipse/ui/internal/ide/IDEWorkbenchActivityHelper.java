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
package org.eclipse.ui.internal.ide;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.ui.internal.WorkbenchActivityHelper;

/**
 * Utility class that manages promotion of activites in response to workspace changes.
 * 
 * @since 3.0
 */
public class IDEWorkbenchActivityHelper {

    /**
     * Resource listener that reacts to new projects (and associated natures) 
     * coming into the workspace.
     */
    private IResourceChangeListener listener;

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
        listener = getChangeListener();
        ResourcesPlugin.getWorkspace().addResourceChangeListener(listener);
   }
    
    /**
     * Get a change listener for listening to resource changes.
     * 
     * @return
     */
    private IResourceChangeListener getChangeListener() {
        return new IResourceChangeListener() {
            /*
             * (non-Javadoc) @see
             * org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
             */
            public void resourceChanged(IResourceChangeEvent event) {

                IResourceDelta mainDelta = event.getDelta();

                if (mainDelta == null)
                    return;
                //Has the root changed?
                if (mainDelta.getKind() == IResourceDelta.CHANGED
                    && mainDelta.getResource().getType() == IResource.ROOT) {

                    try {
                        IResourceDelta[] children = mainDelta.getAffectedChildren();
                        for (int i = 0; i < children.length; i++) {
                            IResourceDelta delta = children[i];
                            if (delta.getResource().getType() == IResource.PROJECT) {
                                IProject project = (IProject) delta.getResource();
                                String[] ids = project.getDescription().getNatureIds();
                                for (int j = 0; j < ids.length; j++) {
                                	// @issue WorkbenchActivityHelper is internal to the generic workbench
                                    WorkbenchActivityHelper.enableActivities(ids[j]);
                                }
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
     * Unhooks the <code>IResourceChangeListener</code>.
     */ 
    public void shutdown() {
        if (listener != null) {
            ResourcesPlugin.getWorkspace().removeResourceChangeListener(listener);
        }        
    }

    
}
