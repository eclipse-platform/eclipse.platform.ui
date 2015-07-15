/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bugs 251884, 194734, 301774
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 *******************************************************************************/

package org.eclipse.core.databinding.observable.set;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import org.eclipse.core.databinding.observable.IDiff;

/**
 * Describes the difference between two sets
 *
 * @param <E>
 *            the type of elements in this diff
 * @since 1.0
 */
public abstract class SetDiff<E> implements IDiff {

	/**
	 * @return the set of added elements
	 */
	public abstract Set<E> getAdditions();

	/**
	 * @return the set of removed elements
	 */
	public abstract Set<E> getRemovals();

	/**
	 * Returns true if the diff has no added or removed elements.
	 *
	 * @return true if the diff has no added or removed elements.
	 * @since 1.2
	 */
	public boolean isEmpty() {
		return getAdditions().isEmpty() && getRemovals().isEmpty();
	}

	/**
	 * Applies the changes in this diff to the given set
	 *
	 * @param set
	 *            the set to which the diff will be applied
	 * @since 1.2
	 */
	public void applyTo(Set<E> set) {
		set.addAll(getAdditions());
		set.removeAll(getRemovals());
	}

	/**
	 * Returns a {@link Set} showing what <code>set</code> would look like if
	 * this diff were applied to it. The passed-in list is presumed to contain
	 * all elements in {@link #getRemovals()}, and none of the elements in
	 * {@link #getAdditions()}.
	 * <p>
	 * <b>Note</b>:the returned list is only guaranteed to be valid while the
	 * passed in set remains unchanged.
	 *
	 * @param set
	 *            the set over which the diff will be simulated
	 * @return a {@link Set} showing what <code>set</code> would look like if it
	 *         were passed to the {@link #applyTo(Set)} method.
	 * @since 1.3
	 */
	public Set<E> simulateOn(Set<E> set) {
		return new DeltaSet<>(set, this);
	}

	private static class DeltaSet<E> extends AbstractSet<E> {
		private Set<E> original;
		private final SetDiff<E> diff;

		public DeltaSet(Set<E> original, SetDiff<E> diff) {
			this.original = original;
			this.diff = diff;
		}

		@Override
		public Iterator<E> iterator() {
			return new Iterator<E>() {
				Iterator<E> orig = original.iterator();
				Iterator<E> add = diff.getAdditions().iterator();

				boolean haveNext = false;
				E next;

				@Override
				public boolean hasNext() {
					return findNext();
				}

				@Override
				public E next() {
					if (!findNext())
						throw new NoSuchElementException();
					E myNext = next;
					haveNext = false;
					next = null;
					return myNext;
				}

				private boolean findNext() {
					if (haveNext)
						return true;
					while (true) {
						E candidate;
						if (orig.hasNext())
							candidate = orig.next();
						else if (add.hasNext())
							candidate = add.next();
						else
							return false;

						if (diff.getRemovals().contains(candidate))
							continue;

						haveNext = true;
						next = candidate;
						return true;
					}
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}

		@Override
		public boolean contains(Object o) {
			return (original.contains(o) || diff.getAdditions().contains(o))
					&& !diff.getRemovals().contains(o);
		}

		@Override
		public int size() {
			return original.size() + diff.getAdditions().size()
					- diff.getRemovals().size();
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(getClass().getName())
				.append("{additions [") //$NON-NLS-1$
				.append(getAdditions() != null ? getAdditions().toString() : "null") //$NON-NLS-1$
				.append("], removals [") //$NON-NLS-1$
				.append(getRemovals() != null ? getRemovals().toString() : "null") //$NON-NLS-1$
				.append("]}"); //$NON-NLS-1$

		return buffer.toString();
	}
}
