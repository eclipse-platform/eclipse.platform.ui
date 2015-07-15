/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bugs 208858, 251884, 194734, 272651, 301774
 *     Stefan Xenos <sxenos@gmail.com> - Bug 335792
 *******************************************************************************/

package org.eclipse.core.databinding.observable.list;

import java.util.AbstractList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.databinding.observable.IDiff;
import org.eclipse.core.internal.databinding.observable.Util;

/**
 * Object describing a diff between two lists.
 *
 * @param <E>
 *            the type of the elements in this diff
 *
 * @since 1.0
 */
public abstract class ListDiff<E> implements IDiff {

	/**
	 * Returns a ListDiffEntry array representing the differences in the list,
	 * in the order they are to be processed.
	 * <P>
	 * This method returns identical results to
	 * <code>getDifferencesAsList</code> except that the results are returned as
	 * a array and so the ListDiffEntry objects are not properly typed. This
	 * form is here for compatibility. Use <code>getDifferencesAsList</code> to
	 * get typed results.
	 *
	 * @return a ListDiffEntry array representing the differences in the list,
	 *         in the order they are to be processed.
	 */
	public abstract ListDiffEntry<E>[] getDifferences();

	/**
	 * Traverses the {@link #getDifferences()} array, calling the appropriate
	 * method in <code>visitor</code> for each difference.
	 * <ol>
	 * <li>{@link ListDiffVisitor#handleReplace(int, Object, Object)} is called
	 * whenever an add entry is adjacent to a remove entry, and both entries
	 * operate on the same location in the list.
	 * <li>{@link ListDiffVisitor#handleMove(int, int, Object)} is called
	 * whenever an add entry is adjacent to a remove entry, and both entries
	 * have equivalent elements.
	 * <li>{@link ListDiffVisitor#handleRemove(int, Object)} is called whenever
	 * a remove entry does not match conditions 1 or 2.
	 * <li>{@link ListDiffVisitor#handleAdd(int, Object)} is called whenever an
	 * add entry does not match conditions in 1 or 2.
	 * </ol>
	 *
	 * @param visitor
	 *            the visitor to receive callbacks.
	 * @see ListDiffVisitor
	 * @since 1.1
	 */
	public void accept(ListDiffVisitor<E> visitor) {
		ListDiffEntry<E>[] differences = getDifferences();
		for (int i = 0; i < differences.length; i++) {
			ListDiffEntry<E> entry = differences[i];
			E elem = entry.getElement();
			int pos = entry.getPosition();
			boolean add = entry.isAddition();

			if (i + 1 < differences.length) {
				ListDiffEntry<E> nextEntry = differences[i + 1];
				if (add != nextEntry.isAddition()) {
					int addPos;
					E addElem;

					int removePos;
					E removeElem;

					if (add) {
						addPos = pos;
						addElem = elem;

						removePos = nextEntry.getPosition();
						removeElem = nextEntry.getElement();

						if (addPos > removePos) {
							// a b c d e f -- start
							// a b c b d e f -- add b at 4
							// a c b d e f -- remove b at 2

							// net effect is the same as:
							// a b c d e f -- start
							// a c d e f -- remove b at 2
							// a c b d e f -- add b at 3

							addPos--;
						} else if (removePos > addPos) {
							// a b c d e f -- start
							// a d b c d e f -- add d at 2
							// a d b c e f -- remove d at 5

							// net effect is the same as
							// a b c d e f -- start
							// a b c e f -- remove d at 4
							// a d b c d e f -- add d at 2

							// So we adjust the remove index to fit the indices
							// of the remove-then-add scenario
							removePos--;
						} else {
							// rare case: element is added and then immediately
							// removed. Handle the add entry and then continue
							// iterating starting at the remove entry
							visitor.handleAdd(pos, elem);
							continue;
						}
					} else {
						removePos = pos;
						removeElem = elem;

						addPos = nextEntry.getPosition();
						addElem = nextEntry.getElement();
					}

					if (removePos == addPos) {
						visitor.handleReplace(pos, removeElem, addElem);
						i++;
						continue;
					}

					if (Util.equals(removeElem, addElem)) {
						visitor.handleMove(removePos, addPos, elem);
						i++;
						continue;
					}
				}
			}

			if (add)
				visitor.handleAdd(pos, elem);
			else
				visitor.handleRemove(pos, elem);
		}
	}

	/**
	 * Returns true if the diff contains no added, removed, moved or replaced
	 * elements.
	 *
	 * @return true if the diff contains no added, removed, moved or replaced
	 *         elements.
	 * @since 1.2
	 */
	public boolean isEmpty() {
		return getDifferences().length == 0;
	}

