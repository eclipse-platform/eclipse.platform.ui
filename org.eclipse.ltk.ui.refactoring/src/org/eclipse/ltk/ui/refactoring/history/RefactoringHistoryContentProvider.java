/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.ui.refactoring.history;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;

import org.eclipse.ltk.internal.ui.refactoring.Assert;
import org.eclipse.ltk.internal.ui.refactoring.history.RefactoringHistoryContainer;
import org.eclipse.ltk.internal.ui.refactoring.history.RefactoringHistoryDate;
import org.eclipse.ltk.internal.ui.refactoring.history.RefactoringHistoryEntry;
import org.eclipse.ltk.internal.ui.refactoring.history.RefactoringHistoryNode;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Tree content provider to display a refactoring history.
 * <p>
 * Note: this class is not indented to be subclassed outside the refactoring
 * framework.
 * </p>
 * <p>
 * Note: This API is considered experimental and may change in the near future.
 * </p>
 * 
 * @since 3.2
 */
public class RefactoringHistoryContentProvider implements ITreeContentProvider {

	/** The no elements constant */
	private static final Object[] NO_ELEMENTS= {};

	/**
	 * Computes the time stamp thresholds.
	 * 
	 * @param stamp
	 *            the time stamp of the oldest refactoring
	 * @return the time stamp threshold array
	 */
	private static long[][] computeTimeStampThresholds(final long stamp) {
		final long time= System.currentTimeMillis();
		final Calendar calendar= Calendar.getInstance();
		calendar.setTimeInMillis(time);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		final int zoneOffset= calendar.get(Calendar.ZONE_OFFSET);
		final int dstOffset= calendar.get(Calendar.DST_OFFSET);
		int count= 0;
		final long[] thresholds= new long[32];
		final int[] kinds= new int[32];
		thresholds[count]= calendar.getTimeInMillis();
		kinds[count]= RefactoringHistoryNode.TODAY;
		count++;
		calendar.add(Calendar.DATE, -1);
		thresholds[count]= calendar.getTimeInMillis();
		kinds[count]= RefactoringHistoryNode.YESTERDAY;
		count++;
		final int day= calendar.get(Calendar.DAY_OF_WEEK);
		if (day != Calendar.SUNDAY) {
			calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			thresholds[count]= calendar.getTimeInMillis();
			kinds[count]= RefactoringHistoryNode.THIS_WEEK;
			count++;
		}
		calendar.add(Calendar.WEEK_OF_YEAR, -1);
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		thresholds[count]= calendar.getTimeInMillis();
		kinds[count]= RefactoringHistoryNode.LAST_WEEK;
		count++;
		final int week= calendar.get(Calendar.WEEK_OF_MONTH);
		if (week != 1) {
			calendar.setTimeInMillis(time);
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			calendar.set(Calendar.DAY_OF_MONTH, 1);
			thresholds[count]= calendar.getTimeInMillis();
			kinds[count]= RefactoringHistoryNode.THIS_MONTH;
			count++;
		}
		calendar.add(Calendar.MONTH, -1);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		thresholds[count]= calendar.getTimeInMillis();
		kinds[count]= RefactoringHistoryNode.LAST_MONTH;
		count++;
		final int month= calendar.get(Calendar.MONTH);
		if (month != 0) {
			calendar.set(Calendar.MONTH, 0);
			calendar.set(Calendar.DAY_OF_MONTH, 1);
			thresholds[count]= calendar.getTimeInMillis();
			kinds[count]= RefactoringHistoryNode.YEAR;
			count++;
		}
		if (stamp > 0) {
			final long localized= stampToLocalizedDate(stamp, zoneOffset, dstOffset);
			calendar.set(Calendar.MONTH, 0);
			calendar.set(Calendar.DAY_OF_MONTH, 1);
			do {
				calendar.add(Calendar.YEAR, -1);
				thresholds[count]= calendar.getTimeInMillis();
				kinds[count]= RefactoringHistoryNode.YEAR;
				count++;
			} while (calendar.getTimeInMillis() > localized);
		}
		final long[][] result= new long[count][2];
		for (int index= 0; index < count - 1; index++) {
			result[index][0]= thresholds[index];
			result[index][1]= kinds[index];
		}
		return result;
	}

	/**
	 * Converts a time stamp to a localized date stamp.
	 * 
	 * @param stamp
	 *            the time stamp to convert
	 * @param zoneOffset
	 *            the time zone offset in ms
	 * @param dstOffset
	 *            the daylight saving time offset in ms
	 * @return the localized date stamp
	 */
	private static long stampToLocalizedDate(final long stamp, final int zoneOffset, final int dstOffset) {
		return stamp + zoneOffset + dstOffset;
	}

