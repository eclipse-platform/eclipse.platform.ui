/*******************************************************************************
 * Copyright (c) 2005, 2021 IBM Corporation and others.
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
 *******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.NodeChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.internal.InternalPolicy;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.util.StatusHandler;
import org.eclipse.ui.statushandlers.IStatusAdapterConstants;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * Utility class for setting up JFace for use by Eclipse.
 *
 * @since 3.1
 */
final class JFaceUtil {

	private JFaceUtil() {
		// prevents instantiation
	}

	/**
	 * Initializes JFace for use by Eclipse.
	 */
	public static void initializeJFace() {
		// Set the SafeRunner to run all SafeRunnables
		SafeRunnable.setRunner(SafeRunner::run);

		// Pass all errors and warnings to the status handling facility
		// and the rest to the main runtime log
		Policy.setLog(status -> {
			if (status.getSeverity() == IStatus.WARNING || status.getSeverity() == IStatus.ERROR) {
				StatusManager.getManager().handle(status);
			} else {
				WorkbenchPlugin.log(status);
			}
		});

		Policy.setStatusHandler(new StatusHandler() {
			@Override
			public void show(IStatus status, String title) {
				StatusAdapter statusAdapter = new StatusAdapter(status);
				statusAdapter.setProperty(IStatusAdapterConstants.TITLE_PROPERTY, title);
				StatusManager.getManager().handle(statusAdapter, StatusManager.SHOW);
			}
		});

		// Get all debug options from Platform
		if ("true".equalsIgnoreCase(Platform.getDebugOption(Policy.JFACE + "/debug"))) { //$NON-NLS-1$ //$NON-NLS-2$
			Policy.DEBUG_DIALOG_NO_PARENT = "true" //$NON-NLS-1$
					.equalsIgnoreCase(Platform.getDebugOption(Policy.JFACE + "/debug/dialog/noparent")); //$NON-NLS-1$
			Policy.TRACE_ACTIONS = "true".equalsIgnoreCase(Platform.getDebugOption(Policy.JFACE + "/trace/actions")); //$NON-NLS-1$ //$NON-NLS-2$
			Policy.TRACE_TOOLBAR = "true" //$NON-NLS-1$
					.equalsIgnoreCase(Platform.getDebugOption(Policy.JFACE + "/trace/toolbarDisposal")); //$NON-NLS-1$
			InternalPolicy.DEBUG_LOG_REENTRANT_VIEWER_CALLS = "true" //$NON-NLS-1$
					.equalsIgnoreCase(Platform.getDebugOption(Policy.JFACE + "/debug/viewers/reentrantViewerCalls")); //$NON-NLS-1$
			InternalPolicy.DEBUG_LOG_EQUAL_VIEWER_ELEMENTS = "true" //$NON-NLS-1$
					.equalsIgnoreCase(Platform.getDebugOption(Policy.JFACE + "/debug/viewers/equalElements")); //$NON-NLS-1$
			InternalPolicy.DEBUG_BIDI_UTILS = "true" //$NON-NLS-1$
					.equalsIgnoreCase(Platform.getDebugOption(Policy.JFACE + "/debug/bidiUtils")); //$NON-NLS-1$
			InternalPolicy.DEBUG_TRACE_URL_IMAGE_DESCRIPTOR = "true" //$NON-NLS-1$
					.equalsIgnoreCase(Platform.getDebugOption(Policy.JFACE + "/debug/trace/URLImageDescriptor")); //$NON-NLS-1$
			InternalPolicy.DEBUG_LOG_URL_IMAGE_DESCRIPTOR_MISSING_2x = "true" //$NON-NLS-1$
					.equalsIgnoreCase(Platform.getDebugOption(Policy.JFACE + "/debug/logURLImageDescriptorMissing2x")); //$NON-NLS-1$
			InternalPolicy.DEBUG_LOAD_URL_IMAGE_DESCRIPTOR_DIRECTLY = "true" //$NON-NLS-1$
					.equalsIgnoreCase(Platform.getDebugOption(Policy.JFACE + "/debug/loadURLImageDescriptorDirectly")); //$NON-NLS-1$
			// loadURLImageDescriptor2x is "true" by default and should stay "true" when
			// absent in the debug options file:
			InternalPolicy.DEBUG_LOAD_URL_IMAGE_DESCRIPTOR_2x = !"false" //$NON-NLS-1$
					.equalsIgnoreCase(Platform.getDebugOption(Policy.JFACE + "/debug/loadURLImageDescriptor2x")); //$NON-NLS-1$
			InternalPolicy.DEBUG_LOAD_URL_IMAGE_DESCRIPTOR_2x_PNG_FOR_GIF = "true".equalsIgnoreCase( //$NON-NLS-1$
					Platform.getDebugOption(Policy.JFACE + "/debug/loadURLImageDescriptor2xPngForGif")); //$NON-NLS-1$
		}
	}

	/**
	 * Adds a preference listener so that the JFace preference store is initialized
	 * as soon as the workbench preference store becomes available.
	 */
	public static void initializeJFacePreferences() {
		IEclipsePreferences rootNode = (IEclipsePreferences) Platform.getPreferencesService().getRootNode()
				.node(InstanceScope.SCOPE);
		final String workbenchName = WorkbenchPlugin.getDefault().getBundle().getSymbolicName();

		rootNode.addNodeChangeListener(new IEclipsePreferences.INodeChangeListener() {
			@Override
			public void added(NodeChangeEvent event) {
				if (!event.getChild().name().equals(workbenchName)) {
					return;
				}
				((IEclipsePreferences) event.getChild())
						.addPreferenceChangeListener(PlatformUIPreferenceListener.getSingleton());

			}

			@Override
			public void removed(NodeChangeEvent event) {
				// Nothing to do here

			}
		});

		JFacePreferences.setPreferenceStore(WorkbenchPlugin.getDefault().getPreferenceStore());
	}
}
