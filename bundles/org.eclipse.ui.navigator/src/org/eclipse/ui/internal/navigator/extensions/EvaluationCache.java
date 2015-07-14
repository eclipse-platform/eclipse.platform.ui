/*******************************************************************************
 * Copyright (c) 2003, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Bug 349224 Navigator content provider "appearsBefore" creates hard reference to named id - paul.fullbright@oracle.com
 *     C. Sean Young <csyoung@google.com> - Bug 436645
 *******************************************************************************/
package org.eclipse.ui.internal.navigator.extensions;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.internal.navigator.VisibilityAssistant;
import org.eclipse.ui.internal.navigator.VisibilityAssistant.VisibilityListener;

/**
 * A cache for evaluated {@link NavigatorContentDescriptor}.
 */
public class EvaluationCache implements VisibilityListener {
	// TODO Have an LRU cache with max size as well as SoftReferences, to help
	// prevent pathological GC performance that can happen where there are a
	// large number of softly reachable objects, as well as helping to reduce
	// dependence on the GC from keeping this cache's size in line in the first
	// place.

	// TODO Counters for cache hits, misses, replacements, etc.

	// TODO Either the overrides and not overrides case should "share" parts of
	// their data structures (for example, this can be a map of key -> pair
	// instead of two maps) OR not bother tracking "overrides or not" state here
	// and instead let users of this class handle it with two instances of this
	// class.
	private final Map<EvaluationReference<Object>, EvaluationValueReference<NavigatorContentDescriptor[]>> evaluations = new HashMap<>();
	private final Map<EvaluationReference<Object>, EvaluationValueReference<NavigatorContentDescriptor[]>> evaluationsWithOverrides = new HashMap<>();

	private final ReferenceQueue<Object> evaluationsQueue = new ReferenceQueue<>();
	private final ReferenceQueue<Object> evaluationsWithOverridesQueue = new ReferenceQueue<>();

	/**
	 * @param anAssistant the VisisbilityAssistant to register with, must be non-null
	 */
	public EvaluationCache(VisibilityAssistant anAssistant) {
		anAssistant.addListener(this);
	}

	private void cleanUpStaleEntries() {
		// TODO Only clean up to a certain number of entries per call when merely accessing or setting?
		// TODO Periodic task to run this every now and then, ala org.eclipse.core.runtime.jobs.Job?
		// If this is done, will need to make this class thread safe.

		// Not thread safe, but this whole class isn't, so that is fine.
		Reference<?> r;
		// Reference#poll thankfully does not block if there is nothing available.
		while ((r = evaluationsQueue.poll()) != null) {
			processStaleEntry(r, evaluations);
		}
		while ((r = evaluationsWithOverridesQueue.poll()) != null) {
			processStaleEntry(r, evaluationsWithOverrides);
		}
	}

	private static void processStaleEntry(Reference<?> r,
			Map<? extends Reference<?>, ? extends Reference<?>> fromMap) {
		if (r instanceof EvaluationReference) {
			// Key has been collected; clear its entry.
			EvaluationValueReference<?> oldVal = (EvaluationValueReference<?>) fromMap.remove(r);
			if (oldVal != null) {
				// Clear the key from the value so we don't try to prematurely
				// remove any potential new mapping upon cleanUpStaleEntries()
				oldVal.clear();
			}
		}
		if (r instanceof EvaluationValueReference) {
			// If the value has been collected, get its key, and then remove that entry.
			EvaluationReference<?> key = ((EvaluationValueReference<?>) r).getKey();
			if (key != null) {
				fromMap.remove(key);
			}
		}
		// All other Reference types we just leave alone.
	}

	private static NavigatorContentDescriptor[] getDescriptorsFromMap(Object anElement,
			Map<EvaluationReference<Object>, EvaluationValueReference<NavigatorContentDescriptor[]>> map) {
		// Need to wrap in the reference type before querying, else it won't be found by HashMap.
		EvaluationReference<Object> key = new EvaluationReference<>(anElement);
		NavigatorContentDescriptor[] cachedDescriptors = null;
		Reference<NavigatorContentDescriptor[]> cache = map.get(key);
		if (cache != null && (cachedDescriptors = cache.get()) == null) {
			// There was an entry, but it has been collected; remove stale mapping.
			EvaluationValueReference<NavigatorContentDescriptor[]> value = map.remove(key);
			if (value != null) {
				// Clear the key from the value so we don't try to prematurely remove any potential new mapping upon cleanUpStaleEntries()
				value.clear();
			}
		}
		return cachedDescriptors;
	}

