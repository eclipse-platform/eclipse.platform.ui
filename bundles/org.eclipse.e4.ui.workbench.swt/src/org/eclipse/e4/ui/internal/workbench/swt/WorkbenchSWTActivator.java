/*******************************************************************************
 * Copyright (c) 2010, 2019 IBM Corporation and others.
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
package org.eclipse.e4.ui.internal.workbench.swt;

import static org.eclipse.e4.ui.internal.workbench.swt.Policy.DEBUG;
import static org.eclipse.e4.ui.internal.workbench.swt.Policy.DEBUG_CMDS;
import static org.eclipse.e4.ui.internal.workbench.swt.Policy.DEBUG_CMDS_FLAG;
import static org.eclipse.e4.ui.internal.workbench.swt.Policy.DEBUG_CONTEXTS;
import static org.eclipse.e4.ui.internal.workbench.swt.Policy.DEBUG_CONTEXTS_FLAG;
import static org.eclipse.e4.ui.internal.workbench.swt.Policy.DEBUG_FLAG;
import static org.eclipse.e4.ui.internal.workbench.swt.Policy.DEBUG_FOCUS;
import static org.eclipse.e4.ui.internal.workbench.swt.Policy.DEBUG_FOCUS_FLAG;
import static org.eclipse.e4.ui.internal.workbench.swt.Policy.DEBUG_MENUS;
import static org.eclipse.e4.ui.internal.workbench.swt.Policy.DEBUG_MENUS_FLAG;
import static org.eclipse.e4.ui.internal.workbench.swt.Policy.DEBUG_RENDERER;
import static org.eclipse.e4.ui.internal.workbench.swt.Policy.DEBUG_RENDERER_FLAG;
import static org.eclipse.e4.ui.internal.workbench.swt.Policy.DEBUG_WORKBENCH;
import static org.eclipse.e4.ui.internal.workbench.swt.Policy.DEBUG_WORKBENCH_FLAG;
import static org.eclipse.e4.ui.internal.workbench.swt.Policy.TRACE;
import static org.eclipse.e4.ui.internal.workbench.swt.Policy.TRACE_FLAG;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;
import org.eclipse.core.internal.runtime.InternalPlatform;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.osgi.service.debug.DebugOptions;
import org.eclipse.osgi.service.debug.DebugOptionsListener;
import org.eclipse.osgi.service.debug.DebugTrace;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The activator class controls the plug-in life cycle
 */
public class WorkbenchSWTActivator implements BundleActivator, DebugOptionsListener {
	public static final String PI_RENDERERS = "org.eclipse.e4.ui.workbench.swt"; //$NON-NLS-1$

	private BundleContext context;
	private ServiceTracker<?, Location> locationTracker;
	private static WorkbenchSWTActivator activator;
	private DebugTrace trace;


	/**
	 * Get the default activator.
	 *
	 * @return a BundleActivator
	 */
	public static WorkbenchSWTActivator getDefault() {
		return activator;
	}

	/**
	 * @return this bundles context
	 */
	public BundleContext getContext() {
		return context;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		activator = this;
		this.context = context;
		Hashtable<String, String> props = new Hashtable<>(2);
		props.put(DebugOptions.LISTENER_SYMBOLICNAME, PI_RENDERERS);
		context.registerService(DebugOptionsListener.class, this, props);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		saveDialogSettings();
	}

	public Bundle getBundle() {
		if (context == null) {
			return null;
		}
		return context.getBundle();
	}

	/**
	 * @return the instance Location service
	 */
	public Location getInstanceLocation() {
		if (locationTracker == null) {
			Filter filter = null;
			try {
				filter = context.createFilter(Location.INSTANCE_FILTER);
			} catch (InvalidSyntaxException e) {
				// ignore this. It should never happen as we have tested the
				// above format.
			}
			locationTracker = new ServiceTracker<>(context, filter, null);
			locationTracker.open();
		}
		return locationTracker.getService();
	}

	public static void trace(String option, String msg, Throwable error) {
		activator.getTrace().trace(option, msg, error);
	}

	@Override
	public void optionsChanged(DebugOptions options) {
		trace = options.newDebugTrace(PI_RENDERERS);
		DEBUG = options.getBooleanOption(PI_RENDERERS + DEBUG_FLAG, false);
		TRACE = options.getBooleanOption(PI_RENDERERS + TRACE_FLAG, false);
		DEBUG_CMDS = options.getBooleanOption(PI_RENDERERS + DEBUG_CMDS_FLAG, false);
		DEBUG_CONTEXTS = options.getBooleanOption(PI_RENDERERS + DEBUG_CONTEXTS_FLAG, false);
		DEBUG_FOCUS = options.getBooleanOption(PI_RENDERERS + DEBUG_FOCUS_FLAG, false);
		DEBUG_MENUS = options.getBooleanOption(PI_RENDERERS + DEBUG_MENUS_FLAG, false);
		DEBUG_RENDERER = options.getBooleanOption(PI_RENDERERS + DEBUG_RENDERER_FLAG, false);
		DEBUG_WORKBENCH = options.getBooleanOption(PI_RENDERERS + DEBUG_WORKBENCH_FLAG, false);
	}

