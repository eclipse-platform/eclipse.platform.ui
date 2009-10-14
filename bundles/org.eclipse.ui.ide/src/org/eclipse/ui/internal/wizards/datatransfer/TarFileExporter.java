/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.wizards.datatransfer;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.zip.GZIPOutputStream;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;

/**
 * Exports resources to a .tar.gz file.
 *
 * @since 3.1
 */
public class TarFileExporter implements IFileExporter {
    private TarOutputStream outputStream;
    private GZIPOutputStream gzipOutputStream;
    

    /**
     *	Create an instance of this class.
     *
     *	@param filename java.lang.String
     *	@param compress boolean
     *	@exception java.io.IOException
     */
    public TarFileExporter(String filename, boolean compress) throws IOException {
    	if(compress) {
    		gzipOutputStream = new GZIPOutputStream(new FileOutputStream(filename));
    		outputStream = new TarOutputStream(new BufferedOutputStream(gzipOutputStream));
    	} else {
    		outputStream = new TarOutputStream(new BufferedOutputStream(new FileOutputStream(filename)));
    	}
    }

    /**
     *	Do all required cleanup now that we're finished with the
     *	currently-open .tar.gz
     *
     *	@exception java.io.IOException
     */
    public void finished() throws IOException {
        outputStream.close();
        if(gzipOutputStream != null) {
        	gzipOutputStream.close();
        }
    }

    /**
     *	Write the contents of the file to the tar archive.
     *
     *	@param entry
     *	@param contents
     *  @exception java.io.IOException
     *  @exception org.eclipse.core.runtime.CoreException
     */
    private void write(TarEntry entry, IFile contents) throws IOException, CoreException {
		final URI location = contents.getLocationURI();
		if (location == null) {
			throw new FileNotFoundException(contents.getFullPath().toOSString());
		}
    	
    	InputStream contentStream = contents.getContents(false);
    	entry.setSize(EFS.getStore(location).fetchInfo().getLength());
    	outputStream.putNextEntry(entry);
        try {
            int n;
            byte[] readBuffer = new byte[4096];
            while ((n = contentStream.read(readBuffer)) > 0) {
                outputStream.write(readBuffer, 0, n);
            }
        } finally {
            if (contentStream != null) {
				contentStream.close();
			}
        }

    	outputStream.closeEntry();    	
    }

    public void write(IContainer container, String destinationPath)
            throws IOException {
        TarEntry newEntry = new TarEntry(destinationPath);
        if(container.getLocalTimeStamp() != IResource.NULL_STAMP) {
        	newEntry.setTime(container.getLocalTimeStamp() / 1000);
        }
        ResourceAttributes attributes = container.getResourceAttributes();
        if (attributes != null && attributes.isExecutable()) {
        	newEntry.setMode(newEntry.getMode() | 0111);
        }
        if (attributes != null && attributes.isReadOnly()) {
        	newEntry.setMode(newEntry.getMode() & ~0222);
        }
        newEntry.setFileType(TarEntry.DIRECTORY);
        outputStream.putNextEntry(newEntry);
    }
    
    /**
     *  Write the passed resource to the current archive.
     *
     *  @param resource org.eclipse.core.resources.IFile
     *  @param destinationPath java.lang.String
     *  @exception java.io.IOException
     *  @exception org.eclipse.core.runtime.CoreException
     */
    public void write(IFile resource, String destinationPath)
            throws IOException, CoreException {

        TarEntry newEntry = new TarEntry(destinationPath);
        if(resource.getLocalTimeStamp() != IResource.NULL_STAMP) {
        	newEntry.setTime(resource.getLocalTimeStamp() / 1000);
        }
        ResourceAttributes attributes = resource.getResourceAttributes();
        if (attributes != null && attributes.isExecutable()) {
        	newEntry.setMode(newEntry.getMode() | 0111);
        }
        if (attributes != null && attributes.isReadOnly()) {
        	newEntry.setMode(newEntry.getMode() & ~0222);
        }
        write(newEntry, resource);
    }
}
