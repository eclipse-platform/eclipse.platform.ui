/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.search;

import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.util.*;

/**
 * Manages indexing and search for all infosets
 */
public class SearchManager {
	/** Search indexes, by locale */
	private Map indexes = new HashMap();
	/** Caches analyzer descriptors for each locale */
	private Map analyzerDescriptors = new HashMap();
	/** Progress Distributors indexed by index */
	private Map progressDistibutors = new HashMap();

	/**
	 * Constructs a Search manager.
	 */
	public SearchManager() {
		super();
	}
	public SearchIndex getIndex(String locale) {
		synchronized (indexes) {
			Object index = indexes.get(locale);
			if (index == null) {
				index = new SearchIndex(locale, getAnalyzer(locale));
				indexes.put(locale, index);
			}
			return (SearchIndex) index;
		}
	}
	private ProgressDistributor getProgressDistributor(SearchIndex index) {
		synchronized (progressDistibutors) {
			Object distributor = progressDistibutors.get(index);
			if (distributor == null) {
				distributor = new ProgressDistributor();
				progressDistibutors.put(index, distributor);
			}
			return (ProgressDistributor) distributor;
		}
	}
	/**
	 * Obtains AnalyzerDescriptor that indexing and search should
	 * use for a given locale.
	 * @param locale 2 or 5 character locale representation
	 */
	private AnalyzerDescriptor getAnalyzer(String locale) {
		// get an analyzer from cache
		AnalyzerDescriptor analyzerDesc =
			(AnalyzerDescriptor) analyzerDescriptors.get(locale);
		if (analyzerDesc != null)
			return analyzerDesc;

		// obtain configured analyzer for this locale
		analyzerDesc = new AnalyzerDescriptor(locale);
		// save analyzer in the cache
		analyzerDescriptors.put(locale, analyzerDesc);
		String lang = analyzerDesc.getLang();
		if (locale != null && !locale.equals(lang))
			analyzerDescriptors.put(lang, analyzerDesc);

		return analyzerDesc;
	}

	/**
	 * Searches index for documents containing an expression.
	 */
	public void search(
		ISearchQuery searchQuery,
		ISearchHitCollector collector,
		IProgressMonitor pm) {
		SearchIndex index = getIndex(searchQuery.getLocale());
		try {
			updateIndex(pm, index);
			if (!index.exists()) {
				//no indexable documents, hence no index
				//or index is corrupted
				return;
			}
		} catch (IndexingOperation.IndexingException ie) {
			if (HelpPlugin.DEBUG_SEARCH) {
				System.out.println(
					this.getClass().getName()
						+ " IndexUpdateException occured.");
			}
		}
		index.search(searchQuery, collector);
	}
	/**
	 * Returns true when the index in the specified locale
	 * must be updated.
	 */
	private boolean isIndexingNeeded(SearchIndex index) {
		if (!index.exists()) {
			return true;
		}
		return index.getDocPlugins().detectChange();
	}
	/**
	 * Updates index.  Checks if all contributions were indexed.
	 * If not, it indexes them (Currently reindexes everything).
	 * @throws OperationCanceledException if indexing was cancelled
	 * @throws Exception if error occured
	 */
	public void updateIndex(IProgressMonitor pm, SearchIndex index)
		throws OperationCanceledException, IndexingOperation.IndexingException {
		ProgressDistributor progressDistrib = getProgressDistributor(index);
		progressDistrib.addMonitor(pm);
		try {
			synchronized (this) {
				if (!isIndexingNeeded(index)) {
					pm.beginTask("", 1);
					pm.worked(1);
					pm.done();
					progressDistrib.removeMonitor(pm);
					return;
				}
				if (HelpPlugin.DEBUG_SEARCH) {
					System.out.println(
						"SearchManager indexing " + index.getLocale());
				}
				// Perform indexing
				try {
					PluginVersionInfo versions = index.getDocPlugins();
					if (versions == null) {
						pm.beginTask("", 1);
						pm.worked(1);
						pm.done();
						progressDistrib.removeMonitor(pm);
						return;
					}
					IndexingOperation indexer = new IndexingOperation(index);
					indexer.execute(progressDistrib);
				} catch (OperationCanceledException oce) {
					progressDistrib.operationCanceled();
					HelpPlugin.logWarning(
						Resources.getString("Search_cancelled"));
					throw oce;
				}
			}
		} finally {
			progressDistrib.removeMonitor(pm);
		}
	}
	/**
	 * Closes all indexes.
	 */
	public void close() {
		for (Iterator it = indexes.values().iterator(); it.hasNext();) {
			((SearchIndex) it.next()).close();
		}
	}
}