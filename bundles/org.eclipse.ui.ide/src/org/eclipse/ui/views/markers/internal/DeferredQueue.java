/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views.markers.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Contains a queue of changes to be applied to a particular TableViewer.
 * This object is not multithread-friendly. The owning object must synchronize
 * its access to this object such that no two threads attempt to access it
 * simultaneously.
 */
final class DeferredQueue {

    private static final String SETTING_CONTENTS = MarkerMessages.DeferredQueue_setting_contents;

    /**
     * Maximum number of items to be added or removed from the viewer in a single update.
     * (Will be used for an initially empty table)
     */
    private static final int MAX_UPDATE = 40;

    /**
     * Minimum number of items to be added or removed from the viewer in a single update
     * (Will be used once the table size reaches (GROWTH_LIMIT)
     */
    private static final int MIN_UPDATE = 1;

    /**
     * Table size beyond which all updates will be approximately MIN_UPDATE
     */
    private static final int GROWTH_LIMIT = 20000;

    // Member variables

    /**
     * Current sort order (note that this object does the sorting -- not the viewer. The
     * view contents are sorted by controlling their insertion order and position.)
     */
    private Comparator sortOrder;

    /**
     * Set of items currently visible in the viewer
     */
    private Set visibleItems = new HashSet();

    /**
     * Set of pending insertions that will occur at the end of the table (if the
     * viewer is sorted, this will be changed into a sorted collection). This
     * will not contain any items currently in the visibleItems set.
     */
    private Set insertionsAtEnd = new HashSet();

    /**
     * Set of pending insertions that will occur in the middle of the table. This
     * remains unsorted. Sorting will occur in the UI thread at insertion-time.
     * This will not contain any items currently in the visibleItems set.
     */
    private Set insertionsInMiddle = new HashSet();

    /**
     * List of pending removals. This is a subset of visibleItems. 
     */
    private Set pendingRemovals = new HashSet();

    /**
     * List of pending changes. This is a subset of visibleItems.
     */
    private Set pendingChanges = new HashSet();

    /**
     * Pointer to the item currently at the end of the table, or null if
     * the table is currently empty.
     */
    private Object lastVisible = null;

    /**
     * True iff there may be items inthe insertionsInMiddle queue
     * that can be moved to the insertionsAtEnd queue. This is set to
     * true when lastVisible is reduced, and set to false when 
     * insertionsInMiddle is repartitioned.
     */
    private boolean lastDirty = false;

    /**
     * pointer to the viewer being populated.
     */
    private TableView view;

    private boolean hasPendingChanges = false;

    /**
     * Constructs a new DeferredQueue.
     * 
     * @param view
     */
    public DeferredQueue(TableView view) {
        this.view = view;
    }

    /**
     * Returns the set of items currently visible in the viewer (read-only)
     * 
     * @return the set of processed items
     */
    public Object[] getVisibleItems() {
        synchronized (visibleItems) {
            return visibleItems.toArray();
        }
    }

    /**
     * Queues the given set of items to be refreshed in the viewer. If there
     * are any items in the viewer (or its queues) that compare as equal(...)
     * to one of these items, the new item will replace the old one. Should
     * be run in a background thread.
     * 
     * @param changes set if items to be refreshed in the viewer
     */
    public void change(Collection changes) {
        Iterator iter = changes.iterator();

        while (iter.hasNext()) {
            Object next = iter.next();
            boolean isVisible = false;
            synchronized (visibleItems) {
                if (visibleItems.contains(next)) {
                    visibleItems.remove(next);
                    visibleItems.add(next);
                    pendingChanges.add(next);
                    isVisible = true;
                    hasPendingChanges = true;
                }
            }

            if (!isVisible) {
                if (insertionsInMiddle.contains(next)) {
                    insertionsInMiddle.remove(next);
                    insertionsInMiddle.add(next);
                    hasPendingChanges = true;
                } else if (insertionsAtEnd.contains(next)) {
                    insertionsAtEnd.remove(next);
                    insertionsAtEnd.add(next);
                    hasPendingChanges = true;
                }
            }
        }
    }

