/*******************************************************************************
 * Copyright (c) 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.quicksearch.internal.core;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.text.quicksearch.internal.core.pathmatch.ResourceMatcher;
import org.eclipse.text.quicksearch.internal.core.pathmatch.ResourceMatchers;
import org.eclipse.text.quicksearch.internal.core.priority.PriorityFunction;
import org.eclipse.text.quicksearch.internal.ui.Messages;
import org.eclipse.text.quicksearch.internal.util.LightSchedulingRule;
import org.eclipse.text.quicksearch.internal.util.LineReader;

public class QuickTextSearcher {
	private static int MAX_BUFFER_LENGTH = 999_999; // read max 1MB bytes => max 2MB chars.
	private final QuickTextSearchRequestor requestor;
	private QuickTextQuery query;

	/**
	 * Keeps track of currently found matches. Items are added as they are found and may also
	 * be removed when the query changed and they become invalid.
	 */
	private final Set<LineItem> matches = ConcurrentHashMap.newKeySet(2000);

	/**
	 * Scheduling rule used by Jobs that work on the matches collection.
	 */
	private final ISchedulingRule matchesRule = new LightSchedulingRule("QuickSearchMatchesRule"); //$NON-NLS-1$

	private final SearchInFilesWalker walker;
	private IncrementalUpdateJob incrementalUpdate;

	/**
	 * This field gets set to request a query change. The new query isn't stuffed directly
	 * into the query field because the query is responded to by the updater job which needs
	 * access to both the original query and the newQuery to decide on an efficient strategy.
	 */
	private QuickTextQuery newQuery;

	public static final int DEFAULT_MAX_RESULTS = 200;
	/**
	 * If number of accumulated results reaches maxResults the search will be suspended.
	 * <p>
	 * Note that more results may still arrive beyond the limit since the searcher does not (yet) have the
	 * capability to suspend/resume a search in the middle of a file.
	 */
	private int maxResults = DEFAULT_MAX_RESULTS;

	/**
	 * If a line of text is encountered longer than this, the searcher will stop searching
	 * that file (this rule avoids searching machine generated text files, like minified javascript).
	 */
	private int MAX_LINE_LEN;

	/**
	 * While searching in a file, this field will be set. This can be used to show the name
	 * of the 'current file' in the progress area of the quicksearch dialog.
	 */
	private volatile IFile currentFile = null;
	public volatile int searchTookMs;

	/**
	 * Flag to disable incremental filtering logic based on incremental
	 * query updates. This forces a full refresh of the search results.
	 */
	private boolean forceRefresh = false;
	private ResourceMatcher pathMatcher = ResourceMatchers.ANY;

	/**
	 * Retrieves the current result limit.
	 */
	public int getMaxResults() {
		return maxResults;
	}

	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}

	public QuickTextSearcher(QuickTextQuery query, PriorityFunction priorities, int maxLineLen, QuickTextSearchRequestor requestor) {
		this.MAX_LINE_LEN = maxLineLen;
		this.requestor = requestor;
		this.query = query;
		this.walker = createWalker(new PriorityFunction() {
			@Override
			public double priority(IResource r) {
				double basePriority = priorities.priority(r);
				if (basePriority==PRIORITY_IGNORE) {
					return basePriority;
				}
				if (r.getType()==IResource.FILE && !pathMatcher.matches(r)) {
					return PRIORITY_IGNORE;
				}
				return basePriority;
			}
		});
	}

	private SearchInFilesWalker createWalker(PriorityFunction priorities) {
		final SearchInFilesWalker job = new SearchInFilesWalker();
		job.setPriorityFun(priorities);
		job.setRule(matchesRule);
		job.schedule();
		return job;
	}

	private final class SearchInFilesWalker extends ResourceWalker {

		@Override
		public IStatus run(IProgressMonitor monitor) {
			searchTookMs = 0;
			long n0 = System.nanoTime();
			try {
				return super.run(monitor);
			} finally {
				currentFile = null;
				long n1 = System.nanoTime();
				if (matches.isEmpty()) {
					searchTookMs = (int) ((n1 - n0) / 1_000_000);
				}
			}
		}

		@Override
		protected boolean searchIn(IFile f, BooleanSupplier canceled) {
			currentFile = f;
			return search(f, canceled, MAX_LINE_LEN, query.pattern, QuickTextSearcher.this::add);
		}

		private static boolean search(IFile f, BooleanSupplier canceled,
				int maxLineLength, Pattern pattern, Consumer<LineItem> add) {
			if (canceled.getAsBoolean()) {
				return false;
			}
			try (LineReader lr = new LineReader(getReader(f),
					maxLineLength)) {
				String line;
				int lineIndex = 1;
				while ((line = lr.readLine()) != null) {
					int offset = lr.getLastLineOffset();
					if (canceled.getAsBoolean()) {
						return false;
					}

					Matcher matcher = pattern.matcher(line);
					if (matcher.find()) {
						LineItem lineItem = new LineItem(f, line, lineIndex, offset);
						add.accept(lineItem);
					}

					lineIndex++;
				}
			} catch (Exception e) {
				// ignored
			}
			return true;
		}

		private static Reader getReader(IFile f) throws UnsupportedEncodingException, CoreException {
			String shortString = toShortString(f);
			if (shortString != null) {
				return new StringReader(shortString);
			} else {
				return new InputStreamReader(f.getContents(true), f.getCharset());
			}
		}

		@Override
		public void resume() {
			//Only resume if we don't already exceed the maxResult limit.
			if (isActive()) {
				super.resume();
			}
		}

	}
	/**
	 * Try to get a content as String. Avoids Streaming.
	 */
	private static String toShortString(IFile file) {
		/**
		 * Just any number such that the most source files will fit in. And not too
		 * big to avoid out of memory.
		 **/
		try {
			byte[] content = file.readNBytes(MAX_BUFFER_LENGTH);
			int length = content.length;
			if (length >= MAX_BUFFER_LENGTH) {
				return null;
			}
			String charset = file.getCharset();
			return new String(content, charset);
		} catch (Exception e) {
			return null;
		}
	}
	/**
	 * This job updates already found matches when the query is changed.
	 * Both the walker job and this job share the same scheduling rule so
	 * only one of them can be executing at the same time.
	 * <p>
	 * This is to avoid problems with concurrent modification of the
	 * matches collection.
	 */
	private class IncrementalUpdateJob extends Job {
		public IncrementalUpdateJob() {
			super(Messages.QuickTextSearch_updateMatchesJob);
			this.setRule(matchesRule);
			//This job isn't started automatically. It should be schedule every time
			// there's a 'newQuery' set by the user/client.
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			QuickTextQuery nq = newQuery; //Copy into local variable to avoid
										  // problems if another thread changes newQuery while we
										  // are still mucking with it.
			if (!forceRefresh && query.isSubFilter(nq)) {
				query = nq;
				performIncrementalUpdate(monitor);
			} else {
				query = nq;
				forceRefresh = false;
				if (!monitor.isCanceled()) { // avoid restart if dialog got closed
					performRestart();
				}
			}
			return monitor.isCanceled() ? Status.CANCEL_STATUS : Status.OK_STATUS;
		}

		private void performIncrementalUpdate(IProgressMonitor mon) {
			Iterator<LineItem> items = matches.iterator();
			while (items.hasNext() && !mon.isCanceled()) {

				LineItem item = items.next();
				Matcher matcher = query.pattern.matcher(item.getText());
				if (matcher.find()) {
					//Match still valid but may need updating highlighted text in the UI:
					requestor.update(item);
				} else {
					items.remove();
					requestor.revoke(item);
				}
			}
			if (!mon.isCanceled()) {
				//Resume searching remaining files, if any.
				walker.resume();
			}
		}

		private void performRestart() {
			//since we are inside Job here that uses same scheduling rule as walker, we
			//know walker is not currently executing. so walker cancel should be instantenous
			matches.clear();
			requestor.clear();
			walker.cancel();
			if (!query.isTrivial()) {
				walker.init(); //Reinitialize the walker work queue to its starting state
				walker.resume(); //Allow walker to resume when we release the scheduling rule.
			} else {
				walker.stop();
			}
		}

	}

	private void add(LineItem line) {
		if (!isActive()) {
			walker.suspend();
		} else if (matches.add(line)) {
			requestor.add(line);
		}
	}

	public void setQuery(QuickTextQuery newQuery, boolean force) {
		if (newQuery.equalsFilter(query) && !force) {
			return;
		}
		this.newQuery = newQuery;
		this.forceRefresh = force;
		scheduleIncrementalUpdate();
	}

	public void setPathMatcher(ResourceMatcher pathMatcher) {
		if (this.pathMatcher.equals(pathMatcher)) {
			return;
		}
		this.pathMatcher = pathMatcher;
		setQuery(query, true);
	}

	public QuickTextQuery getQuery() {
		//We return the newQuery as soon as it was set, even if it has not yet been effectively applied
		// to previously found query results. Most logical since when you call 'setQuery' you would
		// expect 'getQuery' to return the query you just set.
		return newQuery!=null ? newQuery : query;
	}

	private synchronized void scheduleIncrementalUpdate() {
		walker.suspend(); //The walker must be suspended so the update job can run, they share scheduling rule
		 // so only one job can run at any time.

		//Any outstanding incremental update should be canceled since the query has changed again.
		if (incrementalUpdate!=null) {
			incrementalUpdate.cancel();
		}
		incrementalUpdate = new IncrementalUpdateJob();
		incrementalUpdate.schedule();
	}

	public boolean isActive() {
		// Information for the job showing 'Searching ...' if we are still searching.
		// The walker can be suspended for different reasons, not all of them count as
		// inactive. The main situation where the walker is suspended and interpreted as
		// inactive is when the max number of results are reached.
		return !isDone() && matches.size() < maxResults;
	}

	public boolean isDone() {
		//Walker can be null if job was canceled because dialog closed. But stuff like
		//the job that shows 'Searching ...' doesn't instantly stop and may still
		//be asking the incremental update job whether its done.
		return /*(incrementalUpdate != null && incrementalUpdate.getState() != Job.NONE) ||*/ walker.isDone();
	}

	public void cancel() {
		walker.cancel();
		if (incrementalUpdate instanceof IncrementalUpdateJob update) {
			update.cancel();
		}
	}

	public IFile getCurrentFile() {
		return currentFile;
	}

}
