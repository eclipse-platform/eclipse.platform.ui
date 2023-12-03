/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
 */
public class StalenessTracker {

	private Map<IObservable, Boolean> staleMap = new IdentityMap<>();

	private int staleCount = 0;

	private final IStalenessConsumer stalenessConsumer;

	private class ChildListener implements IStaleListener, IChangeListener {
		@Override
		public void handleChange(ChangeEvent event) {
			processStalenessChange((IObservable) event.getSource(), true);
		}

		@Override
		public void handleStale(StaleEvent event) {
			processStalenessChange((IObservable) event.getSource(), true);
		}
	}

	private ChildListener childListener = new ChildListener();

	public StalenessTracker(IObservable[] observables,
			IStalenessConsumer stalenessConsumer) {
		this.stalenessConsumer = stalenessConsumer;
		for (IObservable observable : observables) {
			doAddObservable(observable, false);
		}
		stalenessConsumer.setStale(staleCount > 0);
	}

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

	private boolean getOldChildStale(IObservable child) {
		Boolean oldChildValue = staleMap.get(child);
		return oldChildValue != null && oldChildValue;
	}

	public void addObservable(IObservable observable) {
		doAddObservable(observable, true);
	}

	private void doAddObservable(IObservable observable, boolean callback) {
		processStalenessChange(observable, callback);
		observable.addChangeListener(childListener);
		observable.addStaleListener(childListener);
	}

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
