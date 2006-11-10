/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.internal.databinding.observable;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.internal.databinding.IdentityWrapper;

/**
 * @since 1.0
 * 
 */
public class StalenessTracker {

	private Map staleMap = new HashMap();

	private int staleCount = 0;

	private final IStalenessConsumer stalenessConsumer;

	private class ChildListener implements IStaleListener, IChangeListener {
		public void handleStale(IObservable source) {
			processStalenessChange(source, true);
		}

		public void handleChange(IObservable source) {
			processStalenessChange(source, true);
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
		IdentityWrapper wrappedChild = new IdentityWrapper(child);
		boolean oldChildStale = getOldChildStale(wrappedChild);
		boolean newChildStale = child.isStale();
		if (oldChildStale != newChildStale) {
			if (oldChildStale) {
				staleCount--;
			} else {
				staleCount++;
			}
			staleMap.put(wrappedChild, newChildStale ? Boolean.TRUE : Boolean.FALSE);
		}
		boolean newStale = staleCount > 0;
		if (callback && (newStale != oldStale)) {
			stalenessConsumer.setStale(newStale);
		}
	}

	/**
	 * @param wrappedChild
	 */
	private boolean getOldChildStale(IdentityWrapper wrappedChild) {
		Object oldChildValue = staleMap.get(wrappedChild);
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
		IdentityWrapper wrappedChild = new IdentityWrapper(observable);
		boolean oldChildStale = getOldChildStale(wrappedChild);
		if (oldChildStale) {
			staleCount--;
		}
		staleMap.remove(wrappedChild);
		observable.removeChangeListener(childListener);
		observable.removeStaleListener(childListener);
		boolean newStale = staleCount > 0;
		if (newStale != oldStale) {
			stalenessConsumer.setStale(newStale);
		}
	}

}
