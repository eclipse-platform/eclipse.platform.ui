/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search2.internal.ui.basic.views;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.search.ui.ISearchResultChangedListener;
import org.eclipse.search.ui.SearchJobEvent;
import org.eclipse.search.ui.SearchResultEvent;
import org.eclipse.search.ui.text.IStructureProvider;
import org.eclipse.search.ui.text.ITextSearchResult;

import org.eclipse.search2.internal.ui.text.MatchEvent;
import org.eclipse.search2.internal.ui.text.RemoveAllEvent;


/**
 * @author Thomas Mäder
 *  
 */
public abstract class SearchResultModel implements Runnable, ISearchResultChangedListener {

	protected static final Object[] EMPTY_ARRAY= new Object[0];
	
	protected ITextSearchResult fResult;
	protected DefaultSearchViewPage fPage;

	// batched update handling
	private Set fBatchedInserts;
	private Set fBatchedRemoves;
	private boolean fIsRunning;
	private boolean fIsAsyncRunning;
	private boolean fAsyncResponse;
	
	// revealing the first match.
	private boolean fShouldRevealFirst= false;
	
	SearchResultModel(ITextSearchResult result, DefaultSearchViewPage page) {
		super();
		fResult= result;
		fPage= page;
		fResult.addListener(this);
		fBatchedInserts= new HashSet();
		fBatchedRemoves= new HashSet();
		fIsRunning= true;
		Thread t= new Thread(this);
		t.start();
	}

	public void dispose() {
		synchronized(this) {
			fIsRunning= false;
			notifyAll();
		}
		fResult.removeListener(this);
		fPage= null;
		fResult= null;
	}

	ITextSearchResult getResult() {
		return fResult;
	}
	
	public abstract Object[] getChildren(Object parent);


	protected final synchronized void addBatchedInsert(Object element) {
		fBatchedInserts.add(element);
		notifyAll();
	}

	protected final synchronized void addBatchedRemove(Object element) {
		fBatchedRemoves.add(element);
		notifyAll();
	}

	// must be called on the UI thread.
	protected final synchronized void doFlushBatched() {
		fIsAsyncRunning= true;
		notifyAll();
		while (!fAsyncResponse) {
			try {
				wait();
			} catch (InterruptedException e) {
				// ignore
			}
		}
		try {
			//System.out.println("flushing: "+(fBatchedInserts.size() + fBatchedRemoves.size()));
			if (fPage == null)
				return;
			IStructureProvider structureProvider= fResult.getStructureProvider();
			for (Iterator added = fBatchedInserts.iterator(); added.hasNext();) {
				insert(structureProvider, added.next(), true);
			}
			fBatchedInserts.clear();
			
			for (Iterator removed = fBatchedRemoves.iterator(); removed.hasNext();) {
				remove(structureProvider, removed.next(), true);
			}
			fBatchedRemoves.clear();
		} finally {
			fIsAsyncRunning= false;
			notifyAll();
		}
		if (fShouldRevealFirst) {
			fShouldRevealFirst= false;
			fPage.navigateNext(true);
		}
	}
	
	/**
	 * @param structureProvider
	 * @param object
	 * @param b
	 */
	protected abstract void remove(IStructureProvider structureProvider, Object element, boolean refreshViewer);

	/**
	 * @param structureProvider
	 * @param object
	 * @param b
	 */
	protected abstract void insert(IStructureProvider structureProvider, Object element, boolean refreshViewer);

	protected final synchronized void flushBatched() {
		if (fPage == null)
			return;
		fPage.getControl().getDisplay().asyncExec(new Runnable() {
			public void run() {
				doFlushBatched();
			}

		});
		try {
			fAsyncResponse= false;
			notifyAll();
			while (!fIsAsyncRunning) {
				wait();
			}
			fAsyncResponse= true;
			notifyAll();
			while (fIsAsyncRunning) {
				wait();
			}
		} catch (InterruptedException e) {
			// ignore
		}
	}

	public final void run() {
		fIsAsyncRunning= false;
		while (fIsRunning) {
			synchronized(this) {
				if (fBatchedInserts.size() > 0 || fBatchedRemoves.size() > 0) {
					flushBatched();
				}
			}
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				// ignore
			}
		}
	}

	public final void searchResultsChanged(SearchResultEvent e) {
		if (e instanceof MatchEvent) {
			MatchEvent evt= (MatchEvent) e;
			final Object element= evt.getMatch().getElement();
			if (evt.getKind() == MatchEvent.ADDED) {
				if (fResult.getMatchCount() == 1) {
					fShouldRevealFirst= true;
				}
				addBatchedInsert(element);
			} else if (evt.getKind() == MatchEvent.REMOVED) {
				addBatchedRemove(element);
			}
		} else if (e instanceof RemoveAllEvent) {
			fPage.getControl().getDisplay().asyncExec(new Runnable() {
				public void run() {
					if (fPage != null) {
						clear();
					}
				}

	
			});
		} else if (e instanceof SearchJobEvent)
			flushBatched();
	}
	
	protected void clear() {
		fPage.refresh();
	}
	
}
