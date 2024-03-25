/*******************************************************************************
* Copyright (c) 2020 Ari Kast and others.
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Ari Kast - initial API and implementation
*******************************************************************************/

package org.eclipse.ui.internal.texteditor;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Ari Kast
 *
 *         Tracks history in order of insertion. It can operate either as a ring
 *         or as a line: in ring mode, if history size is N, then calling
 *         goBackward N times brings you full circle back to your current
 *         location in linear mode, if history size is N, then calling
 *         goBackward N times brings you to the beginning, after which
 *         additional calls to goBackward will have no effect until either
 *         goForward is called or a new entry is added. Both linear and ring
 *         mode overwrite history as needed when buffer is full
 * @param <T> the type of the object instances being tracked in history
 *
 * @since 3.15
 */
public class HistoryTracker<T> {
	// the actual historical data
	T[] fHistory;

	// function to determine whether history elements can be merged
	CandidateEvaluator<T> fEvaluator;

	// pointer to current location in history queue
	Navigator<T> fBrowsePoint;

	// pointer to most recent insertion to history queue. New insertions always
	// go to index fMostRecent + 1
	Navigator<T> fInsertionPoint;

	// the size of the dataset contained in history queue
	// grows til it reaches history.length, and generally wont
	// shrink
	int fSize;

	// controls whether navigation wraps around in circular fashion
	// or if it is purely linear
	boolean fUseCircularNavigation;

	/**
	 * @param historySize           the buffer size. additional insertions will
	 *                              overwrite oldest insertions
	 * @param clazz                 the class type of the objects being tracked
	 * @param evaluator             The expression which compares incoming elements
	 *                              against existing elements. If this expression
	 *                              yields true, then the element(s) for which it
	 *                              was true is/are replaced
	 * @param useCircularNavigation when true the history operates in ring mode,
	 *                              otherwise it is linear
	 */
	@SuppressWarnings("unchecked")
	public HistoryTracker(int historySize, Class<T> clazz, CandidateEvaluator<T> evaluator,
			boolean useCircularNavigation) {
		historySize = Math.max(historySize, 1); // size < 1 makes no sense, so
												// enforce at least size 1
		fHistory = (T[]) Array.newInstance(clazz, historySize);
		this.fEvaluator = evaluator;
		this.fUseCircularNavigation = useCircularNavigation;
		fBrowsePoint = new Navigator<>(this);
		fInsertionPoint = new Navigator<>(this);
	}

	public T browseBackward() {
		if (canGoBackward()) {
			fBrowsePoint.decr();
		}
		return getCurrentBrowsePoint();
	}

	public T browseForward() {
		if (canGoForward()) {
			fBrowsePoint.incr();
		}
		return getCurrentBrowsePoint();
	}

	private boolean canGoBackward() {
		return fSize > 0 && (fUseCircularNavigation || fBrowsePoint.getPriorIndex() != fInsertionPoint.getIndex());
	}

	private boolean canGoForward() {
		return fSize > 0 && (fUseCircularNavigation || fBrowsePoint.getIndex() != fInsertionPoint.getIndex());
	}

	public T getCurrentBrowsePoint() {
		return fBrowsePoint.currentItem();
	}

	public T getNext() {
		if (canGoForward()) {
			return getAt(fBrowsePoint.getNextIndex());
		} else {
			return fBrowsePoint.currentItem();
		}
	}

	T getAt(int index) {
		if (fSize == 0) {
			return null;
		}

		int i = moddedIndex(index);
		return fHistory[i];
	}

