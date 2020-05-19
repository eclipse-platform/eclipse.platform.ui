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

	private static final String SKIP_PREFERENCE = "skipAutoRegistration"; //$NON-NLS-1$
	private static final String PROCESSED_SCHEMES_PREFERENCE = "processedSchemes"; //$NON-NLS-1$
	private static final String SCHEME_LIST_PREFERENCE_SEPARATOR = ","; //$NON-NLS-1$
	private static boolean alreadyTriggered = false;
	private IEclipsePreferences preferenceNode;
	private IUriSchemeExtensionReader extensionReader;
	private IOperatingSystemRegistration osRegistration;	

	/**
	 *
	 */
	public AutoRegisterSchemeHandlersJob() {
		this(InstanceScope.INSTANCE.getNode(UriSchemeExtensionReader.PLUGIN_ID),
				IUriSchemeExtensionReader.newInstance(), IOperatingSystemRegistration.getInstance());
	}

	AutoRegisterSchemeHandlersJob(IEclipsePreferences preferenceNode, IUriSchemeExtensionReader extensionReader,
			IOperatingSystemRegistration osRegistration) {
		super(AutoRegisterSchemeHandlersJob.class.getSimpleName());
		this.preferenceNode = preferenceNode;
		this.extensionReader = extensionReader;
		this.osRegistration = osRegistration;
		setSystem(true);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		Collection<String> processedSchemes = new LinkedHashSet<>(Arrays
				.asList(preferenceNode.get(PROCESSED_SCHEMES_PREFERENCE, "").split(SCHEME_LIST_PREFERENCE_SEPARATOR))); //$NON-NLS-1$
		Collection<IScheme> toProcessSchemes = new LinkedHashSet<>(extensionReader.getSchemes());
		toProcessSchemes.removeIf(scheme -> processedSchemes.contains(scheme.getName()));
		if (toProcessSchemes.isEmpty()) {
			alreadyTriggered = true;
			return Status.OK_STATUS;
		}
		try {
			toProcessSchemes = osRegistration.getSchemesInformation(toProcessSchemes).stream() //
					.filter(scheme -> !scheme.schemeIsHandledByOther()) //
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
		} catch (Exception e) {
			Platform.getLog(getClass()).error(e.getMessage(), e);
		}
		return Status.OK_STATUS;
	}

	@Override
	public boolean shouldSchedule() {
		return !(alreadyTriggered || Platform.getPreferencesService().getBoolean(UriSchemeExtensionReader.PLUGIN_ID,
				SKIP_PREFERENCE, false, null));
	}
}
