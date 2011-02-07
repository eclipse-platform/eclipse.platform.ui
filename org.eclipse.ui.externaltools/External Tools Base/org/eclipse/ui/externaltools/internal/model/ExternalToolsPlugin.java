/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.externaltools.internal.model;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.externaltools.internal.IExternalToolConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.externaltools.internal.program.launchConfigurations.ExternalToolsProgramMessages;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * External tools plug-in class
 */
public final class ExternalToolsPlugin extends AbstractUIPlugin implements
		ILaunchListener {
	
	public static final String PLUGIN_ID = "org.eclipse.ui.externaltools"; //$NON-NLS-1$
	
	/**
	 * Status representing no problems encountered during operation.
	 */
	public static final IStatus OK_STATUS = new Status(IStatus.OK, PLUGIN_ID, 0, IExternalToolConstants.EMPTY_STRING, null);
		
	private static ExternalToolsPlugin plugin;

	private static IWindowListener fWindowListener;

	private static ILaunchManager launchManager;

	/**
	 * A window listener that warns the user about any running programs when the
	 * workbench closes. Programs are killed when the VM exits.
	 */
	private class ProgramLaunchWindowListener implements IWindowListener {
		public void windowActivated(IWorkbenchWindow window) {
		}

		public void windowDeactivated(IWorkbenchWindow window) {
		}

		public void windowClosed(IWorkbenchWindow window) {
			IWorkbenchWindow windows[] = PlatformUI.getWorkbench()
					.getWorkbenchWindows();
			if (windows.length > 1) {
				// There are more windows still open.
				return;
			}
			ILaunchManager manager = DebugPlugin.getDefault()
					.getLaunchManager();
			ILaunchConfigurationType programType = manager
					.getLaunchConfigurationType(IExternalToolConstants.ID_PROGRAM_LAUNCH_CONFIGURATION_TYPE);
			if (programType == null) {
				return;
			}
			ILaunch launches[] = manager.getLaunches();
			ILaunchConfigurationType configType;
			ILaunchConfiguration config;
			for (int i = 0; i < launches.length; i++) {
				try {
					config = launches[i].getLaunchConfiguration();
					if (config == null) {
						continue;
					}
					configType = config.getType();
				} catch (CoreException e) {
					continue;
				}
				if (configType.equals(programType)) {
					if (!launches[i].isTerminated()) {
						MessageDialog
								.openWarning(
										window.getShell(),
										ExternalToolsProgramMessages.ProgramLaunchDelegate_Workbench_Closing_1,
										ExternalToolsProgramMessages.ProgramLaunchDelegate_The_workbench_is_exiting);
						break;
					}
				}
			}
		}

		public void windowOpened(IWorkbenchWindow window) {
		}
	}

	/**
	 * Create an instance of the External Tools plug-in.
	 */
	public ExternalToolsPlugin() {
		super();
		plugin = this;
	}

	/**
	 * Returns the default instance of the receiver.
	 * This represents the runtime plugin.
	 */
	public static ExternalToolsPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns a new <code>IStatus</code> for this plug-in
	 */
	public static IStatus newErrorStatus(String message, Throwable exception) {
		if (message == null) {
			return new Status(IStatus.ERROR, PLUGIN_ID, 0, IExternalToolConstants.EMPTY_STRING, exception); 
		}		
		return new Status(IStatus.ERROR, PLUGIN_ID, 0, message, exception);
	}

	/**
	 * Returns a new <code>CoreException</code> for this plug-in
	 */
	public static CoreException newError(String message, Throwable exception) {
		return new CoreException(new Status(IStatus.ERROR, PLUGIN_ID, 0, message, exception));
	}

	/**
	 * Writes the message to the plug-in's log
	 * 
	 * @param message the text to write to the log
	 */
	public void log(String message, Throwable exception) {
		IStatus status = newErrorStatus(message, exception);
		getLog().log(status);
	}
	
	public void log(Throwable exception) {
		//this message is intentionally not internationalized, as an exception may
		// be due to the resource bundle itself
		getLog().log(newErrorStatus("Internal error logged from External Tools UI: ", exception)); //$NON-NLS-1$
	}

	/**
	 * Returns the ImageDescriptor for the icon with the given path
	 * 
	 * @return the ImageDescriptor object
	 */
	public ImageDescriptor getImageDescriptor(String path) {
		try {
			Bundle bundle= getDefault().getBundle();
			URL installURL = bundle.getEntry("/"); //$NON-NLS-1$
			URL url = new URL(installURL, path);
			return ImageDescriptor.createFromURL(url);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	/**
	 * Returns the active workbench window or <code>null</code> if none
	 */
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}
	
	/**
	 * Returns the active workbench page or <code>null</code> if none.
	 */
	public static IWorkbenchPage getActivePage() {
		IWorkbenchWindow window= getActiveWorkbenchWindow();
		if (window != null) {
			return window.getActivePage();
		}
		return null;
	}
	
	/**
	 * Returns the active workbench shell or <code>null</code> if none.
	 */
	public static Shell getActiveWorkbenchShell() {
		IWorkbenchWindow window = getActiveWorkbenchWindow();
		if (window != null) {
			return window.getShell();
		}
		return null;
	}

	/**
	 * Returns the standard display to be used. The method first checks, if
	 * the thread calling this method has an associated display. If so, this
	 * display is returned. Otherwise the method returns the default display.
	 */
	public static Display getStandardDisplay() {
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		return display;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#createImageRegistry()
	 */
	protected ImageRegistry createImageRegistry() {
		return ExternalToolsImages.initializeImageRegistry();
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		try {
			ExternalToolsImages.disposeImageDescriptorRegistry();
		} finally {
			super.stop(context);
		}
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		// Listen to launches to lazily create "launch processors"
		launchManager = DebugPlugin.getDefault().getLaunchManager();
		ILaunch[] launches = launchManager.getLaunches();
		if (launches.length > 0) {
			if (fWindowListener == null) {
				fWindowListener = new ProgramLaunchWindowListener();
				PlatformUI.getWorkbench().addWindowListener(fWindowListener);
			}
		} else {
			// if no launches, wait for first launch to initialize processors
			launchManager.addLaunchListener(this);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchListener#launchAdded(org.eclipse.debug.core.ILaunch)
	 */
	public void launchAdded(ILaunch launch) {
		ILaunchConfiguration launchConfiguration = launch.getLaunchConfiguration();
		try {
			ILaunchConfigurationType launchConfigurationType = launchConfiguration.getType();
			if (launchConfigurationType.getIdentifier().equals(
							IExternalToolConstants.ID_PROGRAM_LAUNCH_CONFIGURATION_TYPE)) {
				if (fWindowListener == null) {
					fWindowListener = new ProgramLaunchWindowListener();
					PlatformUI.getWorkbench().addWindowListener(fWindowListener);
					launchManager.removeLaunchListener(this);
				}
			}
		} catch (CoreException e) {
			log(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchListener#launchChanged(org.eclipse.debug.core.ILaunch)
	 */
	public void launchChanged(ILaunch launch) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchListener#launchRemoved(org.eclipse.debug.core.ILaunch)
	 */
	public void launchRemoved(ILaunch launch) {
	}
}
