/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.wizards.datatransfer;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Input stream for reading files in ustar format (tar) compatible
 * with the specification in IEEE Std 1003.1-2001.  Also supports
 * long filenames encoded using the GNU @LongLink extension.
 * 
 * @since 3.1
 */
public class TarInputStream extends FilterInputStream
{
	private int nextEntry = 0;
	private int nextEOF = 0;
	private int filepos = 0;
	private int bytesread = 0;
	private TarEntry firstEntry = null;
	private String longLinkName = null;

	/**
	 * Creates a new tar input stream on the given input stream.
	 * 
	 * @param in input stream
	 * @throws TarException
	 * @throws IOException
	 */
	public TarInputStream(InputStream in) throws TarException, IOException {
		super(in);

		// Read in the first TarEntry to make sure
		// the input is a valid tar file stream.
		firstEntry = getNextEntry();
	}

	/**
	 * Create a new tar input stream, skipping ahead to the given entry
	 * in the file.
	 * 
	 * @param in input stream
	 * @param entry skips to this entry in the file
	 * @throws TarException
	 * @throws IOException
	 */
	TarInputStream(InputStream in, TarEntry entry) throws TarException, IOException {
		super(in);
		skipToEntry(entry);
	}

	/**
	 *  The checksum of a tar file header is simply the sum of the bytes in
	 *  the header.
	 * 
	 * @param header
	 * @return checksum
	 */
	private long headerChecksum(byte[] header) {
		long sum = 0;

		for(int i = 0; i < 512; i++) {
			sum += header[i];
		}

		return sum;
	}

	/**
	 * Skips ahead to the position of the given entry in the file.
	 * 
	 * @param entry
	 * @returns false if the entry has already been passed
	 * @throws TarException
	 * @throws IOException
	 */
	boolean skipToEntry(TarEntry entry) throws TarException, IOException {
		int bytestoskip = entry.filepos - bytesread;
		if(bytestoskip < 0) return false;
		while(bytestoskip > 0) {
			long ret = in.skip(bytestoskip);
			if(ret < 0) {
				throw new IOException("early end of stream"); //$NON-NLS-1$
			}
			bytestoskip -= ret;
			bytesread += ret;
		}
		filepos = entry.filepos;
		nextEntry = 0;
		nextEOF = 0;
		TarEntry foundEntry = getNextEntry();
		if(!foundEntry.getName().equals(entry.getName())) {
			throw new IOException("inconsistent file"); //$NON-NLS-1$
		}
		return true;
	}

	/**
	 * Returns true if the header checksum is correct.
	 * 
	 * @param header
	 * @return true if this header has a valid checksum
	 */
	private boolean isValidTarHeader(byte[] header) {
		long fileChecksum, calculatedChecksum;
		int pos, i;
		
		
		pos = 148;
		StringBuffer checksumString = new StringBuffer();
		for(i = 0; i < 8; i++) {
			if(header[pos + i] == 0 || !Character.isDigit((char) header[pos + i])) break;
			checksumString.append((char) header[pos + i]);
		}
		if(checksumString.length() == 0) return false;
		if(checksumString.charAt(0) != '0') checksumString.insert(0, '0');
		fileChecksum = Long.decode(checksumString.toString()).longValue();

		// Blank out the checksum.
		for(i = 0; i < 8; i++) {
			header[pos + i] = ' ';
		}
		calculatedChecksum = headerChecksum(header);

		return (fileChecksum == calculatedChecksum);
	}

