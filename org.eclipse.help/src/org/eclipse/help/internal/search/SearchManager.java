package org.eclipse.help.internal.search;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.net.*;
import java.util.*;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.eclipse.core.runtime.*;
import org.eclipse.help.*;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.util.*;
/**
 * Manages indexing and search for all infosets
 */
public class SearchManager {
	// Search indexes, indexed (no pun!) by locale
	private HashMap indexes = new HashMap();
	// Caches analyzers for each locale
	private HashMap analyzers = new HashMap();
	private final Analyzer defaultAnalyzer = new StandardAnalyzer();
	// Progress monitors, indexed by locale
	private HashMap progressMonitors = new HashMap();
	/**
	 * Constructs a Search manager.
	 */
	public SearchManager() {
		super();
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
	 * Obtains Analyzer that indexing and search should
	 * use for a given locale.
	 * @param locale 2 or 5 character locale representation
	 */
	public Analyzer getAnalyzer(String locale) {
		// get an analyzer from cache
		Analyzer analyzer = (Analyzer) analyzers.get(locale);
		if (analyzer != null)
			return analyzer;
		// obtain configured analyzer for this locale
		analyzer = createAnalyzer(locale);
		if (analyzer != null) {
			// save analyzer in the cache
			analyzers.put(locale, analyzer);
			return analyzer;
		}
		// obtains configured analyzer for the language only
		String language = null;
		if (locale.length() > 2) {
			language = locale.substring(0, 2);
			analyzer = createAnalyzer(language);
			if (analyzer != null) {
				// save analyzer in the cache
				analyzers.put(language, analyzer);
				analyzers.put(locale, analyzer);
				return analyzer;
			}
		}
		// create default analyzer
		analyzer = defaultAnalyzer;
		analyzers.put(locale, analyzer);
		if (language != null)
			analyzers.put(language, analyzer);
		return analyzer;
	}
	/**
	 * Creates analyzer for a locale, 
	 * if it is configured in the org.eclipse.help.luceneAnalyzer
	 * extension point.
	 * @return Analyzer or null if no analyzer is configured
	 * for given locale.
	 */
	public Analyzer createAnalyzer(String locale) {
		Collection contributions = new ArrayList();
		// find extension point
		IConfigurationElement configElements[] =
			Platform.getPluginRegistry().getConfigurationElementsFor(
				"org.eclipse.help",
				"luceneAnalyzer");
		for (int i = 0; i < configElements.length; i++) {
			if (!configElements[i].getName().equals("analyzer"))
				continue;
			String analyzerLocale = configElements[i].getAttribute("locale");
			if (analyzerLocale == null || !analyzerLocale.equals(locale))
				continue;
			try {
				Object analyzer = configElements[i].createExecutableExtension("class");
				if (!(analyzer instanceof Analyzer))
					continue;
				return (Analyzer) analyzer;
			} catch (CoreException ce) {
				Logger.logError(
					Resources.getString("ES23", configElements[i].getAttribute("class"), locale),
					ce);
			}
		}
		return null;
	}
	public IProgressMonitor getProgressMonitor(String locale) {
		return (IProgressMonitor) progressMonitors.get(locale);
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
	 * Obtains locale from query string
	 * @return locale in the form ll_CC
	 * @param query String
	 */
	private static String getLocale(String query) {
		int indx = query.indexOf("&lang=");
		if (indx == -1)
			return Locale.getDefault().toString();
		else if (query.charAt(indx + 8) == '_')
			return query.substring(indx + 6, indx + 11);
		else
			return query.substring(indx + 6, indx + 7);
	}
	/**
	 * Searches index for documents containing an expression.
	 * If the index hasn't been built then return null.
	 */
	public synchronized String getSearchResults(String searchQuery) {
		String locale = getLocale(searchQuery);
		SearchIndex index = getIndex(locale);
		if (index == null)
			return null;
		SearchQuery searchQueryObj = new SearchQuery(searchQuery);
		SearchResult result = searchQueryObj.search(index);
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
		// find a possible prebuilt index 
		PrebuiltIndex prebuiltIndex = new PrebuiltIndex(locale);
		if (prebuiltIndex.exists())
			if (prebuiltIndex.install())
				return index.getDocPlugins().detectChange();
		// in all other cases we need to index
		return true;
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
		// monitor indexing
		if (pm == null)
			pm = new IndexProgressMonitor();
		progressMonitors.put(locale, pm);
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
	private Set getAllDocuments(String locale) {
		HashSet hrefs = new HashSet();
		IToc[] tocs = HelpSystem.getTocManager().getTocs(locale);
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