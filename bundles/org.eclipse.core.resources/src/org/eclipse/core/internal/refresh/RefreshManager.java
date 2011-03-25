/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.refresh;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.internal.resources.IManager;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.resources.*;
import org.eclipse.core.resources.refresh.IRefreshMonitor;
import org.eclipse.core.resources.refresh.IRefreshResult;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;

/**
 * Manages auto-refresh functionality, including maintaining the active
 * set of monitors and controlling the job that performs periodic refreshes
 * on out of sync resources.
 * 
 * @since 3.0
 */
public class RefreshManager implements IRefreshResult, IManager, Preferences.IPropertyChangeListener {

	/**
	 * Name of a preference for configuring whether out-of-sync resources are automatically
	 * asynchronously refreshed, when discovered to be out-of-sync by the workspace.
	 * <p>
	 * This preference suppresses out-of-sync CoreException for some read methods, including:
	 * {@link IFile#getContents()} & {@link IFile#getContentDescription()}. 
	 * </p>
	 * <p>
	 * In the future the workspace may enable other lightweight auto-refresh mechanisms when this
	 * preferece is true. (The existing {@link ResourcesPlugin#PREF_AUTO_REFRESH} will continue
	 * to enable filesystem hooks and the existing polling based monitor.)
	 * </p>
	 * This is a is true by default. Integrators should take care when
	 * changing this from the default. See the discussion: https://bugs.eclipse.org/303517
	 * @since 3.7
	 */
	public static final String PREF_LIGHTWEIGHT_AUTO_REFRESH = "refresh.lightweight.enabled"; //$NON-NLS-1$

	public static boolean DEBUG = Policy.DEBUG_AUTO_REFRESH;
	public static final String DEBUG_PREFIX = "Auto-refresh: "; //$NON-NLS-1$
	MonitorManager monitors;
	private RefreshJob refreshJob;

	/**
	 * The workspace.
	 */
	private IWorkspace workspace;

	public RefreshManager(IWorkspace workspace) {
		this.workspace = workspace;
	}

	/*
	 * Starts or stops auto-refresh depending on the auto-refresh preference.
	 */
	protected void manageAutoRefresh(boolean enabled) {
		//do nothing if we have already shutdown
		if (refreshJob == null)
			return;
		if (enabled) {
			refreshJob.start();
			monitors.start();
		} else {
			refreshJob.stop();
			monitors.stop();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.refresh.IRefreshResult#monitorFailed(org.eclipse.core.resources.refresh.IRefreshMonitor, org.eclipse.core.resources.IResource)
	 */
	public void monitorFailed(IRefreshMonitor monitor, IResource resource) {
		monitors.monitorFailed(monitor, resource);
	}

	/**
	 * Checks for changes to the PREF_AUTO_UPDATE property.
	 * @see org.eclipse.core.runtime.Preferences.IPropertyChangeListener#propertyChange(Preferences.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		String property = event.getProperty();
		if (ResourcesPlugin.PREF_AUTO_REFRESH.equals(property)) {
			Preferences preferences = ResourcesPlugin.getPlugin().getPluginPreferences();
			boolean autoRefresh = preferences.getBoolean(ResourcesPlugin.PREF_AUTO_REFRESH);
			manageAutoRefresh(autoRefresh);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.refresh.IRefreshResult#refresh(org.eclipse.core.resources.IResource)
	 */
	public void refresh(IResource resource) {
		//do nothing if we have already shutdown
		if (refreshJob != null)
			refreshJob.refresh(resource);
	}

	/**
	 * Shuts down the refresh manager.  This only happens when
	 * the resources plugin is going away.
	 */
	public void shutdown(IProgressMonitor monitor) {
		ResourcesPlugin.getPlugin().getPluginPreferences().removePropertyChangeListener(this);
		if (monitors != null) {
			monitors.stop();
			monitors = null;
		}
		if (refreshJob != null) {
			refreshJob.stop();
			refreshJob = null;
		}
	}

	/**
	 * Initializes the refresh manager. This does a minimal amount of work
	 * if auto-refresh is turned off.
	 */
	public void startup(IProgressMonitor monitor) {
		Preferences preferences = ResourcesPlugin.getPlugin().getPluginPreferences();
		preferences.addPropertyChangeListener(this);

		refreshJob = new RefreshJob();
		monitors = new MonitorManager(workspace, this);
		boolean autoRefresh = preferences.getBoolean(ResourcesPlugin.PREF_AUTO_REFRESH);
		if (autoRefresh)
			manageAutoRefresh(autoRefresh);
	}
}
