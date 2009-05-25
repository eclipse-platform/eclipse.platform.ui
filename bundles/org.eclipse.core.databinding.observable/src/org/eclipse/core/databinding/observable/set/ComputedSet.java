/*******************************************************************************
 * Copyright (c) 2008, 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 237703)
 *     Matthew Hall - bug 274081
 *******************************************************************************/
package org.eclipse.core.databinding.observable.set;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.StaleEvent;
import org.eclipse.core.databinding.observable.value.IObservableValue;

/**
 * A lazily calculated set that automatically computes and registers listeners
 * on its dependencies as long as all of its dependencies are
 * {@link IObservable} objects. Any change to one of the observable dependencies
 * causes the set to be recomputed.
 * <p>
 * This class is thread safe. All state accessing methods must be invoked from
 * the {@link Realm#isCurrent() current realm}. Methods for adding and removing
 * listeners may be invoked from any thread.
 * </p>
 * <p>
 * Example: compute the set of all primes greater than 1 and less than the value
 * of an {@link IObservableValue} &lt; {@link Integer} &gt;.
 * </p>
 * 
 * <pre>
 * final IObservableValue max = WritableValue.withValueType(Integer.TYPE);
 * max.setValue(new Integer(0));
 * IObservableSet primes = new ComputedSet() {
 * 	protected Set calculate() {
 * 		int maxVal = ((Integer) max.getValue()).intValue();
 * 
 * 		Set result = new HashSet();
 * 		outer: for (int i = 2; i &lt; maxVal; i++) {
 * 			for (Iterator it = result.iterator(); it.hasNext();) {
 * 				Integer knownPrime = (Integer) it.next();
 * 				if (i % knownPrime.intValue() == 0)
 * 					continue outer;
 * 			}
 * 			result.add(new Integer(i));
 * 		}
 * 		return result;
 * 	}
 * };
 * 
 * System.out.println(primes); // =&gt; &quot;[]&quot;
 * 
 * max.setValue(new Integer(20));
 * System.out.println(primes); // =&gt; &quot;[2, 3, 5, 7, 11, 13, 17, 19]&quot;
 * </pre>
 * 
 * @since 1.2
 */
public abstract class ComputedSet extends AbstractObservableSet {
	private Set cachedSet = new HashSet();

	private boolean dirty = true;
	private boolean stale = false;

	private IObservable[] dependencies = new IObservable[0];

	/**
	 * Creates a computed set in the default realm and with an unknown (null)
	 * element type.
	 */
	public ComputedSet() {
		this(Realm.getDefault(), null);
	}

	/**
	 * Creates a computed set in the default realm and with the given element
	 * type.
	 * 
	 * @param elementType
	 *            the element type, may be <code>null</code> to indicate unknown
	 *            element type
	 */
	public ComputedSet(Object elementType) {
		this(Realm.getDefault(), elementType);
	}

	/**
	 * Creates a computed set in given realm and with an unknown (null) element
	 * type.
	 * 
	 * @param realm
	 *            the realm
	 * 
	 */
	public ComputedSet(Realm realm) {
		this(realm, null);
	}

	/**
	 * Creates a computed set in the given realm and with the given element
	 * type.
	 * 
	 * @param realm
	 *            the realm
	 * @param elementType
	 *            the element type, may be <code>null</code> to indicate unknown
	 *            element type
	 */
	public ComputedSet(Realm realm, Object elementType) {
		super(realm);
		this.elementType = elementType;
	}

