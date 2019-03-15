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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.Platform;
import org.eclipse.urischeme.IOperatingSystemRegistration;
import org.eclipse.urischeme.IScheme;
import org.eclipse.urischeme.ISchemeInformation;

@SuppressWarnings("javadoc")
public class RegistrationLinux implements IOperatingSystemRegistration {

	private static final String USER_HOME = System.getProperty("user.home"); //$NON-NLS-1$
	private static final String PATH_TO_LOCAL_SHARE_APPS = USER_HOME + "/.local/share/applications/"; //$NON-NLS-1$
	private static final String DESKTOP_FILE_EXT = ".desktop"; //$NON-NLS-1$

	private static final String XDG_MIME = "xdg-mime"; //$NON-NLS-1$
	private static final String DEFAULT = "default"; //$NON-NLS-1$
	private static final String QUERY = "query"; //$NON-NLS-1$

	private static final String X_SCHEME_HANDLER_PREFIX = "x-scheme-handler/"; //$NON-NLS-1$

	private IFileProvider fileProvider;
	private IProcessExecutor processExecutor;
	private String productName;

	public RegistrationLinux() {
		this(new FileProvider(), new ProcessExecutor(), Platform.getProduct().getName());
	}

	public RegistrationLinux(IFileProvider fileProvider, IProcessExecutor processExecutor, String productName) {
		this.fileProvider = fileProvider;
		this.processExecutor = processExecutor;
		this.productName = productName;
	}

	@Override
	public void handleSchemes(Collection<IScheme> toAdd, Collection<IScheme> toRemove)
			throws Exception {
		String desktopFileName = getDesktopFileName();

		changeDesktopFile(toAdd, toRemove, PATH_TO_LOCAL_SHARE_APPS + desktopFileName);

		registerSchemesWithXdgMime(toAdd, desktopFileName);
	}

	@Override
	public List<ISchemeInformation> getSchemesInformation(Collection<IScheme> schemes) throws Exception {
		List<ISchemeInformation> returnList = new ArrayList<>();

		for (IScheme scheme : schemes) {
			SchemeInformation schemeInfo = new SchemeInformation(scheme.getName(),
					scheme.getDescription());

			String location = determineHandlerLocation(scheme.getName());
			if (location.equals(getEclipseLauncher())) {
				schemeInfo.setHandled(true);
			}
			schemeInfo.setHandlerLocation(location);
			returnList.add(schemeInfo);
		}
		return returnList;
	}

	private String determineHandlerLocation(String uriScheme) throws Exception {
		String desktopFileName = getRegisteredDesktopFileForScheme(uriScheme);
		if (!desktopFileName.isEmpty()) {
			return getHandlerLocationFromDesktopFileIfSchemeRegistered(desktopFileName, uriScheme);
		}
		return ""; //$NON-NLS-1$
	}

	private void changeDesktopFile(Iterable<IScheme> toAdd, Iterable<IScheme> toRemove,
			String desktopFilePath) throws IOException {

		List<String> lines = readFileOrGetInitialContent(desktopFilePath);

		DesktopFileWriter writer = new DesktopFileWriter(lines);
		for (IScheme scheme : toAdd) {
			writer.addScheme(scheme.getName());
		}
		for (IScheme scheme : toRemove) {
			writer.removeScheme(scheme.getName());
		}

		fileProvider.write(desktopFilePath, writer.getResult());
	}

	private List<String> readFileOrGetInitialContent(String desktopFilePath) {
		try {
			return fileProvider.readAllLines(desktopFilePath);
		} catch (IOException e) {
			return DesktopFileWriter.getMinimalDesktopFileContent(getEclipseLauncher(), productName);
		}
	}

	private void registerSchemesWithXdgMime(Collection<IScheme> schemes, String desktopFilePath)
			throws Exception {
		if (schemes.isEmpty()) {
			return;
		}
		String scheme = schemes.stream(). //
				map(s -> s.getName()). //
				collect(Collectors.joining(" " + X_SCHEME_HANDLER_PREFIX, X_SCHEME_HANDLER_PREFIX, "")); //$NON-NLS-1$ //$NON-NLS-2$
		processExecutor.execute(XDG_MIME, DEFAULT, desktopFilePath, scheme);
	}

	private String getHandlerLocationFromDesktopFileIfSchemeRegistered(String desktopFileName, String scheme)
			throws IOException {
		String path = PATH_TO_LOCAL_SHARE_APPS + desktopFileName;
		if (fileProvider.fileExists(path)) {
			List<String> lines = fileProvider.readAllLines(path);
			DesktopFileWriter writer = new DesktopFileWriter(lines);
			if (writer.isRegistered(scheme)) {
				return writer.getExecutableLocation();
			}
		}
		return ""; //$NON-NLS-1$
	}

	private String getRegisteredDesktopFileForScheme(String scheme) throws Exception {
		return processExecutor.execute(XDG_MIME, QUERY, DEFAULT, X_SCHEME_HANDLER_PREFIX + scheme);
	}

	private String getDesktopFileName() {
		String homeLocationProperty = getEclipseHomeLocation();
		homeLocationProperty = homeLocationProperty.replace("/", "_"); //$NON-NLS-1$ //$NON-NLS-2$
		return homeLocationProperty + DESKTOP_FILE_EXT;
	}

	@Override
	public String getEclipseLauncher() {
		return System.getProperty("eclipse.launcher"); //$NON-NLS-1$
	}

	private String getEclipseHomeLocation() {
		String homeLocationProperty = System.getProperty("eclipse.home.location"); //$NON-NLS-1$
		return homeLocationProperty.replaceAll("file:(.*)", "$1"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Only one application can handle a specific uri scheme on Linux. This
	 * information is stored de-centrally in the .desktop file and registered in a
	 * central database. Registering an uri scheme that is already handled by
	 * another application would also include changing the other application's
	 * .desktop file - which can have unknown side effects.
	 *
	 * @return always <code>false</code>
	 */
	@Override
	public boolean canOverwriteOtherApplicationsRegistration() {
		return false;
	}
}
