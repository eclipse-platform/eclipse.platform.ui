package org.eclipse.update.core;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */ 

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

import org.eclipse.update.internal.core.Policy;
import org.eclipse.update.internal.core.URLEncoder;

/**
 * Default content reference. 
 * Implements a simple URL or local File wrapper.
 * </p>
 * @since 2.0
 */
public class ContentReference {

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private static final String FILE_URL_PROTOCOL = "file";	 //$NON-NLS-1$
	
	private String id;
	private URL url;	// reference is either URL reference *OR*
	private File file;	//    local file reference
	private URLConnection connection;
	
	/**
	 * 
	 */
	public static final long UNKNOWN_SIZE = -1;

	/**
	 * Constructor for ContentRef.
	 */
	private ContentReference() {}

	/**
	 * Constructor for ContentRef.
	 */
	public ContentReference(String id, URL url) {
		this.id = (id==null ? EMPTY_STRING : id); //$NON-NLS-1$
		this.url = url; // can be null
		this.file = null;
	}
	
	/**
	 * Constructor for ContentRef.
	 */
	public ContentReference(String id, File file) {
		this.id = (id==null ? EMPTY_STRING : id); //$NON-NLS-1$
		this.file = file; // can be null
		this.url = null;
	}
	
	/**
	 * Clone a new content reference of the same type
	 * 
	 * @since 2.0
	 */
	public ContentReference createContentReference(String id, File file) {
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
			if (connection == null){
				URL resolvedURL = URLEncoder.encode(url);
				connection = resolvedURL.openConnection();
			}
			return connection.getInputStream();
		} else
			throw new IOException(Policy.bind("ContentReference.UnableToCreateInputStream",this.toString())); //$NON-NLS-1$
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
					URL resolvedURL = URLEncoder.encode(url);
			 		connection = resolvedURL.openConnection();
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
			return FILE_URL_PROTOCOL.equals(url.getProtocol()); 
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
			
		if (url!=null && FILE_URL_PROTOCOL.equals(url.getProtocol())) 
			return new File(url.getFile());
			
		throw new IOException(Policy.bind("ContentReference.UnableToReturnReferenceAsFile",this.toString())); //$NON-NLS-1$ 
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
			
		throw new IOException(Policy.bind("ContentReference.UnableToReturnReferenceAsURL",this.toString())); //$NON-NLS-1$
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
