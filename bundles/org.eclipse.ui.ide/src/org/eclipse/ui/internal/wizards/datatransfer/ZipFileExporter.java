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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;


/**
 *	Exports resources to a .zip file
 */
public class ZipFileExporter implements IFileExporter {
    private ZipOutputStream outputStream;

    private StringBuffer manifestContents;

    private boolean generateManifestFile = false;

    private boolean useCompression = true;

    // constants
    private static final String newline = "\r\n"; // is this platform dependent?//$NON-NLS-1$

    private static final String manifestMagic = "Manifest-Version: 1.0" + newline + newline;//$NON-NLS-1$

    private static final String nameLabel = "Name: ";//$NON-NLS-1$

    private static final String digestsLabel = "Digest-Algorithms: SHA MD5" + newline;//$NON-NLS-1$

    private static final String shaLabel = "SHA-Digest: ";//$NON-NLS-1$

    private static final String md5Label = "MD5-Digest: ";//$NON-NLS-1$

    private static final String manifestPath = "meta-inf/Manifest.mf";//$NON-NLS-1$

    /**
     *	Create an instance of this class.
     *
     *	@param filename java.lang.String
     *	@param compress boolean
     *	@param includeManifestFile boolean
     *	@exception java.io.IOException
     */
    public ZipFileExporter(String filename, boolean compress,
            boolean includeManifestFile) throws IOException {
        outputStream = new ZipOutputStream(new FileOutputStream(filename));
        useCompression = compress;
        generateManifestFile = includeManifestFile;

        if (generateManifestFile)
            manifestContents = new StringBuffer(manifestMagic);
    }

    /**
     *  Create a new entry in the manifest file being constructed.
     *
     *  @param pathname java.lang.String
     *  @param file org.eclipse.core.resources.IFile
     *  @exception java.io.IOException
     *  @exception org.eclipse.core.runtime.CoreException
     */
    protected void appendToManifest(String pathname, IFile file)
            throws IOException, CoreException {
        StringBuffer manifestEntry = new StringBuffer();
        manifestEntry.append(nameLabel);
        manifestEntry.append(pathname);
        manifestEntry.append(newline);
        manifestEntry.append(digestsLabel);
        manifestEntry.append(shaLabel);

        byte[] fileContents = null;

        // we don't have to EnsureLocal because it was already done in #write
        InputStream contentStream = file.getContents(false);
        Reader in = new InputStreamReader(contentStream);
        int chunkSize = contentStream.available();
        StringBuffer buffer = new StringBuffer(chunkSize);
        char[] readBuffer = new char[chunkSize];
        int n = in.read(readBuffer);
        while (n > 0) {
            buffer.append(readBuffer);
            n = in.read(readBuffer);
        }
        contentStream.close();
        fileContents = buffer.toString().getBytes();

        try {
            byte[] hashValue = MessageDigest
                    .getInstance("SHA").digest(fileContents);//$NON-NLS-1$
            manifestEntry.append(Base64Encoder.encode(hashValue));
            manifestEntry.append(newline);
            manifestEntry.append(md5Label);
            hashValue = MessageDigest.getInstance("MD5").digest(fileContents);//$NON-NLS-1$
            manifestEntry.append(Base64Encoder.encode(hashValue));
            manifestEntry.append(newline + newline);
        } catch (NoSuchAlgorithmException e) {
            // should never happen
            return;
        }
        manifestContents.append(manifestEntry.toString());
    }

    /**
     *	Do all required cleanup now that we're finished with the
     *	currently-open .zip
     *
     *	@exception java.io.IOException
     */
    public void finished() throws IOException {
        if (generateManifestFile)
            writeManifestFile();

        outputStream.close();
    }

    /**
     *	Create a new ZipEntry with the passed pathname and contents, and write it
     *	to the current archive
     *
     *	@param pathname java.lang.String
     *	@param contents byte[]
     *	@exception java.io.IOException
     */
    protected void write(String pathname, byte[] contents) throws IOException {
        ZipEntry newEntry = new ZipEntry(pathname);

        // if the contents are being compressed then we get the below for free.
        if (!useCompression) {
            newEntry.setMethod(ZipEntry.STORED);
            newEntry.setSize(contents.length);
            CRC32 checksumCalculator = new CRC32();
            checksumCalculator.update(contents);
            newEntry.setCrc(checksumCalculator.getValue());
        }

        outputStream.putNextEntry(newEntry);
        outputStream.write(contents);
        outputStream.closeEntry();
    }

    /**
     *	Create a new ZipEntry with the passed pathname and contents, and write it
     *	to the current archive
     *
     *	@param pathname java.lang.String
     *	@param contents IFile
     *	@exception java.io.IOException
     * @throws CoreException 
     */
    protected void write(String pathname, IFile contents) throws IOException, CoreException {
        ZipEntry newEntry = new ZipEntry(pathname);
        byte[] readBuffer = new byte[4096];
        
        // if the contents are being compressed then we get the below for free.
        if (!useCompression) {
            newEntry.setMethod(ZipEntry.STORED);
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
                if (contentStream != null)
                    contentStream.close();
            }

            newEntry.setSize(length);
            newEntry.setCrc(checksumCalculator.getValue());
        }

        outputStream.putNextEntry(newEntry);
    	InputStream contentStream = contents.getContents(false);
        try {
            int n;
            while ((n = contentStream.read(readBuffer)) > 0) {
                outputStream.write(readBuffer, 0, n);
            }
        } finally {
            if (contentStream != null)
                contentStream.close();
        }
        outputStream.closeEntry();
    }

    /**
     *  Write the passed resource to the current archive
     *
     *  @param resource org.eclipse.core.resources.IFile
     *  @param destinationPath java.lang.String
     *  @exception java.io.IOException
     *  @exception org.eclipse.core.runtime.CoreException
     */
    public void write(IFile resource, String destinationPath)
            throws IOException, CoreException {

        write(destinationPath, resource);
        if (generateManifestFile)
            appendToManifest(destinationPath, resource);
    }

    /**
     *	Write the constructed manifest.mf file to the current archive
     *
     *	@exception java.io.IOException
     */
    protected void writeManifestFile() throws IOException {
        write(manifestPath, manifestContents.toString().getBytes());
    }
}