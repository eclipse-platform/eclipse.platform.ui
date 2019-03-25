/*******************************************************************************
 * Copyright (c) 2018 SAP SE and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP SE - initial version
 *******************************************************************************/
package org.eclipse.urischeme.internal.registration;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.urischeme.IOperatingSystemRegistration;
import org.eclipse.urischeme.IScheme;
import org.eclipse.urischeme.ISchemeInformation;
/**
 * Windows OS specific handling of schemes
 *
 */
public class RegistrationWindows implements IOperatingSystemRegistration {
	IRegistryWriter registryWriter;
	IFileProvider fileProvider;

	/**
	 * Creates an instance of RegistryWriter. The instance would help in
	 * reading,writing and removing entries from Windows Registry.
	 */
	public RegistrationWindows() {
		this(new RegistryWriter(), new FileProvider());
	}

	/**
	 * Creates an instance of RegistryWriter. The instance would help in
	 * reading,writing and removing entries from Windows Registry.
	 *
	 * This constructor is predominantly added for writing unit test methods for
	 * this class
	 *
	 * @param registryWriter the interface for windows registry handling
	 * @param fileProvider   the interface for the file provider
	 */
	public RegistrationWindows(IRegistryWriter registryWriter, IFileProvider fileProvider) {
		this.registryWriter = registryWriter;
		this.fileProvider = fileProvider;
	}

	@Override
	public void handleSchemes(Collection<IScheme> toAdd, Collection<IScheme> toRemove)
			throws Exception {
		for (IScheme scheme : toAdd) {
			registryWriter.addScheme(scheme.getName(), getEclipseLauncher());
		}
		for (IScheme scheme : toRemove) {
			registryWriter.removeScheme(scheme.getName());
		}
	}

	/**
	 * Takes the given schemes,converts them to schemeInformation type by adding all
	 * the properties like schemeName,schemeDescription, handled(is handled by
	 * current instance) and handlerPath. If there is no handlerPath defined it is
	 * set to "<none>"
	 *
	 * @param schemes The schemes that should be checked for registrations.
	 * @return the registered schemes.
	 * @throws Exception
	 */
	@Override
	public List<ISchemeInformation> getSchemesInformation(Collection<IScheme> schemes) throws Exception {
		String launcher = getEclipseLauncher();

		List<ISchemeInformation> schemeInformations = new ArrayList<>();

		for (IScheme scheme : schemes) {
			SchemeInformation schemeInfo = new SchemeInformation(scheme.getName(),
					scheme.getDescription());
			String path = registryWriter.getRegisteredHandlerPath(schemeInfo.getName());
			if (path == null) {
				path = ""; //$NON-NLS-1$
			}
			schemeInfo.setHandled(path.equals(launcher));
			schemeInfo.setHandlerLocation(path);
			schemeInformations.add(schemeInfo);
		}
		return schemeInformations;
	}

	@Override
	public String getEclipseLauncher() {
		String launcher = getLauncherFromLauncherProperty();
		if (launcher != null) {
			return launcher;
		}
		return getLauncherFromHomeLocation();
	}

	/**
	 * Only one application can handle a specific uri scheme on Windows. This
	 * information is stored centrally in the registry. Registering an uri scheme
	 * that is already handled by another application simply overwrites the
	 * registration of the other application in the registry.
	 *
	 * @return always <code>true</code>
	 */
	@Override
	public boolean canOverwriteOtherApplicationsRegistration() {
		return true;
	}

	private String getLauncherFromLauncherProperty() {
		String launcher = System.getProperty("eclipse.launcher"); //$NON-NLS-1$
		if (launcher != null && this.fileProvider.fileExists(launcher) && !fileProvider.isDirectory(launcher)) {
			return launcher;
		}
		return null;
	}

	/**
	 * Launcher may be null in runtime workbenches hosted by PDE. Check home
	 * location for any launcher file as a fallback.
	 *
	 * @return returns the launcher
	 */
	private String getLauncherFromHomeLocation() {
		String homeLocation = System.getProperty("eclipse.home.location"); //$NON-NLS-1$
		Assert.isNotNull(homeLocation, "home location must not be null"); //$NON-NLS-1$

		URL homeLocationUrl;
		try {
			// The property was created using the deprecated java.io.File.toURL,
			// which does not properly escape special characters.
			// Therefore, we also need to use URL instead of URI to parse it now,
			// as the URI parser is more strict.
			homeLocationUrl = new URL(homeLocation);
		} catch (MalformedURLException e) {
			return null;
		}
		if (!"file".equals(homeLocationUrl.getProtocol())) { //$NON-NLS-1$
			return null;
		}

		String directory = fileProvider.getFilePath(homeLocationUrl);
		if (!fileProvider.fileExists(directory) || !fileProvider.isDirectory(directory)) {
			return null;
		}

		try (DirectoryStream<Path> stream = fileProvider.newDirectoryStream(directory, "*.exe")) { //$NON-NLS-1$
			for (Path path : stream) {
				return path.toString();
			}
		} catch (IOException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
		return null;
	}
}
