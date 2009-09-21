/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers.update;

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
 * Model proxy for launch manager.
 */
public class LaunchManagerProxy extends AbstractModelProxy implements ILaunchesListener2 {

	private ILaunchManager fLaunchManager;

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.AbstractModelProxy#init(org.eclipse.debug.internal.ui.viewers.IPresentationContext)
	 */
	public synchronized void init(IPresentationContext context) {
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
		if (launches.length > 0) {
			launchesAdded(launches);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.AbstractModelProxy#dispose()
	 */
	public synchronized void dispose() {	
		super.dispose();
		if (fLaunchManager != null) {
			fLaunchManager.removeLaunchListener(this);
			fLaunchManager = null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchesListener2#launchesTerminated(org.eclipse.debug.core.ILaunch[])
	 */
	public void launchesTerminated(ILaunch[] launches) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchesListener#launchesRemoved(org.eclipse.debug.core.ILaunch[])
	 */
	public void launchesRemoved(ILaunch[] launches) {
		fireDelta(launches, IModelDelta.REMOVED);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchesListener#launchesAdded(org.eclipse.debug.core.ILaunch[])
	 */
	public void launchesAdded(ILaunch[] launches) {
		fireDelta(launches, IModelDelta.ADDED | IModelDelta.INSTALL);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.ILaunchesListener#launchesChanged(org.eclipse.debug.core.ILaunch[])
	 */
	public void launchesChanged(ILaunch[] launches) {	
	}
	
	/**
	 * Convenience method for firing a delta 
	 * @param launches the launches to set in the delta
	 * @param launchFlags the flags for the delta
	 */
	protected void fireDelta(ILaunch[] launches, int launchFlags) {
		ModelDelta delta = new ModelDelta(fLaunchManager, IModelDelta.NO_CHANGE);
		for (int i = 0; i < launches.length; i++) {
			delta.addNode(launches[i], launchFlags);	
		}
		fireModelChanged(delta);		
	}

}
