package org.eclipse.update.core;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */ 

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.EmptyStackException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.core.runtime.CoreException;

/**
 * Base class for feature content providers.
 * </p>
 * @since 2.0
 */

public abstract class FeatureContentProvider implements IFeatureContentProvider {
	
	private URL base;
	protected IFeature feature;
		
	// local file map in temporary area
	private static Map entryMap;
	private static File tmpDir;
	
	// buffer pool
	private static Stack bufferPool;	
	private static final int BUFFER_SIZE = 1024;
	
	/**
	 * Content selector used in archive operations.
	 * Default implementation causes all entries to be selected with
	 * generated identifiers being the same as the original entry name.
	 * 
	 * @since 2.0
	 */
	public class ContentSelector {
		
		/**
		 * Indicates whether the archive content entry should be
		 * selected for the operation
		 * 
		 * @since 2.0
		 */
		public boolean include(String entry) {
			return true;
		}
		
		/**
		 * Defines a content reference identifier for the 
		 * archive content entry
		 * 
		 * @since 2.0
		 */
		public String defineIdentifier(String entry) {
			return entry;
		}
	}
	
	/**
	 * @since 2.0
	 */
	public FeatureContentProvider(URL base) {
		this.base = base;
		this.feature = null;
	}

	/*
	 * @see IFeatureContentProvider#getURL()
	 */
	public URL getURL() {
		return base;
	}

	/*
	 * @see IFeatureContentProvider#getFeatureManifest()
	 */
	public abstract ContentReference getFeatureManifestReference()
		throws CoreException;

	/*
	 * @see IFeatureContentProvider#getArchiveReferences()
	 */
	public abstract ContentReference[] getArchiveReferences()
		throws CoreException;

	/*
	 * @see IFeatureContentProvider#getFeatureEntryArchiveReferences()
	 */
	public abstract ContentReference[] getFeatureEntryArchiveReferences()
		throws CoreException;

	/*
	 * @see IFeatureContentProvider#getPluginEntryArchiveReferences(IPluginEntry)
	 */
	public abstract ContentReference[] getPluginEntryArchiveReferences(IPluginEntry pluginEntry)
		throws CoreException;

	/*
	 * @see IFeatureContentProvider#getNonPluginEntryArchiveReferences(INonPluginEntry)
	 */
	public abstract ContentReference[] getNonPluginEntryArchiveReferences(INonPluginEntry nonPluginEntry)
		throws CoreException;

	/*
	 * @see IFeatureContentProvider#getFeatureEntryContentReferences()
	 */
	public abstract ContentReference[] getFeatureEntryContentReferences()
		throws CoreException;

	/*
	 * @see IFeatureContentProvider#getPluginEntryContentReferences(IPluginEntry)
	 */
	public abstract ContentReference[] getPluginEntryContentReferences(IPluginEntry pluginEntry)
		throws CoreException;
		
	/*
	 * @see IFeatureContentProvider#setFeature(IFeature)
	 */
	public void setFeature(IFeature feature) {
		this.feature = feature;
	}
	
	/**
	 * Returns the specified reference as a local file system reference.
	 * If required, the file represented by the specified content
	 * reference is first downloaded to the local system
	 * 
	 * @since 2.0
	 */
	public ContentReference asLocalReference(ContentReference ref, Feature.ProgressMonitor monitor) throws IOException {
		
		// check to see if this is already a local reference
		if (ref.isLocalReference())
			return ref;
		
		// check to see if we already have a local file for this reference
		String key = ref.toString();
		File localFile = lookupLocalFile(key);
		if (localFile != null)
			return ref.newContentReference(ref.getIdentifier(), localFile);
			
		// download the referenced file into local temporary area
		localFile = createLocalFile(key, null/*name*/);
		InputStream is = null;
		OutputStream os = null;
		try {
			is = ref.getInputStream();
			os = new FileOutputStream(localFile);
			copy(is, os, monitor);
		} catch(IOException e) {
			removeLocalFile(key);
			throw e;
		} finally {
			if (is != null) try { is.close(); } catch(IOException e) {}
			if (os != null) try { os.close(); } catch(IOException e) {}
		}
		return ref.newContentReference(ref.getIdentifier(), localFile);
	}
		
