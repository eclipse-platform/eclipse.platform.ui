/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.search2.internal.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.search.ui.IQueryListener;
import org.eclipse.search.ui.ISearchQuery;

class QueryManager {
	private List<ISearchQuery> fQueries;
	private List<IQueryListener> fListeners;

	public QueryManager() {
		super();
		// an ArrayList should be plenty fast enough (few searches).
		fListeners= new ArrayList<>();
		fQueries= new LinkedList<>();
	}

	public boolean hasQueries() {
		synchronized (this) {
			return !fQueries.isEmpty();
		}
	}

	public int getSize() {
		synchronized (this) {
			return fQueries.size();
		}
	}

	/**
	 * Returns the queries in LRU order. Smaller index means more recently used.
	 *
	 * @return all queries
	 */
	public ISearchQuery[] getQueries() {
		synchronized (this) {
			return fQueries.toArray(new ISearchQuery[fQueries.size()]);
		}
	}

	public void removeQuery(ISearchQuery query) {
		synchronized (this) {
			fQueries.remove(query);
		}
		fireRemoved(query);
	}

	public void addQuery(ISearchQuery query) {
		synchronized (this) {
			if (fQueries.contains(query))
				return;
			fQueries.add(0, query);
		}
		fireAdded(query);
	}

	public void addQueryListener(IQueryListener l) {
		synchronized (fListeners) {
			fListeners.add(l);
		}
	}

	public void removeQueryListener(IQueryListener l) {
		synchronized (fListeners) {
			fListeners.remove(l);
		}
	}

	public void fireAdded(ISearchQuery query) {
		Set<IQueryListener> copiedListeners= new HashSet<>();
		synchronized (fListeners) {
			copiedListeners.addAll(fListeners);
		}
		Iterator<IQueryListener> listeners= copiedListeners.iterator();
		while (listeners.hasNext()) {
			IQueryListener l= listeners.next();
			l.queryAdded(query);
		}
	}

	public void fireRemoved(ISearchQuery query) {
		Set<IQueryListener> copiedListeners= new HashSet<>();
		synchronized (fListeners) {
			copiedListeners.addAll(fListeners);
		}
		Iterator<IQueryListener> listeners= copiedListeners.iterator();
		while (listeners.hasNext()) {
			IQueryListener l= listeners.next();
			l.queryRemoved(query);
		}
	}

	public void fireStarting(ISearchQuery query) {
		Set<IQueryListener> copiedListeners= new HashSet<>();
		synchronized (fListeners) {
			copiedListeners.addAll(fListeners);
		}
		Iterator<IQueryListener> listeners= copiedListeners.iterator();
		while (listeners.hasNext()) {
			IQueryListener l= listeners.next();
			l.queryStarting(query);
		}
	}

	public void fireFinished(ISearchQuery query) {
		Set<IQueryListener> copiedListeners= new HashSet<>();
		synchronized (fListeners) {
			copiedListeners.addAll(fListeners);
		}
		Iterator<IQueryListener> listeners= copiedListeners.iterator();
		while (listeners.hasNext()) {
			IQueryListener l= listeners.next();
			l.queryFinished(query);
		}
	}

	public void removeAll() {
		synchronized (this) {
			List<ISearchQuery> old= fQueries;
			fQueries= new LinkedList<>();
			Iterator<ISearchQuery> iter= old.iterator();
			while (iter.hasNext()) {
				ISearchQuery element= iter.next();
				fireRemoved(element);
			}
		}
	}

	public void queryFinished(ISearchQuery query) {
		fireFinished(query);
	}

	public void queryStarting(ISearchQuery query) {
		fireStarting(query);
	}

	public void touch(ISearchQuery query) {
		synchronized (this) {
			if (fQueries.contains(query)) {
				fQueries.remove(query);
				fQueries.add(0, query);
			}
		}
	}

}
