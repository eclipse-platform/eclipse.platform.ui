/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.core.sourcelookup;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchesListener;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.sourcelookup.*;
import org.eclipse.debug.internal.core.LaunchManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Utility and supporting methods for source location. Most of these
 * utilities should be migrated to the DebugPlugin and LanuchManager
 * when this facility becomes public API.
 * <p>
 * This class is experimental and temporary.
 * </p>
 * @see org.eclipse.debug.core.sourcelookup.AbstractSourceLookupDirector
 * @since 3.0
 */
public class SourceLookupUtils {
	
	//	constants for the CommonSourceLocation extension point and its attributes
	public static final String LOCATION_EXTENSION = "sourceContainerTypes";	 //$NON-NLS-1$
	public static final String DEFAULT_COMP_EXTENSION = "sourcePathComputers"; //$NON-NLS-1$
	public static final String ID_ATTRIBUTE = "id"; //$NON-NLS-1$
	
	// contributed extensions
	private static Hashtable sourceContainerTypes;	
	private static Hashtable defaultComputers;
	
	/**
	 * Cache of shared zip files. Zip files are closed
	 * when this class's plug-in is shutdown, when a project
	 * is about to be closed or deleted, when a launch is
	 * removed, and when a debug target or process terminates. 
	 */
	private static HashMap fgZipFileCache = new HashMap(5);
	private static ArchiveCleaner fgCleaner = null;
	
	/**
	 * Loads source container type and default source path computer extensions.
	 */
	private static void initialize() {
		if (sourceContainerTypes == null) {
			IPluginDescriptor descriptor= DebugPlugin.getDefault().getDescriptor();
			IConfigurationElement[] extensions = descriptor.getExtensionPoint(LOCATION_EXTENSION).getConfigurationElements();
			sourceContainerTypes = new Hashtable();
			for (int i = 0; i < extensions.length; i++) {
				sourceContainerTypes.put(
						extensions[i].getAttribute(ID_ATTRIBUTE),
						new SourceContainerType(extensions[i]));
			}			
			extensions = descriptor.getExtensionPoint(DEFAULT_COMP_EXTENSION).getConfigurationElements();
			defaultComputers = new Hashtable();
			for (int i = 0; i < extensions.length; i++) {
				defaultComputers.put(
						extensions[i].getAttribute(ID_ATTRIBUTE),
						new SourcePathComputer(extensions[i]));
			}
		}
	}
	
	/**
	 * Returns all the available source container types.
	 * @return the available source container types
	 */
	public static ISourceContainerType[] getSourceContainerTypes() {
		initialize();
		Collection containers = sourceContainerTypes.values();
		return (ISourceContainerType[]) containers.toArray(new ISourceContainerType[containers.size()]);
	}
	
	/**
	 * Finds a source container type using the type ID.
	 * @param id the <code>id</code> used when implementing the type extension point
	 * @return the source container type or <code>null</code> if not found
	 */
	public static ISourceContainerType getSourceContainerType(String id) {
		initialize();
		return (ISourceContainerType) sourceContainerTypes.get(id);
	}
	
	/**
	 * Finds a source path computer using the type ID.
	 * @param id the <code>id</code> used when implementing the source path computer extension point.
	 * @return the requested source path computer or <code>null</code> if not found.
	 */
	public static ISourcePathComputer getSourcePathComputer(String id){
		initialize();
		return (ISourcePathComputer) defaultComputers.get(id);
	}
			
	/**
	 * Creates and returns a new XML document.
	 * 
	 * @return a new XML document
	 * @throws CoreException if unable to create a new document
	 */
	public static Document newDocument()throws CoreException {
		try {
			return LaunchManager.getDocument();
		} catch (ParserConfigurationException e) {
			abort(SourceLookupMessages.getString("SourceLookupUtils.3"), e); //$NON-NLS-1$
		}		
		return null;
	}
	
	public static String serializeDocument(Document document) throws CoreException {
		try {
			return LaunchManager.serializeDocument(document);
		} catch (TransformerException e) {
			abort(SourceLookupMessages.getString("SourceLookupUtils.4"), e); //$NON-NLS-1$
		} catch (IOException e) {
			abort(SourceLookupMessages.getString("SourceLookupUtils.5"), e); //$NON-NLS-1$
		}
		return null;
	}

