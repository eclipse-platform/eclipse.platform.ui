package org.eclipse.update.core;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */ 

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Default content reference. 
 * Implements a simple URL or local File wrapper.
 * </p>
 * @since 2.0
 */
public class ContentReference {
	
	private String id;
	private URL url;	// reference is either URL reference *OR*
	private File file;	//    local file reference
		
	private URLConnection connection;
	
	public static final long UNKNOWN_SIZE = -1;

	/**
	 * Constructor for ContentRef.
	 */
	private ContentReference() {}

	/**
	 * Constructor for ContentRef.
	 */
	public ContentReference(String id, URL url) {
		this.id = (id==null ? "" : id);
		this.url = url; // can be null
		this.file = null;
	}
	
	/**
	 * Constructor for ContentRef.
	 */
	public ContentReference(String id, File file) {
		this.id = (id==null ? "" : id);
		this.file = file; // can be null
		this.url = null;
	}
	
	/**
	 * Clone a new content reference of the same type
	 * 
	 * @since 2.0
	 */
	public ContentReference newContentReference(String id, File file) {
		return new ContentReference(id, file);
	}
	
	/**
	 * @since 2.0
	 */
	public String getIdentifier() {
		return id;
	}
	
	/**
	 * @since 2.0
	 */
	public InputStream getInputStream() throws IOException {
		if (file != null)
			return new FileInputStream(file);
		else if (url != null) {
			if (connection == null)
				connection = url.openConnection();
			return connection.getInputStream();
		} else
			throw new IOException("Unable to create input stream");
	}	
	
	/**
	 * @since 2.0
	 */
	public long getInputSize() {
		if (file != null)
			return file.length();
		else if (url != null) {
			if (connection == null) {
				try {
			 		connection = url.openConnection();
				} catch (IOException e) {
					return ContentReference.UNKNOWN_SIZE;
				}
			}
			long size = connection.getContentLength();
			return size == -1 ? ContentReference.UNKNOWN_SIZE : size;
		} else
			return ContentReference.UNKNOWN_SIZE;
	}
	
	/**
	 * @since 2.0
	 */
	public boolean isLocalReference() {
		if (file != null)
			return true;
		else if (url != null)
			return url.getProtocol().equals("file");
		else
			return false;
	}	
		
	/**
	 * Returns a local file for the content reference.
	 * Throws an exception if content reference cannot
	 * be returned as a local file. Note, that this method
	 * <b>does not</b> cause the file to be downloaded if it
	 * is not already local.
	 * 
	 * @since 2.0
	 */
	public File asFile() throws IOException {
		if (file != null)
			return file;
			
		if (url!=null && url.getProtocol().equals("file"))
			return new File(url.getFile());
			
		throw new IOException("Unable to return reference "+(url==null ? "" : url.toExternalForm())+" as file");
	}
		
	/**
	 * Returns a URL for the content reference.
	 * Throws an exception if content reference cannot
	 * be returned as a URL.
	 * 
	 * @since 2.0
	 */
	public URL asURL() throws IOException {
		if (url != null)
			return url;
			
		if (file != null)
			return file.toURL();
			
		throw new IOException("Unable to return reference as URL");
	}
			
	/**
	 * @since 2.0
	 */
	public String toString() {
		if (file != null)
			return file.getAbsolutePath();
		else
			return url.toExternalForm();
	}
	
	/**
	 * @since 2.0
	 */
	public void setIdentifier(String id) {
		this.id = id;
	}
}
