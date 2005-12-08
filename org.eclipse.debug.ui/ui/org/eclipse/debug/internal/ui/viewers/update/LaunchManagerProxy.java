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

	public void init(IPresentationContext context) {
		fLaunchManager = DebugPlugin.getDefault().getLaunchManager();
		fLaunchManager.addLaunchListener(this);
	}

	public void dispose() {		
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

	public void setInitialState() {
		
	}
	
	protected void fireDelta(ILaunch[] launches, int launchFlags) {
		ModelDeltaNode delta = new ModelDeltaNode(fLaunchManager, IModelDelta.NOCHANGE);
		for (int i = 0; i < launches.length; i++) {
			delta.addNode(launches[i], launchFlags);	
		}
		fireModelChanged(delta);		
	}

}