	/**
	 * Returns the specified reference as a local file.
	 * If required, the file represented by the specified content
	 * reference is first downloaded to the local system
	 * 
	 * @since 2.0
	 */
	public File asLocalFile(ContentReference ref, Feature.ProgressMonitor monitor) throws IOException {
		File file = ref.asFile();
		if (file != null)
			return file;
		
		ContentReference localRef = asLocalReference(ref, monitor);
		file = localRef.asFile();
		return file;
	}
	
	/**
	 * Returns local file (in temporary area) matching the
	 * specified key. Returns null if the entry does not exist.
	 * 
	 * @since 2.0
	 */	
	protected synchronized File lookupLocalFile(String key) {
		if (entryMap == null)
			return null;
		return (File) entryMap.get(key);
	}
	
	/**
	 * Create a local file with the specified name in temporary area
	 * and associate it with the specified key. If name is not specified
	 * a temporary name is created. If key is not specified no 
	 * association is made.
	 * 
	 * @since 2.0
	 */	
	protected synchronized File createLocalFile(String key, String name) throws IOException {
		
		// ensure we have a temp directory
		if (tmpDir == null) {		
			String tmpName = System.getProperty("java.io.tmpdir");
			tmpName += "eclipse" + File.separator + ".update" + File.separator + Long.toString((new Date()).getTime()) + File.separator;
			tmpDir = new File(tmpName);
			tmpDir.deleteOnExit();
			tmpDir.mkdirs();
			if (!tmpDir.exists())
				throw new FileNotFoundException(tmpName);
		}
		
		// create the local file
		File temp;
		String filePath;
		if (name != null) {
			// create file with specified name
			filePath = name.replace('/',File.separatorChar);
			if (filePath.startsWith(File.separator))
				filePath = filePath.substring(1);
			temp = new File(tmpDir, filePath);
			if (filePath.endsWith(File.separator))
				temp.mkdirs();
		} else {
			// create file with temp name
			temp = File.createTempFile("eclipse",null,tmpDir);
		}
		temp.deleteOnExit();
		
		// create file association 
		if (key != null) {
			if (entryMap == null)
				entryMap = new HashMap();
			entryMap.put(key,temp);
		}
		
		return temp;
	}
	
	/**
	 * Removes the specified key from the local file map. The file is
	 * not actually deleted until VM termination.
	 * 
	 * @since 2.0
	 */	
	protected synchronized void removeLocalFile(String key) {
		if (entryMap != null)
			entryMap.remove(key);
	}
	
	/**
	 * Copies specified input stream to the output stream.
	 * 
	 * @since 2.0
	 */	
	protected void copy(InputStream is, OutputStream os, Feature.ProgressMonitor monitor) throws IOException {
		// TBA: progress monitor support
		// TBA: indication of download size (xK of yK)
		byte[] buf = getBuffer();
		try {
			long totalLen = 0; // TBA - to contain total expected length
			long currentLen = 0;
			int len = is.read(buf);
			while(len != -1) {
				currentLen += len;
				os.write(buf,0,len);
				len = is.read(buf);
			}
		} finally {
			freeBuffer(buf);
		}
	}
	
	/**
	 * Unpacks the referenced jar archive.
	 * Returns content references to the unpacked file entries
	 * (in temporary area)
	 * 
	 * @since 2.0
	 */
	protected ContentReference[] unpack(JarContentReference jarReference, ContentSelector selector, Feature.ProgressMonitor monitor) throws IOException {
		
		// get archive content
		JarFile jarArchive = jarReference.asJarFile();
		List content = new ArrayList();
		Enumeration entries = jarArchive.entries();
		
		// run through the entries and unjar
		String entryName;
		String entryId;
		JarEntry entry;
		InputStream is;
		OutputStream os;
		File localFile;
		while(entries.hasMoreElements()) {
			entry = (JarEntry) entries.nextElement();
			entryName = entry.getName();
			if (entry != null && selector != null && selector.include(entryName)) {
				is = null;
				os = null;
				entryId = selector==null ? entryName : selector.defineIdentifier(entryName);
				localFile = createLocalFile(null/*key*/, entryId); // create temp file w/o a key map
				if (!localFile.isDirectory()) { // must test for directory (files do not exist at this point)
					try {
						is = jarArchive.getInputStream(entry);
						os = new FileOutputStream(localFile);
						copy(is, os, monitor);
					} finally {
						if (is != null) try { is.close(); } catch(IOException e) {}
						if (os != null) try { os.close(); } catch(IOException e) {}
					}
					content.add(new ContentReference(entryId, localFile));
				}
			}
		}		
		return (ContentReference[]) content.toArray(new ContentReference[0]);
	}
	
