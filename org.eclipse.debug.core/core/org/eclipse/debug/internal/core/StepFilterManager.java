/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.core;

import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.commands.IStepFiltersHandler;
import org.eclipse.debug.internal.core.commands.DebugCommandRequest;

/**
 * As targets are launched, this manager sets its step filter
 * support settings according to the "use step filter" setting.
 * 
 * @since 3.0
 */
public class StepFilterManager implements ILaunchListener {
	
	public static final String PREF_USE_STEP_FILTERS = DebugPlugin.getUniqueIdentifier() + ".USE_STEP_FILTERS"; //$NON-NLS-1$
	
	/**
	 * The step filter manager is instantiated by the debug UI plug-in,
	 * and should be accessed from the <code>DebugUIPlugin</code> class.
	 */
	protected StepFilterManager() {
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
	}
	
	/**
	 * This method is called by the debug UI plug-in at shutdown.
	 */
	public void shutdown() {
		DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchListener#launchAdded(org.eclipse.debug.core.ILaunch)
	 */
	public void launchAdded(ILaunch launch) {
		launchChanged(launch);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchListener#launchChanged(org.eclipse.debug.core.ILaunch)
	 */
	public void launchChanged(ILaunch launch) {
		IStepFiltersHandler command = (IStepFiltersHandler)launch.getAdapter(IStepFiltersHandler.class);
		if (command != null) {
			command.execute(new DebugCommandRequest(new Object[]{launch}));
		}
	}
	
	/**
	 * Returns whether the 'use step filters' preference is on.
	 * 
	 * @return whether to use step filters
	 */
	public boolean isUseStepFilters() {
		return Platform.getPreferencesService().getBoolean(DebugPlugin.getUniqueIdentifier(), PREF_USE_STEP_FILTERS, false, null);
	}
	
	/**
	 * Sets whether to use step filters.
	 * 
	 * @param useFilters whether to use step filters
	 */
	public void setUseStepFilters(boolean useFilters) {
		Preferences.setBoolean(DebugPlugin.getUniqueIdentifier(), PREF_USE_STEP_FILTERS, useFilters, null);
		ILaunch[] launchs = DebugPlugin.getDefault().getLaunchManager().getLaunches();
		for (int i = 0; i < launchs.length; i++) {
			ILaunch launch = launchs[i];
			launchChanged(launch);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchListener#launchRemoved(org.eclipse.debug.core.ILaunch)
	 */
	public void launchRemoved(ILaunch launch) {}
}
