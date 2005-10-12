/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide.model;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IActionFilter;
import org.eclipse.ui.IResourceActionFilter;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * Property tester for detecting the exisitance of a particular persitent
 * property on all the projects of a ResourceMapping.
 *
 * @since 3.1
 */
public class ProjectPersistentPropertyTester extends PropertyTester {
    
    private static final String ALLOW_UNSET_PROJECTS = "allowUnsetProjects";  //$NON-NLS-1$
    
    private static final IActionFilter filter = new WorkbenchResource() {
        
    	/* (non-Javadoc)
    	 * @see org.eclipse.ui.internal.ide.model.WorkbenchResource#getBaseImage(org.eclipse.core.resources.IResource)
    	 */
    	protected ImageDescriptor getBaseImage(IResource resource) {
            return null;
        }
    };

    /* (non-Javadoc)
     * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
     */
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        if (receiver instanceof ResourceMapping) {
            if (property.equals(IResourceActionFilter.PROJECT_PERSISTENT_PROPERTY)) {
                if(args == null) return false;
                String persitentPropertyEntry = (String)args[0];
	            boolean allowUnsetProjects = false;
	            if (args.length > 1)
	                allowUnsetProjects = args[1].equals(ALLOW_UNSET_PROJECTS);
	            IProject[] projects = ((ResourceMapping)receiver).getProjects();
	            boolean atLeastOne = false;
	            for (int i = 0; i < projects.length; i++) {
	                IProject project = projects[i];
	                if (filter.testAttribute(project, property, persitentPropertyEntry)) {
	                    atLeastOne = true;
	                } else if (!allowUnsetProjects) {
	                    return false;
	                } else {
                        // Check to see if the persistant property is present
                        // If it is, we fail since it must be set to somethings else
                        try {
                            if (project != null && project.isAccessible() && project.getPersistentProperty(getPropertyKey(persitentPropertyEntry)) != null)
                                return false;
                        } catch (CoreException e) {
                            final String message = "Core exception while testing project persistent property"; //$NON-NLS-1$
                            IDEWorkbenchPlugin.log(message,
                                    new Status(IStatus.ERROR, IDEWorkbenchPlugin.IDE_WORKBENCH,
                                            IStatus.ERROR, message, e));
                            // Just continue
                        }
                    }
	            }
	            return atLeastOne;
            }
        } else if (receiver instanceof IResource) {
            if (property.equals(IResourceActionFilter.PROJECT_PERSISTENT_PROPERTY)) {
                if(args == null) return false;
                String persitentPropertyEntry = (String)args[0];
                IProject project = ((IResource)receiver).getProject();
                return filter.testAttribute(project, property, persitentPropertyEntry);
            }
        }
        
        return false;
    }

    private QualifiedName getPropertyKey(String value) {
        String propertyName;
        int i = value.indexOf('=');
        if (i != -1) {
            propertyName = value.substring(0, i).trim();
        } else {
            propertyName = value.trim();
        }
        QualifiedName key;
        int dot = propertyName.lastIndexOf('.');
        if (dot != -1) {
            key = new QualifiedName(propertyName.substring(0, dot),
                    propertyName.substring(dot + 1));
        } else {
            key = new QualifiedName(null, propertyName);
        }
        return key;
    }

}