	/**
	 * Applies the changes in this diff to the given list
	 *
	 * @param list
	 *            the list to which the diff will be applied
	 * @since 1.2
	 */
	public void applyTo(final List<E> list) {
		accept(new ListDiffVisitor<E>() {
			@Override
			public void handleAdd(int index, E element) {
				list.add(index, element);
			}

			@Override
			public void handleRemove(int index, E element) {
				list.remove(index);
			}

			@Override
			public void handleReplace(int index, E oldElement, E newElement) {
				list.set(index, newElement);
			}
		});
	}

	/**
	 * Returns a list showing what <code>list</code> would look like if this
	 * diff were applied to it.
	 * <p>
	 * <b>Note</b>: the returned list is only valid until structural changes are
	 * made to the passed-in list.
	 *
	 * @param list
	 *            the list over which the diff will be simulated
	 * @return an unmodifiable list showing what <code>list</code> would look
	 *         like if it were passed to the {@link #applyTo(List)} method.
	 * @see #applyTo(List)
	 * @since 1.3
	 */
	public List<E> simulateOn(List<E> list) {
		class ResultReference {
			List<E> value;
		}
		final ResultReference result = new ResultReference();
		result.value = list;
		accept(new ListDiffVisitor<E>() {
			@Override
			public void handleAdd(int index, E element) {
				List<E> first = result.value.subList(0, index);
				List<E> middle = Collections.singletonList(element);
				List<E> last = result.value.subList(index, result.value.size());
				result.value = ConcatList.cat(first, middle, last);
			}

			@Override
			public void handleRemove(int index, E element) {
				List<E> first = result.value.subList(0, index);
				List<E> last = result.value.subList(index + 1, result.value.size());
				result.value = ConcatList.cat(first, last);
			}

			@Override
			public void handleReplace(int index, E oldElement,
					E newElement) {
				List<E> first = result.value.subList(0, index);
				List<E> middle = Collections.singletonList(newElement);
				List<E> last = result.value.subList(index + 1, result.value.size());
				result.value = ConcatList.cat(first, middle, last);
			}
		});
		return result.value;
	}

	private static class ConcatList<E> extends AbstractList<E> {
		private final List<E> firstSublist;
		private final List<E> middleSublist;
		private final List<E> lastSublist;

		public static <T> List<T> cat(List<T> a, List<T> b, List<T> c) {
			if (a.isEmpty()) {
				return cat(b, c);
			} else if (b.isEmpty()) {
				return cat(a, c);
			} else if (c.isEmpty()) {
				return cat(a, b);
			}
			return new ConcatList<T>(a, b, c);
		}

		public static <T> List<T> cat(List<T> a, List<T> b) {
			if (a.isEmpty()) {
				if (b.isEmpty()) {
					return Collections.emptyList();
				}
				return b;
			} else if (b.isEmpty()) {
				return a;
			}
			return new ConcatList<T>(a, b, Collections.<T> emptyList());
		}

		private ConcatList(List<E> firstSublist, List<E> middleSublist, List<E> lastSublist) {
			this.firstSublist = firstSublist;
			this.middleSublist = middleSublist;
			this.lastSublist = lastSublist;
		}

		@Override
		public E get(int index) {
			int subListIndex = index;
			if (subListIndex < firstSublist.size()) {
				return firstSublist.get(subListIndex);
			}
			subListIndex -= firstSublist.size();
			if (subListIndex < middleSublist.size()) {
				return middleSublist.get(subListIndex);
			}
			subListIndex -= middleSublist.size();
			if (subListIndex < lastSublist.size()) {
				return lastSublist.get(subListIndex);
			}

			throw new IndexOutOfBoundsException();
		}

		@Override
		public int size() {
			return firstSublist.size() + middleSublist.size() + lastSublist.size();
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		ListDiffEntry<E>[] differences = getDifferences();
		StringBuffer buffer = new StringBuffer();
		buffer.append(getClass().getName());

		if (differences == null || differences.length == 0) {
			buffer.append("{}"); //$NON-NLS-1$
		} else {
			buffer.append("{"); //$NON-NLS-1$

			for (int i = 0; i < differences.length; i++) {
				if (i > 0)
					buffer.append(", "); //$NON-NLS-1$

				buffer.append("difference[") //$NON-NLS-1$
						.append(i)
						.append("] [") //$NON-NLS-1$
						.append(differences[i] != null ? differences[i]
								.toString() : "null") //$NON-NLS-1$
						.append("]"); //$NON-NLS-1$
			}
			buffer.append("}"); //$NON-NLS-1$
		}

		return buffer.toString();
	}
}
