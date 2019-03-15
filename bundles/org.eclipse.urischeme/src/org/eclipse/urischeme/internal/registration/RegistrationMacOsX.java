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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.urischeme.IOperatingSystemRegistration;
import org.eclipse.urischeme.IScheme;
import org.eclipse.urischeme.ISchemeInformation;

@SuppressWarnings("javadoc")
public class RegistrationMacOsX implements IOperatingSystemRegistration {

	private static final String PLIST_PATH_SUFFIX = "/Contents/Info.plist"; //$NON-NLS-1$
	private static final String LSREGISTER = "/System/Library/Frameworks/CoreServices.framework/Versions/A/Frameworks/LaunchServices.framework/Versions/A/Support/lsregister"; //$NON-NLS-1$
	private static final String UNREGISTER = "-u"; //$NON-NLS-1$
	private static final String RECURSIVE = "-r"; //$NON-NLS-1$
	private static final String DUMP = "-dump"; //$NON-NLS-1$

	private IFileProvider fileProvider;
	private IProcessExecutor processExecutor;

	public RegistrationMacOsX() {
		this(new FileProvider(), new ProcessExecutor());
	}

	public RegistrationMacOsX(IFileProvider fileProvider, IProcessExecutor processExecutor) {
		this.fileProvider = fileProvider;
		this.processExecutor = processExecutor;
	}

	@Override
	public void handleSchemes(Collection<IScheme> toAdd, Collection<IScheme> toRemove)
			throws Exception {
		String pathToEclipseApp = getPathToEclipseApp();

		changePlistFile(toAdd, toRemove, pathToEclipseApp);

		registerAppWithLsregister(pathToEclipseApp);
	}

	@Override
	public List<ISchemeInformation> getSchemesInformation(Collection<IScheme> schemes) throws Exception {
		List<ISchemeInformation> returnList = new ArrayList<>();

		String lsRegisterOutput = processExecutor.execute(LSREGISTER, DUMP);

		String[] lsRegisterEntries = lsRegisterOutput.split("-{80}\n"); //$NON-NLS-1$

		for (IScheme scheme : schemes) {

			SchemeInformation schemeInfo = new SchemeInformation(scheme.getName(),
					scheme.getDescription());

			String location = determineHandlerLocation(lsRegisterEntries, scheme.getName());
			if (location != "" && getEclipseLauncher().startsWith(location)) { //$NON-NLS-1$
				schemeInfo.setHandled(true);
			}
			schemeInfo.setHandlerLocation(location);

			returnList.add(schemeInfo);
		}
		return returnList;
	}

	private String determineHandlerLocation(String[] lsRegisterEntries, String scheme) throws Exception {

		List<String> splitList = Stream.of(lsRegisterEntries). //
				parallel().//
				filter(s -> s.startsWith("BundleClass")).// //$NON-NLS-1$
				filter(s -> s.contains(scheme + ":")).// //$NON-NLS-1$
				collect(Collectors.toList());

		String lines = "(?:.*\\n)*"; //$NON-NLS-1$
		Pattern pattern = Pattern.compile(
				"^" + lines + "\\spath:\\s*(.*)\\n" + lines + "\\s*bindings:.*" + scheme + ":", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				Pattern.MULTILINE);

		for (String entry : splitList) {
			Matcher matcher = pattern.matcher(entry);
			if (matcher.find()) {
				return matcher.group(1);
			}
		}
		return ""; //$NON-NLS-1$
	}

	private PlistFileWriter getPlistFileWriter(String plistPath) throws IOException {
		return new PlistFileWriter(fileProvider.newReader(plistPath));
	}

	@Override
	public String getEclipseLauncher() {
		return getPathToEclipseApp();
	}

	private void registerAppWithLsregister(String pathToEclipseApp) throws Exception {
		processExecutor.execute(LSREGISTER, UNREGISTER, pathToEclipseApp);
		processExecutor.execute(LSREGISTER, RECURSIVE, pathToEclipseApp);
	}

	private void changePlistFile(Collection<IScheme> toAdd, Collection<IScheme> toRemove,
			String pathToEclipseApp) throws IOException {
		String plistPath = pathToEclipseApp + PLIST_PATH_SUFFIX;

		PlistFileWriter writer = getPlistFileWriter(plistPath);

		for (IScheme scheme : toAdd) {
			writer.addScheme(scheme.getName(), scheme.getDescription());
		}

		for (IScheme scheme : toRemove) {
			writer.removeScheme(scheme.getName());
		}

		writer.writeTo(fileProvider.newWriter(plistPath));
	}

	private String getPathToEclipseApp() {
		String homeLocationProperty = System.getProperty("eclipse.home.location"); //$NON-NLS-1$
		return homeLocationProperty.replaceAll("file:(.*.app).*", "$1"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Only one application can handle a specific uri scheme on macOS. This
	 * information is stored de-centrally in the Info.plist file and registered in a
	 * central launch database (e.g. via "lsregister"). Registering an uri scheme
	 * that is already handled by another application would also include changing
	 * the other application's Info.plist file - which can have unknown side
	 * effects.
	 *
	 * @return always <code>false</code>
	 */
	@Override
	public boolean canOverwriteOtherApplicationsRegistration() {
		return false;
	}
}