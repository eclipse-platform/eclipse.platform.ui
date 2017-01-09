/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.actions;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.jface.action.IAction;

/**
 * Removes all terminated/detached launches from the
 * active debug view.
 */
public class RemoveAllTerminatedAction extends AbstractRemoveAllActionDelegate implements ILaunchesListener2 {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.selection.AbstractRemoveAllActionDelegate#isEnabled()
	 */
	@Override
	protected boolean isEnabled() {
		ILaunch[] launches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
		if (launches != null) {
			for (int i= 0; i < launches.length; i++) {
				if (launches[i].isTerminated()) {
					return true;
				}
			}
		}
		return false;
	}

	public static void removeTerminatedLaunches(ILaunch[] elements) {
		List<ILaunch> removed = new ArrayList<ILaunch>();
		for (int i = 0; i < elements.length; i++) {
			ILaunch launch = elements[i];
			if (launch.isTerminated()) {
				removed.add(launch);
			}
		}
		if (!removed.isEmpty()) {
			ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
			manager.removeLaunches(removed.toArray(new ILaunch[removed.size()]));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.selection.AbstractRemoveAllActionDelegate#initialize()
	 */
	@Override
	protected void initialize() {
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.actions.selection.AbstractRemoveAllActionDelegate#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchesListener#launchesAdded(org.eclipse.debug.core.ILaunch[])
	 */
	@Override
	public void launchesAdded(ILaunch[] launches) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchesListener#launchesChanged(org.eclipse.debug.core.ILaunch[])
	 */
	@Override
	public void launchesChanged(ILaunch[] launches) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchesListener#launchesRemoved(org.eclipse.debug.core.ILaunch[])
	 */
	@Override
	public void launchesRemoved(ILaunch[] launches) {
		IAction action = getAction();
		if (action != null) {
			if (action.isEnabled()) {
				update();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchesListener2#launchesTerminated(org.eclipse.debug.core.ILaunch[])
	 */
	@Override
	public void launchesTerminated(ILaunch[] launches) {
		update();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action) {
		ILaunch[] launches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
		removeTerminatedLaunches(launches);
	}
}

