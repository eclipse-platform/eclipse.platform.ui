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
package org.eclipse.help.internal.search;

import java.nio.channels.*;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.base.*;
import org.eclipse.help.internal.search.IndexingOperation.*;

/**
 * Manages indexing and search for all infosets
 */
public class SearchManager implements ITocsChangedListener {
	/** Search indexes, by locale */
	private Map indexes = new HashMap();
	/** Caches analyzer descriptors for each locale */
	private Map analyzerDescriptors = new HashMap();
	/**
	 * Constructs a Search manager.
	 */
	public SearchManager() {
		super();
		HelpPlugin.getDefault().addTocsChangedListener(this);
	}
	/**
	 * Public for use by indexing tool
	 */
	public SearchIndexWithIndexingProgress getIndex(String locale) {
		synchronized (indexes) {
			Object index = indexes.get(locale);
			if (index == null) {
				index = new SearchIndexWithIndexingProgress(locale,
						getAnalyzer(locale), HelpPlugin.getTocManager());
				indexes.put(locale, index);
			}
			return (SearchIndexWithIndexingProgress) index;
		}
	}
	/**
	 * Obtains AnalyzerDescriptor that indexing and search should use for a
	 * given locale.
	 * 
	 * @param locale
	 *            2 or 5 character locale representation
	 */
	private AnalyzerDescriptor getAnalyzer(String locale) {
		// get an analyzer from cache
		AnalyzerDescriptor analyzerDesc = (AnalyzerDescriptor) analyzerDescriptors
				.get(locale);
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
	public void search(ISearchQuery searchQuery, ISearchHitCollector collector,
			IProgressMonitor pm) throws QueryTooComplexException {

		SearchIndexWithIndexingProgress index = getIndex(searchQuery
				.getLocale());
		try {
			ensureIndexUpdated(pm, index);
			if (!index.exists()) {
				//no indexable documents, hence no index
				//or index is corrupted
				return;
			}
		} catch (IndexingOperation.IndexingException ie) {
			if (HelpBasePlugin.DEBUG_SEARCH) {
				System.out.println(this.getClass().getName()
						+ " IndexUpdateException occurred."); //$NON-NLS-1$
			}
		}
		index.search(searchQuery, collector);
	}

	/**
	 * Updates index. Checks if all contributions were indexed. If not, it
	 * indexes them.
	 * 
	 * @throws OperationCanceledException
	 *             if indexing was cancelled
	 */
	public void ensureIndexUpdated(IProgressMonitor pm,
			SearchIndexWithIndexingProgress index)
			throws OperationCanceledException,
			IndexingOperation.IndexingException {

		ProgressDistributor progressDistrib = index.getProgressDistributor();
		progressDistrib.addMonitor(pm);
		boolean configurationLocked = false;
		try {
			// Prevent two workbench or stand-alone help instances from updating
			// index concurently. Lock is created for every search request, so
			// do not use it in infocenter, for performance (administrator will
			// need to ensure index is updated before launching another
			// infocenter instance on the same configuration).
			if (BaseHelpSystem.MODE_INFOCENTER != BaseHelpSystem.getMode()) {
				try {
					configurationLocked = index.tryLock();
					if (!configurationLocked) {
						// Index is being updated by another proces
						// do not update or wait, just continue with search
						pm.beginTask("", 1); //$NON-NLS-1$
						pm.worked(1);
						pm.done();
						return;
					}
				} catch (OverlappingFileLockException ofle) {
					// Another thread in this process is indexing and using the
					// lock
				}
			}
			// Only one index update occurs in VM at a time,
			// but progress SearchProgressMonitor for other locales
			// are waiting until we know if indexing is needed
			// to prevent showing progress on first search after launch
			// if no indexing is needed
			if (index.isClosed() || !index.needsUpdating()) {
				// very good, can search
				pm.beginTask("", 1); //$NON-NLS-1$
				pm.worked(1);
				pm.done();
				return;
			} else {
				if (pm instanceof SearchProgressMonitor) {
					((SearchProgressMonitor) pm).started();
				}
			}
			//
			updateIndex(pm, index, progressDistrib);
		} finally {
			progressDistrib.removeMonitor(pm);
			if (configurationLocked) {
				index.releaseLock();
			}
		}
	}
	/**
	 * @param pm
	 * @param index
	 * @param progressDistrib
	 * @throws IndexingException
	 */
	private synchronized void updateIndex(IProgressMonitor pm,
			SearchIndex index, ProgressDistributor progressDistrib)
			throws IndexingException {
		if (index.isClosed() || !index.needsUpdating()) {
			pm.beginTask("", 1); //$NON-NLS-1$
			pm.worked(1);
			pm.done();
			return;
		}
		if (HelpBasePlugin.DEBUG_SEARCH) {
			System.out.println("SearchManager indexing " + index.getLocale()); //$NON-NLS-1$
		}
		// Perform indexing
		try {
			PluginVersionInfo versions = index.getDocPlugins();
			if (versions == null) {
				pm.beginTask("", 1); //$NON-NLS-1$
				pm.worked(1);
				pm.done();
				return;
			}
			IndexingOperation indexer = new IndexingOperation(index);
			indexer.execute(progressDistrib);
			return;
		} catch (OperationCanceledException oce) {
			progressDistrib.operationCanceled();
			HelpBasePlugin.logWarning(HelpBaseResources
					.getString("Search_cancelled")); //$NON-NLS-1$
			throw oce;
		}
	}
	/**
	 * Closes all indexes.
	 */
	public void close() {
		synchronized (indexes) {
			for (Iterator it = indexes.values().iterator(); it.hasNext();) {
				((SearchIndex) it.next()).close();
			}
		}
	}
	public synchronized void tocsChanged() {
		Collection activeIndexes = new ArrayList();
		synchronized (indexes) {
			activeIndexes.addAll(indexes.values());
		}
		for (Iterator it = activeIndexes.iterator(); it.hasNext();) {
			SearchIndexWithIndexingProgress ix = (SearchIndexWithIndexingProgress) it
					.next();
			ix.close();
			synchronized (indexes) {
				indexes.remove(ix.getLocale());
				ProgressDistributor pm = ix.getProgressDistributor();
				pm.beginTask("", 1); //$NON-NLS-1$
				pm.worked(1);
				pm.done();
				SearchProgressMonitor.reinit(ix.getLocale());
			}
		}

	}
}