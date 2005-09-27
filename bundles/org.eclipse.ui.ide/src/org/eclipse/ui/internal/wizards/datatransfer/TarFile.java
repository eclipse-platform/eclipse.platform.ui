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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;

/**
 * Reads a .tar or .tar.gz archive file, providing an index enumeration
 * and allows for accessing an InputStream for arbitrary files in the
 * archive.
 * 
 * @since 3.1
 */
public class TarFile {
	private File file;
	private TarInputStream entryEnumerationStream;
	private TarEntry curEntry;
	private TarInputStream entryStream;
	
	/**
	 * Create a new TarFile for the given file.
	 * 
	 * @param file
	 * @throws TarException
	 * @throws IOException
	 */
	public TarFile(File file) throws TarException, IOException {
		this.file = file;

		InputStream in = new FileInputStream(file);
		// First, check if it's a GZIPInputStream.
		try {
			in = new GZIPInputStream(in);
		} catch(IOException e) {
			//If it is not compressed we close
			//the old one and recreate
			in.close();		
			in = new FileInputStream(file);
		}
		entryEnumerationStream = new TarInputStream(in);
		curEntry = entryEnumerationStream.getNextEntry();
	}
	
	/**
	 * Close the tar file input stream.
	 * 
	 * @throws IOException if the file cannot be successfully closed
	 */
	public void close() throws IOException {
		entryEnumerationStream.close();
	}

	/**
	 * Create a new TarFile for the given path name.
	 * 
	 * @param filename
	 * @throws TarException
	 * @throws IOException
	 */
	public TarFile(String filename) throws TarException, IOException {
		this(new File(filename));
	}

	/**
	 * Returns an enumeration cataloguing the tar archive.
	 * 
	 * @return enumeration of all files in the archive
	 */
	public Enumeration entries() {
		return new Enumeration() {			
			public boolean hasMoreElements() {
				return (curEntry != null);
			}
			
			public Object nextElement() {
				TarEntry oldEntry = curEntry;
				try {
					curEntry = entryEnumerationStream.getNextEntry();
				} catch(TarException e) {
					curEntry = null;
				} catch(IOException e) {
					curEntry = null;
				}
				return oldEntry;
			}
		};
	}

	/**
	 * Returns a new InputStream for the given file in the tar archive.
	 * 
	 * @param entry
	 * @return an input stream for the given file
	 * @throws TarException
	 * @throws IOException
	 */
	public InputStream getInputStream(TarEntry entry) throws TarException, IOException {
		if(entryStream == null || !entryStream.skipToEntry(entry)) {
			InputStream in = new FileInputStream(file);
			// First, check if it's a GZIPInputStream.
			try {
				in = new GZIPInputStream(in);
			} catch(IOException e) {
				in = new FileInputStream(file);
			}
			entryStream = new TarInputStream(in, entry) {
				public void close() {
					// Ignore close() since we want to reuse the stream.
				}
			};
		}
		if(entryStream == null) {
			System.out.println("huh?"); //$NON-NLS-1$
		}
		return entryStream;
	}

	/**
	 * Returns the path name of the file this archive represents.
	 * 
	 * @return path
	 */
	public String getName() {
		return file.getPath();
	}
}
