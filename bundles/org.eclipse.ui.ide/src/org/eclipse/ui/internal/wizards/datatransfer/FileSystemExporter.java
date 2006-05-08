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
package org.eclipse.ui.internal.wizards.datatransfer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * Helper class for exporting resources to the file system.
 */
public class FileSystemExporter {
	private static final int DEFAULT_BUFFER_SIZE = 16*1024;
	
    /**
     *  Creates the specified file system directory at <code>destinationPath</code>.
     *  This creates a new file system directory.
     *  
     *  @param destinationPath location to which files will be written
     */
    public void createFolder(IPath destinationPath) {
        new File(destinationPath.toOSString()).mkdir();
    }

    /**
     *  Writes the passed resource to the specified location recursively.
     *  
     *  @param resource the resource to write out to the file system
     *  @param destinationPath location where the resource will be written
     *  @exception CoreException if the operation fails 
     *  @exception IOException if an I/O error occurs when writing files
     */
    public void write(IResource resource, IPath destinationPath)
            throws CoreException, IOException {
        if (resource.getType() == IResource.FILE) {
			writeFile((IFile) resource, destinationPath);
		} else {
			writeChildren((IContainer) resource, destinationPath);
		}
    }

    /**
     *  Exports the passed container's children
     */
    protected void writeChildren(IContainer folder, IPath destinationPath)
            throws CoreException, IOException {
        if (folder.isAccessible()) {
            IResource[] children = folder.members();
            for (int i = 0; i < children.length; i++) {
                IResource child = children[i];
                writeResource(child, destinationPath.append(child.getName()));
            }
        }
    }

    /**
     *  Writes the passed file resource to the specified destination on the local
     *  file system
     */
    protected void writeFile(IFile file, IPath destinationPath)
            throws IOException, CoreException {
        OutputStream output = null;
        InputStream contentStream = null;

        try {
            contentStream = new BufferedInputStream(file.getContents(false));
            output = new BufferedOutputStream(new FileOutputStream(destinationPath.toOSString()));
            // for large files, need to make sure the chunk size can be handled by the VM
            int available = contentStream.available();
            available = available <= 0 ? DEFAULT_BUFFER_SIZE : available;
            int chunkSize = Math.min(DEFAULT_BUFFER_SIZE, available);
            byte[] readBuffer = new byte[chunkSize];
            int n = contentStream.read(readBuffer);

            while (n > 0) {
            	// only write the number of bytes read
                output.write(readBuffer, 0, n);
                n = contentStream.read(readBuffer);
            }
        } finally {
            if (contentStream != null) {
            	// wrap in a try-catch to ensure attempt to close output stream
            	try {
            		contentStream.close();
            	}
            	catch(IOException e){
            		IDEWorkbenchPlugin
							.log(
									"Error closing input stream for file: " + file.getLocation(), e); //$NON-NLS-1$
            	}
			}
        	if (output != null) {
        		// propogate this error to the user
           		output.close();
			}
        }
    }

    /**
     *  Writes the passed resource to the specified location recursively
     */
    protected void writeResource(IResource resource, IPath destinationPath)
            throws CoreException, IOException {
        if (resource.getType() == IResource.FILE) {
			writeFile((IFile) resource, destinationPath);
		} else {
            createFolder(destinationPath);
            writeChildren((IContainer) resource, destinationPath);
        }
    }
}
