/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

	private boolean resolveLinks;

	/**
	 * Create an instance of this class.
	 *
	 * @param filename
	 *            java.lang.String
	 * @param compress
	 *            boolean
	 * @param resolveLinks
	 *            boolean
	 * @exception java.io.IOException
	 */
	public ZipFileExporter(String filename, boolean compress, boolean resolveLinks) throws IOException {
		this.resolveLinks = resolveLinks;
		outputStream = new ZipOutputStream(new FileOutputStream(filename));
		useCompression = compress;
	}

	/**
	 *	Do all required cleanup now that we're finished with the
	 *	currently-open .zip
	 *
	 *	@exception java.io.IOException
	 */
	@Override
	public void finished() throws IOException {
		outputStream.close();
	}

	/**
	 *	Write the contents of the file to the tar archive.
	 *
	 *  @exception java.io.IOException
	 *  @exception org.eclipse.core.runtime.CoreException
	 */
	private void write(ZipEntry entry, IFile contents) throws IOException, CoreException {
		byte[] readBuffer = new byte[4096];

		// If the contents are being compressed then we get the below for free.
		if (!useCompression) {
			entry.setMethod(ZipEntry.STORED);
			try (InputStream contentStream = contents.getContents(false)) {
				int length = 0;
				CRC32 checksumCalculator = new CRC32();
				int n;
				while ((n = contentStream.read(readBuffer)) > 0) {
					checksumCalculator.update(readBuffer, 0, n);
					length += n;
				}
				entry.setSize(length);
				entry.setCrc(checksumCalculator.getValue());
			}
		}

		// set the timestamp
		long localTimeStamp = contents.getLocalTimeStamp();
		if(localTimeStamp != IResource.NULL_STAMP)
			entry.setTime(localTimeStamp);

		outputStream.putNextEntry(entry);
		try (InputStream contentStream = contents.getContents(false)) {
			int n;
			while ((n = contentStream.read(readBuffer)) > 0) {
				outputStream.write(readBuffer, 0, n);
			}
		}
		outputStream.closeEntry();
	}

	@Override
	public void write(IContainer container, String destinationPath)
			throws IOException {
		if (!resolveLinks && container.isLinked(IResource.DEPTH_INFINITE)) {
			return;
		}
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
	@Override
	public void write(IFile resource, String destinationPath)
			throws IOException, CoreException {
		if (!resolveLinks && resource.isLinked(IResource.DEPTH_INFINITE)) {
			return;
		}
		ZipEntry newEntry = new ZipEntry(destinationPath);
		write(newEntry, resource);
	}
}
