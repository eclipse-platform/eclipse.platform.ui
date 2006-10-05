/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.databinding.observable.value;

import org.eclipse.jface.databinding.observable.IChangeListener;
import org.eclipse.jface.databinding.observable.IObservable;
import org.eclipse.jface.databinding.observable.IStaleListener;
import org.eclipse.jface.databinding.observable.ObservableTracker;

/**
 * A Lazily calculated value that automatically computes and registers listeners
 * on its dependencies as long as all of its dependencies are IObservable
 * objects
 * 
 * @since 1.0
 */
public abstract class ComputedValue extends AbstractObservableValue {

	private boolean dirty = true;

	private boolean stale = false;

	private Object cachedValue = null;

	/**
	 * Dependencies list. This is a collection that contains no duplicates. It
	 * is normally an ArrayList to conserve memory, but if it ever grows above a
	 * certain number of elements, a HashSet is substited to conserve runtime.
	 */
	private IObservable[] dependencies = new IObservable[0];

	/**
	 * 
	 */
	public ComputedValue() {
		this(Object.class);
	}

	/**
	 * @param valueType
	 */
	public ComputedValue(Object valueType) {
		this.valueType = valueType;
	}

	/**
	 * Inner class that implements interfaces that we don't want to expose as
	 * public API. Each interface could have been implemented using a separate
	 * anonymous class, but we combine them here to reduce the memory overhead
	 * and number of classes.
	 * 
	 * <p>
	 * The Runnable calls computeValue and stores the result in cachedValue.
	 * </p>
	 * 
	 * <p>
	 * The IUpdatableListener stores each updatable in the dependencies list.
	 * This is registered as the listener when calling ObservableTracker, to
	 * detect every updatable that is used by computeValue.
	 * </p>
	 * 
	 * <p>
	 * The IChangeListener is attached to every dependency.
	 * </p>
	 * 
	 */
	private class PrivateInterface implements Runnable, IChangeListener,
			IStaleListener {
		public void run() {
			cachedValue = calculate();
		}

		public void handleStale(IObservable source) {
			if (!dirty && !stale) {
				stale = true;
				fireStale();
			}
		}

		public void handleChange(IObservable source) {
			makeDirty();
		}
	}

	private PrivateInterface privateInterface = new PrivateInterface();

	private Object valueType;

	public final Object doGetValue() {
		if (dirty) {
			// This line will do the following:
			// - Run the computeValue method
			// - While doing so, add any updatable that is touched to the
			// dependencies list
			IObservable[] newDependencies = ObservableTracker.runAndMonitor(
					privateInterface, privateInterface, null);

			stale = false;
			for (int i = 0; i < newDependencies.length; i++) {
				IObservable observable = newDependencies[i];
				// Add a change listener to the new dependency.
				if (observable.isStale()) {
					stale = true;
				} else {
					observable.addStaleListener(privateInterface);
				}
			}

			dependencies = newDependencies;

			dirty = false;
		}

		return cachedValue;
	}

	/**
	 * Subclasses must override this method to provide the object's value.
	 * 
	 * @return the object's value
	 */
	protected abstract Object calculate();

	protected final void makeDirty() {
		if (!dirty) {
			dirty = true;

			// Stop listening for dependency changes.
			for (int i = 0; i < dependencies.length; i++) {
				IObservable observable = dependencies[i];

				observable.removeChangeListener(privateInterface);
				observable.removeStaleListener(privateInterface);
			}

			// copy the old value
			final Object oldValue = cachedValue;
			// Fire the "dirty" event. This implementation recomputes the new
			// value lazily.
			fireValueChange(new ValueDiff() {

				public Object getOldValue() {
					return oldValue;
				}

				public Object getNewValue() {
					return getValue();
				}
			});
		}
	}

	public boolean isStale() {
		// we need to recompute, otherwise staleness wouldn't mean anything
		getValue();
		return stale;
	}

	public Object getValueType() {
		return valueType;
	}

	public void addChangeListener(IChangeListener listener) {
		super.addChangeListener(listener);
		// If somebody is listening, we need to make sure we attach our own
		// listeners
		getValue();
	}

	public void addValueChangeListener(IValueChangeListener listener) {
		super.addValueChangeListener(listener);
		// If somebody is listening, we need to make sure we attach our own
		// listeners
		getValue();
	}

}
