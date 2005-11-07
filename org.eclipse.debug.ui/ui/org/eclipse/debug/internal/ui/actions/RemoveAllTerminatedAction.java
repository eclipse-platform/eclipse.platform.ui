/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchesListener;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.ui.IViewPart;
 
/**
 * Removes all terminated/detached launches from the
 * active debug view.
 */
public class RemoveAllTerminatedAction extends AbstractRemoveAllActionDelegate implements ILaunchesListener {

	/**
	 * @see ListenerActionDelegate#doHandleDebugEvent(DebugEvent)
	 */	
	protected void doHandleDebugEvent(DebugEvent event) {	
		if (event.getKind() == DebugEvent.TERMINATE) {
			Object source = event.getSource();
			if (event.getKind() == DebugEvent.TERMINATE && (source instanceof IDebugTarget || source instanceof IProcess)) {
				update();
			}
		}
	}

	/** 
	 * Updates the enabled state of this action to enabled if at
	 * least one launch is terminated and relative to the current perspective.
	 */
	protected void update() {
		ILaunch[] launches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
		if (launches != null) {
			for (int i= 0; i < launches.length; i++) {
				if (launches[i].isTerminated()) {
					getAction().setEnabled(true);
					return;
				}
			}
		}
		getAction().setEnabled(false);
	}

	protected void doAction() {
		ILaunch[] launches = DebugPlugin.getDefault().getLaunchManager().getLaunches();
		removeTerminatedLaunches(launches);
	}

	public static void removeTerminatedLaunches(ILaunch[] elements) {
		List removed = new ArrayList();
		for (int i = 0; i < elements.length; i++) {
			ILaunch launch = elements[i];
			if (launch.isTerminated()) {
				removed.add(launch);
			}
		}
		if (!removed.isEmpty()) {
			ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
			manager.removeLaunches((ILaunch[])removed.toArray(new ILaunch[removed.size()]));
		}				
	}
	
	/**
	 * @see IViewActionDelegate#init(IViewPart)
	 */
	public void init(IViewPart view) {
		super.init(view);
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
		DebugPlugin.getDefault().addDebugEventListener(this);
	}
		
	/**
	 * @see IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		//removes as a debug event listener
		super.dispose();
		DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
        IViewPart view = getView();
        if (view != null) {
            view.getSite().getSelectionProvider().removeSelectionChangedListener((ISelectionChangedListener) getAction());
        }
	}
	
	/**
	 * @see ILaunchesListener#launchesAdded(ILaunch[])
	 */
	public void launchesAdded(ILaunch[] launches) {
	}

	/**
	 * @see ILaunchesListener#launchesChanged(ILaunch[])
	 */
	public void launchesChanged(ILaunch[] launches) {
	}

	/**
	 * @see ILaunchesListener#launchesRemoved(ILaunch[])
	 */
	public void launchesRemoved(ILaunch[] launches) {
		if (getAction().isEnabled()) {
			update();
		}
	}
}

