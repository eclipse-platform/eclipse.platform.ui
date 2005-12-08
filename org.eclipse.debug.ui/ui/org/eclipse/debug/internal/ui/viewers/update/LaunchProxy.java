package org.eclipse.debug.internal.ui.viewers.update;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.debug.internal.ui.viewers.AbstractModelProxy;
import org.eclipse.debug.internal.ui.viewers.IModelDeltaNode;
import org.eclipse.debug.internal.ui.viewers.IPresentationContext;

public class LaunchProxy extends AbstractModelProxy implements ILaunchesListener2 {

	private ILaunchManager fLaunchManager = DebugPlugin.getDefault().getLaunchManager();
	private ILaunch fLaunch;

	LaunchProxy(ILaunch launch) {
		fLaunch = launch;
	}
	
	public void init(IPresentationContext context) {
		fLaunchManager.addLaunchListener(this);
	}

	public void dispose() {
		fLaunchManager.removeLaunchListener(this);
	}

	public void launchesTerminated(ILaunch[] launches) {
		fireDelta(launches, IModelDeltaNode.CHANGED | IModelDeltaNode.CONTENT);	
	}

	public void launchesRemoved(ILaunch[] launches) {
	}

	public void launchesAdded(ILaunch[] launches) {
	}

	public void launchesChanged(ILaunch[] launches) {
		fireDelta(launches, IModelDeltaNode.CHANGED | IModelDeltaNode.CONTENT);	
	}

	public void setInitialState() {
		
	}

	protected void fireDelta(ILaunch[] launches, int launchFlags) {
		ModelDeltaNode delta = new ModelDeltaNode(fLaunchManager, IModelDeltaNode.NOCHANGE);
		for (int i = 0; i < launches.length; i++) {
			delta.addNode(launches[i], launchFlags);	
		}
		fireModelChanged(delta);		
	}	
}
