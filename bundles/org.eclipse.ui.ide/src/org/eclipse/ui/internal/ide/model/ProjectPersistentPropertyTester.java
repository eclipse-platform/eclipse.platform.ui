/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide.model;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IActionFilter;
import org.eclipse.ui.IResourceActionFilter;

/**
 * Property tester for detecting the exisitance of a particular persitent
 * property on all the projects of a ResourceMapping.
 *
 * @since 3.1
 */
public class ProjectPersistentPropertyTester extends PropertyTester {
    
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
                IProject[] projects = ((ResourceMapping)receiver).getProjects();
                for (int i = 0; i < projects.length; i++) {
                    IProject project = projects[i];
                    if (!filter.testAttribute(project, property, persitentPropertyEntry))
                        return false;
                }
                // All projects have the persistent property
                return true;
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

}
