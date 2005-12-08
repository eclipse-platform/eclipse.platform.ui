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
package org.eclipse.debug.internal.ui.viewers;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.internal.ui.DebugUIPlugin;

public abstract class AbstractModelProxy implements IModelProxy {

	protected ListenerList fListeners = new ListenerList();

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

	public void fireModelChanged(final IModelDeltaNode delta) {
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
