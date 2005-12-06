package org.eclipse.debug.internal.ui.viewers.update;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.debug.internal.ui.viewers.AbstractModelProxy;
import org.eclipse.debug.internal.ui.viewers.IModelDelta;
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
		for (int i = 0; i < launches.length; i++) {
			if (launches[i] == fLaunch) {
				ModelDelta delta = new ModelDelta();
				IModelDeltaNode node = delta.addNode(fLaunchManager, IModelDelta.NOCHANGE);
				node.addNode(fLaunch, IModelDelta.CHANGED | IModelDelta.CONTENT);
				fireModelChanged(delta);
				return;
			}
		}
	}

	public void launchesRemoved(ILaunch[] launches) {
	}

	public void launchesAdded(ILaunch[] launches) {
	}

	public void launchesChanged(ILaunch[] launches) {
		for (int i = 0; i < launches.length; i++) {
			if (launches[i] == fLaunch) {
				ModelDelta delta = new ModelDelta();
				IModelDeltaNode node = delta.addNode(fLaunchManager, IModelDelta.NOCHANGE);
				node.addNode(fLaunch, IModelDelta.CHANGED | IModelDelta.CONTENT);
				fireModelChanged(delta);
				return;
			}
		}		
	}

	public void setInitialState() {
		
	}

}