    /**
     * Sets the desired contents of the viewer, enqueueing any additions and removals
     * necessary such that the final contents of the viewer will be as specified.
     * Should be run in a background thread. If the given montor is canceled (possibly
     * in another thread), the operation will be aborted without modifying the queue.
     * 
     * @param newPendingContents desired contents of the viewer
     * @param mon progress monitor
     */
    public void set(Collection newPendingContents, IProgressMonitor mon) {
        mon.beginTask(SETTING_CONTENTS, 100);

        // Set of items in newPendingContents that are not currently in visibleItems
        Set newInsertions = new HashSet();

        // New pendingRemovals queue
        Set newPendingRemovals = new HashSet();
        // New insertionsInMiddle queue
        Set newInsertionsInMiddle = new HashSet();
        // New insertionsAtEnd queue
        Set newInsertionsAtEnd = newEndSet();

        addWithProgress(newInsertions, newPendingContents, mon, 5);
        synchronized (visibleItems) {
            addWithProgress(newPendingRemovals, visibleItems, mon, 5);
            removeWithProgress(newPendingRemovals, newInsertions, mon, 5);
            removeWithProgress(newInsertions, visibleItems, mon, 5);
        }

        if (newInsertions.isEmpty() && newPendingRemovals.isEmpty()) {
            return;
        }

        SubProgressMonitor sub = new SubProgressMonitor(mon, 80);
        SortUtil.partition(newInsertionsInMiddle, newInsertionsAtEnd,
                newInsertionsAtEnd, newInsertions, sortOrder, lastVisible, sub);

        // Do nothing if the operation was cancelled.
        if (mon.isCanceled()) {
            mon.done();
            return;
        }

        // Now we've computed everything. Apply all the computed changes.
        hasPendingChanges = true;
        lastDirty = false;
        insertionsAtEnd.clear();
        insertionsAtEnd = newInsertionsAtEnd;

        insertionsInMiddle.clear();
        insertionsInMiddle = newInsertionsInMiddle;

        pendingRemovals.clear();
        pendingRemovals = newPendingRemovals;

        mon.done();
    }

    /**
     * Applies the next set of changes to the table. Returns the number of items
     * actually refreshed. Must run in the UI thread.
     * 
     * @param maximumToChange maximum number of queued changes to apply
     * @return the number of changes actually applied.
     */
    private int nextChange(int maximumToChange) {
        Collection result = SortUtil.removeFirst(pendingChanges,
                maximumToChange);

        view.getViewer().update(result.toArray(), null);

        return result.size();
    }

    /**
     * Applies the next set of removals from the table. Must run in the UI thread.
     * 
     * @param maximumToRemove maximum number of items to remove from the table.
     * @return the number of items actually removed.
     */
    private int nextRemoval(int maximumToRemove) {
        ArrayList result = new ArrayList(maximumToRemove);

        int counter = maximumToRemove;

        Iterator iter = pendingRemovals.iterator();
        while (iter.hasNext() && counter > 0) {
            Object next = iter.next();

            result.add(next);

            if (lastVisible != null && lastVisible.equals(next)) {
                lastDirty = true;
            }

            iter.remove();
            counter--;
        }

        synchronized (visibleItems) {
            visibleItems.removeAll(result);
        }

        TreeViewer viewer = view.getViewer();
        	
        viewer.remove(result.toArray());

        if (lastDirty) {
            lastVisible =  getElementAt(viewer,viewer.getTree().getItemCount() - 1);
        }

        return result.size();
    }

    /**
     * Get the element in viewer at index of the root.
     * @param viewer
     * @param index
     * @return Object
     */
    private Object getElementAt(TreeViewer viewer, int index) {
    	
    	Tree tree = viewer.getTree();
    	
    	if (index >= 0 && index < tree.getItemCount()) {
			TreeItem i = tree.getItem(index);
			if (i != null)
				return i.getData();
		}
		return null;
	}

	/**
     * Applies the next set of insertions into the middle of the queue.
     * 
     * @param maximumToInsert
     * @return
     */
    private int nextInsertionInMiddle(int maximumToInsert) {

        refreshQueues(new NullProgressMonitor());

        Collection result = SortUtil.removeFirst(insertionsInMiddle,
                maximumToInsert);

        synchronized (visibleItems) {
            visibleItems.addAll(result);
        }

        // We manually compute the insertion position because setting a sorter on 
        // the viewer would force a refresh, which can be very slow with a large number
        // of items.
        TreeViewer viewer = view.getViewer();
        viewer.add(view.getViewerInput(), result.toArray());

        return result.size();
    }

    /**
     * Applies the next insertion at the end of the table. Returns the number of
     * items actually inserted.
     * 
     * @param maximumToInsert maximum number of items to insert into the end
     * of the table
     * @return the number of items actually inserted into the table
     */
    private int nextInsertionAtEnd(int maximumToInsert) {
        refreshQueues(new NullProgressMonitor());

        List result = new ArrayList(maximumToInsert);

        Iterator iter = insertionsAtEnd.iterator();
        for (int counter = 0; counter < maximumToInsert && iter.hasNext(); counter++) {
            lastVisible = iter.next();

            result.add(lastVisible);

            iter.remove();
        }

        synchronized (visibleItems) {
            visibleItems.addAll(result);
        }

        view.getViewer().add(view.getViewerInput(),result.toArray());

        return result.size();
    }

    /**
     * Clears the set of visible items, and reinserts everything from scratch.
     */
    public void reset() {
        synchronized (visibleItems) {
            visibleItems.removeAll(pendingRemovals);

            insertionsInMiddle.addAll(visibleItems);
            lastVisible = null;
            lastDirty = true;

            visibleItems.clear();
        }
        pendingRemovals.clear();
        hasPendingChanges = true;
    }

    /**
     * Returns true iff there are pending changes remaining to be applied.
     * 
     * @return true iff there are pending changes to be applied to the table
     */
    public boolean hasPendingChanges() {
        return hasPendingChanges;
    }

