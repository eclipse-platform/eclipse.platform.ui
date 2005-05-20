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
package org.eclipse.ant.internal.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.ui.IPathEditorInput;

public class AntBuildFilePropertyTester extends PropertyTester {

    private static final String CONTENT_TYPE = "org.eclipse.ant.core.antBuildFile"; //$NON-NLS-1$

    /* (non-Javadoc)
     * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
     */
    public boolean test(Object receiver, String method, Object[] args, Object expectedValue) {
        IResource resource = (IResource) ((IAdaptable) receiver).getAdapter(IResource.class);
        if (resource != null) {
            return matchesContentType(resource, CONTENT_TYPE);
        }
        IPathEditorInput editorInput = (IPathEditorInput) ((IAdaptable) receiver).getAdapter(IPathEditorInput.class);
        if (editorInput != null) {
            IPath path= editorInput.getPath();
            File file= path.toFile();
            if (file.exists()) {
                try {
                    FileReader reader= new FileReader(file);
                    IContentType contentType= Platform.getContentTypeManager().getContentType(CONTENT_TYPE);
                    IContentDescription description= contentType.getDescriptionFor(reader, IContentDescription.ALL);
                    reader.close();
                    if (description != null) {
                        IContentType type = description.getContentType();
                        return type != null && CONTENT_TYPE.equals(type.getId());
                    }
                } catch (FileNotFoundException e) {
                    return false;
                } catch (IOException e) {
                    return false;
                }
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
