package org.eclipse.update.core;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */ 

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.jar.JarFile;

/**
 * Local jar content reference. 
 * 
 * @since 2.0
 * 
 */

public class JarContentReference extends ContentReference {
	
	private JarFile jarFile;

	public JarContentReference(String id, File file) {
		super(id, file);
		this.jarFile = null;
	}

	public JarContentReference(String id, URL url) {
		super(id, url);
		this.jarFile = null;
	}
	
	/*
	 * @see ContentReference#newContentReference(String, File)
	 */
	public ContentReference newContentReference(String id, File file) {
		return new JarContentReference(id, file);
	}

	public JarFile asJarFile() throws IOException {
		if (this.jarFile == null)
			this.jarFile = new JarFile(asFile());
		return jarFile;
	}
}
