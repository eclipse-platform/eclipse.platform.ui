/*******************************************************************************
 * Copyright (c) 2020 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.urischeme;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.urischeme.internal.UriSchemeExtensionReader;

/**
 * Looks at all handlers, and register all handlers that were not attempted to
 * be registered earlier. This typically will try associating newly installed
 * handlers or handlers that were recently "freed" from other default
 * application.
 *
 * @since 1.1
 */
public class AutoRegisterSchemeHandlersJob extends Job {

	/**
	 * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=562426#c14 and
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=541653#c1 about skipping on
	 * Windows with Java 11+
	 */
	private static final boolean IS_WIN_JAVA_11 = Platform.getOS().equals(Platform.OS_WIN32)
			&& Integer.parseInt(System.getProperty("java.version").split("\\.")[0].split("-")[0]) >= 11; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	private static final String SKIP_PREFERENCE = "skipAutoRegistration"; //$NON-NLS-1$
	private static final String PROCESSED_SCHEMES_PREFERENCE = "processedSchemes"; //$NON-NLS-1$
	private static final String SCHEME_LIST_PREFERENCE_SEPARATOR = ","; //$NON-NLS-1$
	private static boolean alreadyTriggered = false;
	final private IEclipsePreferences preferenceNode;

	/**
	 *
	 */
	public AutoRegisterSchemeHandlersJob() {
		super(AutoRegisterSchemeHandlersJob.class.getSimpleName());
		preferenceNode = InstanceScope.INSTANCE.getNode(UriSchemeExtensionReader.PLUGIN_ID);
		setSystem(true);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		IUriSchemeExtensionReader extensionReader = IUriSchemeExtensionReader.newInstance();
		Collection<String> processedSchemes = new LinkedHashSet<>(Arrays
				.asList(preferenceNode.get(PROCESSED_SCHEMES_PREFERENCE, "").split(SCHEME_LIST_PREFERENCE_SEPARATOR))); //$NON-NLS-1$
		Collection<IScheme> toProcessSchemes = new LinkedHashSet<>(extensionReader.getSchemes());
		toProcessSchemes.removeIf(scheme -> processedSchemes.contains(scheme.getName()));
		if (toProcessSchemes.isEmpty()) {
			alreadyTriggered = true;
			return Status.OK_STATUS;
		}
		IOperatingSystemRegistration osRegistration = IOperatingSystemRegistration.getInstance();
		try {
			toProcessSchemes = osRegistration.getSchemesInformation(toProcessSchemes).stream() //
					.filter(scheme -> !scheme.isHandled()) //
					.collect(Collectors.toSet());
			if (toProcessSchemes.isEmpty()) {
				alreadyTriggered = true;
				return Status.OK_STATUS;
			}
			osRegistration.handleSchemes(toProcessSchemes, Collections.emptyList());
			processedSchemes.addAll(toProcessSchemes.stream().map(IScheme::getName).collect(Collectors.toList()));
			preferenceNode.put(PROCESSED_SCHEMES_PREFERENCE,
					processedSchemes.stream().collect(Collectors.joining(SCHEME_LIST_PREFERENCE_SEPARATOR)));
			preferenceNode.flush();
			alreadyTriggered = true;
			return Status.OK_STATUS;
		} catch (Exception e) {
			return new Status(IStatus.ERROR, UriSchemeExtensionReader.PLUGIN_ID, e.getMessage(), e);
		}
	}

	@Override
	public boolean shouldSchedule() {
		return !(IS_WIN_JAVA_11 || alreadyTriggered || Platform.getPreferencesService()
				.getBoolean(UriSchemeExtensionReader.PLUGIN_ID, SKIP_PREFERENCE, false, null));
	}
}
