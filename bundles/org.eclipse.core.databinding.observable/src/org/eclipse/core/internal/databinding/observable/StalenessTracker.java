/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bug 262269
 ******************************************************************************/

package org.eclipse.core.internal.databinding.observable;

import java.util.Map;

import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.StaleEvent;
import org.eclipse.core.internal.databinding.identity.IdentityMap;

/**
 * @since 1.0
 * 
 */
public class StalenessTracker {

	private Map staleMap = new IdentityMap();

	private int staleCount = 0;

	private final IStalenessConsumer stalenessConsumer;

	private class ChildListener implements IStaleListener, IChangeListener {
		public void handleStale(StaleEvent event) {
			processStalenessChange((IObservable) event.getSource(), true);
		}

		public void handleChange(ChangeEvent event) {
			processStalenessChange((IObservable) event.getSource(), true);
		}
	}

	private ChildListener childListener = new ChildListener();

	/**
	 * @param observables
	 * @param stalenessConsumer 
	 */
	public StalenessTracker(IObservable[] observables,
			IStalenessConsumer stalenessConsumer) {
		this.stalenessConsumer = stalenessConsumer;
		for (int i = 0; i < observables.length; i++) {
			IObservable observable = observables[i];
			doAddObservable(observable, false);
		}
		stalenessConsumer.setStale(staleCount > 0);
	}

	/**
	 * @param child
	 * @param callback
	 */
	public void processStalenessChange(IObservable child, boolean callback) {
		boolean oldStale = staleCount > 0;
		boolean oldChildStale = getOldChildStale(child);
		boolean newChildStale = child.isStale();
		if (oldChildStale != newChildStale) {
			if (oldChildStale) {
				staleCount--;
			} else {
				staleCount++;
			}
			staleMap.put(child, newChildStale ? Boolean.TRUE : Boolean.FALSE);
		}
		boolean newStale = staleCount > 0;
		if (callback && (newStale != oldStale)) {
			stalenessConsumer.setStale(newStale);
		}
	}

	/**
	 * @param child
	 */
	private boolean getOldChildStale(IObservable child) {
		Object oldChildValue = staleMap.get(child);
		boolean oldChildStale = oldChildValue == null ? false
				: ((Boolean) oldChildValue).booleanValue();
		return oldChildStale;
	}

	/**
	 * @param observable
	 */
	public void addObservable(IObservable observable) {
		doAddObservable(observable, true);
	}

	private void doAddObservable(IObservable observable, boolean callback) {
		processStalenessChange(observable, callback);
		observable.addChangeListener(childListener);
		observable.addStaleListener(childListener);
	}

	/**
	 * @param observable
	 */
	public void removeObservable(IObservable observable) {
		boolean oldStale = staleCount > 0;
		boolean oldChildStale = getOldChildStale(observable);
		if (oldChildStale) {
			staleCount--;
		}
		staleMap.remove(observable);
		observable.removeChangeListener(childListener);
		observable.removeStaleListener(childListener);
		boolean newStale = staleCount > 0;
		if (newStale != oldStale) {
			stalenessConsumer.setStale(newStale);
		}
	}

}