	/**
	 * Inner class that implements interfaces that we don't want to expose as
	 * public API. Each interface could have been implemented using a separate
	 * anonymous class, but we combine them here to reduce the memory overhead
	 * and number of classes.
	 * 
	 * <p>
	 * The Runnable calls calculate and stores the result in cachedSet.
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
			cachedSet = calculate();
			if (cachedSet == null)
				cachedSet = Collections.EMPTY_SET;
		}

		public void handleStale(StaleEvent event) {
			if (!dirty)
				makeStale();
		}

		public void handleChange(ChangeEvent event) {
			makeDirty();
		}
	}

	private PrivateInterface privateInterface = new PrivateInterface();

	private Object elementType;

	protected int doGetSize() {
		return doGetSet().size();
	}

	private final Set getSet() {
		getterCalled();
		return doGetSet();
	}

	protected Set getWrappedSet() {
		return doGetSet();
	}

	final Set doGetSet() {
		if (dirty) {
			// This line will do the following:
			// - Run the calculate method
			// - While doing so, add any observable that is touched to the
			// dependencies list
			IObservable[] newDependencies = ObservableTracker.runAndMonitor(
					privateInterface, privateInterface, null);

			// If any dependencies are stale, a stale event will be fired here
			// even if we were already stale before recomputing. This is in case
			// clients assume that a set change is indicative of non-staleness.
			stale = false;
			for (int i = 0; i < newDependencies.length; i++) {
				if (newDependencies[i].isStale()) {
					makeStale();
					break;
				}
			}

			if (!stale) {
				for (int i = 0; i < newDependencies.length; i++) {
					newDependencies[i].addStaleListener(privateInterface);
				}
			}

			dependencies = newDependencies;

			dirty = false;
		}

		return cachedSet;
	}

	/**
	 * Subclasses must override this method to calculate the set contents. Any
	 * dependencies used to calculate the set must be {@link IObservable}, and
	 * implementers must use one of the interface methods tagged TrackedGetter
	 * for ComputedSet to recognize it as a dependency.
	 * 
	 * @return the object's set.
	 */
	protected abstract Set calculate();

	private void makeDirty() {
		if (!dirty) {
			dirty = true;

			makeStale();

			stopListening();

			// copy the old set
			final Set oldSet = new HashSet(cachedSet);
			// Fire the "dirty" event. This implementation recomputes the new
			// set lazily.
			fireSetChange(new SetDiff() {
				SetDiff delegate;

				private SetDiff getDelegate() {
					if (delegate == null)
						delegate = Diffs.computeSetDiff(oldSet, getSet());
					return delegate;
				}

				public Set getAdditions() {
					return getDelegate().getAdditions();
				}

				public Set getRemovals() {
					return getDelegate().getRemovals();
				}
			});
		}
	}

	private void stopListening() {
		if (dependencies != null) {
			for (int i = 0; i < dependencies.length; i++) {
				IObservable observable = dependencies[i];

				observable.removeChangeListener(privateInterface);
				observable.removeStaleListener(privateInterface);
			}
			dependencies = null;
		}
	}

	private void makeStale() {
		if (!stale) {
			stale = true;
			fireStale();
		}
	}

	public boolean isStale() {
		// recalculate set if dirty, to ensure staleness is correct.
		getSet();
		return stale;
	}

	public Object getElementType() {
		return elementType;
	}

	public synchronized void addChangeListener(IChangeListener listener) {
		super.addChangeListener(listener);
		// If somebody is listening, we need to make sure we attach our own
		// listeners
		computeSetForListeners();
	}

	public synchronized void addSetChangeListener(ISetChangeListener listener) {
		super.addSetChangeListener(listener);
		// If somebody is listening, we need to make sure we attach our own
		// listeners
		computeSetForListeners();
	}

	private void computeSetForListeners() {
		// Some clients just add a listener and expect to get notified even if
		// they never called getValue(), so we have to call getValue() ourselves
		// here to be sure. Need to be careful about realms though, this method
		// can be called outside of our realm.
		// See also bug 198211. If a client calls this outside of our realm,
		// they may receive change notifications before the runnable below has
		// been executed. It is their job to figure out what to do with those
		// notifications.
		getRealm().exec(new Runnable() {
			public void run() {
				if (dependencies == null) {
					// We are not currently listening.
					// But someone is listening for changes. Call getValue()
					// to make sure we start listening to the observables we
					// depend on.
					getSet();
				}
			}
		});
	}

	public synchronized void dispose() {
		stopListening();
		super.dispose();
	}
}
