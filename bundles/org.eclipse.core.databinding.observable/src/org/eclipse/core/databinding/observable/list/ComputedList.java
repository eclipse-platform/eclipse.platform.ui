/************************************************************************************************************
 * Copyright (c) 2007, 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Matthew Hall - initial API and implementation
 * 		IBM Corporation - initial API and implementation
 * 		Brad Reynolds - initial API and implementation (through bug 116920 and bug 147515)
 * 		Matthew Hall - bugs 211786, 274081
 ***********************************************************************************************************/
package org.eclipse.core.databinding.observable.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
 * A lazily calculated list that automatically computes and registers listeners
 * on its dependencies as long as all of its dependencies are
 * {@link IObservable} objects. Any change to one of the observable dependencies
 * causes the list to be recomputed.
 * <p>
 * This class is thread safe. All state accessing methods must be invoked from
 * the {@link Realm#isCurrent() current realm}. Methods for adding and removing
 * listeners may be invoked from any thread.
 * </p>
 * <p>
 * Example: compute the fibonacci sequence, up to as many elements as the value
 * of an {@link IObservableValue} &lt; {@link Integer} &gt;.
 * </p>
 * 
 * <pre>
 * final IObservableValue count = WritableValue.withValueType(Integer.TYPE);
 * count.setValue(new Integer(0));
 * IObservableList fibonacci = new ComputedList() {
 * 	protected List calculate() {
 * 		int size = ((Integer) count.getValue()).intValue();
 * 
 * 		List result = new ArrayList();
 * 		for (int i = 0; i &lt; size; i++) {
 * 			if (i == 0)
 * 				result.add(new Integer(0));
 * 			else if (i == 1)
 * 				result.add(new Integer(1));
 * 			else {
 * 				Integer left = (Integer) result.get(i - 2);
 * 				Integer right = (Integer) result.get(i - 1);
 * 				result.add(new Integer(left.intValue() + right.intValue()));
 * 			}
 * 		}
 * 		return result;
 * 	}
 * };
 * 
 * System.out.println(fibonacci); // =&gt; &quot;[]&quot;
 * 
 * count.setValue(new Integer(5));
 * System.out.println(fibonacci); // =&gt; &quot;[0, 1, 1, 2, 3]&quot;
 * </pre>
 * 
 * @since 1.1
 */
public abstract class ComputedList extends AbstractObservableList {
	private List cachedList = new ArrayList();

	private boolean dirty = true;
	private boolean stale = false;

	private IObservable[] dependencies = new IObservable[0];

	/**
	 * Creates a computed list in the default realm and with an unknown (null)
	 * element type.
	 */
	public ComputedList() {
		this(Realm.getDefault(), null);
	}

	/**
	 * Creates a computed list in the default realm and with the given element
	 * type.
	 * 
	 * @param elementType
	 *            the element type, may be <code>null</code> to indicate unknown
	 *            element type
	 */
	public ComputedList(Object elementType) {
		this(Realm.getDefault(), elementType);
	}

	/**
	 * Creates a computed list in given realm and with an unknown (null) element
	 * type.
	 * 
	 * @param realm
	 *            the realm
	 * 
	 */
	public ComputedList(Realm realm) {
		this(realm, null);
	}

	/**
	 * Creates a computed list in the given realm and with the given element
	 * type.
	 * 
	 * @param realm
	 *            the realm
	 * @param elementType
	 *            the element type, may be <code>null</code> to indicate unknown
	 *            element type
	 */
	public ComputedList(Realm realm, Object elementType) {
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
	 * The Runnable calls calculate and stores the result in cachedList.
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
			cachedList = calculate();
			if (cachedList == null)
				cachedList = Collections.EMPTY_LIST;
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
		return doGetList().size();
	}

	public Object get(int index) {
		getterCalled();
		return doGetList().get(index);
	}

	private final List getList() {
		getterCalled();
		return doGetList();
	}

	final List doGetList() {
		if (dirty) {
			// This line will do the following:
			// - Run the calculate method
			// - While doing so, add any observable that is touched to the
			// dependencies list
			IObservable[] newDependencies = ObservableTracker.runAndMonitor(
					privateInterface, privateInterface, null);

			// If any dependencies are stale, a stale event will be fired here
			// even if we were already stale before recomputing. This is in case
			// clients assume that a list change is indicative of non-staleness.
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

		return cachedList;
	}

	private void getterCalled() {
		ObservableTracker.getterCalled(this);
	}

	/**
	 * Subclasses must override this method to calculate the list contents. Any
	 * dependencies used to calculate the list must be {@link IObservable}, and
	 * implementers must use one of the interface methods tagged TrackedGetter
	 * for ComputedList to recognize it as a dependency.
	 * 
	 * @return the object's list.
	 */
	protected abstract List calculate();

	private void makeDirty() {
		if (!dirty) {
			dirty = true;

			makeStale();

			stopListening();

			// copy the old list
			final List oldList = new ArrayList(cachedList);
			// Fire the "dirty" event. This implementation recomputes the new
			// list lazily.
			fireListChange(new ListDiff() {
				ListDiffEntry[] differences;

				public ListDiffEntry[] getDifferences() {
					if (differences == null)
						differences = Diffs.computeListDiff(oldList, getList())
								.getDifferences();
					return differences;
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
		// recalculate list if dirty, to ensure staleness is correct.
		getList();
		return stale;
	}

	public Object getElementType() {
		return elementType;
	}

	public synchronized void addChangeListener(IChangeListener listener) {
		super.addChangeListener(listener);
		// If somebody is listening, we need to make sure we attach our own
		// listeners
		computeListForListeners();
	}

	public synchronized void addListChangeListener(IListChangeListener listener) {
		super.addListChangeListener(listener);
		// If somebody is listening, we need to make sure we attach our own
		// listeners
		computeListForListeners();
	}

	private void computeListForListeners() {
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
					getList();
				}
			}
		});
	}

	public synchronized void dispose() {
		stopListening();
		super.dispose();
	}
}
