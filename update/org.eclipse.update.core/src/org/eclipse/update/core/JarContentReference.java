package org.eclipse.update.core;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */ 

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.update.internal.core.Policy;
import org.eclipse.update.internal.core.UpdateManagerUtils;

/**
 * Local jar content reference. 
 * 
 * @since 2.0
 * 
 */

public class JarContentReference extends ContentReference {

	private JarFile jarFile;


	/**
	 * Content selector used in archive operations.
	 * Default implementation causes all file entries to be selected with
	 * generated identifiers being the same as the original entry name.
	 * 
	 * @since 2.0
	 */
	public static class ContentSelector {
		
		/**
		 * Indicates whether the archive content entry should be
		 * selected for the operation. Default behavior is to select
		 * all non-directory entries.
		 * 
		 * @since 2.0
		 */
		public boolean include(JarEntry entry) {
			return entry == null ? false : !entry.isDirectory();
		}
		
		/**
		 * Defines a content reference identifier for the 
		 * archive content entry. Default identifier is the
		 * same as the jar entry name.
		 * 
		 * @since 2.0
		 */
		public String defineIdentifier(JarEntry entry) {
			return entry==null ? null : entry.getName();
		}
	}	

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
	public ContentReference createContentReference(String id, File file) {
		return new JarContentReference(id, file);
	}

	protected JarFile asJarFile() throws IOException {
		if (this.jarFile == null)
			this.jarFile = new JarFile(asFile());
		return jarFile;
	}

	/**
	 * Unpacks the referenced jar archive.
	 * Returns content references to the unpacked file entries
	 * (in temporary area)
	 * 
	 * @since 2.0
	 */
	public ContentReference[] unpack(File dir, ContentSelector selector, InstallMonitor monitor) throws IOException {
		
		// make sure we have a selector
		if (selector == null)
			selector = new ContentSelector();
			
		// get archive content
		JarFile jarArchive = this.asJarFile();
		List content = new ArrayList();
		Enumeration entries = jarArchive.entries();
			
		// run through the entries and unjar
		String entryId;
		JarEntry entry;
		InputStream is;
		OutputStream os;
		File localFile;
		try {			
			if (monitor != null) {
				monitor.saveState();
				monitor.setTaskName(Policy.bind("JarContentReference.Unpacking")); //$NON-NLS-1$
				monitor.subTask(this.getIdentifier());
				monitor.showCopyDetails(false);
			}
			while(entries.hasMoreElements()) {
				entry = (JarEntry) entries.nextElement();
				if (entry != null && selector.include(entry)) {
					is = null;
					os = null;
					entryId = selector.defineIdentifier(entry);
					localFile = Utilities.createLocalFile(dir, null/*key*/, entryId); // create temp file w/o a key map
					if (!entry.isDirectory()) { 
						try {
							is = jarArchive.getInputStream(entry);
							os = new FileOutputStream(localFile);
							Utilities.copy(is, os, monitor);
						} finally {
							if (is != null) try { is.close(); } catch(IOException e) {}
							if (os != null) try { os.close(); } catch(IOException e) {}
						}
						content.add(new ContentReference(entryId, localFile));
					}
				}
			}	
		} finally {
			if (monitor != null) monitor.restoreState();
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
	public ContentReference unpack(File dir, String entryName, ContentSelector selector, InstallMonitor monitor) throws IOException {
						
		// make sure we have a selector
		if (selector == null)		
			selector = new ContentSelector();
			
		// unjar the entry
		JarFile jarArchive = this.asJarFile();
		entryName = entryName.replace(File.separatorChar,'/');
		JarEntry entry = jarArchive.getJarEntry(entryName);
		String entryId;
		if (entry != null) {
			InputStream is = null;
			OutputStream os = null;
			entryId = selector.defineIdentifier(entry);
			File localFile = Utilities.createLocalFile(dir, null/*key*/, entryId); // create temp file w/o a key map
			if (!entry.isDirectory()) { 
				try {
					is = jarArchive.getInputStream(entry);
					os = new FileOutputStream(localFile);
					Utilities.copy(is, os, monitor);
				} finally {
					if (is != null) try { is.close(); } catch(IOException e) {}
					if (os != null) try { os.close(); } catch(IOException e) {}
				}
				return new ContentReference(entryId, localFile);
			} else
				return null; // entry was a directory
		} else
			throw new FileNotFoundException(this.asFile().getAbsolutePath()+" "+entryName); //$NON-NLS-1$
	}


	/**
	 * Peeks into the referenced jar archive.
	 * Returns content references to the packed jar entries within the archive.
	 * 
	 * @since 2.0
	 */
	public ContentReference[] peek( ContentSelector selector, InstallMonitor monitor) throws IOException {
						
		// make sure we have a selector
		if (selector == null)		
			selector = new ContentSelector();
			
		// get archive content
		JarFile jarArchive = this.asJarFile();
		List content = new ArrayList();
		Enumeration entries = jarArchive.entries();
		
		// run through the entries and create content references
		JarEntry entry;
		String entryId;
		while(entries.hasMoreElements()) {
			entry = (JarEntry) entries.nextElement();
			if (selector.include(entry)) {
				entryId = selector.defineIdentifier(entry);
				content.add(new JarEntryContentReference(entryId, this, entry));
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
	public ContentReference peek( String entryName, ContentSelector selector, InstallMonitor monitor) throws IOException {
				
		// make sure we have a selector
		if (selector == null)
			selector = new ContentSelector();
			
		// assume we have a reference that represents a jar archive.
		JarFile jarArchive = this.asJarFile();
		entryName = entryName.replace(File.separatorChar,'/');
		JarEntry entry = jarArchive.getJarEntry(entryName);
		String entryId = selector.defineIdentifier(entry);
		return new JarEntryContentReference(entryId, this, entry);
	}


	/**
	 * Peeks into the referenced jar archive.
	 * Returns list of entry names contained in the archive.
	 * 
	 * @since 2.0
	 */
	public String[] peek( InstallMonitor monitor) throws IOException {
		
		// get archive content
		JarFile jarArchive = this.asJarFile();
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

	


	

	
}
