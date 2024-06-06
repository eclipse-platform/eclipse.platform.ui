/*******************************************************************************
 * Copyright (c) 2018, 2021 SAP SE and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP SE - initial version
 *******************************************************************************/
package org.eclipse.urischeme.internal.registration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.urischeme.IOperatingSystemRegistration;
import org.eclipse.urischeme.IScheme;
import org.eclipse.urischeme.ISchemeInformation;

public class RegistrationMacOsX implements IOperatingSystemRegistration {

	private static final String PLIST_PATH_SUFFIX = "/Contents/Info.plist"; //$NON-NLS-1$
	private static final String LSREGISTER = "/System/Library/Frameworks/CoreServices.framework/Versions/A/Frameworks/LaunchServices.framework/Versions/A/Support/lsregister"; //$NON-NLS-1$
	private static final String UNREGISTER = "-u"; //$NON-NLS-1$
	private static final String RECURSIVE = "-r"; //$NON-NLS-1$
	private static final String DUMP = "-dump"; //$NON-NLS-1$
	private static final String ANY_LINES = "(?:.*\\n)*"; //$NON-NLS-1$
	private static final String TRAILING_HEX_VALUE_WITH_BRACKETS = "\\s\\(0x\\w*\\)"; //$NON-NLS-1$
	private static final String PATH_WITH_CAPTURING_GROUP = "path:\\s*(.*)"; //$NON-NLS-1$

	private IFileProvider fileProvider;
	private IProcessExecutor processExecutor;

	private String lsRegisterOutput = null;

	public RegistrationMacOsX() {
		this(new FileProvider(), new ProcessExecutor());
	}

	public RegistrationMacOsX(IFileProvider fileProvider, IProcessExecutor processExecutor) {
		this.fileProvider = fileProvider;
		this.processExecutor = processExecutor;
	}

	@Override
	public void handleSchemes(Collection<IScheme> toAdd, Collection<IScheme> toRemove) throws Exception {
		String pathToEclipseApp = getPathToEclipseApp();

		changePlistFile(toAdd, toRemove, pathToEclipseApp);

		registerAppWithLsregister(pathToEclipseApp);
	}

	@Override
	public List<ISchemeInformation> getSchemesInformation(Collection<IScheme> schemes) throws Exception {
		List<ISchemeInformation> returnList = new ArrayList<>();

		String eclipseLauncher = getEclipseLauncher();
		for (IScheme scheme : schemes) {

			SchemeInformation schemeInfo = new SchemeInformation(scheme.getName(), scheme.getDescription());

			String location = determineHandlerLocation(getLsRegisterOutput(), scheme.getName());
			if (!location.isEmpty() && eclipseLauncher.startsWith(location)) {
				schemeInfo.setHandled(true);
			}
			schemeInfo.setHandlerLocation(location);

			returnList.add(schemeInfo);
		}
		return returnList;
	}

	private String getLsRegisterOutput() throws Exception {
		if (this.lsRegisterOutput != null) {
			return this.lsRegisterOutput;
		}
		this.lsRegisterOutput = processExecutor.execute(LSREGISTER, DUMP);
		return this.lsRegisterOutput;
	}

