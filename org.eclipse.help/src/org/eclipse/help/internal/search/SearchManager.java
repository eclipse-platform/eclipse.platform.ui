package org.eclipse.help.internal.search;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.net.*;
import java.util.*;
import org.eclipse.core.runtime.*;
import org.eclipse.help.*;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.util.*;
/**
 * Manages indexing and search for all infosets
 */
public class SearchManager implements ISearchEngine {
	private HashMap indexes = new HashMap(/* of Index by locale */
	);
	/**
	 * Constructs a Search manager.
	 */
	public SearchManager() {
		super();
	}
	public synchronized int[][] getHighlightInfo(
		String locale,
		byte[] docBuffer,
		int docNumber) {
		return getIndex(locale).getHighlightInfo(docBuffer, docNumber);
	}
	public SearchIndex getIndex(String locale) {
		SearchIndex index = (SearchIndex) indexes.get(locale);
		if (index == null) {
			index = new SearchIndex(locale);
			indexes.put(locale, index);
		}
		return index;
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
		Set urls = getAllDocuments();
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
	 * Obtains locale from query string
	 * @return locale in the form ll_CC
	 * @param query String
	 */
	private static String getLocale(String query) {
		String locale = "en_US";
		int indexNumberOfLang = query.indexOf("&lang=");
		if (query.charAt(indexNumberOfLang + 8) == '_') {
			locale = query.substring(indexNumberOfLang + 6, indexNumberOfLang + 11);
		} else {
			locale = query.substring(indexNumberOfLang + 6, indexNumberOfLang + 7);
		}
		return locale;
	}
	/**
	 * Searches index for documents containing an expression.
	 * If the index hasn't been built then return null.
	 */
	public synchronized String getSearchResults(String query) {
		String locale = getLocale(query);
		ISearchIndex index = getIndex(locale);
		if (index == null)
			return null;
		SearchQuery searchQuery = new SearchQuery(query);
		SearchResult result = searchQuery.search(index);
		// TO DO: Filtering...
		///result.filterTopicsFromExcludedCategories(searchQuery.getExcludedCategories());
		if (Logger.DEBUG)
			Logger.logDebugMessage(
				"Search Manager",
				"search results:\n" + result.toString());
		return result.toString();
	}
	/**
	 * Returns true when the index for this infosetId in the specified locale
	 * must be (re)built.
	 * Side effect: if a pre-built index exists, the first time we call this
	 * function, we will unzip the pre-built index and check if there are
	 * more plugins with docs to index.
	 */
	public boolean isIndexingNeeded(String locale) {
		// If there is an index, check the plugin differences.
		// If there is no index, copy the pre-built index, if any
		SearchIndex index = getIndex(locale);
		if (index.exists())
			return index.getDocPlugins().detectChange();
		else {
			// find a possible prebuilt index 
			PrebuiltIndex prebuiltIndex = new PrebuiltIndex(locale);
			if (prebuiltIndex.exists())
				if (prebuiltIndex.install())
					return index.getDocPlugins().detectChange();
			// in all other cases we need to index
			return true;
		}
	}
	/**
	 * Updates index.  Checks if all contributions were indexed.
	 * If not, it indexes them (Currently reindexes everything).
	 * @throws OperationCanceledException if indexing was cancelled
	 * @throws Exception if error occured
	 */
	public synchronized void updateIndex(IProgressMonitor pm, String locale)
		throws OperationCanceledException, Exception {
		// NOTE: If any infosetId was deleted, or upgraded
		// we recreate the whole index.
		if (!isIndexingNeeded(locale))
			return;
		SearchIndex index = getIndex(locale);
		if (Logger.DEBUG)
			Logger.logDebugMessage("Search Manager", "indexing " + locale);
		// Perform indexing
		try {
			PluginVersionInfo versions = index.getDocPlugins();
			if (versions == null)
				return;
			Collection removedDocs = getRemovedDocuments(index);
			Collection addedDocs = getAddedDocuments(index);
			IndexingOperation indexer =
				new IndexingOperation(index, removedDocs, addedDocs);
			indexer.execute(pm);
		} catch (OperationCanceledException oce) {
			Logger.logWarning(Resources.getString("Search_cancelled"));
			throw oce;
		} finally {
		}
	}
	private boolean isIndexable(String url) {
		String fileName = url.toLowerCase();
		return fileName.endsWith(".htm")
			|| fileName.endsWith(".html")
			|| fileName.endsWith(".txt")
			|| fileName.endsWith(".xml");
	}
	/**
	 * Returns the collection of href's of all the help topics.
	 */
	private Set getAllDocuments() {
		HashSet hrefs = new HashSet();
		IToc[] tocs = HelpSystem.getTocManager().getTocs();
		for (int i = 0; i < tocs.length; i++) {
			ITopic[] topics = tocs[i].getTopics();
			for (int j = 0; j < topics.length; j++)
				add(topics[j], hrefs);
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