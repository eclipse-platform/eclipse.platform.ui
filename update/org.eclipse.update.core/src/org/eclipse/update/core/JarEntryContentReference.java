package org.eclipse.update.core;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */ 

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.JarEntry;

/**
 * Local jar entry content reference. 
 * 
 * @since 2.0
 * 
 */

public class JarEntryContentReference extends ContentReference {
	
	private JarContentReference jarContentReference;
	private JarEntry entry;
	
	/**
	 * Constructor for ContentReference
	 */
	public JarEntryContentReference(String id, JarContentReference jarContentReference, JarEntry entry) {
		super(id, (File)null);
		this.jarContentReference = jarContentReference;
		this.entry = entry;
	}

	/*
	 * @see ContentReference#getInputStream()
	 */
	public InputStream getInputStream() throws IOException {		
		return jarContentReference.asJarFile().getInputStream(entry);
	}

	/*
	 * @see ContentReference#getInputSize()
	 */
	public long getInputSize() {
		return entry.getSize();
	}
	
	/*
	 * @see ContentReference#isLocalReference()
	 */
	public boolean isLocalReference() {
		return jarContentReference.isLocalReference();
	}

	/*
	 * @see ContentReference#asURL()
	 */
	public URL asURL() throws IOException {
		String fileName = jarContentReference.asFile().getAbsolutePath().replace(File.separatorChar,'/');
		return new URL("jar:file:"+fileName+"!/"+entry.getName());
	}

	/*
	 * @see Object#toString()
	 */
	public String toString() {
		URL url;
		try {
			url = asURL();
		} catch(IOException e) {
			url = null;
		}
		if (url != null)
			return url.toExternalForm();
		else
			return getClass().getName()+"@"+hashCode();
	}
}
