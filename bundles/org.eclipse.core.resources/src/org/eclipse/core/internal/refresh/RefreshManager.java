/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.refresh;

import org.eclipse.core.internal.resources.IManager;
import org.eclipse.core.resources.*;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.refresh.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;

/**
 * Manages auto-refresh functionality, including maintaining the active
 * set of monitors and controlling the refresh job.
 */
public class RefreshManager implements IRefreshResult, IManager, Preferences.IPropertyChangeListener {
	public static boolean DEBUG = true;
	public static final String DEBUG_PREFIX = "Auto-refresh: "; //$NON-NLS-1$
	private MonitorManager monitors;
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
	protected void manageAutoRefresh() {
		Preferences preferences= ResourcesPlugin.getPlugin().getPluginPreferences();
		boolean autoRefresh= preferences.getBoolean(ResourcesPlugin.PREF_AUTO_REFRESH);
		if (autoRefresh) {
			refreshJob.start();
			monitors.start();
		} else {
			refreshJob.stop();
			monitors.stop();
		}		
	}
	public void monitorFailed(IRefreshMonitor monitor, IResource resource) {
	}
	/**
	 * Checks for changes to the the PREF_AUTO_UPDATE property.
	 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		String property= event.getProperty();
		if (ResourcesPlugin.PREF_AUTO_REFRESH.equals(property)) {
			manageAutoRefresh();
		} else if (ResourcesPlugin.PREF_REFRESH_POLLING_DELAY.equals(property)) {
			Preferences preferences= ResourcesPlugin.getPlugin().getPluginPreferences();
			long delay = preferences.getLong(ResourcesPlugin.PREF_REFRESH_POLLING_DELAY);
			monitors.setPollingDelay(delay);
		} 
	}
	
	public void refresh(IResource resources) {
		refreshJob.refresh(resources);
	}
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
	public void startup(IProgressMonitor monitor) {
		Preferences preferences= ResourcesPlugin.getPlugin().getPluginPreferences();
		preferences.setDefault(ResourcesPlugin.PREF_AUTO_REFRESH, true);
		preferences.setDefault(ResourcesPlugin.PREF_REFRESH_POLLING_DELAY, 30000);
		preferences.addPropertyChangeListener(this);
		long pollingDelay = preferences.getLong(ResourcesPlugin.PREF_REFRESH_POLLING_DELAY);
		
		refreshJob = new RefreshJob();
		monitors = new MonitorManager(workspace, this);
		monitors.setPollingDelay(pollingDelay);
		manageAutoRefresh();
	}
}