	/**
	 * Returns the next entry in the tar file.  Does not handle
	 * GNU @LongLink extensions.
	 * 
	 * @return the next entry in the tar file
	 * @throws TarException
	 * @throws IOException
	 */
	TarEntry getNextEntryInternal() throws TarException, IOException {
		byte[] header = new byte[512];
		int pos = 0;
		int i;

		if(firstEntry != null) {
			TarEntry entryReturn = firstEntry;
			firstEntry = null;
			return entryReturn;
		}

		while(nextEntry > 0) {
			long ret = in.skip(nextEntry);
			if(ret < 0) {
				throw new IOException("early end of stream"); //$NON-NLS-1$
			}
			nextEntry -= ret;
			bytesread += ret;
		}

		int bytestoread = 512;
		while(bytestoread > 0) {
			int ret = super.read(header, 512 - bytestoread, bytestoread);
			if( ret < 0 ) {
				throw new IOException("early end of stream"); //$NON-NLS-1$
			}
			bytestoread -= ret;
			bytesread += ret;
		}

		// If we have a header of all zeros, this marks the end of the file.
		if(headerChecksum(header) == 0) {
			// We are at the end of the file.
			if(filepos > 0) {
				return null;
			}
			
			// Invalid stream.
			throw new TarException("not in tar format"); //$NON-NLS-1$
		}
		
		// Validate checksum.
		if(!isValidTarHeader(header)) {
			throw new TarException("not in tar format"); //$NON-NLS-1$
		}

		StringBuffer name = new StringBuffer();
		while(pos < 100 && header[pos] != 0) {
			name.append((char) header[pos]);
			pos++;
		}
		// Prepend the prefix here.
		pos = 345;
		if(header[pos] != 0) {
			StringBuffer prefix = new StringBuffer();
			while(pos < 500 && header[pos] != 0) {
				prefix.append((char) header[pos]);
				pos++;
			}
			prefix.append('/');
			name.insert(0, prefix);
		}
		
		TarEntry entry;
		if(longLinkName != null) {
			entry = new TarEntry(longLinkName, filepos);
			longLinkName = null;
		} else {
			entry = new TarEntry(name.toString(), filepos);
		}
		if(header[156] != 0) {
			entry.setFileType(header[156]);
		}
		
		pos = 100;
		StringBuffer mode = new StringBuffer();
		for(i = 0; i < 8; i++) {
			if(header[pos + i] == 0) break;
			mode.append((char) header[pos + i]);
		}
		if(mode.length() > 0 && mode.charAt(0) != '0') mode.insert(0, '0');
		long fileMode = Long.decode(mode.toString()).longValue();
		entry.setMode(fileMode);
		
		pos = 100 + 24;
		StringBuffer size = new StringBuffer();
		for(i = 0; i < 12; i++) {
			if(header[pos + i] == 0) break;
			size.append((char) header[pos + i]);
		}
		if(size.charAt(0) != '0') size.insert(0, '0');
		int fileSize = Integer.decode(size.toString()).intValue();
		entry.setSize(fileSize);
		nextEOF = fileSize;
		if(fileSize % 512 > 0) {
			nextEntry = fileSize + (512 - (fileSize % 512));
		} else {
			nextEntry = fileSize;
		}
		filepos += (nextEntry + 512);
		return entry;
	}

	/**
	 * Moves ahead to the next file in the tar archive and returns
	 * a TarEntry object describing it.
	 * 
	 * @return the next entry in the tar file
	 * @throws TarException
	 * @throws IOException
	 */
	public TarEntry getNextEntry() throws TarException, IOException {
		TarEntry entry = getNextEntryInternal();

		if(entry != null && entry.getName().equals("././@LongLink")) { //$NON-NLS-1$
			// This is a GNU extension for doing long filenames.
			// We get a file called ././@LongLink which just contains
			// the real pathname.
			byte[] longNameData = new byte[(int) entry.getSize()];
			read(longNameData, 0, longNameData.length);

			StringBuffer filename = new StringBuffer();
			int pos = 0;
			
			while(pos < longNameData.length && longNameData[pos] != 0) {
				filename.append((char) longNameData[pos]);
				pos++;
			}
			longLinkName = filename.toString();
			return getNextEntryInternal();
		}
		return entry;
	}

	/* (non-Javadoc)
	 * @see java.io.FilterInputStream#read(byte[], int, int)
	 */
	public int read(byte[] b, int off, int len) throws IOException {
		if(nextEOF == 0) return -1;
		if(len > nextEOF) len = nextEOF;
		int size = super.read(b, off, len);
		nextEntry -= size;
		nextEOF -= size;
		bytesread += size;
		return size;
	}
}
