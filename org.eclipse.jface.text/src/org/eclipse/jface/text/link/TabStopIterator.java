/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.link;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.text.Position;



/**
 * Iterator that leaps over the double occurrence of an element when switching from forward
 * to backward iteration that is shown by <code>ListIterator</code>.
 * <p>
 * Package private, only for use by LinkedModeUI.
 * </p>
 * @since 3.0
 */
class TabStopIterator {
	/**
	 * Comparator for <code>LinkedPosition</code>s. If the sequence number of two positions is equal, the
	 * offset is used.
	 */
	private static class SequenceComparator implements Comparator {

		/**
		 * {@inheritDoc}
		 *
		 * <p><code>o1</code> and <code>o2</code> are required to be instances
		 * of <code>LinkedPosition</code>.</p>
		 */
		public int compare(Object o1, Object o2) {
			LinkedPosition p1= (LinkedPosition)o1;
			LinkedPosition p2= (LinkedPosition)o2;
			int i= p1.getSequenceNumber() - p2.getSequenceNumber();
			if (i != 0)
				return i;
			return p1.getOffset() - p2.getOffset();
		}

	}

	/** The comparator to sort the list of positions. */
	private static final Comparator fComparator= new SequenceComparator();

	/** The iteration sequence. */
	private final ArrayList fList;
	/** The size of <code>fList</code>. */
	private int fSize;
	/** Index of the current element, to the first one initially. */
	private int fIndex;
	/** Cycling property. */
	private boolean fIsCycling= false;

	TabStopIterator(List positionSequence) {
		Assert.isNotNull(positionSequence);
		fList= new ArrayList(positionSequence);
		Collections.sort(fList, fComparator);
		fSize= fList.size();
		fIndex= -1;
		Assert.isTrue(fSize > 0);
	}

	boolean hasNext(LinkedPosition current) {
		return getNextIndex(current) != fSize;
	}

	private int getNextIndex(LinkedPosition current) {
		if (current != null && fList.get(fIndex) != current)
			return findNext(current);
		else if (fIsCycling && fIndex == fSize - 1)
			return 0;
		else
			// default: increase
			return fIndex + 1;
	}

	/**
	 * Finds the closest position in the iteration set that follows after
	 * <code>current</code> and sets <code>fIndex</code> accordingly. If <code>current</code>
	 * is in the iteration set, the next in turn is chosen.
	 *
	 * @param current the current position
	 * @return <code>true</code> if there is a next position, <code>false</code> otherwise
	 */
	private int findNext(LinkedPosition current) {
		Assert.isNotNull(current);
		// if the position is in the iteration set, jump to the next one
		int index= fList.indexOf(current);
		if (index != -1) {
			if (fIsCycling && index == fSize - 1)
				return 0;
			return index + 1;
		}

		// index == -1

		// find the position that follows closest to the current position
		LinkedPosition found= null;
		for (Iterator it= fList.iterator(); it.hasNext(); ) {
			LinkedPosition p= (LinkedPosition) it.next();
			if (p.offset > current.offset)
				if (found == null || found.offset > p.offset)
					found= p;
		}

		if (found != null) {
			return fList.indexOf(found);
		} else if (fIsCycling) {
			return 0;
		} else
			return fSize;
	}

	boolean hasPrevious(LinkedPosition current) {
		return getPreviousIndex(current) != -1;
	}

	private int getPreviousIndex(LinkedPosition current) {
		if (current != null && fList.get(fIndex) != current)
			return findPrevious(current);
		else if (fIsCycling && fIndex == 0)
			return fSize - 1;
		else
			return fIndex - 1;
	}

	/**
	 * Finds the closest position in the iteration set that precedes
	 * <code>current</code>. If <code>current</code>
	 * is in the iteration set, the previous in turn is chosen.
	 *
	 * @param current the current position
	 * @return the index of the previous position
	 */
	private int findPrevious(LinkedPosition current) {
		Assert.isNotNull(current);
		// if the position is in the iteration set, jump to the next one
		int index= fList.indexOf(current);
		if (index != -1) {
			if (fIsCycling && index == 0)
				return fSize - 1;
			return index - 1;
		}

		// index == -1

		// find the position that follows closest to the current position
		LinkedPosition found= null;
		for (Iterator it= fList.iterator(); it.hasNext(); ) {
			LinkedPosition p= (LinkedPosition) it.next();
			if (p.offset < current.offset)
				if (found == null || found.offset < p.offset)
					found= p;
		}
		if (found != null) {
			return fList.indexOf(found);
		} else if (fIsCycling) {
			return fSize - 1;
		} else
			return -1;
	}

	LinkedPosition next(LinkedPosition current) {
		if (!hasNext(current))
			throw new NoSuchElementException();
		return (LinkedPosition) fList.get(fIndex= getNextIndex(current));
	}

	LinkedPosition previous(LinkedPosition current) {
		if (!hasPrevious(current))
			throw new NoSuchElementException();
		return (LinkedPosition) fList.get(fIndex= getPreviousIndex(current));
	}

	void setCycling(boolean mode) {
		fIsCycling= mode;
	}

	void addPosition(Position position) {
		fList.add(fSize++, position);
		Collections.sort(fList, fComparator);
	}

	void removePosition(Position position) {
		if (fList.remove(position))
			fSize--;
	}

	/**
	 * @return Returns the isCycling.
	 */
	boolean isCycling() {
		return fIsCycling;
	}

	LinkedPosition[] getPositions() {
		return (LinkedPosition[]) fList.toArray(new LinkedPosition[fSize]);
	}
}
