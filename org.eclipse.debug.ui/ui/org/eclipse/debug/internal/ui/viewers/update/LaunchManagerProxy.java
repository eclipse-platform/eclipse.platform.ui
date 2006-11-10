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
package org.eclipse.debug.internal.ui.viewers.update;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy;
import org.eclipse.jface.viewers.Viewer;

public class LaunchManagerProxy extends AbstractModelProxy implements ILaunchesListener2 {

	private ILaunchManager fLaunchManager;
	/**
	 * Map of each launch to its previous children. When a child is added,
	 * its model proxy is installed.
	 */
	private Map fPrevChildren = new HashMap(); 

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.AbstractModelProxy#init(org.eclipse.debug.internal.ui.viewers.IPresentationContext)
	 */
	public void init(IPresentationContext context) {
		super.init(context);
		fLaunchManager = DebugPlugin.getDefault().getLaunchManager();
		fLaunchManager.addLaunchListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy#installed(org.eclipse.jface.viewers.Viewer)
	 */
	public void installed(Viewer viewer) {
		// expand existing launches
		ILaunch[] launches = fLaunchManager.getLaunches();
		launchesAdded(launches);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.AbstractModelProxy#dispose()
	 */
	public void dispose() {	
		super.dispose();
		fLaunchManager.removeLaunchListener(this);
		fLaunchManager = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchesListener2#launchesTerminated(org.eclipse.debug.core.ILaunch[])
	 */
	public void launchesTerminated(ILaunch[] launches) {
		fireDelta(launches, IModelDelta.CONTENT);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchesListener#launchesRemoved(org.eclipse.debug.core.ILaunch[])
	 */
	public void launchesRemoved(ILaunch[] launches) {
		fireDelta(launches, IModelDelta.REMOVED);
		// clear the children cache
		for (int i = 0; i < launches.length; i++) {
			fPrevChildren.remove(launches[i]);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchesListener#launchesAdded(org.eclipse.debug.core.ILaunch[])
	 */
	public void launchesAdded(ILaunch[] launches) {
		fireDelta(launches, IModelDelta.ADDED | IModelDelta.EXPAND);
		// install model proxies
		for (int i = 0; i < launches.length; i++) {
			ILaunch launch = launches[i];
			fPrevChildren.put(launch, new HashSet());
		}
		installModelProxies(launches);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchesListener#launchesChanged(org.eclipse.debug.core.ILaunch[])
	 */
	public void launchesChanged(ILaunch[] launches) {
		fireDelta(launches, IModelDelta.STATE | IModelDelta.CONTENT);
		// install model proxies for new children
		installModelProxies(launches);	
	}
	
	/**
	 * Installs model proxies for any new children in the given launches.
	 * 
	 * @param launches
	 */
	protected void installModelProxies(ILaunch[] launches) {
		boolean changes = false;
		ILaunch[] allLaunches = fLaunchManager.getLaunches();
		ModelDelta root = new ModelDelta(fLaunchManager, 0, IModelDelta.NO_CHANGE, allLaunches.length);
		for (int i = 0; i < launches.length; i++) {
			ILaunch launch = launches[i];
			Object[] children = launch.getChildren();
			ModelDelta launchDelta = root.addNode(launch, indexOf(launch, allLaunches), IModelDelta.EXPAND | IModelDelta.NO_CHANGE, children.length);
			Set set = (Set) fPrevChildren.get(launch);
			for (int j = 0; j < children.length; j++) {
				Object child = children[j];
				if (set.add(child)) {
					changes = true;
					launchDelta.addNode(child, indexOf(child, children), IModelDelta.INSTALL, -1);
				}
			}
		}
		if (changes) {
			fireModelChanged(root);
		}
	}
	
	protected int indexOf(Object element, Object[] list) {
		for (int i = 0; i < list.length; i++) {
			if (element == list[i]) {
				return i;
			}
		}
		return -1;
	}
	
	protected void fireDelta(ILaunch[] launches, int launchFlags) {
		ModelDelta delta = new ModelDelta(fLaunchManager, IModelDelta.NO_CHANGE);
		for (int i = 0; i < launches.length; i++) {
			delta.addNode(launches[i], launchFlags);	
		}
		fireModelChanged(delta);		
	}

}
