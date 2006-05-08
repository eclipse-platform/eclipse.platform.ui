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
package org.eclipse.ui.wizards.datatransfer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.operation.ModalContext;
import org.eclipse.ui.dialogs.FileSystemElement;
import org.eclipse.ui.internal.wizards.datatransfer.MinimizedFileSystemElement;

/**
 * The PopulateFilesOperation is an operation used to populate a FileSystemElement one
 * level deep rather than the whole way.
 */
public class PopulateRootOperation extends SelectFilesOperation {
    /**
     * Create a new <code>PopulateFilesOperation</code>.
     * @param rootObject the object to be populated
     * @param structureProvider the object that defines how we are to populate it.
     */
    public PopulateRootOperation(Object rootObject,
            IImportStructureProvider structureProvider) {
        super(rootObject, structureProvider);
    }

    /**
     * Creates and returns a <code>FileSystemElement</code> if the specified
     * file system object merits one.  The criteria for this are:
     * - if the file system object is a container then it must have either a
     *   child container or an associated file
     * - if the file system object is a file then it must have an extension
     *   suitable for selection
     */
    protected FileSystemElement createElement(FileSystemElement parent,
            Object fileSystemObject) throws InterruptedException {

        //Iterate on level deep
        return createElement(parent, fileSystemObject, 2);

    }

    /**
     * Creates and returns a <code>FileSystemElement</code> if the specified
     * file system object merits one.  The criteria for this are:
     * - if the file system object is a container then it must have either a
     *   child container or an associated file
     * - if the file system object is a file then it must have an extension
     *   suitable for selection
     * 	recurse down for depth to populate children
     */
    protected FileSystemElement createElement(FileSystemElement parent,
            Object fileSystemObject, int depth) throws InterruptedException {
        ModalContext.checkCanceled(monitor);
        boolean isContainer = provider.isFolder(fileSystemObject);
        String elementLabel = parent == null ? provider
                .getFullPath(fileSystemObject) : provider
                .getLabel(fileSystemObject);

        MinimizedFileSystemElement result = new MinimizedFileSystemElement(
                elementLabel, parent, isContainer);
        result.setFileSystemObject(fileSystemObject);

        if (isContainer) {
            if (depth > 0) {
                List children = provider.getChildren(fileSystemObject);
                if (children == null) {
					children = new ArrayList(1);
				}
                Iterator childrenEnum = children.iterator();
                while (childrenEnum.hasNext()) {
                    createElement(result, childrenEnum.next(), depth - 1);
                }
                result.setPopulated();
            }

        }

        return result;
    }
}
