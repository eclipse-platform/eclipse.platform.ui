/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Boris Bokowski, IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.core.databinding.observable;

import org.eclipse.core.databinding.observable.value.AbstractObservableValue;

/**
 * @since 1.1
 * 
 */
class StalenessObservableValue extends AbstractObservableValue {

	private class MyListener implements IChangeListener, IStaleListener {
		public void handleChange(ChangeEvent event) {
			if (stale && !event.getObservable().isStale()) {
				stale = false;
				fireValueChange(Diffs.createValueDiff(Boolean.TRUE,
						Boolean.FALSE));
			}
		}

		public void handleStale(StaleEvent staleEvent) {
			if (!stale) {
				stale = true;
				fireValueChange(Diffs.createValueDiff(Boolean.FALSE,
						Boolean.TRUE));
			}
		}
	}

	private IObservable tracked;
	private boolean stale;
	private MyListener listener = new MyListener();

	StalenessObservableValue(IObservable observable) {
		this.tracked = observable;
		this.stale = observable.isStale();
		tracked.addChangeListener(listener);
		tracked.addStaleListener(listener);
	}

	protected Object doGetValue() {
		return tracked.isStale() ? Boolean.TRUE : Boolean.FALSE;
	}

	public Object getValueType() {
		return Boolean.TYPE;
	}

	public synchronized void dispose() {
		tracked.removeChangeListener(listener);
		tracked.removeStaleListener(listener);
		tracked = null;
		listener = null;
		super.dispose();
	}

}