	/**
	 * Parses the given XML document, returning its root element.
	 * 
	 * @param document XML document as a string
	 * @return the document's root element
	 * @throws CoreException if unable to parse the document
	 */
	public static Element parseDocument(String document) throws CoreException {
		Element root = null;
		InputStream stream = null;
		try{		
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			stream = new ByteArrayInputStream(document.getBytes());
			root = parser.parse(stream).getDocumentElement();
		} catch (ParserConfigurationException e) {
			abort(SourceLookupMessages.getString("SourceLookupUtils.6"), e); //$NON-NLS-1$
		} catch (FactoryConfigurationError e) {
			abort(SourceLookupMessages.getString("SourceLookupUtils.7"), e); //$NON-NLS-1$
		} catch (SAXException e) {
			abort(SourceLookupMessages.getString("SourceLookupUtils.8"), e); //$NON-NLS-1$
		} catch (IOException e) {
			abort(SourceLookupMessages.getString("SourceLookupUtils.9"), e); //$NON-NLS-1$
		} finally { 
			try{
				stream.close();
			} catch(IOException e) {
				abort(SourceLookupMessages.getString("SourceLookupUtils.10"), e); //$NON-NLS-1$
			}
		}		
		return root;
	}
	
	/**
	 * Throws a new exception with the given message an underlying exception.
	 * 
	 * @param message error message
	 * @param throwable underlying exception or <code>null</code> if none
	 * @throws CoreException
	 */
	public static void abort(String message, Throwable throwable) throws CoreException {
		IStatus status = new Status(IStatus.ERROR, DebugPlugin.getUniqueIdentifier(), DebugPlugin.INTERNAL_ERROR, message, throwable);
		throw new CoreException(status);
	}
	
	/**
	 * Returns a zip file with the given name
	 * 
	 * @param name zip file name
	 * @return The zip file with the given name
	 * @exception IOException if unable to create the specified zip
	 * 	file
	 */
	public static ZipFile getZipFile(String name) throws IOException {
		synchronized (fgZipFileCache) {
			if (fgCleaner == null) {
				fgCleaner = new ArchiveCleaner();
				DebugPlugin.getDefault().addDebugEventListener(fgCleaner);
				DebugPlugin.getDefault().getLaunchManager().addLaunchListener(fgCleaner);
				ResourcesPlugin.getWorkspace().addResourceChangeListener(fgCleaner, IResourceChangeEvent.PRE_DELETE | IResourceChangeEvent.PRE_CLOSE);
			}
			ZipFile zip = (ZipFile)fgZipFileCache.get(name);
			if (zip == null) {
				zip = new ZipFile(name);
				fgZipFileCache.put(name, zip);
			}
			return zip;
		}
	}
	
	/**
	 * Closes all zip files that have been opened,
	 * and removes them from the zip file cache.
	 * This method is only to be called by the debug
	 * plug-in.
	 */
	public static void closeArchives() {
		synchronized (fgZipFileCache) {
			Iterator iter = fgZipFileCache.values().iterator();
			while (iter.hasNext()) {
				ZipFile file = (ZipFile)iter.next();
				synchronized (file) {
					try {
						file.close();
					} catch (IOException e) {
						DebugPlugin.log(e);
					}
				}
			}
			fgZipFileCache.clear();
		}
	}	
	
	/**
	 * Called when the debug plug-in shuts down.
	 */
	public static void shutdown() {
		closeArchives();
		if (fgCleaner != null) {
			DebugPlugin.getDefault().removeDebugEventListener(fgCleaner);
			DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(fgCleaner);
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(fgCleaner);			
		}
	}
	
	/**
	 * Clears the cache of open zip files when a debug target or process
	 * terminates, when a launch is removed, or when a project is about
	 * to be deleted or closed.
	 */
	static class ArchiveCleaner implements IDebugEventSetListener, ILaunchesListener, IResourceChangeListener {

		/* (non-Javadoc)
		 * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse.debug.core.DebugEvent[])
		 */
		public void handleDebugEvents(DebugEvent[] events) {
			for (int i = 0; i < events.length; i++) {
				DebugEvent event = events[i];
				if (event.getKind() == DebugEvent.TERMINATE) {
					Object source = event.getSource();
					if (source instanceof IDebugTarget || source instanceof IProcess) {
						SourceLookupUtils.closeArchives();
					}
				}
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.core.ILaunchesListener#launchesRemoved(org.eclipse.debug.core.ILaunch[])
		 */
		public void launchesRemoved(ILaunch[] launches) {
			SourceLookupUtils.closeArchives();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.core.ILaunchesListener#launchesAdded(org.eclipse.debug.core.ILaunch[])
		 */
		public void launchesAdded(ILaunch[] launches) {			
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.core.ILaunchesListener#launchesChanged(org.eclipse.debug.core.ILaunch[])
		 */
		public void launchesChanged(ILaunch[] launches) {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
		 */
		public void resourceChanged(IResourceChangeEvent event) {
			SourceLookupUtils.closeArchives();
		}
		
	}	
}
