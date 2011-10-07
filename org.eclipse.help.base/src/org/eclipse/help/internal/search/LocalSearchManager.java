/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Holger Voormann - Fix for Bug 352434
 *******************************************************************************/
package org.eclipse.help.internal.search;

import java.io.IOException;
import java.net.URL;
import java.nio.channels.OverlappingFileLockException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.IHelpResource;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.eclipse.help.internal.search.IndexingOperation.IndexingException;
import org.eclipse.help.internal.util.URLCoder;
import org.eclipse.help.search.LuceneSearchParticipant;
import org.eclipse.help.search.SearchParticipant;
import org.osgi.framework.Bundle;

/*
 * Manages indexing and searching for all local help content.
 */
public class LocalSearchManager {

	private static final String LUCENE_SEARCH_PARTICIPANT_XP_FULLNAME = "org.eclipse.help.base.luceneSearchParticipants"; //$NON-NLS-1$
	private static final String SEARCH_PARTICIPANT_XP_FULLNAME = "org.eclipse.help.base.searchParticipant"; //$NON-NLS-1$
	private static final String SEARCH_PARTICIPANT_XP_NAME = "searchParticipant"; //$NON-NLS-1$
	private static final String BINDING_XP_NAME = "binding"; //$NON-NLS-1$
	private static final ArrayList<ParticipantDescriptor> PARTICIPANTS_NOT_FOUND = new ArrayList<ParticipantDescriptor>();
	private Map<String, Object> indexes = new HashMap<String, Object>();
	private Map<String, AnalyzerDescriptor> analyzerDescriptors = new HashMap<String, AnalyzerDescriptor>();
	private Map<String, ParticipantDescriptor> searchParticipantsById = new HashMap<String, ParticipantDescriptor>();
	private Map<String, ArrayList<ParticipantDescriptor>> searchParticipantsByPlugin = new HashMap<String, ArrayList<ParticipantDescriptor>>();
	private ArrayList<ParticipantDescriptor> globalSearchParticipants;

	private static class ParticipantDescriptor implements IHelpResource {

		private IConfigurationElement element;
		private SearchParticipant participant;

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

		public IHelpResource getCategory() {
			return this;
		}

