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

import java.util.HashSet;
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

/**
 * Model proxy for launch object.
 * 
 * @since 3.3
 */
public class LaunchProxy extends AbstractModelProxy implements ILaunchesListener2 {

	private ILaunch fLaunch;
	
	/**
	 * Set of launch's previous children. When a child is added,
	 * its model proxy is installed.
	 */
	private Set fPrevChildren = new HashSet(); 

	/**
	 * Constructs a new model proxy for the given launch.
	 * 
	 * @param launch
	 */
	public LaunchProxy(ILaunch launch) {
		fLaunch = launch;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.AbstractModelProxy#init(org.eclipse.debug.internal.ui.viewers.IPresentationContext)
	 */
	public void init(IPresentationContext context) {
		super.init(context);
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.provisional.AbstractModelProxy#installed(org.eclipse.jface.viewers.Viewer)
	 */
	public void installed(Viewer viewer) {
		// install model proxies for existing children
		installModelProxies();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.AbstractModelProxy#dispose()
	 */
	public void dispose() {	
		super.dispose();
		DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
		fPrevChildren.clear();
		fLaunch = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchesListener2#launchesTerminated(org.eclipse.debug.core.ILaunch[])
	 */
	public void launchesTerminated(ILaunch[] launches) {
		for (int i = 0; i < launches.length; i++) {
			if (launches[i] == fLaunch) {
				fireDelta(IModelDelta.STATE | IModelDelta.CONTENT | IModelDelta.UNINSTALL);
			}
		}		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchesListener#launchesRemoved(org.eclipse.debug.core.ILaunch[])
	 */
	public void launchesRemoved(ILaunch[] launches) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchesListener#launchesAdded(org.eclipse.debug.core.ILaunch[])
	 */
	public void launchesAdded(ILaunch[] launches) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchesListener#launchesChanged(org.eclipse.debug.core.ILaunch[])
	 */
	public void launchesChanged(ILaunch[] launches) {
		for (int i = 0; i < launches.length; i++) {
			if (launches[i] == fLaunch) {
				fireDelta(IModelDelta.STATE | IModelDelta.CONTENT);
				installModelProxies();
			}
		}
	}
	
	/**
	 * Installs model proxies for any new children in the given launch.
	 * 
	 * @param launch
	 */
	protected void installModelProxies() {
		boolean changes = false;
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		ILaunch[] allLaunches = manager.getLaunches();
		ModelDelta root = new ModelDelta(manager, 0, IModelDelta.NO_CHANGE, allLaunches.length);
		Object[] children = fLaunch.getChildren();
		ModelDelta launchDelta = root.addNode(fLaunch, indexOf(fLaunch, allLaunches), IModelDelta.EXPAND, children.length);
		for (int j = 0; j < children.length; j++) {
			Object child = children[j];
			if (fPrevChildren.add(child)) {
				changes = true;
				launchDelta.addNode(child, indexOf(child, children), IModelDelta.INSTALL, -1);
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
	
	protected void fireDelta(int flags) {
		ModelDelta delta = new ModelDelta(DebugPlugin.getDefault().getLaunchManager(), IModelDelta.NO_CHANGE);
		delta.addNode(fLaunch, flags);	
		fireModelChanged(delta);		
	}

}