	/**
	 * Unpacks the referenced jar archive.
	 * Returns content references for the specified jar entry
	 * (in temparary area).
	 * 
	 * @since 2.0
	 */
	protected ContentReference unpack(JarContentReference jarReference, String entryName, ContentSelector selector, Feature.ProgressMonitor monitor) throws IOException {
				
		// unjar the entry
		JarFile jarArchive = jarReference.asJarFile();
		entryName = entryName.replace(File.separatorChar,'/');
		JarEntry entry = jarArchive.getJarEntry(entryName);
		String entryId;
		if (entry != null) {
			InputStream is = null;
			OutputStream os = null;
			entryId = selector==null ? entryName : selector.defineIdentifier(entryName);
			File localFile = createLocalFile(null/*key*/, entryId); // create temp file w/o a key map
			if (!localFile.isDirectory()) {  // must test for directory (files do not exist at this point)
				try {
					is = jarArchive.getInputStream(entry);
					os = new FileOutputStream(localFile);
					copy(is, os, monitor);
				} finally {
					if (is != null) try { is.close(); } catch(IOException e) {}
					if (os != null) try { os.close(); } catch(IOException e) {}
				}
				return new ContentReference(entryId, localFile);
			} else
				return null; // entry was a directory
		} else
			throw new FileNotFoundException(jarReference.asFile().getAbsolutePath()+" "+entryName);
	}
	
	/**
	 * Peeks into the referenced jar archive.
	 * Returns content references to the packed jar entries within the archive.
	 * 
	 * @since 2.0
	 */
	protected ContentReference[] peek(JarContentReference jarReference, ContentSelector selector, Feature.ProgressMonitor monitor) throws IOException {
				
		// get archive content
		JarFile jarArchive = jarReference.asJarFile();
		List content = new ArrayList();
		Enumeration entries = jarArchive.entries();
		
		// run through the entries and create content references
		JarEntry entry;
		String entryName;
		String entryId;
		while(entries.hasMoreElements()) {
			entry = (JarEntry) entries.nextElement();
			entryName = entry.getName();
			if (selector != null && selector.include(entryName)) {
				entryId = selector==null ? entryName : selector.defineIdentifier(entryName);
				content.add(new JarEntryContentReference(entryId, jarReference, entry));
			}
		}		
		return (ContentReference[]) content.toArray(new ContentReference[0]);
	}
	
	/**
	 * Peeks into the referenced jar archive.
	 * Returns content references for the specified jar entry.
	 * 
	 * @since 2.0
	 */
	protected ContentReference peek(JarContentReference jarReference, String entryName, ContentSelector selector, Feature.ProgressMonitor monitor) throws IOException {
		
		// assume we have a reference that represents a jar archive.
		JarFile jarArchive = jarReference.asJarFile();
		entryName = entryName.replace(File.separatorChar,'/');
		JarEntry entry = jarArchive.getJarEntry(entryName);
		String entryId = selector==null ? entryName : selector.defineIdentifier(entryName);
		return new JarEntryContentReference(entryId, jarReference, entry);
	}
	
	/**
	 * Peeks into the referenced jar archive.
	 * Returns list of entry names contained in the archive.
	 * 
	 * @since 2.0
	 */
	protected String[] peek(JarContentReference jarReference, Feature.ProgressMonitor monitor) throws IOException {
		
		// get archive content
		JarFile jarArchive = jarReference.asJarFile();
		List content = new ArrayList();
		Enumeration entries = jarArchive.entries();
		
		// run through the entries and collect entry names
		JarEntry entry;
		while(entries.hasMoreElements()) {
			entry = (JarEntry) entries.nextElement();
			content.add(entry.getName());
		}		
		return (String[]) content.toArray(new String[0]);
	}
	
	/**
	 * @since 2.0
	 */
	protected synchronized byte[] getBuffer() {
		if (bufferPool == null) {
			return new byte[BUFFER_SIZE];
		}
		
		try {
			return (byte[]) bufferPool.pop();
		} catch (EmptyStackException e) {
			return new byte[BUFFER_SIZE];
		}
	}
		
	/**
	 * @since 2.0
	 */
	protected synchronized void freeBuffer(byte[] buf) {
		if (bufferPool == null)
			bufferPool = new Stack();
		bufferPool.push(buf);
	}
}