		public SearchParticipant getParticipant() {
			if (participant == null) {
				try {
					Object obj = element.createExecutableExtension("participant"); //$NON-NLS-1$
					if (obj instanceof SearchParticipant) {
						participant = (SearchParticipant)obj;
						participant.init(getId());
					} else if (obj instanceof LuceneSearchParticipant) {
						LuceneSearchParticipant luceneParticipant = (LuceneSearchParticipant) obj;
						participant = new LuceneSearchParticipantAdapter(luceneParticipant);
						participant.init(getId());
					}
				} catch (Throwable t) {
					HelpPlugin.logError("Exception occurred creating Lucene search participant.", t); //$NON-NLS-1$
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
			if (participant != null) {
				try {
					participant.clear();
				}
				catch (Throwable t) {
					HelpBasePlugin.logError("Error occured in search participant's clear() operation: " + participant.getClass().getName(), t); //$NON-NLS-1$
				}
			}
		}
	}

	/**
	 * Converts the given TopDocs object into a List of raw SearchHits.
	 * Hits objects are immutable and can't be instantiated from outside
	 * Lucene.
	 * @param searcher 
	 * 
	 * @param hits the TopDocs object to convert
	 * @return a List of raw SearchHits
	 */

	public static List<SearchHit> asList(TopDocs topDocs, IndexSearcher searcher) {
		List<SearchHit> list = new ArrayList<SearchHit>(topDocs.scoreDocs.length);
		
		for (int i=0; i<topDocs.scoreDocs.length; ++i) {
			try {
				Document doc = searcher.doc(topDocs.scoreDocs[i].doc); 
				float score = topDocs.scoreDocs[i].score;
				String href = doc.get("name"); //$NON-NLS-1$
				String summary = doc.get("summary");			 //$NON-NLS-1$
				String id = doc.get("id"); //$NON-NLS-1$
				String participantId = doc.get("participantId"); //$NON-NLS-1$
				String label = doc.get("raw_title"); //$NON-NLS-1$
				boolean isPotentialHit = (doc.get("filters") != null); //$NON-NLS-1$
				list.add(new SearchHit(href, label, summary, score, null, id, participantId, isPotentialHit));
			}
			catch (IOException e) {
				HelpBasePlugin.logError("An error occured while reading search hits", e); //$NON-NLS-1$
				continue;
			}
		}
		return list;
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

	/**
	 * Obtains AnalyzerDescriptor that indexing and search should use for a given locale.
	 * 
	 * @param locale
	 *            2 or 5 character locale representation
	 */
	private AnalyzerDescriptor getAnalyzer(String locale) {
		// get an analyzer from cache
		AnalyzerDescriptor analyzerDesc = analyzerDescriptors.get(locale);
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
		ArrayList<ParticipantDescriptor> list = getParticipantDescriptors(getPluginId(url));
		if (list == null)
			return false;
		int dotLoc = url.lastIndexOf('.');
		String ext = url.substring(dotLoc + 1);
		for (int i = 0; i < list.size(); i++) {
			ParticipantDescriptor desc = list.get(i);
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

	public SearchParticipant getGlobalParticipant(String participantId) {
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
			ParticipantDescriptor desc = globalSearchParticipants.get(i);
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
	public SearchParticipant getParticipant(String participantId) {
		ParticipantDescriptor desc = searchParticipantsById.get(participantId);
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

	public SearchParticipant getParticipant(String pluginId, String fileName) {
		ArrayList<ParticipantDescriptor> list = getParticipantDescriptors(pluginId);
		if (list == null)
			return null;
		int dotLoc = fileName.lastIndexOf('.');
		String ext = fileName.substring(dotLoc + 1);
		for (int i = 0; i < list.size(); i++) {
			ParticipantDescriptor desc = list.get(i);
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
		List<ParticipantDescriptor> list = getParticipantDescriptors(pluginId);
		if (list != null) {
			Iterator<ParticipantDescriptor> iter = list.iterator();
			while (iter.hasNext()) {
				ParticipantDescriptor desc = iter.next();
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

	public Set<String> getPluginsWithSearchParticipants() {
		HashSet<String> set = new HashSet<String>();
		addSearchBindings(set);
		addLuceneSearchBindings(set);
		// must ask global search participants directly
	    SearchParticipant[] gps = getGlobalParticipants();
		for (int i = 0; i < gps.length; i++) {
			Set<String> ids;
			try {
				ids = gps[i].getContributingPlugins();
			}
			catch (Throwable t) {
				HelpBasePlugin.logError("Error getting the contributing plugins from help search participant: " + gps[i].getClass().getName() + ". skipping this one.", t); //$NON-NLS-1$ //$NON-NLS-2$
				continue;
			}
			set.addAll(ids);
		}
		return set;
	}

	private void addSearchBindings(HashSet<String> set) {
		IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(
				SEARCH_PARTICIPANT_XP_FULLNAME);

		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement element = elements[i];
			if (element.getName().equals("binding") || element.getName().equals("searchParticipant"))  //$NON-NLS-1$//$NON-NLS-2$
				set.add(element.getContributor().getName());
		}
	}
	
	private void addLuceneSearchBindings(HashSet<String> set) {
		IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(
				LUCENE_SEARCH_PARTICIPANT_XP_FULLNAME);

		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement element = elements[i];
			if (element.getName().equals("binding") || element.getName().equals("searchParticipant"))  //$NON-NLS-1$//$NON-NLS-2$
				set.add(element.getContributor().getName());
		}
	}

	/**
	 * Loops through all the loaded search participants and notifies them that they can drop the
	 * cached data to reduce runtime memory footprint.
	 */
	public void clearSearchParticipants() {
		Iterator<ParticipantDescriptor> iter = searchParticipantsById.values().iterator();
		while (iter.hasNext()) {
			ParticipantDescriptor desc = iter.next();
			desc.clear();
		}
	}

	private ArrayList<ParticipantDescriptor> createSearchParticipants(String pluginId) {
		ArrayList<ParticipantDescriptor> list = null;
		list = getBindingsForPlugin(pluginId, list, SEARCH_PARTICIPANT_XP_FULLNAME);
		list = getBindingsForPlugin(pluginId, list, LUCENE_SEARCH_PARTICIPANT_XP_FULLNAME);
		return list;
	}

	private ArrayList<ParticipantDescriptor> getBindingsForPlugin(String pluginId, ArrayList<ParticipantDescriptor> list, String extensionPointName) {
		IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(
				extensionPointName);
		ArrayList<IConfigurationElement> binding = null;
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
					String id = rel.getAttribute("id"); //$NON-NLS-1$
					// don't allow binding the global participants
					if (rel.getAttribute("extensions") == null) //$NON-NLS-1$
						continue;
					if (id != null && id.equals(refId)) {
						// match
						if (binding == null)
							binding = new ArrayList<IConfigurationElement>();
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
					list = new ArrayList<ParticipantDescriptor>();
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

	private ArrayList<ParticipantDescriptor> addBoundDescriptors(ArrayList<ParticipantDescriptor> list, ArrayList<IConfigurationElement> binding) {
		for (int i = 0; i < binding.size(); i++) {
			IConfigurationElement refEl = binding.get(i);
			Collection<ArrayList<ParticipantDescriptor>> collection = searchParticipantsByPlugin.values();
			boolean found = false;
			for (Iterator<ArrayList<ParticipantDescriptor>> iter = collection.iterator(); iter.hasNext();) {
				if (found)
					break;
				ArrayList<ParticipantDescriptor> participants = iter.next();
				if (participants == PARTICIPANTS_NOT_FOUND)
					continue;
				//ArrayList participants = (ArrayList) entry;
				for (int j = 0; j < participants.size(); j++) {
					ParticipantDescriptor desc = (ParticipantDescriptor) participants.get(j);
					if (desc.contains(refEl)) {
						// found the matching descriptor - add it to the list
						if (list == null)
							list = new ArrayList<ParticipantDescriptor>();
						list.add(desc);
						found = true;
						break;
					}
				}
			}
			if (!found) {
				if (list == null)
					list = new ArrayList<ParticipantDescriptor>();
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

	public SearchParticipant[] getGlobalParticipants() {
		if (globalSearchParticipants == null) {
			createGlobalSearchParticipants();
		}
		ArrayList<SearchParticipant> result = new ArrayList<SearchParticipant>();
		for (int i = 0; i < globalSearchParticipants.size(); i++) {
			ParticipantDescriptor desc = globalSearchParticipants.get(i);
			SearchParticipant p = desc.getParticipant();
			if (p != null)
				result.add(p);
		}
		return result.toArray(new SearchParticipant[result.size()]);
	}

	private void createGlobalSearchParticipants() {
		globalSearchParticipants = new ArrayList<ParticipantDescriptor>();
		addSearchParticipants();
		addLuceneSearchParticipants();
	}
	
	private void addSearchParticipants() {
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

	private void addLuceneSearchParticipants() {
		IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(
				LUCENE_SEARCH_PARTICIPANT_XP_FULLNAME);
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

	private ArrayList<ParticipantDescriptor> getParticipantDescriptors(String pluginId) {
		ArrayList<ParticipantDescriptor> result = searchParticipantsByPlugin.get(pluginId);
		if (result == null) {
			result = createSearchParticipants(pluginId);
			if (result == null)
				result = PARTICIPANTS_NOT_FOUND;
			searchParticipantsByPlugin.put(pluginId, result);
		}
		if (result == PARTICIPANTS_NOT_FOUND)
			return null;
		return (ArrayList<ParticipantDescriptor>) result;
	}

	public void search(ISearchQuery searchQuery, ISearchHitCollector collector, IProgressMonitor pm)
			throws QueryTooComplexException {
		SearchIndexWithIndexingProgress index = getIndex(searchQuery.getLocale());
		ensureIndexUpdated(pm, index);
		if (index.exists()) {
			index.search(searchQuery, collector);
		}
	}
	
	/**
	 * Updates index. Checks if all contributions were indexed. If not, it indexes them.
	 * 
	 * @throws OperationCanceledException
	 *             if indexing was cancelled
	 */
	public void ensureIndexUpdated(IProgressMonitor pm, SearchIndexWithIndexingProgress index)
			throws OperationCanceledException {

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

	private synchronized void updateIndex(IProgressMonitor pm, SearchIndex index,
			ProgressDistributor progressDistrib) {
		if (index.isClosed() || !index.needsUpdating()) {
			pm.beginTask("", 1); //$NON-NLS-1$
			pm.worked(1);
			pm.done();
			return;
		}
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
		}
		catch (OperationCanceledException e) {
			progressDistrib.operationCanceled();
			throw e;
		}
		catch (IndexingException e) {
			String msg = "Error indexing documents"; //$NON-NLS-1$
			HelpBasePlugin.logError(msg, e);
		}
	}

	/*
	 * Closes all indexes.
	 */
	public void close() {
		synchronized (indexes) {
			for (Iterator<Object> it = indexes.values().iterator(); it.hasNext();) {
				((SearchIndex) it.next()).close();
			}
		}
	}

	public synchronized void tocsChanged() {
		Collection<Object> activeIndexes = new ArrayList<Object>();
		synchronized (indexes) {
			activeIndexes.addAll(indexes.values());
		}
		for (Iterator<Object> it = activeIndexes.iterator(); it.hasNext();) {
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

}
