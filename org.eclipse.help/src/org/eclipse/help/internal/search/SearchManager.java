/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.search;

import java.net.*;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.help.*;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.toc.Toc;
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
	private SearchIndex getIndex(String locale) {
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
		if (locale!=null && !locale.equals(lang)) 
			analyzerDescriptors.put(lang, analyzerDesc);

		return analyzerDesc;
	}

	/**
	 * Returns the documents to be added to index. 
	 * The collection consists of the associated PluginURL objects.
	 */
	private Collection getAddedDocuments(SearchIndex index) {
		// Get the list of added plugins
		Collection addedPlugins = index.getDocPlugins().getAdded();
		if (addedPlugins == null || addedPlugins.isEmpty())
			return new ArrayList(0);
		// get the list of all navigation urls. 
		Set urls = getAllDocuments(index.getLocale());
		ArrayList addedDocs = new ArrayList(urls.size());
		for (Iterator docs = urls.iterator(); docs.hasNext();) {
			String url = (String) docs.next();
			// only process documents that can be indexed
			if (!isIndexable(url))
				continue;
			// Assume the url is /pluginID/path_to_topic.html
			int i = url.indexOf('/', 1);
			String plugin = i == -1 ? "" : url.substring(1, i);
			if (addedPlugins.contains(plugin))
				try {
					addedDocs.add(new URL("help:" + url + "?lang=" + index.getLocale()));
				} catch (MalformedURLException mue) {
				}
		}
		return addedDocs;
	}
	/**
	 * Returns the documents to be removed from index. 
	 * The collection consists of the associated PluginURL objects.
	 */
	private Collection getRemovedDocuments(SearchIndex index) {
		// Get the list of removed plugins
		Collection removedPlugins = index.getDocPlugins().getRemoved();
		if (removedPlugins == null || removedPlugins.isEmpty())
			return new ArrayList(0);
		// get the list of indexed docs. This is a hashtable  (url, plugin)
		HelpProperties indexedDocs = index.getIndexedDocs();
		ArrayList removedDocs = new ArrayList(indexedDocs.size());
		for (Iterator docs = indexedDocs.keySet().iterator(); docs.hasNext();) {
			String url = (String) docs.next();
			// Assume the url is /pluginID/path_to_topic.html
			int i = url.indexOf('/', 1);
			String plugin = i == -1 ? "" : url.substring(1, i);
			if (removedPlugins.contains(plugin))
				try {
					removedDocs.add(new URL("help:" + url + "?lang=" + index.getLocale()));
				} catch (MalformedURLException mue) {
				}
		}
		return removedDocs;
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
		} catch (IndexingOperation.IndexingException ie) {
			Logger.logDebugMessage(
				this.getClass().getName(),
				"IndexUpdateException occured.");
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
	private void updateIndex(IProgressMonitor pm, SearchIndex index)
		throws OperationCanceledException, IndexingOperation.IndexingException {
		ProgressDistributor progressDistrib = getProgressDistributor(index);
		progressDistrib.addMonitor(pm);
		synchronized (this) {
			if (!isIndexingNeeded(index)) {
				pm.beginTask("", 1);
				pm.worked(1);
				pm.done();
				return;
			}
			if (Logger.DEBUG)
				Logger.logDebugMessage("Search Manager", "indexing " + index.getLocale());
			// Perform indexing
			try {
				PluginVersionInfo versions = index.getDocPlugins();
				if (versions == null)
					return;
				Collection removedDocs = getRemovedDocuments(index);
				Collection addedDocs = getAddedDocuments(index);
				IndexingOperation indexer =
					new IndexingOperation(index, removedDocs, addedDocs);
				indexer.execute(progressDistrib);
			} catch (OperationCanceledException oce) {
				Logger.logWarning(Resources.getString("Search_cancelled"));
				throw oce;
			}
		}
		progressDistrib.removeMonitor(pm);
	}
	private boolean isIndexable(String url) {
		String fileName = url.toLowerCase();
		return fileName.endsWith(".htm")
			|| fileName.endsWith(".html")
			|| fileName.endsWith(".txt")
			|| fileName.endsWith(".xml");
	}
	/**
	 * Returns the collection of href's for all the help topics.
	 */
	private Set getAllDocuments(String locale) {
		HashSet hrefs = new HashSet();
		IToc[] tocs = HelpSystem.getTocManager().getTocs(locale);
		for (int i = 0; i < tocs.length; i++) {
			ITopic[] topics = tocs[i].getTopics();
			for (int j = 0; j < topics.length; j++) {
				add(topics[j], hrefs);
			}
			if (tocs[i] instanceof Toc) {
				topics = ((Toc) tocs[i]).getExtraTopics();
				for (int j = 0; j < topics.length; j++) {
					add(topics[j], hrefs);
				}
			}
		}
		return hrefs;
	}
	/**
	 * Adds the topic and its subtopics to the list of documents
	 */
	private void add(ITopic topic, Set hrefs) {
		String href = topic.getHref();
		if (href != null && !href.equals("") && !href.startsWith("http://"))
			hrefs.add(href);
		ITopic[] subtopics = topic.getSubtopics();
		for (int i = 0; i < subtopics.length; i++)
			add(subtopics[i], hrefs);
	}
}