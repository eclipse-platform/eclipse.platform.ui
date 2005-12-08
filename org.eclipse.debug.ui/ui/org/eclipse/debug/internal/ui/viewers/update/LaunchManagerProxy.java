package org.eclipse.debug.internal.ui.viewers.update;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.debug.internal.ui.viewers.AbstractModelProxy;
import org.eclipse.debug.internal.ui.viewers.IModelDeltaNode;
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
		fireDelta(launches, IModelDeltaNode.CHANGED | IModelDeltaNode.CONTENT);
	}

	public void launchesRemoved(ILaunch[] launches) {
		fireDelta(launches, IModelDeltaNode.REMOVED);
	}

	public void launchesAdded(ILaunch[] launches) {
		fireDelta(launches, IModelDeltaNode.ADDED | IModelDeltaNode.EXPAND);
	}

	public void launchesChanged(ILaunch[] launches) {
		fireDelta(launches, IModelDeltaNode.CHANGED | IModelDeltaNode.STATE);
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
