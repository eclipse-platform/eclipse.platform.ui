/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
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
import org.eclipse.debug.internal.ui.viewers.AbstractModelProxy;
import org.eclipse.debug.internal.ui.viewers.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.IPresentationContext;

public class LaunchManagerProxy extends AbstractModelProxy implements ILaunchesListener2 {

	private ILaunchManager fLaunchManager;

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.AbstractModelProxy#init(org.eclipse.debug.internal.ui.viewers.IPresentationContext)
	 */
	public void init(IPresentationContext context) {
		super.init(context);
		fLaunchManager = DebugPlugin.getDefault().getLaunchManager();
		fLaunchManager.addLaunchListener(this);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.AbstractModelProxy#installed()
	 */
	public void installed() {
		// expand existing launches
		ILaunch[] launches = fLaunchManager.getLaunches();
		if (launches.length > 0) {
			fireDelta(launches, IModelDelta.EXPAND);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.viewers.AbstractModelProxy#dispose()
	 */
	public void dispose() {	
		super.dispose();
		fLaunchManager.removeLaunchListener(this);
		fLaunchManager = null;
	}

	public void launchesTerminated(ILaunch[] launches) {
		fireDelta(launches, IModelDelta.CHANGED | IModelDelta.CONTENT);
	}

	public void launchesRemoved(ILaunch[] launches) {
		fireDelta(launches, IModelDelta.REMOVED);
	}

	public void launchesAdded(ILaunch[] launches) {
		fireDelta(launches, IModelDelta.ADDED | IModelDelta.EXPAND);
	}

	public void launchesChanged(ILaunch[] launches) {
		fireDelta(launches, IModelDelta.CHANGED | IModelDelta.STATE | IModelDelta.CONTENT);
	}
	
	protected void fireDelta(ILaunch[] launches, int launchFlags) {
		ModelDelta delta = new ModelDelta(fLaunchManager, IModelDelta.NOCHANGE);
		for (int i = 0; i < launches.length; i++) {
			delta.addNode(launches[i], launchFlags);	
		}
		fireModelChanged(delta);		
	}

}
