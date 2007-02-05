/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.NodeChangeEvent;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.util.ILogDialog;
import org.eclipse.jface.util.ILogger;
import org.eclipse.jface.util.ISafeRunnableRunner;
import org.eclipse.jface.util.Policy;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * Utility class for setting up JFace for use by Eclipse.
 * 
 * @since 3.1
 */
final class JFaceUtil {

	private JFaceUtil() {
		// prevents intantiation
	}

	/**
	 * Initializes JFace for use by Eclipse.
	 */
	public static void initializeJFace() {
		// Set the Platform to run all SafeRunnables
		SafeRunnable.setRunner(new ISafeRunnableRunner() {
			public void run(ISafeRunnable code) {
				Platform.run(code);
			}
		});

		// Pass all errors and warnings to the status handling facility
		// and the rest to the main runtime log
		Policy.setLog(new ILogger() {
			public void log(IStatus status) {
				if (status.getSeverity() == IStatus.WARNING
						|| status.getSeverity() == IStatus.ERROR) {
					StatusManager.getManager().handle(status);
				} else {
					WorkbenchPlugin.log(status);
				}
			}
		});

		// All JFace errors and warnings are forwarded
		// to the status handling facility instead of showing them in a dialog
		Policy.setLogDialog(new ILogDialog() {
			
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.util.ILogDialog#log(org.eclipse.swt.widgets.Shell,
			 *      java.lang.String, java.lang.String,
			 *      org.eclipse.core.runtime.IStatus, int)
			 */
			public int log(Shell parent, String title, String message,
					IStatus status, int displayMask) {
				if (status == null) {
					status = new Status(IStatus.ERROR,
							WorkbenchPlugin.PI_WORKBENCH, message);
				}
				StatusManager.getManager().handle(status, StatusManager.SHOW);
				return Window.OK;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.util.ILogDialog#log(org.eclipse.swt.widgets.Shell,
			 *      int, java.lang.String, java.lang.String)
			 */
			public int log(Shell parent, int severity, String title,
					String message) {
				if (severity == IStatus.ERROR || severity == IStatus.WARNING) {
					IStatus status = new Status(severity,
							WorkbenchPlugin.PI_WORKBENCH, message);
					StatusManager.getManager().handle(status,
							StatusManager.SHOW);
					return Window.OK;
				}

				int dialogConstant = MessageDialog.NONE;
				if (severity == IStatus.INFO) {
					dialogConstant = MessageDialog.INFORMATION;
				}
				MessageDialog dialog = new MessageDialog(parent, title,
						null, // accept the default window icon
						message, dialogConstant,
						new String[] { IDialogConstants.OK_LABEL }, 0);
				return dialog.open();
			}
		});

		// Get all debug options from Platform
		if ("true".equalsIgnoreCase(Platform.getDebugOption("/debug"))) { //$NON-NLS-1$ //$NON-NLS-2$
			Policy.DEBUG_DIALOG_NO_PARENT = "true".equalsIgnoreCase(Platform.getDebugOption(Policy.JFACE + "/debug/dialog/noparent")); //$NON-NLS-1$ //$NON-NLS-2$
			Policy.TRACE_ACTIONS = "true".equalsIgnoreCase(Platform.getDebugOption(Policy.JFACE + "/trace/actions")); //$NON-NLS-1$ //$NON-NLS-2$
			Policy.TRACE_TOOLBAR = "true".equalsIgnoreCase(Platform.getDebugOption(Policy.JFACE + "/trace/toolbarDisposal")); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * Adds a preference listener so that the JFace preference store is initialized
	 * as soon as the workbench preference store becomes available.
	 */
	public static void initializeJFacePreferences() {
		IEclipsePreferences rootNode = (IEclipsePreferences) Platform.getPreferencesService().getRootNode().node(InstanceScope.SCOPE);
		final String workbenchName = WorkbenchPlugin.getDefault().getBundle().getSymbolicName();
		
		rootNode.addNodeChangeListener(new IEclipsePreferences.INodeChangeListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.preferences.IEclipsePreferences.INodeChangeListener#added(org.eclipse.core.runtime.preferences.IEclipsePreferences.NodeChangeEvent)
			 */
			public void added(NodeChangeEvent event) {
				if (!event.getChild().name().equals(workbenchName)) {
					return;
				}
				((IEclipsePreferences) event.getChild()).addPreferenceChangeListener(PlatformUIPreferenceListener.getSingleton());

			}
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.core.runtime.preferences.IEclipsePreferences.INodeChangeListener#removed(org.eclipse.core.runtime.preferences.IEclipsePreferences.NodeChangeEvent)
			 */
			public void removed(NodeChangeEvent event) {
				// Nothing to do here

			}
		});
		
		JFacePreferences.setPreferenceStore(WorkbenchPlugin.getDefault().getPreferenceStore());
	}
}
