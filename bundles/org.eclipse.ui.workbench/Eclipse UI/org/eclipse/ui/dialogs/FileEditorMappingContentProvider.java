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
package org.eclipse.ui.dialogs;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IFileEditorMapping;

/**
 * A content provider for displaying of <code>IFileEditorMapping</code>
 * objects in viewers.
 * <p>
 * This class has a singleton instance, 
 * <code>FileEditorMappingContentProvider.INSTANCE</code>,
 * which can be used any place this kind of content provider is needed.
 * </p>
 *
 * @see org.eclipse.jface.viewers.IContentProvider
 */
public class FileEditorMappingContentProvider implements
        IStructuredContentProvider {

    /**
     * Singleton instance accessor.
     */
    public final static FileEditorMappingContentProvider INSTANCE = new FileEditorMappingContentProvider();

    /**
     * Creates an instance of this class.  The private visibility of this
     * constructor ensures that this class is only usable as a singleton.
     */
    private FileEditorMappingContentProvider() {
        super();
    }

    /* (non-Javadoc)
     * Method declared on IContentProvider.
     */
    public void dispose() {
    }

    /* (non-Javadoc)
     * Method declared on IStructuredContentProvider.
     */
    public Object[] getElements(Object element) {
        IFileEditorMapping[] array = (IFileEditorMapping[]) element;
        return array == null ? new Object[0] : array;
    }

    /* (non-Javadoc)
     * Method declared on IContentProvider.
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }
}
