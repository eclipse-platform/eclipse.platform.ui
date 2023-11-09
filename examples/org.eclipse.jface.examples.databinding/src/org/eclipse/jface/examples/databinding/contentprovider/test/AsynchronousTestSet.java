/*******************************************************************************
 * Copyright (c) 2005, 2018 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.jface.examples.databinding.contentprovider.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.set.ObservableSet;
import org.eclipse.swt.widgets.Display;

/**
 * Test set that simulates asynchronously computed elements. The elements of the
 * set are randomly generated Integers. Whenever the "recompute" method is
 * called, the set will spin off a job that sleeps for a period of time and then
 * randomly adds and removes elements from the set.
 *
 * <p>
 * This simulates a set that wraps a database query or network communication.
 * These would follow the same pattern (report the set as "stale", perform some
 * slow operation, then make changes to the set).
 * </p>
 *
 * @since 1.0
 */
public class AsynchronousTestSet extends ObservableSet<Object> {

	private static Random randomNumberGenerator = new Random();

	private final Display display;

	private final boolean stale = false;

	/**
	 * Average number of elements to add or remove
	 */
	private static final int AVERAGE_DELTA = 4;

	/**
	 * Average "computation" time -- time taken to do the simulated work (ms)
	 */
	private static final int AVERAGE_BUSY_TIME = 1000;

	/**
	 * List of all undisposed AsynchronousTestSet instances. Used for the
	 * recomputeAll method.
	 */
	private static List<Object> allSets = new ArrayList<>();

	public AsynchronousTestSet() {
		super(new HashSet<>(), Object.class);
		display = Display.getCurrent();
		if (display == null) {
			throw new IllegalStateException(
					"This object can only be created in the UI thread"); //$NON-NLS-1$
		}
		recompute();
	}

	@Override
	protected void firstListenerAdded() {
		super.firstListenerAdded();
		allSets.add(this);
	}

	@Override
	protected void lastListenerRemoved() {
		allSets.remove(this);
		super.lastListenerRemoved();
	}

	public static void recomputeAll() {
		for (Object object : allSets) {
			AsynchronousTestSet next = (AsynchronousTestSet) object;

			next.recompute();
		}
	}

	public void remove(Collection<Object> toRemove) {
		Set<Object> removed = new HashSet<>();
		removed.addAll(toRemove);
		removed.retainAll(wrappedSet);

		wrappedSet.removeAll(removed);
		fireSetChange(Diffs.createSetDiff(Collections.emptySet(), removed));
	}

	@Override
	public boolean isStale() {
		return stale;
	}

	public void recompute() {
		if (!isStale()) {
			setStale(true);
			final int sleepTime = (int) (randomNumberGenerator.nextDouble() * (AVERAGE_BUSY_TIME * 2));
			Thread newThread = new Thread(() -> {

				// Simulate work by sleeping
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
				}

				// Add and remove some elements -- important: fire all
				// events in the UI thread
				display.asyncExec(() -> {
					final Set<Object> toAdd = new HashSet<>();
					final Set<Object> toRemove = new HashSet<>();

					// Compute elements to add and remove (basically
					// just fills the toAdd
					// and toRemove sets with random elements)
					int delta = (randomNumberGenerator.nextInt(AVERAGE_DELTA * 4) - AVERAGE_DELTA * 2);
					int extraAdds = randomNumberGenerator.nextInt(AVERAGE_DELTA);
					int addCount = delta + extraAdds;
					int removeCount = -delta + extraAdds;

					if (addCount > 0) {
						for (int i1 = 0; i1 < addCount; i1++) {
							toAdd.add(Integer.valueOf(randomNumberGenerator.nextInt(20)));
						}
					}

					if (removeCount > 0) {
						Iterator<Object> oldElements = wrappedSet.iterator();
						for (int i2 = 0; i2 < removeCount && oldElements.hasNext(); i2++) {
							toRemove.add(oldElements.next());
						}
					}

					toAdd.removeAll(wrappedSet);
					wrappedSet.addAll(toAdd);
					wrappedSet.removeAll(toRemove);

					setStale(false);
					fireSetChange(Diffs.createSetDiff(toAdd, toRemove));
				});
			});

			newThread.start();
		}
	}
}
