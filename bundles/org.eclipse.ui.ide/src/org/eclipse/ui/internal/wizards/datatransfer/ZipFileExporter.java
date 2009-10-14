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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;


/**
 *	Exports resources to a .zip file
 */
public class ZipFileExporter implements IFileExporter {
    private ZipOutputStream outputStream;

    private boolean useCompression = true;

    /**
     *	Create an instance of this class.
     *
     *	@param filename java.lang.String
     *	@param compress boolean
     *	@exception java.io.IOException
     */
    public ZipFileExporter(String filename, boolean compress) throws IOException {
        outputStream = new ZipOutputStream(new FileOutputStream(filename));
        useCompression = compress;
    }

    /**
     *	Do all required cleanup now that we're finished with the
     *	currently-open .zip
     *
     *	@exception java.io.IOException
     */
    public void finished() throws IOException {
        outputStream.close();
    }

    /**
     *	Write the contents of the file to the tar archive.
     *
     *	@param entry
     *	@param contents
     *  @exception java.io.IOException
     *  @exception org.eclipse.core.runtime.CoreException
     */
    private void write(ZipEntry entry, IFile contents) throws IOException, CoreException {
        byte[] readBuffer = new byte[4096];

        // If the contents are being compressed then we get the below for free.
        if (!useCompression) {
            entry.setMethod(ZipEntry.STORED);
        	InputStream contentStream = contents.getContents(false);
        	int length = 0;
            CRC32 checksumCalculator = new CRC32();
            try {
                int n;
                while ((n = contentStream.read(readBuffer)) > 0) {
                    checksumCalculator.update(readBuffer, 0, n);
                    length += n;
                }
            } finally {
                if (contentStream != null) {
					contentStream.close();
				}
            }

            entry.setSize(length);
            entry.setCrc(checksumCalculator.getValue());
        }

        // set the timestamp
        long localTimeStamp = contents.getLocalTimeStamp();
        if(localTimeStamp != IResource.NULL_STAMP)
        	entry.setTime(localTimeStamp);
        
        outputStream.putNextEntry(entry);
    	InputStream contentStream = contents.getContents(false);
        try {
            int n;
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
        ZipEntry newEntry = new ZipEntry(destinationPath);
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
        ZipEntry newEntry = new ZipEntry(destinationPath);
        write(newEntry, resource);
    }
}
