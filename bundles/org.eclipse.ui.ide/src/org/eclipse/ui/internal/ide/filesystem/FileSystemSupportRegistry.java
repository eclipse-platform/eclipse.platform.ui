/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.ide.filesystem;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.fileSystem.FileSystemContributor;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * @since 3.2
 * 
 */
public class FileSystemSupportRegistry implements IExtensionChangeHandler {

	private static final String FILESYSTEM_SUPPORT = "filesystemSupport";//$NON-NLS-1$

	protected static final String ATT_CLASS = "class"; //$NON-NLS-1$

	private static final String LABEL = "label";//$NON-NLS-1$

	private static final Object LOCAL_FILE_SYSTEM = "org.eclipse.core.filesystem.local";//$NON-NLS-1$

	private static final String FILESYSTEM = "filesystem";//$NON-NLS-1$

	private static FileSystemSupportRegistry singleton;

	/**
	 * Get the instance of the registry.
	 * 
	 * @return MarkerSupportRegistry
	 */
	public static FileSystemSupportRegistry getInstance() {
		if (singleton == null)
			singleton = new FileSystemSupportRegistry();
		return singleton;
	}

	private Collection registeredContributions = new HashSet(0);

	private FileSystemConfiguration defaultFileSystem;

	/**
	 * Create a new instance of the receiver.
	 */
	public FileSystemSupportRegistry() {

		IExtensionTracker tracker = PlatformUI.getWorkbench()
				.getExtensionTracker();
		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint(IDEWorkbenchPlugin.IDE_WORKBENCH,
						FILESYSTEM_SUPPORT);
		if (point == null)
			return;
		IExtension[] extensions = point.getExtensions();
		// initial population
		for (int i = 0; i < extensions.length; i++) {
			IExtension extension = extensions[i];
			processExtension(tracker, extension);
		}
		tracker.registerHandler(this, ExtensionTracker
				.createExtensionPointFilter(point));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler#addExtension(org.eclipse.core.runtime.dynamichelpers.IExtensionTracker,
	 *      org.eclipse.core.runtime.IExtension)
	 */
	public void addExtension(IExtensionTracker tracker, IExtension extension) {
		processExtension(tracker, extension);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler#removeExtension(org.eclipse.core.runtime.IExtension,
	 *      java.lang.Object[])
	 */
	public void removeExtension(IExtension extension, Object[] objects) {
		for (int i = 0; i < objects.length; i++) {
			registeredContributions.remove(objects[i]);
		}

	}

	/**
	 * Process the extension and register the result with the tracker.
	 * 
	 * @param tracker
	 * @param extension
	 */
	private void processExtension(IExtensionTracker tracker,
			IExtension extension) {
		IConfigurationElement[] elements = extension.getConfigurationElements();
		for (int j = 0; j < elements.length; j++) {
			IConfigurationElement element = elements[j];
			FileSystemConfiguration contribution = newConfiguration(element);
			registeredContributions.add(contribution);
			tracker.registerObject(extension, contribution,
					IExtensionTracker.REF_STRONG);

		}
	}

	/**
	 * Return a new FileSystemContribution.
	 * 
	 * @param element
	 * @return FileSystemContribution or <code>null</code> if there is an
	 *         exception.
	 */
	private FileSystemConfiguration newConfiguration(
			final IConfigurationElement element) {

		final FileSystemContributor[] contributors = new FileSystemContributor[1];
		final CoreException[] exceptions = new CoreException[1];

		Platform.run(new ISafeRunnable() {
			public void run() {
				try {
					contributors[0] = (FileSystemContributor) IDEWorkbenchPlugin
							.createExtension(element, ATT_CLASS);

				} catch (CoreException exception) {
					exceptions[0] = exception;
				}
			}

			/*
			 * (non-Javadoc) Method declared on ISafeRunnable.
			 */
			public void handleException(Throwable e) {
				// Do nothing as Core will handle the logging
			}
		});

		if (exceptions[0] != null)
			return null;
		String name = element.getAttribute(LABEL);
		String fileSystem = element.getAttribute(FILESYSTEM);
		FileSystemConfiguration config = new FileSystemConfiguration(name,
				contributors[0]);
		if (fileSystem.equals(LOCAL_FILE_SYSTEM))
			defaultFileSystem = config;

		return config;

	}

	/**
	 * Return the FileSystemConfiguration defined in the receiver.
	 * 
	 * @return FileSystemConfiguration[]
	 */
	public FileSystemConfiguration[] getConfigurations() {
		FileSystemConfiguration[] configs = new FileSystemConfiguration[registeredContributions
				.size()];
		registeredContributions.toArray(configs);
		return configs;
	}

	/**
	 * Return the default file system configuration (the local file system
	 * extension in the ide plug-in).
	 * 
	 * @return FileSystemConfiguration
	 */
	public FileSystemConfiguration getDefaultConfiguration() {
		return defaultFileSystem;
	}

	/**
	 * Return whether or not there is only one file 
	 * system registered.
	 * @return <code>true</code> if there is only one file 
	 * system.
	 */
	public boolean hasOneFileSystem() {
		return registeredContributions.size() == 1;
	}
}
