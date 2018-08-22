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
 *     Matthew Hall - initial API and implementation (bug 218269)
 *     Matthew Hall - bug 226289
 ******************************************************************************/

package org.eclipse.core.internal.databinding.validation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.map.IMapChangeListener;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.map.MapDiff;
import org.eclipse.core.databinding.observable.map.ObservableMap;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;

/**
 * @param <K>
 *            The key type.
 * @param <V>
 *            The value type.
 * @since 3.3
 *
 */
public class ValidatedObservableMap<K, V> extends ObservableMap<K, V> {
	private IObservableMap<K, V> target;
	private IObservableValue<IStatus> validationStatus;

	// Only true when out of sync with target due to validation status
	private boolean stale;

	// True when validation status changes from invalid to valid.
	private boolean computeNextDiff = false;

	private boolean updatingTarget = false;

	private IMapChangeListener<K, V> targetChangeListener = event -> {
		if (updatingTarget)
			return;
		IStatus status = validationStatus.getValue();
		if (isValid(status)) {
			if (stale) {
				// this.stale means we are out of sync with target,
				// so reset wrapped list to exactly mirror target
				stale = false;
				updateWrappedMap(new HashMap<>(target));
			} else {
				MapDiff<? extends K, ? extends V> diff = event.diff;
				if (computeNextDiff) {
					diff = Diffs.computeMapDiff(wrappedMap, target);
					computeNextDiff = false;
				}
				applyDiff(diff, wrappedMap);
				fireMapChange(Diffs.unmodifiableDiff(diff));
			}
		} else {
			makeStale();
		}
	};

	private IStaleListener targetStaleListener = staleEvent -> fireStale();

	private IValueChangeListener<IStatus> validationStatusChangeListener = event -> {
		IStatus oldStatus = event.diff.getOldValue();
		IStatus newStatus = event.diff.getNewValue();
		if (stale && !isValid(oldStatus) && isValid(newStatus)) {
			// this.stale means we are out of sync with target,
			// reset wrapped map to exactly mirror target
			stale = false;
			updateWrappedMap(new HashMap<>(target));

			// If the validation status becomes valid because of a change in
			// target observable
			computeNextDiff = true;
		}
	};

	/**
	 * @param target
	 * @param validationStatus
	 */
	public ValidatedObservableMap(final IObservableMap<K, V> target, final IObservableValue<IStatus> validationStatus) {
		super(target.getRealm(), new HashMap<K, V>(target));
		Assert.isNotNull(validationStatus,
				"Validation status observable cannot be null"); //$NON-NLS-1$
		Assert.isTrue(target.getRealm().equals(validationStatus.getRealm()),
				"Target and validation status observables must be on the same realm"); //$NON-NLS-1$
		this.target = target;
		this.validationStatus = validationStatus;
		target.addMapChangeListener(targetChangeListener);
		target.addStaleListener(targetStaleListener);
		validationStatus.addValueChangeListener(validationStatusChangeListener);
	}

	private void updateWrappedMap(Map<K, V> newMap) {
		Map<K, V> oldMap = wrappedMap;
		MapDiff<K, V> diff = Diffs.computeMapDiff(oldMap, newMap);
		wrappedMap = newMap;
		fireMapChange(diff);
	}

	private static boolean isValid(IStatus status) {
		return status.isOK() || status.matches(IStatus.INFO | IStatus.WARNING);
	}

	private void applyDiff(MapDiff<? extends K, ? extends V> diff, Map<K, V> map) {
		for (Iterator<? extends K> iterator = diff.getRemovedKeys().iterator(); iterator.hasNext();)
			map.remove(iterator.next());
		for (Iterator<? extends K> iterator = diff.getChangedKeys().iterator(); iterator.hasNext();) {
			K key = iterator.next();
			map.put(key, diff.getNewValue(key));
		}
		for (Iterator<? extends K> iterator = diff.getAddedKeys().iterator(); iterator.hasNext();) {
			K key = iterator.next();
			map.put(key, diff.getNewValue(key));
		}
	}

	private void makeStale() {
		if (!stale) {
			stale = true;
			fireStale();
		}
	}

	private void updateTargetMap(MapDiff<K, V> diff) {
		updatingTarget = true;
		try {
			if (stale) {
				stale = false;
				applyDiff(Diffs.computeMapDiff(target, wrappedMap), target);
			} else {
				applyDiff(diff, target);
			}
		} finally {
			updatingTarget = false;
		}
	}

	@Override
	public boolean isStale() {
		getterCalled();
		return stale || target.isStale();
	}

	@Override
	public void clear() {
		checkRealm();
		if (isEmpty())
			return;
		MapDiff<K, V> diff = Diffs.computeMapDiff(wrappedMap, Collections.emptyMap());
		wrappedMap = new HashMap<>();
		updateTargetMap(diff);
		fireMapChange(diff);
	}

	@Override
	public V put(K key, V value) {
		checkRealm();
		MapDiff<K, V> diff;
		V oldValue;
		if (wrappedMap.containsKey(key)) {
			oldValue = wrappedMap.put(key, value);
			if (wrappedMap.containsKey(key)) { // Changed
				diff = Diffs.createMapDiffSingleChange(key, oldValue, value);
			} else { // Removed
				diff = Diffs.createMapDiffSingleRemove(key, oldValue);
			}
		} else { // Added
			oldValue = wrappedMap.put(key, value);
			diff = Diffs.createMapDiffSingleAdd(key, value);
		}
		updateTargetMap(diff);
		fireMapChange(diff);
		return oldValue;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		checkRealm();
		Map<K, V> map = new HashMap<>(wrappedMap);
		map.putAll(m);
		MapDiff<K, V> diff = Diffs.computeMapDiff(wrappedMap, map);
		wrappedMap = map;
		updateTargetMap(diff);
		fireMapChange(diff);
	}

	@Override
	public V remove(Object key) {
		checkRealm();
		if (!wrappedMap.containsKey(key))
			return null;

		V oldValue = wrappedMap.remove(key);
		@SuppressWarnings("unchecked")
		MapDiff<K, V> diff = Diffs.createMapDiffSingleRemove((K) key, oldValue);
		updateTargetMap(diff);
		fireMapChange(diff);
		return oldValue;
	}

	@Override
	public Object getKeyType() {
		return target.getKeyType();
	}

	@Override
	public Object getValueType() {
		return target.getValueType();
	}

	@Override
	public synchronized void dispose() {
		target.removeMapChangeListener(targetChangeListener);
		target.removeStaleListener(targetStaleListener);
		validationStatus.removeValueChangeListener(validationStatusChangeListener);
		super.dispose();
	}
}
