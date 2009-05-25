/*******************************************************************************
 * Copyright (c) 2008, 2009 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.core.databinding.observable.StaleEvent;
import org.eclipse.core.databinding.observable.map.IMapChangeListener;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.map.MapChangeEvent;
import org.eclipse.core.databinding.observable.map.MapDiff;
import org.eclipse.core.databinding.observable.map.ObservableMap;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;

/**
 * @since 3.3
 * 
 */
public class ValidatedObservableMap extends ObservableMap {
	private IObservableMap target;
	private IObservableValue validationStatus;

	// Only true when out of sync with target due to validation status
	private boolean stale;

	// True when validation status changes from invalid to valid.
	private boolean computeNextDiff = false;

	private boolean updatingTarget = false;

	private IMapChangeListener targetChangeListener = new IMapChangeListener() {
		public void handleMapChange(MapChangeEvent event) {
			if (updatingTarget)
				return;
			IStatus status = (IStatus) validationStatus.getValue();
			if (isValid(status)) {
				if (stale) {
					// this.stale means we are out of sync with target,
					// so reset wrapped list to exactly mirror target
					stale = false;
					updateWrappedMap(new HashMap(target));
				} else {
					MapDiff diff = event.diff;
					if (computeNextDiff) {
						diff = Diffs.computeMapDiff(wrappedMap, target);
						computeNextDiff = false;
					}
					applyDiff(diff, wrappedMap);
					fireMapChange(diff);
				}
			} else {
				makeStale();
			}
		}
	};

	private IStaleListener targetStaleListener = new IStaleListener() {
		public void handleStale(StaleEvent staleEvent) {
			fireStale();
		}
	};

	private IValueChangeListener validationStatusChangeListener = new IValueChangeListener() {
		public void handleValueChange(ValueChangeEvent event) {
			IStatus oldStatus = (IStatus) event.diff.getOldValue();
			IStatus newStatus = (IStatus) event.diff.getNewValue();
			if (stale && !isValid(oldStatus) && isValid(newStatus)) {
				// this.stale means we are out of sync with target,
				// reset wrapped map to exactly mirror target
				stale = false;
				updateWrappedMap(new HashMap(target));

				// If the validation status becomes valid because of a change in
				// target observable
				computeNextDiff = true;
			}
		}
	};

	/**
	 * @param target
	 * @param validationStatus
	 */
	public ValidatedObservableMap(final IObservableMap target,
			final IObservableValue validationStatus) {
		super(target.getRealm(), new HashMap(target));
		Assert.isNotNull(validationStatus,
				"Validation status observable cannot be null"); //$NON-NLS-1$
		Assert
				.isTrue(target.getRealm().equals(validationStatus.getRealm()),
						"Target and validation status observables must be on the same realm"); //$NON-NLS-1$
		this.target = target;
		this.validationStatus = validationStatus;
		target.addMapChangeListener(targetChangeListener);
		target.addStaleListener(targetStaleListener);
		validationStatus.addValueChangeListener(validationStatusChangeListener);
	}

	private void updateWrappedMap(Map newMap) {
		Map oldMap = wrappedMap;
		MapDiff diff = Diffs.computeMapDiff(oldMap, newMap);
		wrappedMap = newMap;
		fireMapChange(diff);
	}

	private static boolean isValid(IStatus status) {
		return status.isOK() || status.matches(IStatus.INFO | IStatus.WARNING);
	}

	private void applyDiff(MapDiff diff, Map map) {
		for (Iterator iterator = diff.getRemovedKeys().iterator(); iterator
				.hasNext();)
			map.remove(iterator.next());
		for (Iterator iterator = diff.getChangedKeys().iterator(); iterator
				.hasNext();) {
			Object key = iterator.next();
			map.put(key, diff.getNewValue(key));
		}
		for (Iterator iterator = diff.getAddedKeys().iterator(); iterator
				.hasNext();) {
			Object key = iterator.next();
			map.put(key, diff.getNewValue(key));
		}
	}

	private void makeStale() {
		if (!stale) {
			stale = true;
			fireStale();
		}
	}

	private void updateTargetMap(MapDiff diff) {
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

	public boolean isStale() {
		getterCalled();
		return stale || target.isStale();
	}

	public void clear() {
		checkRealm();
		if (isEmpty())
			return;
		MapDiff diff = Diffs.computeMapDiff(wrappedMap, Collections.EMPTY_MAP);
		wrappedMap = new HashMap();
		updateTargetMap(diff);
		fireMapChange(diff);
	}

	public Object put(Object key, Object value) {
		checkRealm();
		MapDiff diff;
		Object oldValue;
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

	public void putAll(Map m) {
		checkRealm();
		Map map = new HashMap(wrappedMap);
		map.putAll(m);
		MapDiff diff = Diffs.computeMapDiff(wrappedMap, map);
		wrappedMap = map;
		updateTargetMap(diff);
		fireMapChange(diff);
	}

	public Object remove(Object key) {
		checkRealm();
		if (!wrappedMap.containsKey(key))
			return null;
		Object oldValue = wrappedMap.remove(key);
		MapDiff diff = Diffs.createMapDiffSingleRemove(key, oldValue);
		updateTargetMap(diff);
		fireMapChange(diff);
		return oldValue;
	}

	public Object getKeyType() {
		return target.getKeyType();
	}

	public Object getValueType() {
		return target.getValueType();
	}

	public synchronized void dispose() {
		target.removeMapChangeListener(targetChangeListener);
		target.removeStaleListener(targetStaleListener);
		validationStatus
				.removeValueChangeListener(validationStatusChangeListener);
		super.dispose();
	}
}
