/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.internal.ui.commands.actions.ActionRequestMonitor;
import org.eclipse.debug.ui.commands.IStepFiltersCommand;

/**
 * As targets are launched, this manager sets its step filter
 * support settings according to the "use step filter" setting.
 * 
 * @since 3.0
 */
public class StepFilterManager implements ILaunchListener {
	
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
		IStepFiltersCommand command = (IStepFiltersCommand)launch.getAdapter(IStepFiltersCommand.class);
		if (command != null) {
			command.execute(launch, new ActionRequestMonitor());
		}
	}
	
	/**
	 * Returns whether the 'use step filters' preference is on.
	 * 
	 * @return whether to use step filters
	 */
	public boolean isUseStepFilters() {
		return DebugUIPlugin.getDefault().getPreferenceStore().getBoolean(IInternalDebugUIConstants.PREF_USE_STEP_FILTERS);
	}
	
	/**
	 * Sets whether to use step filters.
	 * 
	 * @param useFilters whether to use step filters
	 */
	public void setUseStepFilters(boolean useFilters) {
		DebugUIPlugin.getDefault().getPreferenceStore().setValue(IInternalDebugUIConstants.PREF_USE_STEP_FILTERS, useFilters);
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
