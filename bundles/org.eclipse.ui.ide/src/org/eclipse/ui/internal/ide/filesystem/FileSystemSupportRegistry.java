/*******************************************************************************
 * Copyright (c) 2006, 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Christian Dietrich (itemis AG) - fix for bug #549409
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - Bug 549409
 ******************************************************************************/

package org.eclipse.ui.internal.ide.filesystem;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Stream;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.fileSystem.FileSystemContributor;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.dialogs.IDEResourceInfoUtils;

/**
 * @since 3.2
 */
public class FileSystemSupportRegistry implements IExtensionChangeHandler {

	private static final String FILESYSTEM_SUPPORT = "filesystemSupport";//$NON-NLS-1$

	protected static final String ATT_CLASS = "class"; //$NON-NLS-1$

	private static final String LABEL = "label";//$NON-NLS-1$

	private static final String SCHEME = "scheme";//$NON-NLS-1$

	private static FileSystemSupportRegistry singleton;

	/**
	 * Get the instance of the registry.
	 *
	 * @return MarkerSupportRegistry
	 */
	public static FileSystemSupportRegistry getInstance() {
		if (singleton == null) {
			singleton = new FileSystemSupportRegistry();
		}
		return singleton;
	}

	private Collection<FileSystemConfiguration> registeredContributions = new HashSet<>(0);

	FileSystemConfiguration defaultConfiguration = new FileSystemConfiguration(
			FileSystemMessages.DefaultFileSystem_name, new FileSystemContributor() {
				@Override
				public URI browseFileSystem(String initialPath, Shell shell) {

					DirectoryDialog dialog = new DirectoryDialog(shell, SWT.SHEET);
					dialog
							.setMessage(IDEWorkbenchMessages.ProjectLocationSelectionDialog_directoryLabel);

					if (!initialPath.equals(IDEResourceInfoUtils.EMPTY_STRING)) {
						IFileInfo info = IDEResourceInfoUtils
								.getFileInfo(initialPath);
						if (info != null && info.exists()) {
							dialog.setFilterPath(initialPath);
						}
					}

					String selectedDirectory = dialog.open();
					if (selectedDirectory == null) {
						return null;
					}
					return new File(selectedDirectory).toURI();

				}
			}, null);

	/**
	 * Create a new instance of the receiver.
	 */
	public FileSystemSupportRegistry() {
		IExtensionTracker tracker = PlatformUI.getWorkbench().getExtensionTracker();
		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint(IDEWorkbenchPlugin.IDE_WORKBENCH,
						FILESYSTEM_SUPPORT);
		if (point == null) {
			return;
		}
		// initial population
		for (IExtension extension : point.getExtensions()) {
			processExtension(tracker, extension);
		}
		tracker.registerHandler(this, ExtensionTracker.createExtensionPointFilter(point));
	}

	@Override
	public void addExtension(IExtensionTracker tracker, IExtension extension) {
		processExtension(tracker, extension);
	}

	@Override
	public void removeExtension(IExtension extension, Object[] objects) {
		for (Object object : objects) {
			registeredContributions.remove(object);
		}
	}

	/**
	 * Process the extension and register the result with the tracker.
	 */
	private void processExtension(IExtensionTracker tracker, IExtension extension) {
		for (IConfigurationElement configElement : extension.getConfigurationElements()) {
			FileSystemConfiguration contribution = newConfiguration(configElement);
			if (contribution != null) {
				registeredContributions.add(contribution);
				tracker.registerObject(extension, contribution, IExtensionTracker.REF_STRONG);
			}
		}
	}

	/**
	 * Return a new FileSystemContribution.
	 *
	 * @return FileSystemContribution or <code>null</code> if there is an
	 *         exception.
	 */
	private FileSystemConfiguration newConfiguration(
			final IConfigurationElement element) {

		final FileSystemContributor[] contributors = new FileSystemContributor[1];
		final CoreException[] exceptions = new CoreException[1];

		SafeRunner.run(new ISafeRunnable() {
			@Override
			public void run() {
				try {
					contributors[0] = (FileSystemContributor) IDEWorkbenchPlugin
							.createExtension(element, ATT_CLASS);

				} catch (CoreException exception) {
					exceptions[0] = exception;
					IDEWorkbenchPlugin.getDefault().getLog().log(exception.getStatus());
				}
			}

			@Override
			public void handleException(Throwable e) {
				IDEWorkbenchPlugin.log(FileSystemMessages.FileSystemSupportRegistry_e_creating_extension, e);
			}
		});

		if (exceptions[0] != null) {
			return null;
		}
		String name = element.getAttribute(LABEL);
		String fileSystem = element.getAttribute(SCHEME);
		return new FileSystemConfiguration(name,
				contributors[0], fileSystem);

	}

	/**
	 * Return the FileSystemConfiguration defined in the receiver.
	 *
	 * @return FileSystemConfiguration[]
	 */
	public FileSystemConfiguration[] getConfigurations() {
		return Stream.concat(Stream.of(defaultConfiguration), registeredContributions.stream())
				.toArray(FileSystemConfiguration[]::new);
	}

	/**
	 * Return the default file system configuration (the local file system
	 * extension in the ide plug-in).
	 *
	 * @return FileSystemConfiguration
	 */
	public FileSystemConfiguration getDefaultConfiguration() {
		return defaultConfiguration;
	}

	/**
	 * Return whether or not there is only one file system registered.
	 *
	 * @return <code>true</code> if there is only one file system.
	 */
	public boolean hasOneFileSystem() {
		return registeredContributions.isEmpty();
	}
}
