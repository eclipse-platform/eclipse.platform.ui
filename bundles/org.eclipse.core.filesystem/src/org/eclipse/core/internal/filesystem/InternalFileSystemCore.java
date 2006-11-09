/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.filesystem;

import java.net.URI;
import java.util.HashMap;
import org.eclipse.core.filesystem.*;
import org.eclipse.core.filesystem.provider.FileSystem;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.util.NLS;

/**
 * The class manages internal implementation of methods on EFS.
 * This includes maintaining a list of file system extensions.
 */
public class InternalFileSystemCore implements IRegistryChangeListener {
	private static final InternalFileSystemCore INSTANCE = new InternalFileSystemCore();

	/**
	 * A map (String -> (IConfigurationElement or IFileSystem)) mapping URI
	 * scheme to the file system for that scheme.  If the corresponding file
	 * system has never been accessed, then the map contains the configuration
	 * element for the extension.  Once the file system has been created, the
	 * map contains the IFileSystem instance for that scheme.
	 */
	private HashMap fileSystems;

	/**
	 * Returns the singleton instance of this class.
	 * @return The singleton instance.
	 */
	public static InternalFileSystemCore getInstance() {
		return INSTANCE;
	}

	/**
	 * This class has a singleton instance.
	 */
	private InternalFileSystemCore() {
		super();
		RegistryFactory.getRegistry().addRegistryChangeListener(this);
	}

	/**
	 * Implements the method EFS#getFileSystem(String)
	 * 
	 * @param scheme The URI scheme of the file system
	 * @return The file system
	 * @throws CoreException
	 */
	public IFileSystem getFileSystem(String scheme) throws CoreException {
		if (scheme == null)
			throw new NullPointerException();
		final HashMap registry = getFileSystemRegistry();
		Object result = registry.get(scheme);
		if (result == null)
			Policy.error(EFS.ERROR_INTERNAL, NLS.bind(Messages.noFileSystem, scheme));
		if (result instanceof IFileSystem)
			return (IFileSystem) result;
		try {
			IConfigurationElement element = (IConfigurationElement) result;
			FileSystem fs = (FileSystem) element.createExecutableExtension("run"); //$NON-NLS-1$
			fs.initialize(scheme);
			//store the file system instance so we don't have to keep recreating it
			registry.put(scheme, fs);
			return fs;
		} catch (CoreException e) {
			//remove this invalid file system from the registry
			registry.remove(scheme);
			throw e;
		}
	}

	/**
	 * Implements the method EFS#getLocalFileSystem()
	 * 
	 * @return The local file system
	 */
	public IFileSystem getLocalFileSystem() {
		try {
			return getFileSystem(EFS.SCHEME_FILE);
		} catch (CoreException e) {
			//the local file system is always present
			throw new Error(e);
		}
	}

	/**
	 * Implements the method EFS#getStore(URI)
	 * 
	 * @param uri The URI of the store to retrieve
	 * @return The file store corresponding to the given URI
	 * @throws CoreException
	 */
	public IFileStore getStore(URI uri) throws CoreException {
		final String scheme = uri.getScheme();
		if (scheme == null)
			Policy.error(EFS.ERROR_INTERNAL, Messages.noScheme + uri);
		return getFileSystem(scheme).getStore(uri);
	}

	/**
	 * Returns the fully initialized file system registry
	 * @return The file system registry
	 */
	private synchronized HashMap getFileSystemRegistry() {
		if (fileSystems == null) {
			fileSystems = new HashMap();
			IExtensionPoint point = RegistryFactory.getRegistry().getExtensionPoint(EFS.PI_FILE_SYSTEM, EFS.PT_FILE_SYSTEMS);
			IExtension[] extensions = point.getExtensions();
			for (int i = 0; i < extensions.length; i++) {
				IConfigurationElement[] elements = extensions[i].getConfigurationElements();
				for (int j = 0; j < elements.length; j++) {
					if ("filesystem".equals(elements[j].getName())) { //$NON-NLS-1$
						String scheme = elements[j].getAttribute("scheme"); //$NON-NLS-1$
						if (scheme != null)
							fileSystems.put(scheme, elements[j]);
					}
				}
			}
		}
		return fileSystems;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.IRegistryChangeListener#registryChanged(org.eclipse.core.runtime.IRegistryChangeEvent)
	 */
	public void registryChanged(IRegistryChangeEvent event) {
		IExtensionDelta[] changes = event.getExtensionDeltas(EFS.PI_FILE_SYSTEM, EFS.PT_FILE_SYSTEMS);
		if (changes.length == 0)
			return;
		synchronized (this) {
			//let the registry be rebuilt lazily
			fileSystems = null;
		}
	}

	/**
	 * Implements {@link EFS#getNullFileSystem()}.
	 * @return The null file system
	 */
	public IFileSystem getNullFileSystem() {
		try {
			return getFileSystem(EFS.SCHEME_NULL);
		} catch (CoreException e) {
			//the local file system is always present
			throw new Error(e);
		}
	}
}