	/**
	 * This method always adds the parameter element at the current history
	 * location. If history is full (capacity == size) then an existing element is
	 * overwritten in the process. If there exists an element such that
	 * evaluator.canReplace() yields true, then that element is prioritized for
	 * being overwritten
	 *
	 * @param newItem The object instance being added to history
	 * @return The element which was evicted to make room for the incoming element.
	 *         Returns null if nothing was evicted
	 */
	public T addOrReplace(T newItem) {

		T answer = null;
		// if a replacement candidate exists, delete it since this
		// incoming will be replacing it

		/**
		 * This loop could potentially degrade to N^2 performance in case of multiple
		 * deletions, but in practice it 1) shouldnt matter for our small history sizes,
		 * and 2) will seldom delete more than 1 item for O(N) performance.
		 *
		 * If performance is ever a concern, several improvements could be made: 1)
		 * perform all deletions in a single pass, then do a single additional pass for
		 * compaction for a total of 2 passes 2) uncomment the "break" statement as
		 * explained below 3) use a different data structure altogether, eg maybe an
		 * ordered tree of some kind
		 */
		for (int i = fInsertionPoint.getIndex(); i > fInsertionPoint.getIndex() - fSize; i--) {
			T candidate = getAt(i);
			if (candidate != null && fEvaluator.canReplace(newItem, candidate)) {
				answer = deleteAt(i);
				/**
				 * if performance is ever a concern, the below break could be uncommented so
				 * that this method only de-dupes first match instead of all matches. generally
				 * first match would be sufficient, except there can be drift over time eg if
				 * history contains [10,20,30] and vicinity threshold defined as distance 2,
				 * then you insert the following series: 11, 12, 13, 14, 15, 16, 17, 18, 19, 20
				 * even if each insert overwrote the prior, you still could end up with dataset
				 * [20, 20, 30]. By not breaking here we prevent that state from occuring, but
				 * at some computation cost. Therefore if cost (performance) ever a problem, we
				 * could just accept that occasional suboptimal history state for better
				 * performance
				 */
				// break;
			}
		}

		T replaced = addLast(newItem);
		if (answer == null) {
			answer = replaced;
		}

		return answer;
	}

	private T addLast(T newItem) {
		if (newItem == null) {
			return deleteLast();
		}

		if (fSize >= fHistory.length) {
			// no space, so just overwrite next slot
			fInsertionPoint.incr();
			fBrowsePoint.jumpTo(fInsertionPoint);
			return replaceAt(newItem, fInsertionPoint);
		} else {
			// there's at least one empty slot so data size can grow
			expand();
			fInsertionPoint.incr();
			fBrowsePoint.jumpTo(fInsertionPoint);
			return shiftInsert(newItem);
		}

	}

	// inserts here and shifts existing values until either empty space is found
	// or end is reached, in which case last evaluated slot is discarded
	private T shiftInsert(T newItem) {

		T answer = replaceAt(newItem, fInsertionPoint);
		T tmp = answer;

		// shift from here to the end until empty slot found
		for (int i = fInsertionPoint.getIndex() + 1; i < fSize; i++) {
			tmp = replaceAt(tmp, i);
		}

		return answer;
	}

	// deletes and shifts to fill in the hole created by deletion
	private T deleteAt(int index) {
		if (fSize == 0) {
			return null;
		}
		int modIndex = moddedIndex(index);
		T answer = replaceAt(null, modIndex);

		// shift to fill in any gaps to keep data contiguous so that mod
		// calculations work
		T priorVal = null;
		for (int i = fSize - 1; i >= modIndex; i--) {
			priorVal = replaceAt(priorVal, i);
		}

		if (answer != null) {
			fSize--;
		}

		// adjust insertion point if it was affected by the shift
		if (fInsertionPoint.getIndex() >= modIndex && fSize > 0) {
			fInsertionPoint.decr();
		}

		// adjust browse point if it was affected by the shift
		if (fBrowsePoint.getIndex() >= modIndex && fSize > 0) {
			fBrowsePoint.decr();
		}

		return answer;
	}

	public T deleteLast() {
		T answer = deleteAt(fInsertionPoint.getIndex());
		return answer;
	}

	private T replaceAt(T newItem, int index) {
		if (fSize == 0) {
			return null;
		}
		int i = moddedIndex(index);

		T replaced = getAt(i);
		fHistory[i] = newItem;
		return replaced;
	}

	private T replaceAt(T newItem, Navigator<T> navigator) {
		return replaceAt(newItem, navigator.getIndex());
	}

	public T replaceLast(T newItem) {
		if (newItem == null) {
			return deleteLast();
		}
		fBrowsePoint.jumpTo(fInsertionPoint);
		return replaceAt(newItem, fInsertionPoint);
	}