	/**
	 * Finds the cached descriptors for the given key, or returns {@code null}
	 * if not currently in the cache.
	 *
	 * @param anElement
	 *            the key to lookup
	 * @param toComputeOverrides
	 *            whether overrides are to be considered
	 * @return the cached descriptors for the given key, or {@code null} if not
	 *         currently in the cache
	 */
	public final NavigatorContentDescriptor[] getDescriptors(Object anElement, boolean toComputeOverrides) {
		cleanUpStaleEntries();
		if (anElement == null)
			return null;

		if (toComputeOverrides) {
			return getDescriptorsFromMap(anElement, evaluations);
		}
		return getDescriptorsFromMap(anElement, evaluationsWithOverrides);
	}

	private static void setDescriptorsInMap(Object anElement, NavigatorContentDescriptor[] theDescriptors,
			Map<EvaluationReference<Object>, EvaluationValueReference<NavigatorContentDescriptor[]>> map,
			ReferenceQueue<Object> queue) {
		// Ideally, we would use a WeakReference wrapper if the object given uses identity equality
		// (we can test if the class uses Object's equals or has its own override), and only use a SoftReference
		// if the object overrides equals, but that is a bit too unwieldy to check (it would require
		// checking reflective data) to be worth it.
		EvaluationReference<Object> key = new EvaluationReference<>(anElement, queue);
		EvaluationValueReference<NavigatorContentDescriptor[]> newValue =
				new EvaluationValueReference<>(theDescriptors, key, queue);
		EvaluationValueReference<NavigatorContentDescriptor[]> oldValue = map.put(key, newValue);
		if (oldValue != null) {
			// "Swap" the correct key instance when swapping the value, or else the above, temporary
			// lookup key will be collected too early (not the actual anElement, but the Reference object).
			// Not truly needed, but it will help the point of this field not go to waste.
			newValue.swapKey(oldValue);
			// Clear the key so we don't try to prematurely remove the new mapping upon cleanUpStaleEntries()
			oldValue.clear();
		}
	}

	/**
	 * Caches the given descriptors with the given key.
	 *
	 * @param anElement
	 *            the key to associate with the given descriptors
	 * @param theDescriptors
	 *            the descriptors to cache against the given key
	 * @param toComputeOverrides
	 *            whether overrides were considered in the computation of the
	 *            given descriptors
	 */
	public final void setDescriptors(Object anElement, NavigatorContentDescriptor[] theDescriptors,
			boolean toComputeOverrides) {
		cleanUpStaleEntries();
		if (anElement != null) {
			if (toComputeOverrides) {
				setDescriptorsInMap(anElement, theDescriptors, evaluations, evaluationsQueue);
			} else {
				setDescriptorsInMap(anElement, theDescriptors, evaluationsWithOverrides, evaluationsWithOverridesQueue);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * For an EvaluationCache, this means invalidating all cached descriptors.
	 */
	@Override
	public void onVisibilityOrActivationChange() {
		// Dump everything in the reference queues.
		// Don't bother removing from the map based on references, we are about to clear everything anyways.
		// This might lead to some premature removals because yet to be collected values are not clearing
		// their key reference, but that is worth having a fast clearing of the maps.
		// Thankfully, this should be rare, as when reference objects themselves are GCed before being added
		// to a reference queue, they don't get added at all.
		while (evaluationsQueue.poll() != null) {
			// No need to do anything with the reference, we just need to drain
			// the queue.
		}
		while (evaluationsWithOverridesQueue.poll() != null) {
			// No need to do anything with the reference, we just need to drain
			// the queue.
		}
		evaluations.clear();
		evaluationsWithOverrides.clear();
	}
}