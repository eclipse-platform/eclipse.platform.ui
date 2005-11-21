package org.eclipse.debug.internal.ui.viewers.update;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.debug.internal.ui.viewers.AbstractModelProxy;
import org.eclipse.debug.internal.ui.viewers.IModelDelta;
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
		ModelDelta delta = new ModelDelta();
		IModelDeltaNode node = delta.addNode(fLaunchManager, IModelDelta.NOCHANGE);
		for (int i = 0; i < launches.length; i++) {
			ILaunch launch = launches[i];
			node.addNode(launch, IModelDelta.CHANGED | IModelDelta.CONTENT);	
		}
		fireModelChanged(delta);
	}

	public void launchesRemoved(ILaunch[] launches) {
		ModelDelta delta = new ModelDelta();
		IModelDeltaNode node = delta.addNode(fLaunchManager, IModelDelta.NOCHANGE);
		for (int i = 0; i < launches.length; i++) {
			ILaunch launch = launches[i];
			node.addNode(launch, IModelDelta.REMOVED);	
		}
		fireModelChanged(delta);
	}

	public void launchesAdded(ILaunch[] launches) {
		ModelDelta delta = new ModelDelta();
		IModelDeltaNode node = delta.addNode(fLaunchManager, IModelDelta.NOCHANGE);
		for (int i = 0; i < launches.length; i++) {
			ILaunch launch = launches[i];
			node.addNode(launch, IModelDelta.ADDED | IModelDelta.EXPAND);
		}
		fireModelChanged(delta);
	}

	public void launchesChanged(ILaunch[] launches) {
		ModelDelta delta = new ModelDelta();
		IModelDeltaNode node = delta.addNode(fLaunchManager, IModelDelta.NOCHANGE);
		for (int i = 0; i < launches.length; i++) {
			ILaunch launch = launches[i];
			node.addNode(launch, IModelDelta.CHANGED | IModelDelta.STATE);	
		}
		fireModelChanged(delta);
	}

}