	void expand() {
		fSize = Math.max(fSize, (fSize + 1) % (fHistory.length + 1));
	}

	private int moddedIndex(int index) {
		return Math.floorMod(index, fSize);
	}

	public boolean isEmpty() {
		return fInsertionPoint.currentItem() == null;
	}

	public int getSize() {
		return fSize;
	}

	public Navigator<T> navigator() {
		Navigator<T> answer = new Navigator<>(this);
		answer.jumpTo(fInsertionPoint);
		return answer;
	}

	// for internal use, not public
	Navigator<T> navigator(int index) {
		Navigator<T> answer = new Navigator<>(this);
		answer.jumpTo(index);
		return answer;
	}

	/**
	 * This method is intended for testing/troubleshooting, not for general use
	 * Beware it has O(N) performance
	 *
	 * @param item The item to check whether this history contains
	 * @return true if history contains item
	 */
	public boolean contains(T item) {
		if (item == null) {
			return false;
		}

		for (int i = 0; i < fSize; i++) {
			if (item.equals(getAt(i))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * This method is intended for testing/troubleshooting
	 *
	 * @return true if the state of this object is healthy/as expected
	 */
	public boolean isHealthy() {
		// make sure nulls are consolidated not scattered
		boolean priorWasNull = false;
		int flipCount = 0;
		for (int i = 0; i < fHistory.length; i++) {
			boolean isNull = (fHistory[i] == null);
			if (priorWasNull != isNull) {
				flipCount++;
				priorWasNull = isNull;
			}
		}
		return flipCount < 2;
	}

	/**
	 *
	 * used during history compaction to consolidate like candidates in the history
	 */
	public static interface CandidateEvaluator<T> {
		public boolean canReplace(T a, T b);
	}

	/**
	 * @return Stream with all non-null history elements, may be empty
	 */
	public Stream<T> rawHistory() {
		return Arrays.asList(fHistory).stream().filter(x -> x != null);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("HistoryTracker ["); //$NON-NLS-1$
		builder.append("history="); //$NON-NLS-1$
		builder.append(rawHistory().map(x -> x.toString()).collect(Collectors.joining(", "))); //$NON-NLS-1$
		builder.append(", "); //$NON-NLS-1$
		if (fBrowsePoint != null) {
			builder.append("browsePoint="); //$NON-NLS-1$
			builder.append(fBrowsePoint);
			builder.append(", "); //$NON-NLS-1$
		}
		builder.append("size="); //$NON-NLS-1$
		builder.append(fSize);
		builder.append(", circular="); //$NON-NLS-1$
		builder.append(fUseCircularNavigation);
		builder.append("]"); //$NON-NLS-1$
		return builder.toString();
	}

	/**
	 * for easy traversing thru history
	 */
	public static class Navigator<T> {
		HistoryTracker<T> historyTracker;
		int fIndex;

		public Navigator(HistoryTracker<T> tracker) {
			this.historyTracker = tracker;
			fIndex = Math.floorMod(-1, tracker.fHistory.length);
		}

		void incr() {
			if (historyTracker.fSize == 0) {
				return;
			}

			fIndex = getNextIndex();
		}

		void decr() {
			if (historyTracker.fSize == 0) {
				return;
			}

			fIndex = getPriorIndex();
		}

		int getIndex() {
			return fIndex;
		}

		int getNextIndex() {
			return historyTracker.moddedIndex(fIndex + 1);
		}

		int getPriorIndex() {
			return historyTracker.moddedIndex(fIndex - 1);
		}

		public T currentItem() {
			return historyTracker.getAt(fIndex);
		}

		public T nextItem() {
			incr();
			return historyTracker.getAt(fIndex);
		}

		public T priorItem() {
			decr();
			return historyTracker.getAt(fIndex);
		}

		void jumpTo(Navigator<T> b) {
			this.fIndex = b.fIndex;
		}

		public void jumpTo(int index) {
			fIndex = historyTracker.moddedIndex(index);
		}
	}
}
