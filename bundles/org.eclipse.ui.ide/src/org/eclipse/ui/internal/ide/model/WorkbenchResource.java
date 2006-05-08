/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IResourceActionFilter;
import org.eclipse.ui.actions.SimpleWildcardTester;
import org.eclipse.ui.model.WorkbenchAdapter;

/**
 * An IWorkbenchAdapter that represents IResources.
 */
public abstract class WorkbenchResource extends WorkbenchAdapter implements
        IResourceActionFilter {

    /**
     *	Answer the appropriate base image to use for the resource.
     */
    protected abstract ImageDescriptor getBaseImage(IResource resource);

    /**
     * Returns an image descriptor for this object.
     */
    public ImageDescriptor getImageDescriptor(Object o) {
        IResource resource = getResource(o);
        return resource == null ? null : getBaseImage(resource);
    }

    /**
     * getLabel method comment.
     */
    public String getLabel(Object o) {
        IResource resource = getResource(o);
        return resource == null ? null : resource.getName();
    }

    /**
     * Returns the parent of the given object.  Returns null if the
     * parent is not available.
     */
    public Object getParent(Object o) {
        IResource resource = getResource(o);
        return resource == null ? null : resource.getParent();
    }

    /**
     * Returns the resource corresponding to this object,
     * or null if there is none.
     */
    protected IResource getResource(Object o) {
        if (o instanceof IResource) {
            return (IResource) o;
        }
        if (o instanceof IAdaptable) {
            return (IResource) ((IAdaptable) o).getAdapter(IResource.class);
        }
        return null;
    }

    /**
     * Returns whether the specific attribute matches the state of the target
     * object.
     *
     * @param target the target object
     * @param name the attribute name
     * @param value the attribute value
     * @return <code>true</code> if the attribute matches; <code>false</code> otherwise
     */
    public boolean testAttribute(Object target, String name, String value) {
        if (!(target instanceof IResource)) {
            return false;
        }
        IResource res = (IResource) target;
        if (name.equals(NAME)) {
            return SimpleWildcardTester.testWildcardIgnoreCase(value, res
                    .getName());
        } else if (name.equals(PATH)) {
            return SimpleWildcardTester.testWildcardIgnoreCase(value, res
                    .getFullPath().toString());
        } else if (name.equals(EXTENSION)) {
            return SimpleWildcardTester.testWildcardIgnoreCase(value, res
                    .getFileExtension());
        } else if (name.equals(READ_ONLY)) {
            return (res.isReadOnly() == value.equalsIgnoreCase("true"));//$NON-NLS-1$
        } else if (name.equals(PROJECT_NATURE)) {
            try {
                IProject proj = res.getProject();
                return proj.isAccessible() && proj.hasNature(value);
            } catch (CoreException e) {
                return false;
            }
        } else if (name.equals(PERSISTENT_PROPERTY)) {
            return testProperty(res, true, false, value);
        } else if (name.equals(PROJECT_PERSISTENT_PROPERTY)) {
            return testProperty(res, true, true, value);
        } else if (name.equals(SESSION_PROPERTY)) {
            return testProperty(res, false, false, value);
        } else if (name.equals(PROJECT_SESSION_PROPERTY)) {
            return testProperty(res, false, true, value);
        } else if (name.equals(CONTENT_TYPE_ID)) {
            return testContentTypeProperty(res, value);
        }
        return false;
    }

    /**
     * Tests whether the content type for <code>resource</code> matches the
     * <code>contentTypeId</code>. It is possible that this method call could
     * cause the resource to be read. It is also possible (through poor plug-in
     * design) for this method to load plug-ins.
     * 
     * @param resource
     *            The resource for which the content type should be determined;
     *            must not be <code>null</code>.
     * @param contentTypeId
     *            The expected content type; must not be <code>null</code>.
     * @return <code>true</code> iff the best matching content type has an
     *         identifier that matches <code>contentTypeId</code>;
     *         <code>false</code> otherwise.
     */
    private final boolean testContentTypeProperty(final IResource resource,
            final String contentTypeId) {
        final String expectedValue = contentTypeId.trim();

        if (!(resource instanceof IFile)) {
            return false;
        }

        final IFile file = (IFile) resource;
        String actualValue = null;

        try {
            final IContentDescription contentDescription = file
                    .getContentDescription();

            if (contentDescription != null) {
                final IContentType contentType = contentDescription
                        .getContentType();
                actualValue = contentType.getId();
            }
        } catch (CoreException e) {
            //ignore - this just means the file does not exist or is not accessible
        }

        return expectedValue == null || expectedValue.equals(actualValue);
    }

    /**
     * Tests whether a session or persistent property on the resource or its project
     * matches the given value.
     * 
     * @param resource
     *            the resource to check
     * @param persistentFlag
     *            <code>true</code> for a persistent property, <code>false</code>
     *            for a session property
     * @param projectFlag
     *            <code>true</code> to check the resource's project,
     *            <code>false</code> to check the resource itself
     * @param value
     *            the attribute value, which has either the form "propertyName" or
     *            "propertyName=propertyValue"
     * @return whether there is a match
     */
    private boolean testProperty(IResource resource, boolean persistentFlag,
            boolean projectFlag, String value) {
        String propertyName;
        String expectedVal;
        int i = value.indexOf('=');
        if (i != -1) {
            propertyName = value.substring(0, i).trim();
            expectedVal = value.substring(i + 1).trim();
        } else {
            propertyName = value.trim();
            expectedVal = null;
        }
        try {
            QualifiedName key;
            int dot = propertyName.lastIndexOf('.');
            if (dot != -1) {
                key = new QualifiedName(propertyName.substring(0, dot),
                        propertyName.substring(dot + 1));
            } else {
                key = new QualifiedName(null, propertyName);
            }
            IResource resToCheck = projectFlag ? resource.getProject()
                    : resource;
            // getProject() on workspace root can be null
            if (resToCheck == null) {
                return false;
            }
            if (persistentFlag) {
                String actualVal = resToCheck.getPersistentProperty(key);
                if (actualVal == null) {
                    return false;
                }
                return expectedVal == null || expectedVal.equals(actualVal);
            } 

            Object actualVal = resToCheck.getSessionProperty(key);
             if (actualVal == null) {
				return false;
			}
              
             return expectedVal == null
                        || expectedVal.equals(actualVal.toString());
            
        } catch (CoreException e) {
            // ignore
        }
        return false;
    }

}
