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
package org.eclipse.debug.internal.ui;

import java.util.regex.Pattern;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;

/**
 * ResourceExtender provides property testers for the XML expression language
 * evaluation. We provide a copy in Debug so that launch shortcuts can add
 * contextual launch enablement that does not require their plugins to be
 * loaded. Copied from
 * org.eclipse.jdt.internal.corext.refactoring.participants.xml.ResourceExtender
 */
public class ResourceExtender extends PropertyTester {

    private static final String PROPERTY_MATCHES_PATTERN = "matchesPattern"; //$NON-NLS-1$

    private static final String PROJECT_NATURE = "projectNature"; //$NON-NLS-1$

    private static final String PROPERTY_MATCHES_CONTENT_TYPE = "matchesContentType"; //$NON-NLS-1$

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jdt.internal.corext.refactoring.participants.properties.IPropertyEvaluator#test(java.lang.Object,
     *      java.lang.String, java.lang.String)
     */
    public boolean test(Object receiver, String method, Object[] args, Object expectedValue) {
        IResource resource = (IResource) ((IAdaptable) receiver).getAdapter(IResource.class);
        if (resource != null) {
            if (PROPERTY_MATCHES_PATTERN.equals(method)) { //$NON-NLS-1$
                String fileName = resource.getName();
                String expected = (String) expectedValue;
                expected = expected.replaceAll("\\.", "\\\\.");  //$NON-NLS-1$//$NON-NLS-2$
                expected = expected.replaceAll("\\*", "\\.\\*");  //$NON-NLS-1$//$NON-NLS-2$
                Pattern pattern = Pattern.compile((String) expected);
                boolean retVal = pattern.matcher(fileName).find();
                return retVal;
            } else if (PROJECT_NATURE.equals(method)) {
                try {
                    IProject proj = resource.getProject();
                    return proj.isAccessible() && proj.hasNature((String) expectedValue);
                } catch (CoreException e) {
                    return false;
                }
            } else if (PROPERTY_MATCHES_CONTENT_TYPE.equals(method)) {
                return matchesContentType(resource, (String) expectedValue);
            }
        }
        return false;
    }

    /**
     * Returns whether or not the given file's content type matches the
     * specified content type.
     * 
     * Content types are looked up in the content type registry.
     * 
     * @return whether or not the given resource has the given content type
     */
    private boolean matchesContentType(IResource resource, String contentType) {
        if (resource == null || !(resource instanceof IFile) || !resource.exists()) {
            return false;
        }
        IFile file = (IFile) resource;
        IContentDescription description;
        try {
            description = file.getContentDescription();
        } catch (CoreException e) {
            return false;
        }
        if (description != null) {
            IContentType type = description.getContentType();
            return type != null && contentType.equals(type.getId());
        }
        return false;
    }

}