	private String determineHandlerLocation(String lsRegisterDump, String scheme) {

		String[] lsRegisterEntries = lsRegisterDump.split("-{80}\n"); //$NON-NLS-1$
		String keyOfFirstLine;
		String keyOfSchemeList;

		if (Stream.of(lsRegisterEntries).parallel().anyMatch(s -> s.startsWith("BundleClass:"))) { //$NON-NLS-1$
			// pre macOS 10.15.3
			keyOfFirstLine = "BundleClass"; //$NON-NLS-1$
			keyOfSchemeList = "bindings:"; //$NON-NLS-1$
		} else {
			// starting with macOS 10.15.3
			keyOfFirstLine = "bundle id"; //$NON-NLS-1$
			keyOfSchemeList = "claimed schemes:"; //$NON-NLS-1$

		}

		Pattern pattern = Pattern.compile(
				"^" + ANY_LINES + "\\s*" + PATH_WITH_CAPTURING_GROUP + "\\n" + ANY_LINES + "\\s*" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				+ keyOfSchemeList + ".*" //$NON-NLS-1$
				+ Pattern.quote(scheme) + ":", //$NON-NLS-1$
				Pattern.MULTILINE);

		return Stream.of(lsRegisterEntries). //
				parallel().//
				filter(s -> s.startsWith(keyOfFirstLine)).//
				filter(s -> s.contains(scheme + ":")).// //$NON-NLS-1$
				map(pattern::matcher).//
				filter(Matcher::find).//
				map(m -> m.group(1)).findFirst().map(s -> s.replaceFirst(TRAILING_HEX_VALUE_WITH_BRACKETS, "")) //$NON-NLS-1$
				.orElse(""); //$NON-NLS-1$
	}

	private PlistFileWriter getPlistFileWriter(String plistPath) {
		return new PlistFileWriter(() -> fileProvider.newReader(plistPath));
	}

	@Override
	public String getEclipseLauncher() {
		return getPathToEclipseApp();
	}

	private void registerAppWithLsregister(String pathToEclipseApp) throws Exception {
		processExecutor.execute(LSREGISTER, UNREGISTER, pathToEclipseApp);
		processExecutor.execute(LSREGISTER, RECURSIVE, pathToEclipseApp);
	}

	private void changePlistFile(Collection<IScheme> toAdd, Collection<IScheme> toRemove, String pathToEclipseApp) {
		if (!supportsRegistration()) {
			return;
		}
		String plistPath = pathToEclipseApp + PLIST_PATH_SUFFIX;

		PlistFileWriter writer = getPlistFileWriter(plistPath);

		for (IScheme scheme : toAdd) {
			writer.addScheme(scheme.getName(), scheme.getDescription());
		}

		for (IScheme scheme : toRemove) {
			writer.removeScheme(scheme.getName());
		}

		writer.writeTo(() -> fileProvider.newWriter(plistPath));
	}

	/**
	 * Depending if Eclipse is running stand alone (productive use-case) or started
	 * from another Eclipse ("Run as Eclipse Application" use-case) the
	 * "eclipse.home.location" property looks differently. The
	 * "eclipse.home.location" property is also dependent on the how the target
	 * platform is defined.<br>
	 *
	 * <ol>
	 * <li>Productive Use:
	 * <code>file:/path/to/Eclipse.app/Contents/Eclipse/</code></li>
	 * <li>As Eclipse Application (running Platform):
	 * <code>file:/path/to/Eclipse.app/Contents/Eclipse/</code></li>
	 * <li>As Eclipse Application :
	 * <code>file:/path/to/FolderwithoutDotApp/Contents/Eclipse/</code></li>
	 * <li>As Eclipse Application :
	 * <code>file:/path/to/FolderwithoutDotApp/Contents/Eclipse/plugins/</code></li>
	 * </ol>
	 *
	 * @return The path the app
	 */
	private String getPathToEclipseApp() {
		String homeLocationProperty = System.getProperty("eclipse.home.location"); //$NON-NLS-1$
		// remove the "file:" prefix and everything starting at "/Contents/Eclipse"
		return homeLocationProperty.replaceAll("file:(.*)\\/Contents\\/Eclipse.*", "$1"); //$NON-NLS-1$ //$NON-NLS-2$
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

	@Override
	public boolean supportsRegistration() {
		// if the application is signed we cannot register URI schemes because this
		// would break the signature of the applications
		// applications with broken signature cannot be executed any more (at least on
		// newer macOS versions).
		return !fileProvider.fileExists(getPathToEclipseApp() + "/Contents/_CodeSignature"); //$NON-NLS-1$
	}
}