	public DebugTrace getTrace() {
		return trace;
	}

	// //////////////////////////////////////////////////////////////////////
	// The following code was copied from AbstractUIPlugin class.

	/**
	 * The name of the dialog settings file (value
	 * <code>"dialog_settings.xml"</code>).
	 */
	private static final String FN_DIALOG_SETTINGS = "dialog_settings.xml"; //$NON-NLS-1$
	/**
	 * Storage for dialog and wizard data; <code>null</code> if not yet
	 * initialized.
	 */
	private IDialogSettings dialogSettings = null;

	/**
	 * Returns the dialog settings for this UI plug-in. The dialog settings is
	 * used to hold persistent state data for the various wizards and dialogs of
	 * this plug-in in the context of a workbench.
	 * <p>
	 * If an error occurs reading the dialog store, an empty one is quietly
	 * created and returned.
	 * </p>
	 * <p>
	 * Subclasses may override this method but are not expected to.
	 * </p>
	 *
	 * @return the dialog settings
	 */
	public IDialogSettings getDialogSettings() {
		if (dialogSettings == null) {
			loadDialogSettings();
		}
		return dialogSettings;
	}

	/**
	 * Loads the dialog settings for this plug-in. The default implementation
	 * first looks for a standard named file in the plug-in's read/write state
	 * area; if no such file exists, the plug-in's install directory is checked
	 * to see if one was installed with some default settings; if no file is
	 * found in either place, a new empty dialog settings is created. If a
	 * problem occurs, an empty settings is silently used.
	 * <p>
	 * This framework method may be overridden, although this is typically
	 * unnecessary.
	 * </p>
	 */
	protected void loadDialogSettings() {
		dialogSettings = new DialogSettings("Workbench"); //$NON-NLS-1$

		// bug 69387: The instance area should not be created (in the call to
		// #getStateLocation) if -data @none or -data @noDefault was used
		IPath dataLocation = getStateLocationOrNull();
		if (dataLocation != null) {
			// try r/w state area in the local file system
			String readWritePath = dataLocation.append(FN_DIALOG_SETTINGS).toOSString();
			File settingsFile = new File(readWritePath);
			if (settingsFile.exists()) {
				try {
					dialogSettings.load(readWritePath);
				} catch (IOException e) {
					// load failed so ensure we have an empty settings
					dialogSettings = new DialogSettings("Workbench"); //$NON-NLS-1$
				}

				return;
			}
		}

		// otherwise look for bundle specific dialog settings
		Bundle bundle = context.getBundle();
		URL dsURL = FileLocator.find(bundle, IPath.fromOSString(FN_DIALOG_SETTINGS), null);
		if (dsURL == null) {
			return;
		}

		try (InputStream is = dsURL.openStream()) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
			dialogSettings.load(reader);
		} catch (IOException e) {
			// load failed so ensure we have an empty settings
			dialogSettings = new DialogSettings("Workbench"); //$NON-NLS-1$
		}
	}

	/**
	 * Saves this plug-in's dialog settings. Any problems which arise are
	 * silently ignored.
	 */
	protected void saveDialogSettings() {
		if (dialogSettings == null) {
			return;
		}

		try {
			IPath path = getStateLocationOrNull();
			if (path == null) {
				return;
			}
			String readWritePath = path.append(FN_DIALOG_SETTINGS).toOSString();
			dialogSettings.save(readWritePath);
		} catch (IOException | IllegalStateException e) {
			// spec'ed to ignore problems
		}
	}

	/**
	 * FOR INTERNAL WORKBENCH USE ONLY.
	 *
	 * Returns the path to a location in the file system that can be used to
	 * persist/restore state between workbench invocations. If the location did
	 * not exist prior to this call it will be created. Returns
	 * <code>null</code> if no such location is available.
	 *
	 * @return path to a location in the file system where this plug-in can
	 *         persist data between sessions, or <code>null</code> if no such
	 *         location is available.
	 * @since 3.1
	 */
	private IPath getStateLocationOrNull() {
		// TBD the state location is only accessible from Plugin class
		// However, using it causes problems in the activation order
		// So, for now, we get it directly.
		try {
			return InternalPlatform.getDefault().getStateLocation(context.getBundle(), true);
		} catch (IllegalStateException e) {
			// This occurs if -data=@none is explicitly specified, so ignore
			// this silently.
			// Is this OK? See bug 85071.
			return null;
		}
	}

}
