/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.ui.internal.services;

import java.util.Comparator;
import java.util.Objects;
import org.eclipse.ui.internal.util.Util;

/**
 * <p>
 * Compares two evaluation result caches ({@link IEvaluationResultCache}). The
 * cache with the lowest source priority is considered "greater". In the event
 * of a tie, other characteristics are checked.
 * </p>
 * <p>
 * This class is only intended for use within the
 * <code>org.eclipse.ui.workbench</code> plug-in.
 * </p>
 *
 * @since 3.2
 *
 */
public final class EvaluationResultCacheComparator implements Comparator {

	@Override
	public int compare(final Object object1, final Object object2) {
		if (Objects.equals(object2, object1)) {
			return 0;
		}

		final IEvaluationResultCache cache1 = (IEvaluationResultCache) object1;
		final IEvaluationResultCache cache2 = (IEvaluationResultCache) object2;
		int comparison;

		/*
		 * Note: all of the comparisons are flipped intentionally. This allows those
		 * items with greater values to appear earlier when using an iterator.
		 */
		// if objects went to the trouble to implement Comparable
		// we should use it ... this algorithm can accept a natural ordering
		// that's not compatible with equals.
		if (object1 instanceof Comparable && object2 instanceof Comparable) {
			comparison = Util.compare((Comparable) object2, (Comparable) object1);
			if (comparison != 0) {
				return comparison;
			}
		}

		return Util.compareIdentity(cache2, cache1);
	}
}
