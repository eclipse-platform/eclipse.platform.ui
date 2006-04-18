/***************************************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 **************************************************************************************************/
package org.eclipse.help.internal.search;

import java.io.IOException;
import java.net.URL;
import java.nio.channels.OverlappingFileLockException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Hits;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.HelpSystem;
import org.eclipse.help.IHelpResource;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.ITocsChangedListener;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.eclipse.help.internal.base.IHelpBaseConstants;
import org.eclipse.help.internal.search.IndexingOperation.IndexingException;
import org.eclipse.help.internal.search.federated.FederatedSearchEntry;
import org.eclipse.help.internal.search.federated.FederatedSearchJob;
import org.eclipse.help.internal.util.URLCoder;
import org.eclipse.help.internal.xhtml.XHTMLSupport;
import org.eclipse.help.search.LuceneSearchParticipant;
import org.osgi.framework.Bundle;

/**
 * Manages indexing and search for all infosets
 */
public class SearchManager implements ITocsChangedListener {

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private static final String SEARCH_PARTICIPANT_XP_FULLNAME = "org.eclipse.help.base.luceneSearchParticipants"; //$NON-NLS-1$
	private static final String SEARCH_PARTICIPANT_XP_NAME = "searchParticipant"; //$NON-NLS-1$
	private static final String BINDING_XP_NAME = "binding"; //$NON-NLS-1$
	private static final Object PARTICIPANTS_NOT_FOUND = new Object();
	/** Search indexes, by locale */
	private Map indexes = new HashMap();
	private Map indexCaches = new HashMap();

	/** Caches analyzer descriptors for each locale */
	private Map analyzerDescriptors = new HashMap();

	private Map searchParticipantsById = new HashMap();
	private Map searchParticipantsByPlugin = new HashMap();
	private ArrayList globalSearchParticipants;

	private static class ParticipantDescriptor implements IHelpResource {

		private IConfigurationElement element;
		private LuceneSearchParticipant participant;

		public ParticipantDescriptor(IConfigurationElement element) {
			this.element = element;
		}

		public String getId() {
			return element.getAttribute("id"); //$NON-NLS-1$
		}

		public boolean matches(String extension) {
			String ext = element.getAttribute("extensions"); //$NON-NLS-1$
			if (ext == null)
				return false;
			StringTokenizer stok = new StringTokenizer(ext, ","); //$NON-NLS-1$
			for (; stok.hasMoreTokens();) {
				String token = stok.nextToken().trim();
				if (token.equalsIgnoreCase(extension))
					return true;
			}
			return false;
		}

		public boolean hasExtensions() {
			return element.getAttribute("extensions") != null; //$NON-NLS-1$
		}

		public IHelpResource getCategory() {
			return this;
		}

		public LuceneSearchParticipant getParticipant() {
			if (participant == null) {
				try {
					Object obj = element.createExecutableExtension("participant"); //$NON-NLS-1$
					if (obj instanceof LuceneSearchParticipant) {
						participant = (LuceneSearchParticipant) obj;
						participant.init(getId());
					}
				} catch (CoreException e) {
					HelpPlugin.logError("Exception occurred creating Lucene search participant.", e); //$NON-NLS-1$
				}
			}
			return participant;
		}

		public boolean contains(IConfigurationElement el) {
			return element.equals(el);
		}

		public String getHref() {
			return null;
		}

		public String getLabel() {
			return element.getAttribute("name"); //$NON-NLS-1$
		}

		public URL getIconURL() {
			String relativePath = element.getAttribute("icon"); //$NON-NLS-1$
			if (relativePath == null)
				return null;
			String bundleId = element.getContributor().getName();
			Bundle bundle = Platform.getBundle(bundleId);
			if (bundle == null)
				return null;
			return FileLocator.find(bundle, new Path(relativePath), null);
		}

		public void clear() {
			if (participant != null)
				participant.clear();
		}
	}

	/**
	 * Constructs a Search manager.
	 */
	public SearchManager() {
		super();
		HelpPlugin.getDefault().addTocsChangedListener(this);
	}