	/** Should time information be displayed? */
	private boolean fDisplayTime= true;

	/** The refactoring history, or <code>null</code> */
	private RefactoringHistory fRefactoringHistory= null;

	/**
	 * {@inheritDoc}
	 */
	public void dispose() {
		// Do nothing
	}

	/**
	 * {@inheritDoc}
	 */
	public Object[] getChildren(final Object element) {
		if (element instanceof RefactoringHistoryNode) {
			final RefactoringHistoryNode node= (RefactoringHistoryNode) element;
			final int kind= node.getKind();
			switch (kind) {
				case RefactoringHistoryNode.COLLECTION:
					if (fRefactoringHistory != null)
						return getRefactoringHistoryEntries(fRefactoringHistory);
					break;
				default: {
					if (node instanceof RefactoringHistoryDate) {
						final RefactoringHistoryDate date= (RefactoringHistoryDate) node;
						switch (kind) {
							case RefactoringHistoryNode.TODAY:
								return getRefactoringHistoryEntries(fRefactoringHistory);
							case RefactoringHistoryNode.YESTERDAY:
							case RefactoringHistoryNode.THIS_WEEK:
							case RefactoringHistoryNode.LAST_WEEK:
							case RefactoringHistoryNode.THIS_MONTH:
							case RefactoringHistoryNode.LAST_MONTH:
							case RefactoringHistoryNode.DAY:
							case RefactoringHistoryNode.WEEK:
							case RefactoringHistoryNode.MONTH:
							case RefactoringHistoryNode.YEAR:
						}
					}
				}
			}
		}
		return NO_ELEMENTS;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object[] getElements(final Object element) {
		if (element instanceof RefactoringHistory) {
			if (fDisplayTime)
				return getRefactoringHistoryRoots((RefactoringHistory) element);
			else if (fRefactoringHistory != null && !fRefactoringHistory.isEmpty())
				return new Object[] { new RefactoringHistoryContainer()};
		}
		return NO_ELEMENTS;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object getParent(final Object element) {
		if (element instanceof RefactoringHistoryNode) {
			final RefactoringHistoryNode node= (RefactoringHistoryNode) element;
			return node.getParent();
		}
		return null;
	}

	/**
	 * Returns the refactoring history entries.
	 * 
	 * @param history
	 *            the refactoring history
	 * @return the refactoring history entries
	 */
	private Object[] getRefactoringHistoryEntries(final RefactoringHistory history) {
		Assert.isNotNull(history);
		final RefactoringDescriptorProxy[] proxies= history.getDescriptors();
		final RefactoringHistoryEntry[] entries= new RefactoringHistoryEntry[proxies.length];
		for (int index= 0; index < proxies.length; index++)
			entries[index]= new RefactoringHistoryEntry(null, proxies[index]);
		return entries;
	}

	/**
	 * Returns the refactoring history roots.
	 * 
	 * @param history
	 *            the refactoring history
	 * @return the refactoring history roots
	 */
	private Object[] getRefactoringHistoryRoots(final RefactoringHistory history) {
		Assert.isNotNull(history);
		final List list= new ArrayList();
		if (!history.isEmpty()) {
			final RefactoringDescriptorProxy[] proxies= history.getDescriptors();
			final long[][] thresholds= computeTimeStampThresholds(proxies[0].getTimeStamp());
			for (int index= 0; index < thresholds.length; index++) {
				list.add(new RefactoringHistoryDate(null, thresholds[index][0], (int) thresholds[index][1]));
			}
		}
		return list.toArray();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasChildren(final Object element) {
		return !(element instanceof RefactoringHistoryEntry);
	}

	/**
	 * {@inheritDoc}
	 */
	public void inputChanged(final Viewer viewer, final Object predecessor, final Object successor) {
		if (successor instanceof RefactoringHistory)
			fRefactoringHistory= (RefactoringHistory) successor;
		else
			fRefactoringHistory= null;
	}

	/**
	 * Determines whether time information should be displayed.
	 * <p>
	 * Note: the default value is <code>true</code>.
	 * </p>
	 * 
	 * @param display
	 *            <code>true</code> to display time information,
	 *            <code>false</code> otherwise
	 */
	public void setDisplayTime(final boolean display) {
		fDisplayTime= display;
	}
}
