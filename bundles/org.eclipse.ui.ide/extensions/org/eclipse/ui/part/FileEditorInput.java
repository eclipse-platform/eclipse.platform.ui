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
package org.eclipse.ui.part;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/**
 * Adapter for making a file resource a suitable input for an editor.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class FileEditorInput implements IFileEditorInput, IPathEditorInput,
        IPersistableElement {
    private IFile file;

    /**
     * Creates an editor input based of the given file resource.
     *
     * @param file the file resource
     */
    public FileEditorInput(IFile file) {
        if (file == null) {
            throw new IllegalArgumentException();
        }
        this.file = file;
    }

    /* (non-Javadoc)
     * Method declared on Object.
     */
    public int hashCode() {
        return file.hashCode();
    }

    /* (non-Javadoc)
     * Method declared on Object.
     *
     * The <code>FileEditorInput</code> implementation of this <code>Object</code>
     * method bases the equality of two <code>FileEditorInput</code> objects on the
     * equality of their underlying <code>IFile</code> resources.
     */
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof IFileEditorInput))
            return false;
        IFileEditorInput other = (IFileEditorInput) obj;
        return file.equals(other.getFile());
    }

    /* (non-Javadoc)
     * Method declared on IEditorInput.
     */
    public boolean exists() {
        return file.exists();
    }

    /* (non-Javadoc)
     * Method declared on IAdaptable.
     */
    public Object getAdapter(Class adapter) {
        if (adapter == IFile.class)
            return file;
        return file.getAdapter(adapter);
    }

    /* (non-Javadoc)
     * Method declared on IPersistableElement.
     */
    public String getFactoryId() {
        return FileEditorInputFactory.getFactoryId();
    }

    /* (non-Javadoc)
     * Method declared on IFileEditorInput.
     */
    public IFile getFile() {
        return file;
    }

    /* (non-Javadoc)
     * Method declared on IEditorInput.
     */
    public ImageDescriptor getImageDescriptor() {
        IContentType contentType = IDE.getContentType(file);
		return PlatformUI.getWorkbench().getEditorRegistry()
                .getImageDescriptor(file.getName(), contentType);
    }

    /* (non-Javadoc)
     * Method declared on IEditorInput.
     */
    public String getName() {
        return file.getName();
    }

    /* (non-Javadoc)
     * Method declared on IEditorInput.
     */
    public IPersistableElement getPersistable() {
        return this;
    }

    /* (non-Javadoc)
     * Method declared on IStorageEditorInput.
     */
    public IStorage getStorage() throws CoreException {
        return file;
    }

    /* (non-Javadoc)
     * Method declared on IEditorInput.
     */
    public String getToolTipText() {
        return file.getFullPath().makeRelative().toString();
    }

    /* (non-Javadoc)
     * Method declared on IPersistableElement.
     */
    public void saveState(IMemento memento) {
        FileEditorInputFactory.saveState(memento, this);
    }

    /* (non-Javadoc)
     * Method declared on IPathEditorInput
     * @since 3.0
     * @issue consider using an internal adapter for IPathEditorInput rather than adding this as API
     */
    public IPath getPath() {
        return file.getLocation();
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return getClass().getName() + "(" + getFile().getFullPath() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
