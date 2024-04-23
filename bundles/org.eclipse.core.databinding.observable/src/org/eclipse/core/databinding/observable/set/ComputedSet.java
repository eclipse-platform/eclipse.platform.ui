/*******************************************************************************
 * Copyright (c) 2008, 2017 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 237703)
 *     Matthew Hall - bug 274081
 *     Abel Hegedus - bug 414297
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 *******************************************************************************/
package org.eclipse.core.databinding.observable.set;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

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
 * max.setValue(Integer.valueOf(0));
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
 * 			result.add(Integer.valueOf(i));
 * 		}
 * 		return result;
 * 	}
 * };
 *
 * System.out.println(primes); // =&gt; &quot;[]&quot;
 *
 * max.setValue(Integer.valueOf(20));
 * System.out.println(primes); // =&gt; &quot;[2, 3, 5, 7, 11, 13, 17, 19]&quot;
 * </pre>
 *
 * @param <E>
 *            the type of the elements in this set
 *
 * @since 1.2
 */
public abstract class ComputedSet<E> extends AbstractObservableSet<E> {
	private Set<E> cachedSet = new HashSet<>();

	private boolean dirty = true;
	private boolean stale = false;

	private IObservable[] dependencies = new IObservable[0];

	/**
	 * Factory method to create {@link ComputedSet} objects in an easy manner.
	 * <p>
	 * The created list has a null {@link IObservableSet#getElementType}.
	 *
	 * @param supplier {@link Supplier}, which is tracked using
	 *                 {@link ObservableTracker} to find out observables it uses, in
	 *                 the same manner as {@link #calculate}.
	 * @return {@link ComputedSet} whose elements are computed using the given
	 *         {@link Supplier}.
	 * @since 1.12
	 */
	public static <E> IObservableSet<E> create(Supplier<Set<E>> supplier) {
		Objects.requireNonNull(supplier);
		return new ComputedSet<>() {
			@Override
			protected Set<E> calculate() {
				return supplier.get();
			}
		};
	}

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
	 */
	private class PrivateInterface implements Runnable, IChangeListener,
			IStaleListener {
		@Override
		public void run() {
			cachedSet = calculate();
			if (cachedSet == null)
				cachedSet = Collections.EMPTY_SET;
		}

		@Override
		public void handleStale(StaleEvent event) {
			if (!dirty)
				makeStale();
		}

		@Override
		public void handleChange(ChangeEvent event) {
			makeDirty();
		}
	}

	private PrivateInterface privateInterface = new PrivateInterface();

	private Object elementType;

	protected int doGetSize() {
		return doGetSet().size();
	}

	private final Set<E> getSet() {
		getterCalled();
		return doGetSet();
	}

	@Override
	protected Set<E> getWrappedSet() {
		return doGetSet();
	}

	final Set<E> doGetSet() {
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
			for (IObservable newDependency : newDependencies) {
				if (newDependency.isStale()) {
					makeStale();
					break;
				}
			}

			if (!stale) {
				for (IObservable newDependency : newDependencies) {
					newDependency.addStaleListener(privateInterface);
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
	protected abstract Set<E> calculate();

	private void makeDirty() {
		if (!dirty) {
			dirty = true;

			// copy the old set
			// bug 414297: moved before makeStale(), as cachedSet may be
			// overwritten
			// in makeStale() if a listener calls isStale()
			final Set<E> oldSet = new HashSet<>(cachedSet);
			makeStale();

			stopListening();

			// Fire the "dirty" event. This implementation recomputes the new
			// set lazily.
			fireSetChange(new SetDiff<E>() {
				SetDiff<E> delegate;

				private SetDiff<E> getDelegate() {
					if (delegate == null)
						delegate = Diffs.computeSetDiff(oldSet, getSet());
					return delegate;
				}

				@Override
				public Set<E> getAdditions() {
					return getDelegate().getAdditions();
				}

				@Override
				public Set<E> getRemovals() {
					return getDelegate().getRemovals();
				}
			});
		}
	}

	private void stopListening() {
		if (dependencies != null) {
			for (IObservable observable : dependencies) {
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

	@Override
	public boolean isStale() {
		// recalculate set if dirty, to ensure staleness is correct.
		getSet();
		return stale;
	}

	@Override
	public Object getElementType() {
		return elementType;
	}

	@Override
	public synchronized void addChangeListener(IChangeListener listener) {
		super.addChangeListener(listener);
		// If somebody is listening, we need to make sure we attach our own
		// listeners
		computeSetForListeners();
	}

	@Override
	public synchronized void addSetChangeListener(
			ISetChangeListener<? super E> listener) {
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
		getRealm().exec(() -> {
			if (dependencies == null) {
				// We are not currently listening.
				// But someone is listening for changes. Call getValue()
				// to make sure we start listening to the observables we
				// depend on.
				getSet();
			}
		});
	}

	@Override
	public synchronized void dispose() {
		stopListening();
		super.dispose();
	}
}
