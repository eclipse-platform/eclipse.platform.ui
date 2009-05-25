/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bugs 116920, 147515
 *     Matthew Hall - bug 274081
 *******************************************************************************/
package org.eclipse.core.databinding.observable.value;

import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.StaleEvent;
import org.eclipse.core.databinding.observable.list.IObservableList;

/**
 * A Lazily calculated value that automatically computes and registers listeners
 * on its dependencies as long as all of its dependencies are
 * {@link IObservable} objects. Any change to one of the observable dependencies
 * causes the value to be recomputed.
 * <p>
 * This class is thread safe. All state accessing methods must be invoked from
 * the {@link Realm#isCurrent() current realm}. Methods for adding and removing
 * listeners may be invoked from any thread.
 * </p>
 * <p>
 * Example: compute the sum of all elements in an {@link IObservableList} &lt;
 * {@link Integer} &gt;.
 * </p>
 * 
 * <pre>
 * final IObservableList addends = WritableValue.withValueType(Integer.TYPE);
 * addends.add(new Integer(0));
 * addends.add(new Integer(1));
 * addends.add(new Integer(2));
 * 
 * IObservableValue sum = new ComputedValue() {
 * 	protected Object calculate() {
 * 		int sum = 0;
 * 		for (Iterator it = addends.iterator(); it.hasNext();) {
 * 			Integer addend = (Integer) it.next();
 * 			sum += addend.intValue();
 * 		}
 * 		return sum;
 * 	}
 * };
 * 
 * System.out.println(sum.getValue()); // =&gt; 3
 * 
 * addends.add(new Integer(10));
 * System.out.println(sum.getValue()); // =&gt; 13
 * </pre>
 * 
 * @since 1.0
 */
public abstract class ComputedValue extends AbstractObservableValue {

	private boolean dirty = true;

	private boolean stale = false;

	private Object cachedValue = null;

	/**
	 * Array of observables this computed value depends on. This field has a
	 * value of <code>null</code> if we are not currently listening.
	 */
	private IObservable[] dependencies = null;

	/**
	 * 
	 */
	public ComputedValue() {
		this(Realm.getDefault(), null);
	}

	/**
	 * @param valueType
	 *            can be <code>null</code>
	 */
	public ComputedValue(Object valueType) {
		this(Realm.getDefault(), valueType);
	}

	/**
	 * @param realm
	 * 
	 */
	public ComputedValue(Realm realm) {
		this(realm, null);
	}

	/**
	 * @param realm
	 * @param valueType
	 */
	public ComputedValue(Realm realm, Object valueType) {
		super(realm);
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
	 * The IChangeListener stores each observable in the dependencies list. This
	 * is registered as the listener when calling ObservableTracker, to detect
	 * every observable that is used by computeValue.
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

		public void handleStale(StaleEvent event) {
			if (!dirty && !stale) {
				stale = true;
				fireStale();
			}
		}

		public void handleChange(ChangeEvent event) {
			makeDirty();
		}
	}

	private PrivateInterface privateInterface = new PrivateInterface();

	private Object valueType;

	protected final Object doGetValue() {
		if (dirty) {
			// This line will do the following:
			// - Run the calculate method
			// - While doing so, add any observable that is touched to the
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
	 * Subclasses must override this method to provide the object's value. Any
	 * dependencies used to calculate the value must be {@link IObservable}, and
	 * implementers must use one of the interface methods tagged TrackedGetter
	 * for ComputedValue to recognize it as a dependency.
	 * 
	 * @return the object's value
	 */
	protected abstract Object calculate();

	protected final void makeDirty() {
		if (!dirty) {
			dirty = true;

			stopListening();

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

	/**
	 * 
	 */
	private void stopListening() {
		// Stop listening for dependency changes.
		if (dependencies != null) {
			for (int i = 0; i < dependencies.length; i++) {
				IObservable observable = dependencies[i];

				observable.removeChangeListener(privateInterface);
				observable.removeStaleListener(privateInterface);
			}
			dependencies = null;
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

	// this method exists here so that we can call it from the runnable below.
	/**
	 * @since 1.1
	 */
	protected boolean hasListeners() {
		return super.hasListeners();
	}

	public synchronized void addChangeListener(IChangeListener listener) {
		super.addChangeListener(listener);
		// If somebody is listening, we need to make sure we attach our own
		// listeners
		computeValueForListeners();
	}

	/**
	 * Some clients just add a listener and expect to get notified even if they
	 * never called getValue(), so we have to call getValue() ourselves here to
	 * be sure. Need to be careful about realms though, this method can be
	 * called outside of our realm. See also bug 198211. If a client calls this
	 * outside of our realm, they may receive change notifications before the
	 * runnable below has been executed. It is their job to figure out what to
	 * do with those notifications.
	 */
	private void computeValueForListeners() {
		getRealm().exec(new Runnable() {
			public void run() {
				if (dependencies == null) {
					// We are not currently listening.
					if (hasListeners()) {
						// But someone is listening for changes. Call getValue()
						// to make sure we start listening to the observables we
						// depend on.
						getValue();
					}
				}
			}
		});
	}

	public synchronized void addValueChangeListener(
			IValueChangeListener listener) {
		super.addValueChangeListener(listener);
		// If somebody is listening, we need to make sure we attach our own
		// listeners
		computeValueForListeners();
	}

	public synchronized void dispose() {
		super.dispose();
		stopListening();
	}

}
