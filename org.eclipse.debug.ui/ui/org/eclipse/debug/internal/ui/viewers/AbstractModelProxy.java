package org.eclipse.debug.internal.ui.viewers;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.internal.core.ListenerList;
import org.eclipse.debug.internal.ui.DebugUIPlugin;

public abstract class AbstractModelProxy implements IModelProxy {

	protected ListenerList fListeners = new ListenerList(1);

	protected Object[] getListeners() {
		synchronized (fListeners) {
			return fListeners.getListeners();
		}
	}

	public void addModelChangedListener(IModelChangedListener listener) {
		synchronized (fListeners) {
			fListeners.add(listener);
		}
	}

	public void removeModelChangedListener(IModelChangedListener listener) {
		synchronized (fListeners) {
			fListeners.remove(listener);
		}
	}

	public void fireModelChanged(final IModelDelta delta) {
		Object[] listeners = getListeners();
		for (int i = 0; i < listeners.length; i++) {
			final IModelChangedListener listener = (IModelChangedListener) listeners[i];
			ISafeRunnable safeRunnable = new ISafeRunnable() {
				public void handleException(Throwable exception) {
					DebugUIPlugin.log(exception);
				}

				public void run() throws Exception {
					listener.modelChanged(delta);
				}

			};
			Platform.run(safeRunnable);
		}
	}

}
