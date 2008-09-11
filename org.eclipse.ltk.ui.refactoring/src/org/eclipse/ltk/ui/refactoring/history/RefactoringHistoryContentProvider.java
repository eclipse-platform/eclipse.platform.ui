/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
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

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;
import org.eclipse.ltk.internal.ui.refactoring.history.RefactoringHistoryCollection;
import org.eclipse.ltk.internal.ui.refactoring.history.RefactoringHistoryDate;
import org.eclipse.ltk.internal.ui.refactoring.history.RefactoringHistoryEntry;
import org.eclipse.ltk.internal.ui.refactoring.history.RefactoringHistoryNode;

/**
 * Tree content provider to display a refactoring history. This content provider
 * may be used with {@link RefactoringHistory} elements and returns model
 * elements suitable to be rendered using
 * {@link RefactoringHistoryLabelProvider}.
 * <p>
 * Note: this class is not indented to be subclassed outside the refactoring
 * framework.
 * </p>
 *
 * @see IRefactoringHistoryControl
 * @see RefactoringHistoryControlConfiguration
 *
 * @since 3.2
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class RefactoringHistoryContentProvider implements ITreeContentProvider {

	/** The no elements constant */
	private static final Object[] NO_ELEMENTS= {};

	/**
	 * Implements a binary search which computes the index of the number, or a
	 * nearest approximative index if the number has not been found.
	 * <p>
	 * The array must be sorted in descending order.
	 * </p>
	 *
	 * @param array
	 *            the array, sorted in descending order, to search for the
	 *            number
	 * @param number
	 *            the number to search for
	 * @return the index of the number in the array, or the nearest index,
	 *         depending on <code>right</code>
	 */
	private static int binarySearch(final long[] array, final long number) {
		int left= 0;
		int right= array.length - 1;
		int median;
		do {
			median= (left + right) / 2;
			if (number > array[median])
				right= median - 1;
			else
				left= median + 1;
		} while (number != array[median] && left <= right);
		if (number == array[median])
			return median;
		return left;
	}

	/**
	 * Returns the index of the specified root kind in the structure.
	 *
	 * @param structure
	 *            the structure
	 * @param kind
	 *            the root kind
	 * @return the index, or <code>-1</code>
	 */
	private static int getRefactoringRootKindIndex(final long[][] structure, final int kind) {
		for (int index= structure.length - 1; index >= 0; index--) {
			if (kind >= structure[index][1])
				return index;
		}
		return -1;
	}

	/** The refactoring history control configuration to use */
	private final RefactoringHistoryControlConfiguration fControlConfiguration;

	/** The refactoring history, or <code>null</code> */
	private RefactoringHistory fRefactoringHistory= null;

	/** The refactoring root structure, or <code>null</code> */
	private long[][] fRefactoringRoots= null;

	/** The refactoring time stamps, in descending order, or <code>null</code> */
	private long[] fRefactoringStamps= null;

	/**
	 * Creates a new refactoring history content provider.
	 *
	 * @param configuration
	 *            the refactoring history control configuration
	 */
	public RefactoringHistoryContentProvider(final RefactoringHistoryControlConfiguration configuration) {
		Assert.isNotNull(configuration);
		fControlConfiguration= configuration;
	}

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
			final RefactoringDescriptorProxy[] proxies= getRefactoringDescriptorProxies();
			if (proxies.length > 0) {
				final long[][] structure= getRefactoringRootStructure(proxies[0].getTimeStamp());
				final int kind= node.getKind();
				switch (kind) {
					case RefactoringHistoryNode.COLLECTION:
						return getRefactoringHistoryEntries(node);
					default: {
						if (node instanceof RefactoringHistoryDate) {
							final RefactoringHistoryDate date= (RefactoringHistoryDate) node;
							final long stamp= date.getTimeStamp();
							switch (kind) {
								case RefactoringHistoryNode.TODAY:
									return getRefactoringHistoryEntries(date, stamp, Long.MAX_VALUE);
								case RefactoringHistoryNode.YESTERDAY:
									return getRefactoringHistoryEntries(date, stamp, structure[getRefactoringRootKindIndex(structure, RefactoringHistoryNode.TODAY)][0] - 1);
								case RefactoringHistoryNode.THIS_WEEK:
									return getRefactoringHistoryDays(date, stamp, structure[getRefactoringRootKindIndex(structure, RefactoringHistoryNode.YESTERDAY)][0] - 1);
								case RefactoringHistoryNode.LAST_WEEK:
									return getRefactoringHistoryDays(date, stamp, structure[getRefactoringRootKindIndex(structure, RefactoringHistoryNode.THIS_WEEK)][0] - 1);
								case RefactoringHistoryNode.THIS_MONTH:
									return getRefactoringHistoryWeeks(date, stamp, structure[getRefactoringRootKindIndex(structure, RefactoringHistoryNode.LAST_WEEK)][0] - 1);
								case RefactoringHistoryNode.LAST_MONTH:
									return getRefactoringHistoryWeeks(date, stamp, structure[getRefactoringRootKindIndex(structure, RefactoringHistoryNode.THIS_MONTH)][0] - 1);
								case RefactoringHistoryNode.DAY:
									return getRefactoringHistoryEntries(date, stamp, stamp + 1000 * 60 * 60 * 24 - 1);
								case RefactoringHistoryNode.WEEK:
									return getRefactoringHistoryDays(date, stamp, stamp + 1000 * 60 * 60 * 24 * 7 - 1);
								case RefactoringHistoryNode.MONTH: {
									final Calendar calendar= Calendar.getInstance();
									calendar.setTimeInMillis(stamp);
									calendar.add(Calendar.MONTH, 1);
									return getRefactoringHistoryWeeks(date, stamp, calendar.getTimeInMillis() - 1);
								}
								case RefactoringHistoryNode.YEAR: {
									final Calendar calendar= Calendar.getInstance();
									calendar.setTimeInMillis(stamp);
									calendar.add(Calendar.YEAR, 1);
									return getRefactoringHistoryMonths(date, stamp, calendar.getTimeInMillis() - 1);
								}
							}
						}
					}
				}
			}
		} else if (element instanceof RefactoringHistory)
			return getElements(element);
		return NO_ELEMENTS;
	}

	/**
	 * {@inheritDoc}
	 */
	public Object[] getElements(final Object element) {
		if (element instanceof RefactoringHistory) {
			if (fControlConfiguration.isTimeDisplayed())
				return getRootElements();
			else if (fRefactoringHistory != null && !fRefactoringHistory.isEmpty())
				return new Object[] { new RefactoringHistoryCollection()};
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
	 * Returns the refactoring descriptor proxies, sorted in descending order of
	 * their time stamps, and caches time stamp information.
	 *
	 * @return the refactoring descriptor proxies
	 */
	private RefactoringDescriptorProxy[] getRefactoringDescriptorProxies() {
		final RefactoringDescriptorProxy[] proxies;
		if (fRefactoringHistory == null)
			proxies= new RefactoringDescriptorProxy[0];
		else
			proxies= fRefactoringHistory.getDescriptors();
		if (fRefactoringStamps == null) {
			final int length= proxies.length;
			fRefactoringStamps= new long[length];
			for (int index= 0; index < length; index++)
				fRefactoringStamps[index]= proxies[index].getTimeStamp();
		}
		return proxies;
	}

	/**
	 * Returns the refactoring history days.
	 *
	 * @param parent
	 *            the parent node, or <code>null</code>
	 * @param start
	 *            the start time stamp, inclusive
	 * @param end
	 *            the end time stamp. inclusive
	 *
	 * @return the refactoring history days
	 */
	private Object[] getRefactoringHistoryDays(final RefactoringHistoryDate parent, final long start, final long end) {
		final long time= parent.getTimeStamp();
		final Calendar calendar= Calendar.getInstance();
		final RefactoringDescriptorProxy[] proxies= getRefactoringDescriptorProxies();
		final int[] range= getRefactoringHistoryRange(start, end);
		final List list= new ArrayList(proxies.length);
		int last= -1;
		for (int index= range[0]; index <= range[1]; index++) {
			long stamp= proxies[index].getTimeStamp();
			if (stamp >= time) {
				calendar.setTimeInMillis(stamp);
				final int day= calendar.get(Calendar.DAY_OF_YEAR);
				if (day != last) {
					last= day;
					calendar.set(Calendar.MILLISECOND, 0);
					calendar.set(Calendar.SECOND, 0);
					calendar.set(Calendar.MINUTE, 0);
					calendar.set(Calendar.HOUR_OF_DAY, 0);
					stamp= calendar.getTimeInMillis();
					if (stamp < time)
						list.add(new RefactoringHistoryDate(parent, time, RefactoringHistoryNode.DAY));
					else
						list.add(new RefactoringHistoryDate(parent, stamp, RefactoringHistoryNode.DAY));
				}
			}
		}
		return list.toArray();
	}

	/**
	 * Returns the refactoring history entries.
	 *
	 * @param parent
	 *            the parent node, or <code>null</code>
	 * @param start
	 *            the start time stamp, inclusive
	 * @param end
	 *            the end time stamp. inclusive
	 *
	 * @return the refactoring history entries
	 */
	private Object[] getRefactoringHistoryEntries(final RefactoringHistoryDate parent, final long start, final long end) {
		final RefactoringDescriptorProxy[] proxies= getRefactoringDescriptorProxies();
		final int[] range= getRefactoringHistoryRange(start, end);
		final List list= new ArrayList(proxies.length);
		for (int index= range[0]; index <= range[1]; index++)
			list.add(new RefactoringHistoryEntry(parent, proxies[index]));
		return list.toArray();
	}

	/**
	 * Returns the refactoring history entries.
	 *
	 * @param parent
	 *            the parent node, or <code>null</code>
	 *
	 * @return the refactoring history entries
	 */
	private Object[] getRefactoringHistoryEntries(final RefactoringHistoryNode parent) {
		final RefactoringDescriptorProxy[] proxies= getRefactoringDescriptorProxies();
		final List list= new ArrayList(proxies.length);
		for (int index= 0; index < proxies.length; index++)
			list.add(new RefactoringHistoryEntry(parent, proxies[index]));
		return list.toArray();
	}

	/**
	 * Returns the refactoring history months.
	 *
	 * @param parent
	 *            the parent node, or <code>null</code>
	 * @param start
	 *            the start time stamp, inclusive
	 * @param end
	 *            the end time stamp. inclusive
	 *
	 * @return the refactoring history months
	 */
	private Object[] getRefactoringHistoryMonths(final RefactoringHistoryDate parent, final long start, final long end) {
		final long time= parent.getTimeStamp();
		final Calendar calendar= Calendar.getInstance();
		final RefactoringDescriptorProxy[] proxies= getRefactoringDescriptorProxies();
		final int[] range= getRefactoringHistoryRange(start, end);
		final List list= new ArrayList(proxies.length);
		int last= -1;
		for (int index= range[0]; index <= range[1]; index++) {
			long stamp= proxies[index].getTimeStamp();
			if (stamp >= time) {
				calendar.setTimeInMillis(stamp);
				final int month= calendar.get(Calendar.MONTH);
				if (month != last) {
					last= month;
					calendar.set(Calendar.MILLISECOND, 0);
					calendar.set(Calendar.SECOND, 0);
					calendar.set(Calendar.MINUTE, 0);
					calendar.set(Calendar.HOUR_OF_DAY, 0);
					calendar.set(Calendar.DAY_OF_MONTH, 1);
					stamp= calendar.getTimeInMillis();
					if (stamp < time)
						list.add(new RefactoringHistoryDate(parent, time, RefactoringHistoryNode.MONTH));
					else
						list.add(new RefactoringHistoryDate(parent, stamp, RefactoringHistoryNode.MONTH));
				}
			}
		}
		return list.toArray();
	}

	/**
	 * Returns the refactoring history range for the specified time stamps.
	 *
	 * @param start
	 *            the start time stamp, inclusive
	 * @param end
	 *            the end time stamp. inclusive
	 * @return An array containing the index range
	 */
	private int[] getRefactoringHistoryRange(final long start, final long end) {
		final int[] range= new int[2];
		range[1]= binarySearch(fRefactoringStamps, start) - 1;
		range[0]= binarySearch(fRefactoringStamps, end);
		return range;
	}

	/**
	 * Returns the refactoring history weeks.
	 *
	 * @param parent
	 *            the parent node, or <code>null</code>
	 * @param start
	 *            the start time stamp, inclusive
	 * @param end
	 *            the end time stamp. inclusive
	 *
	 * @return the refactoring history weeks
	 */
	private Object[] getRefactoringHistoryWeeks(final RefactoringHistoryDate parent, final long start, final long end) {
		final long time= parent.getTimeStamp();
		final Calendar calendar= Calendar.getInstance();
		final RefactoringDescriptorProxy[] proxies= getRefactoringDescriptorProxies();
		final int[] range= getRefactoringHistoryRange(start, end);
		final List list= new ArrayList(proxies.length);
		int last= -1;
		for (int index= range[0]; index <= range[1]; index++) {
			long stamp= proxies[index].getTimeStamp();
			if (stamp >= time) {
				calendar.setTimeInMillis(stamp);
				final int week= calendar.get(Calendar.WEEK_OF_YEAR);
				if (week != last) {
					last= week;
					calendar.set(Calendar.MILLISECOND, 0);
					calendar.set(Calendar.SECOND, 0);
					calendar.set(Calendar.MINUTE, 0);
					calendar.set(Calendar.HOUR_OF_DAY, 0);
					calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
					stamp= calendar.getTimeInMillis();
					if (stamp < time)
						list.add(new RefactoringHistoryDate(parent, time, RefactoringHistoryNode.WEEK));
					else
						list.add(new RefactoringHistoryDate(parent, stamp, RefactoringHistoryNode.WEEK));
				}
			}
		}
		return list.toArray();
	}

	/**
	 * Computes and returns the refactoring root structure if necessary.
	 *
	 * @param stamp
	 *            the time stamp of the oldest refactoring
	 * @return the refactoring root structure
	 */
	private long[][] getRefactoringRootStructure(final long stamp) {
		if (fRefactoringRoots == null) {
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
				calendar.add(Calendar.WEEK_OF_YEAR, -1);
			}
			calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			thresholds[count]= calendar.getTimeInMillis();
			kinds[count]= RefactoringHistoryNode.LAST_WEEK;
			count++;
			calendar.setTimeInMillis(time);
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			calendar.set(Calendar.DAY_OF_MONTH, 1);
			if (thresholds[count - 1] >= calendar.getTimeInMillis()) {
				thresholds[count]= calendar.getTimeInMillis();
				kinds[count]= RefactoringHistoryNode.THIS_MONTH;
				count++;
			}
			calendar.add(Calendar.MONTH, -1);
			thresholds[count]= calendar.getTimeInMillis();
			kinds[count]= RefactoringHistoryNode.LAST_MONTH;
			count++;
			final int month= calendar.get(Calendar.MONTH);
			if (month != 0) {
				calendar.set(Calendar.MONTH, 0);
				thresholds[count]= calendar.getTimeInMillis();
				kinds[count]= RefactoringHistoryNode.YEAR;
				count++;
			}
			if (stamp > 0) {
				final long localized= stamp + zoneOffset + dstOffset;
				calendar.set(Calendar.MONTH, 0);
				do {
					calendar.add(Calendar.YEAR, -1);
					thresholds[count]= calendar.getTimeInMillis();
					kinds[count]= RefactoringHistoryNode.YEAR;
					count++;
				} while (calendar.getTimeInMillis() > localized);
			}
			final long[][] result= new long[count][2];
			for (int index= 0; index < count; index++) {
				result[index][0]= thresholds[index];
				result[index][1]= kinds[index];
			}
			fRefactoringRoots= result;
		}
		return fRefactoringRoots;
	}

	/**
	 * Returns the refactoring history root elements.
	 * <p>
	 * This method must only be called for refactoring histories with associated
	 * time information.
	 * </p>
	 *
	 * @return the refactoring history root elements
	 */
	public Object[] getRootElements() {
		final List list= new ArrayList(16);
		if (fRefactoringHistory != null && !fRefactoringHistory.isEmpty()) {
			final RefactoringDescriptorProxy[] proxies= getRefactoringDescriptorProxies();
			if (proxies.length > 0) {
				final long[][] structure= getRefactoringRootStructure(proxies[0].getTimeStamp());
				int begin= 0;
				long end= Long.MAX_VALUE;
				for (int index= 0; index < proxies.length; index++) {
					final long stamp= proxies[index].getTimeStamp();
					for (int offset= begin; offset < structure.length; offset++) {
						final long start= structure[offset][0];
						if (stamp >= start && stamp <= end) {
							list.add(new RefactoringHistoryDate(null, start, (int) structure[offset][1]));
							begin= offset + 1;
							end= start - 1;
							break;
						}
					}
				}
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
		if (predecessor == successor || fRefactoringHistory == successor)
			return;
		if (successor instanceof RefactoringHistory) {
			if (successor.equals(fRefactoringHistory))
				return;
			fRefactoringHistory= (RefactoringHistory) successor;
		} else
			fRefactoringHistory= null;
		fRefactoringRoots= null;
		fRefactoringStamps= null;
	}
}