    /**
     * Returns an estimate of the work remaining (the result is meaningful with respect
     * to the return value of nextUpdate())
     * 
     * @return an estimate of the work remaining
     */
    public int workRemaining() {
        return pendingRemovals.size() + insertionsAtEnd.size()
                + insertionsInMiddle.size() + pendingChanges.size();
    }

    /**
     * 
     * Cancels all pending insertions and removals.
     */
    public void cancelPending() {
        insertionsAtEnd.clear();
        insertionsInMiddle.clear();
        pendingRemovals.clear();
        hasPendingChanges = false;
    }

    public void setComparator(Comparator c) {
        if (sortOrder == c) {
            return;
        }

        sortOrder = c;

        lastVisible = null;

        insertionsInMiddle.addAll(insertionsAtEnd);
        insertionsAtEnd = newEndSet();
        lastDirty = true;

        reset();
    }

    /**
     * Applies any deferred sorting. 
     * @param mon
     */
    public void refreshQueues(IProgressMonitor mon) {
        if (lastDirty) {
            if (mon.isCanceled()) {
                return;
            }
            HashSet newInsertionsInMiddle = new HashSet();
            SortUtil.partition(newInsertionsInMiddle, insertionsAtEnd,
                    insertionsAtEnd, insertionsInMiddle, sortOrder,
                    lastVisible, mon);

            if (mon.isCanceled()) {
                insertionsInMiddle.removeAll(insertionsAtEnd);
            } else {
                insertionsInMiddle = newInsertionsInMiddle;
                lastDirty = false;
            }
        }
    }

    /**
     * Performs a single update to the viewer. Based on the contents of the pending* queues,
     * items will either be removed, added, or refreshed in the viewer (in that order). This
     * should only be called within a synchronized block, since the various queues shouldn't
     * be modified during an update. This method is invoked repeatedly by jobs to gradually
     * apply the pending changes.
     */
    public int nextUpdate() {

        int pendingRemovalsSize = pendingRemovals.size();

        if (pendingRemovalsSize > 0) {
            int currentSize = countVisibleItems();
            // Determine if we should remove incrementally or rebuild the table from scratch.
            int finalSize = currentSize - pendingRemovalsSize;

            if (finalSize * finalSize * 2 <= currentSize * currentSize) {

                // If we're removing enough items that it would be faster just to rebuild
                // the table from scratch, do it that way.

                reset();
                getViewer().refresh();

                return 0;
            }

            return nextRemoval(nextUpdateSize());
        } else if (insertionsInMiddle.size() > 0) {
            return nextInsertionInMiddle(nextUpdateSize());
        } else if (insertionsAtEnd.size() > 0) {
            return nextInsertionAtEnd(MAX_UPDATE);
        } else if (pendingChanges.size() > 0) {
            return nextChange(MAX_UPDATE);
        }

        hasPendingChanges = false;

        return 0;
    }

    /**
     * @return
     */
    int countVisibleItems() {
        synchronized (visibleItems) {
            return visibleItems.size();
        }
    }

    /**
     * Returns the number of items that should be added or removed in the next
     * incremental update. This is used for the operations whose runtime increases
     * with the size of the visible items.
     * 
     * @return the number of changes that should be applied in the next update
     */
    private int nextUpdateSize() {
        int size = GROWTH_LIMIT * (MAX_UPDATE - MIN_UPDATE)
                / (GROWTH_LIMIT + countVisibleItems()) + MIN_UPDATE;

        return size;
    }

    /**
     * Returns the TableViewer that is being populated.
     * 
     * @return the TableViewer that is being modified.
     */
    public TreeViewer getViewer() {
        return view.getViewer();
    }

    /**
     * Returns a new empty set to be used for insertionsAtEnd
     * 
     * @return
     */
    private Set newEndSet() {
        if (sortOrder == null) {
            return new HashSet();
        } else {
            return new TreeSet(sortOrder);
        }
    }

    /**
     * Removes all the given items from the target collection, and updates
     * the given progress monitor by the given amount. If the monitor is cancelled,
     * no changes are made to the target collection.
     *  
     * @param target items will be removed from this collection
     * @param toRemove items to be removed from the collection
     * @param mon progress monitor to be updated 
     * @param worked amount to update the monitor
     */
    private static void removeWithProgress(Collection target,
            Collection toRemove, IProgressMonitor mon, int worked) {
        if (mon.isCanceled()) {
            return;
        }

        target.removeAll(toRemove);

        mon.worked(worked);
    }

    /**
     * Inserts all the given items into the target collection, and updates
     * the progress monitor. If the monitor is cancelled, no changes will
     * be made to the target.
     * 
     * @param target collection into which items will be inserted
     * @param toInsert items to be inserted into the collection
     * @param mon progress monitor that will be updated
     * @param worked amount to update the progress monitor
     */
    private static void addWithProgress(Collection target, Collection toInsert,
            IProgressMonitor mon, int worked) {
        if (mon.isCanceled()) {
            return;
        }

        target.addAll(toInsert);

        mon.worked(worked);
    }

    /**
     * @return
     */
    public Comparator getSorter() {
        return sortOrder;
    }

	/**
	 * Return the view for the receiver.
	 * @return TableView
	 */
	public TableView getView() {
		return view;
	}

}
