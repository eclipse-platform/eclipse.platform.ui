/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.wizards.datatransfer;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPOutputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
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
     *	Create a new TarEntry with the passed pathname and contents, and write it
     *	to the current archive.
     *
     *	@param entry
     *	@param contents byte[]
     *	@exception java.io.IOException
     */
    private void write(TarEntry entry, byte[] contents) throws IOException {
    	entry.setSize(contents.length);
    	outputStream.putNextEntry(entry);
    	outputStream.write(contents);
    	outputStream.closeEntry();    	
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
        ByteArrayOutputStream output = null;
        InputStream contentStream = null;

        try {
            output = new ByteArrayOutputStream();
            contentStream = resource.getContents(false);
            int chunkSize = contentStream.available();
            byte[] readBuffer = new byte[chunkSize];
            int n = contentStream.read(readBuffer);

            while (n > 0) {
                output.write(readBuffer);
                n = contentStream.read(readBuffer);
            }
        } finally {
            if (output != null)
                output.close();
            if (contentStream != null)
                contentStream.close();
        }

        TarEntry newEntry = new TarEntry(destinationPath);
        if(resource.getLocalTimeStamp() != IResource.NULL_STAMP) {
        	newEntry.setTime(resource.getLocalTimeStamp() / 1000);
        }
        write(newEntry,output.toByteArray());
    }
}