	/**
	 * Converts the given Hits object into a List of raw SearchHits.
	 * Hits objects are immutable and can't be instantiated from outside
	 * Lucene.
	 * 
	 * @param hits the Hits object to convert
	 * @return a List of raw SearchHits
	 */
	public static List asList(Hits hits) {
		List list = new ArrayList(hits.length());
		for (int i=0;i<hits.length();++i) {
			try {
				Document doc = hits.doc(i);
				float score = hits.score(i);
				String href = doc.get("name"); //$NON-NLS-1$
				String summary = doc.get("summary");			 //$NON-NLS-1$
				String id = doc.get("id"); //$NON-NLS-1$
				String participantId = doc.get("participantId"); //$NON-NLS-1$
				String label = doc.get("raw_title"); //$NON-NLS-1$
				String filters = doc.get("filters"); //$NON-NLS-1$
				list.add(new SearchHit(href, label, summary, score, null, id, participantId, filters));
			}
			catch (IOException e) {
				HelpBasePlugin.logError("An error occured while reading search hits", e); //$NON-NLS-1$
				continue;
			}
		}
		return list;
	}
	
	/**
	 * Returns whether or not the given filters match the current filterable
	 * property values. For example, if the filters contain "os=win32", the filter
	 * matches only if the OS is windows. 
	 * 
	 * @param filters the filters to check, e.g. "os=linux,ws!=gtk,arch=x86"
	 * @return whether or not the filters are satisfied
	 */
	private boolean filtersMatch(String filters) {
		StringTokenizer tok = new StringTokenizer(filters, ","); //$NON-NLS-1$
		while (tok.hasMoreTokens()) {
			String filter = tok.nextToken();
			if (!XHTMLSupport.getFilterProcessor().isFilteredIn(filter)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Public for use by indexing tool
	 */
	public SearchIndexWithIndexingProgress getIndex(String locale) {
		synchronized (indexes) {
			Object index = indexes.get(locale);
			if (index == null) {
				index = new SearchIndexWithIndexingProgress(locale, getAnalyzer(locale), HelpPlugin
						.getTocManager());
				indexes.put(locale, index);
			}
			return (SearchIndexWithIndexingProgress) index;
		}
	}

	private SearchIndexCache getIndexCache(String locale) {
		synchronized (indexCaches) {
			Object index = indexCaches.get(locale);
			if (index == null) {
				index = new SearchIndexCache(locale, getAnalyzer(locale), HelpPlugin
						.getTocManager());
				indexCaches.put(locale, index);
			}
			return (SearchIndexCache) index;
		}
	}
	
	/**
	 * Obtains AnalyzerDescriptor that indexing and search should use for a given locale.
	 * 
	 * @param locale
	 *            2 or 5 character locale representation
	 */
	private AnalyzerDescriptor getAnalyzer(String locale) {
		// get an analyzer from cache
		AnalyzerDescriptor analyzerDesc = (AnalyzerDescriptor) analyzerDescriptors.get(locale);
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

	public static String trimQuery(String href) {
		// trim the query
		int qloc = href.indexOf('?');
		if (qloc != -1)
			return href.substring(0, qloc);
		return href;
	}

	public boolean isIndexable(String url) {
		url = trimQuery(url);
		ArrayList list = getParticipantDescriptors(getPluginId(url));
		if (list == null)
			return false;
		int dotLoc = url.lastIndexOf('.');
		String ext = url.substring(dotLoc + 1);
		for (int i = 0; i < list.size(); i++) {
			ParticipantDescriptor desc = (ParticipantDescriptor) list.get(i);
			if (desc.matches(ext))
				return true;
		}
		return false;
	}
	
	/**
	 * Returns whether or not a participant with the given headless attribute should be
	 * enabled in the current mode. Participants that support headless are always enabled, and
	 * those that don't are only enabled when in workbench mode.
	 * 
	 * @param headless whether or not the participant supports headless mode
	 * @return whether or not the participant should be enabled
	 */
	private static boolean isParticipantEnabled(boolean headless) {
		if (!headless) {
			return (BaseHelpSystem.getMode() == BaseHelpSystem.MODE_WORKBENCH);
		}
		return true;
	}

	public static String getPluginId(String href) {
		href = trimQuery(href);
		// Assume the url is pluginID/path_to_topic.html
		if (href.charAt(0) == '/')
			href = href.substring(1);
		int i = href.indexOf('/');
		String pluginId = i == -1 ? "" : href.substring(0, i); //$NON-NLS-1$
		pluginId = URLCoder.decode(pluginId);
		if ("PRODUCT_PLUGIN".equals(pluginId)) { //$NON-NLS-1$
			IProduct product = Platform.getProduct();
			if (product != null) {
				pluginId = product.getDefiningBundle().getSymbolicName();
			}
		}
		return pluginId;
	}

	public LuceneSearchParticipant getGlobalParticipant(String participantId) {
		ParticipantDescriptor desc = getGlobalParticipantDescriptor(participantId);
		return desc != null ? desc.getParticipant() : null;
	}

	public IHelpResource getParticipantCategory(String participantId) {
		ParticipantDescriptor desc = getGlobalParticipantDescriptor(participantId);
		return desc != null ? desc.getCategory() : null;
	}

	public URL getParticipantIconURL(String participantId) {
		ParticipantDescriptor desc = getGlobalParticipantDescriptor(participantId);
		return desc != null ? desc.getIconURL() : null;
	}

	private ParticipantDescriptor getGlobalParticipantDescriptor(String participantId) {
		if (globalSearchParticipants == null) {
			createGlobalSearchParticipants();
		}
		for (int i = 0; i < globalSearchParticipants.size(); i++) {
			ParticipantDescriptor desc = (ParticipantDescriptor) globalSearchParticipants.get(i);
			if (desc.getId().equals(participantId)) {
				return desc;
			}
		}
		return null;
	}

	/**
	 * Returns the lucene search participant with the given id, or null if it could not
	 * be found.
	 * 
	 * @param participantId the participant's unique id
	 * @return the participant with the given id
	 */
	public LuceneSearchParticipant getParticipant(String participantId) {
		ParticipantDescriptor desc = (ParticipantDescriptor)searchParticipantsById.get(participantId);
		if (desc != null) {
			return desc.getParticipant();
		}
		return null;
	}
	
	/**
	 * Returns a TOC file participant for the provided plug-in and file name.
	 * 
	 * @param pluginId
	 * @param fileName
	 * @return
	 */

	public LuceneSearchParticipant getParticipant(String pluginId, String fileName) {
		ArrayList list = getParticipantDescriptors(pluginId);
		if (list == null)
			return null;
		int dotLoc = fileName.lastIndexOf('.');
		String ext = fileName.substring(dotLoc + 1);
		for (int i = 0; i < list.size(); i++) {
			ParticipantDescriptor desc = (ParticipantDescriptor) list.get(i);
			if (desc.matches(ext))
				return desc.getParticipant();
		}
		return null;
	}

	/**
	 * Returns whether or not the given search participant is bound to the given
	 * plugin.
	 * 
	 * @param pluginId the id of the plugin
	 * @param participantId the id of the search participant
	 * @return whether or not the participant is bound to the plugin
	 */
	public boolean isParticipantBound(String pluginId, String participantId) {
		List list = getParticipantDescriptors(pluginId);
		if (list != null) {
			Iterator iter = list.iterator();
			while (iter.hasNext()) {
				ParticipantDescriptor desc = (ParticipantDescriptor)iter.next();
				if (participantId.equals(desc.getId())) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Returns a set of plug-in Ids that have search participants or bindings.
	 * 
	 * @return a set of plug-in Ids
	 */

	public Set getPluginsWithSearchParticipants() {
		HashSet set = new HashSet();
		IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(
				SEARCH_PARTICIPANT_XP_FULLNAME);

		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement element = elements[i];
			if (element.getName().equals("binding") || element.getName().equals("searchParticipant"))  //$NON-NLS-1$//$NON-NLS-2$
				set.add(element.getContributor().getName());
		}
		// must ask global search participants directly
		LuceneSearchParticipant[] gps = getGlobalParticipants();
		for (int i = 0; i < gps.length; i++) {
			Set ids = gps[i].getContributingPlugins();
			set.addAll(ids);
		}
		return set;
	}

	/**
	 * Loops through all the loaded search participants and notifies them that they can drop the
	 * cached data to reduce runtime memory footprint.
	 */
	public void clearSearchParticipants() {
		Iterator iter = searchParticipantsById.values().iterator();
		while (iter.hasNext()) {
			ParticipantDescriptor desc = (ParticipantDescriptor)iter.next();
			desc.clear();
		}
	}

	private ArrayList createSearchParticipants(String pluginId) {
		IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(
				SEARCH_PARTICIPANT_XP_FULLNAME);
		if (elements.length == 0)
			return null;
		ArrayList list = null;

		ArrayList binding = null;
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement element = elements[i];
			if (!element.getContributor().getName().equals(pluginId)) {
				continue;
			}
			if (BINDING_XP_NAME.equals(element.getName())) {
				// binding - locate the referenced participant
				String refId = element.getAttribute("participantId"); //$NON-NLS-1$
				for (int j = 0; j < elements.length; j++) {
					IConfigurationElement rel = elements[j];
					if (!rel.getName().equals("searchParticipant")) //$NON-NLS-1$
						continue;
					// don't allow binding the global participants
					if (rel.getAttribute("extensions") == null) //$NON-NLS-1$
						continue;
					String id = rel.getAttribute("id"); //$NON-NLS-1$
					if (id != null && id.equals(refId)) {
						// match
						if (binding == null)
							binding = new ArrayList();
						binding.add(rel);
						break;
					}
				}
			} else if (SEARCH_PARTICIPANT_XP_NAME.equals(element.getName())) {
				// ignore global participant
				if (element.getAttribute("extensions") == null) //$NON-NLS-1$
					continue;
				if (!isParticipantEnabled(String.valueOf(true).equals(element.getAttribute("headless")))) //$NON-NLS-1$
					continue;
				if (list == null)
					list = new ArrayList();
				ParticipantDescriptor desc = new ParticipantDescriptor(element); 
				list.add(desc);
				searchParticipantsById.put(desc.getId(), desc);
			}
		}
		if (binding != null)
			list = addBoundDescriptors(list, binding);
		return list;
	}

	/**
	 * Locates the
	 * 
	 * @param list
	 * @param binding
	 * @return
	 */

	private ArrayList addBoundDescriptors(ArrayList list, ArrayList binding) {
		for (int i = 0; i < binding.size(); i++) {
			IConfigurationElement refEl = (IConfigurationElement) binding.get(i);
			Collection collection = searchParticipantsByPlugin.values();
			boolean found = false;
			for (Iterator iter = collection.iterator(); iter.hasNext();) {
				if (found)
					break;
				Object entry = iter.next();
				if (entry == PARTICIPANTS_NOT_FOUND)
					continue;
				ArrayList participants = (ArrayList) entry;
				for (int j = 0; j < participants.size(); j++) {
					ParticipantDescriptor desc = (ParticipantDescriptor) participants.get(j);
					if (desc.contains(refEl)) {
						// found the matching rescriptor - add it to the list
						if (list == null)
							list = new ArrayList();
						list.add(desc);
						found = true;
						break;
					}
				}
			}
			if (!found) {
				if (list == null)
					list = new ArrayList();
				ParticipantDescriptor d = new ParticipantDescriptor(refEl);
				list.add(d);
				searchParticipantsById.put(d.getId(), d);
			}
		}
		return list;
	}

	/**
	 * Returns an array of search participants with the global scope (no extensions).
	 * 
	 * @return an array of the global search participants.
	 */

	public LuceneSearchParticipant[] getGlobalParticipants() {
		if (globalSearchParticipants == null) {
			createGlobalSearchParticipants();
		}
		ArrayList result = new ArrayList();
		for (int i = 0; i < globalSearchParticipants.size(); i++) {
			ParticipantDescriptor desc = (ParticipantDescriptor) globalSearchParticipants.get(i);
			LuceneSearchParticipant p = desc.getParticipant();
			if (p != null)
				result.add(p);
		}
		return (LuceneSearchParticipant[]) result.toArray(new LuceneSearchParticipant[result.size()]);
	}

	private void createGlobalSearchParticipants() {
		globalSearchParticipants = new ArrayList();
		IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(
				SEARCH_PARTICIPANT_XP_FULLNAME);
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement element = elements[i];
			if (!element.getName().equals(SEARCH_PARTICIPANT_XP_NAME))
				continue;
			if (element.getAttribute("extensions") != null) //$NON-NLS-1$
				continue;
			if (!isParticipantEnabled(String.valueOf(true).equals(element.getAttribute("headless")))) //$NON-NLS-1$
				continue;
			ParticipantDescriptor desc = new ParticipantDescriptor(element);
			globalSearchParticipants.add(desc);
		}
	}

	private ArrayList getParticipantDescriptors(String pluginId) {
		Object result = searchParticipantsByPlugin.get(pluginId);
		if (result == null) {
			result = createSearchParticipants(pluginId);
			if (result == null)
				result = PARTICIPANTS_NOT_FOUND;
			searchParticipantsByPlugin.put(pluginId, result);
		}
		if (result == PARTICIPANTS_NOT_FOUND)
			return null;
		return (ArrayList) result;
	}

	/**
	 * Searches index for documents containing an expression. Searching is
	 * done in potentially several phases. There are two indexes in play; the
	 * master index, which has all documents indexed unfiltered, and the cache
	 * index, which has a subset of all documents indexed filtered, and is created
	 * on demand.
	 * 
	 * The procedure for searching is as follows:
	 * 
	 * 1. Search the master index. This will yield potential false positives
	 *    because the master index docs were unfiltered.
	 * 2. For those docs that didn't have filters, mark them as hits. For the
	 *    potential false positives, search the cache index.
	 * 3. For each hit in the cache index, check whether the filters used at the
	 *    time of indexing agree with the current filters. If yes, mark as hit. If
	 *    no, reindex those that didn't match with the current filters (or weren't
	 *    found at all).
	 * 4. Search the now-updated cache for these remaining documents.
	 */
	public void search(ISearchQuery searchQuery, ISearchHitCollector collector, IProgressMonitor pm)
			throws QueryTooComplexException {

		SearchIndexWithIndexingProgress index = getIndex(searchQuery.getLocale());
		try {
			ensureIndexUpdated(pm, index);
			if (!index.exists()) {
				// no indexable documents, hence no index
				// or index is corrupted
				return;
			}
		} catch (IndexingOperation.IndexingException ie) {
			if (HelpBasePlugin.DEBUG_SEARCH) {
				System.out.println(this.getClass().getName() + " IndexUpdateException occurred."); //$NON-NLS-1$
			}
		}
		
		final List hits = new ArrayList();
		final List potentialHits = new ArrayList();
		final List needReindexingHits = new ArrayList();
		final String[] highlightTerms = new String[1];

		/*
		 * Pass 1: Search the master index. This will yield definite hits,
		 * potentially false hits, and the terms to highlight.
		 */
		searchPass1(searchQuery, hits, potentialHits, highlightTerms);

		if (!potentialHits.isEmpty()) {
			/*
			 * Pass 2: Ensure that the cache index is up to date. This will yield
			 * all potential false hits' docs that need to be reindexed for the cache.
			 */
			searchPass2(searchQuery, potentialHits, needReindexingHits);
			if (!needReindexingHits.isEmpty()) {
				reindex(needReindexingHits, searchQuery.getLocale());
			}

			/*
			 * Pass 3: Now that cache is up to date, search the cache and add
			 * to the definite hits.
			 */
			searchPass3(searchQuery, hits);
			
			// sort by score
			Collections.sort(hits);
		}
		
		// send out the final results
		if (highlightTerms[0] == null) {
			highlightTerms[0] = EMPTY_STRING;
		}
		collector.addHits(hits, highlightTerms[0]);
	}
	
	/**
	 * Performs the federated search.
	 */
	public void search(String expression, FederatedSearchEntry[] entries) {
		for (int i = 0; i < entries.length; i++) {
			FederatedSearchJob job = new FederatedSearchJob(expression, entries[i]);
			job.schedule();
		}
	}
	
	/**
	 * Performs the initial search pass. This searches the master index
	 * (unfiltered documents). This will yield definite hits and potentially
	 * false hits, added to the corresponding collection parameters. This
	 * also yields the terms to highlight in the result.
	 * 
	 * Infocenter doesn't filter, so in this mode there are never
	 * potential false hits.
	 * 
	 * @param searchQuery what to search for
	 * @param hits those hits that we know for sure are not false hits
	 * @param potentialFalseHits hits that may be false hits (we are not sure)
	 * @param highlightTerms the terms to highlight
	 */
	private void searchPass1(ISearchQuery searchQuery, Collection hits, Collection potentialFalseHits, String[] highlightTerms) {
		final Collection fHits = hits;
		final Collection fPotentialFalseHits = potentialFalseHits;
		final String[] fHighlightTerms = highlightTerms;
		ISearchHitCollector collector = new ISearchHitCollector() {
			public void addHits(List hits, String wordsSearched) {
				boolean isInfocenter = HelpSystem.isShared();
				boolean showPotentialHits = HelpBasePlugin.getDefault().getPluginPreferences()
					.getBoolean(IHelpBaseConstants.P_KEY_SHOW_POTENTIAL_HITS);
				fHighlightTerms[0] = wordsSearched;
				Iterator iter = hits.iterator();
				while (iter.hasNext()) {
					SearchHit hit = (SearchHit)iter.next();
					String filters = hit.getFilters();
					
					// if it has filters it is potentially a false hit
					if (!showPotentialHits && !isInfocenter && filters != null) {
						fPotentialFalseHits.add(hit);
					}
					else {
						fHits.add(hit);
					}
				}
			}
		};
		
		/*
		 * Perform the initial search pass on the master index. This will
		 * find all potential hits.
		 */
		SearchIndex index = getIndex(searchQuery.getLocale());
		index.search(searchQuery, collector);
	}
	
	/**
	 * Performs the second search pass. The only purpose for this pass is
	 * to check whether the cache index is up to date or not. This will search
	 * the cache, check whether the filters match the current values, and find
	 * the potential false hit documents that haven't yet been indexed.
	 * 
	 * @param searchQuery what to search for
	 * @param potentialFalseHits the potentially false hits (not modified)
	 * @param needReindexingHits the hits whose docs need to be indexed or reindexed
	 */
	private void searchPass2(ISearchQuery searchQuery, Collection potentialFalseHits, Collection needReindexingHits) {
		final List secondPassDefiniteHits = new ArrayList();
		final Collection fNeedReindexingHits = needReindexingHits;
		ISearchHitCollector collector = new ISearchHitCollector() {
			public void addHits(List hits, String wordsSearched) {
				Iterator iter = hits.iterator();
				while (iter.hasNext()) {
					SearchHit hit = (SearchHit)iter.next();
					String filters = hit.getFilters();
					
					/*
					 * If the current filter property values (e.g. os,
					 * ws) match those used at indexing time we are ok.
					 * Otherwise we need to reindex with updated filters.
					 */
					if (filtersMatch(filters)) {
						secondPassDefiniteHits.add(hit);
					}
					else {
						fNeedReindexingHits.add(hit);
					}
				}
			}
		};
		
		// perform the second search pass
		SearchIndexCache indexCache = getIndexCache(searchQuery.getLocale());
		if (indexCache.exists()) {
			indexCache.search(searchQuery, collector);
		}
		
		// are all the potential false hits accounted for?
		// for ones that aren't, check if need reindexing
		Set unaccountedFor = new HashSet(potentialFalseHits);
		unaccountedFor.removeAll(secondPassDefiniteHits);
		unaccountedFor.removeAll(needReindexingHits);
		
		Iterator iter = unaccountedFor.iterator();
		while (iter.hasNext()) {
			SearchHit hit = (SearchHit)iter.next();
			String filters = (String)indexCache.getIndexedDocs().get(hit.getHref());
			if (filters == null || !filtersMatch(filters)) {
				needReindexingHits.add(hit);
			}
		}
	}
	
	/**
	 * Performs the third and final search pass. This searches for all the
	 * previously collected potential false hits in the cache. Those found are
	 * now known to be definite hits, since the cache was updated in pass 2.
	 * 
	 * @param searchQuery what to search for
	 * @param definiteHits the definite hits
	 */
	private void searchPass3(ISearchQuery searchQuery, Collection definiteHits) {
		final Collection fDefiniteHits = definiteHits;
		ISearchHitCollector collector = new ISearchHitCollector() {
			public void addHits(List hits, String wordsSearched) {
				fDefiniteHits.addAll(hits);
			}
		};
		SearchIndexCache indexCache = getIndexCache(searchQuery.getLocale());
		indexCache.search(searchQuery, collector);
	}

	/**
	 * Updates index. Checks if all contributions were indexed. If not, it indexes them.
	 * 
	 * @throws OperationCanceledException
	 *             if indexing was cancelled
	 */
	public void ensureIndexUpdated(IProgressMonitor pm, SearchIndexWithIndexingProgress index)
			throws OperationCanceledException, IndexingOperation.IndexingException {

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
			}
			if (pm instanceof SearchProgressMonitor) {
				((SearchProgressMonitor) pm).started();
			}
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
	private synchronized void updateIndex(IProgressMonitor pm, SearchIndex index,
			ProgressDistributor progressDistrib) throws IndexingException {
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
			HelpBasePlugin.logWarning("Search cancelled."); //$NON-NLS-1$
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
			SearchIndexWithIndexingProgress ix = (SearchIndexWithIndexingProgress) it.next();
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
	
	private void reindex(List hits, String locale) {
		SearchIndexCache indexCache = getIndexCache(locale);
		if (indexCache.exists()) {
			indexCache.beginDeleteBatch();
			Iterator iter = hits.iterator();
			while (iter.hasNext()) {
				SearchHit hit = (SearchHit)iter.next();
				indexCache.removeDocument(hit.getHref());
			}
			indexCache.endDeleteBatch();
		}
		indexCache.beginAddBatch(false);
		Iterator iter = hits.iterator();
		while (iter.hasNext()) {
			SearchHit hit = (SearchHit)iter.next();
			String filters = hit.getFilters();
			filters = setCurrentValues(filters);
			indexCache.addDocument(hit.getHref(), SearchIndex.getIndexableURL(locale, hit.getHref()), filters);
		}
		indexCache.endAddBatch(true, true);
	}
	
	/**
	 * Takes in a list of general filters that a document is sensitive to, and inserts
	 * current specific values, e.g. "os,plugin=my.plugin.id" -> "os=win32,plugin!=my.plugin.id".
	 * 
	 * For single-value filters (e.g. os, ws, arch), the general form has the filter key,
	 * and the specific form is "[key]=[current_value]".
	 * 
	 * For multi-value filters (e.g. plugin), the general form has "[name]=[value]", e.g.
	 * "plugin=my.plugin.id" which means the document is sensitive to whether or not
	 * my.plugin.id is present. The specific form is the same except if the plugin is
	 * not currently there it uses a "!=" instead of "=".
	 * 
	 * @param filters the general filters, e.g. "os,ws,plugin=my.plugin"
	 * @return the current specific filters, e.g. "os=win32,ws=win32,plugin!=my.plugin"
	 */
	private String setCurrentValues(String filters) {
		StringBuffer buf = new StringBuffer();
		StringTokenizer tok = new StringTokenizer(filters, ","); //$NON-NLS-1$
		boolean first = true;
		while (tok.hasMoreTokens()) {
			if (!first) {
				buf.append(',');
			}
			first = false;
			String filter = tok.nextToken();
			int index = filter.indexOf('=');
			
			// multi-value filter, e.g. "plugin=my.plugin.id" (there can be many plugins)
			if (index > 0) {
				String key = filter.substring(0, index);
				String value = filter.substring(index + 1);
				boolean isPositive = (XHTMLSupport.getFilterProcessor().isFilteredIn(key, value, true));
				buf.append(key + (isPositive ? "=" : "!=") + value);  //$NON-NLS-1$//$NON-NLS-2$
			}
			// single-value filter, e.g. "os=win32" (there can only be one OS)
			else {
				buf.append(filter + '=' + XHTMLSupport.getFilterProcessor().getCurrentValue(filter));
			}
		}
		return buf.toString();
	}
}
