/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.search;

import java.net.URL;
import java.nio.channels.OverlappingFileLockException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.IHelpResource;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.ITocsChangedListener;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.eclipse.help.internal.search.IndexingOperation.IndexingException;
import org.eclipse.help.internal.search.federated.FederatedSearchEntry;
import org.eclipse.help.internal.search.federated.FederatedSearchJob;
import org.eclipse.help.internal.util.URLCoder;
import org.eclipse.help.search.LuceneSearchParticipant;
import org.osgi.framework.Bundle;

/**
 * Manages indexing and search for all infosets
 */
public class SearchManager implements ITocsChangedListener {
	private static final String SEARCH_PARTICIPANT_XP_FULLNAME = "org.eclipse.help.base.luceneSearchParticipants";
	private static final String SEARCH_PARTICIPANT_XP_NAME = "searchParticipant";
	private static final String BINDING_XP_NAME = "binding";
	private static final Object PARTICIPANTS_NOT_FOUND = new Object();
	/** Search indexes, by locale */
	private Map indexes = new HashMap();

	/** Caches analyzer descriptors for each locale */
	private Map analyzerDescriptors = new HashMap();
	
	/**
	 * Caches search participants
	 */
	private Map searchParticipants = new HashMap();
	
	private ArrayList globalSearchParticipants;
	
	private static class ParticipantDescriptor implements IHelpResource {
		private IConfigurationElement element;
		private LuceneSearchParticipant participant;

		public ParticipantDescriptor(IConfigurationElement element) {
			this.element = element;
		}
		public String getId() {
			return element.getAttribute("id");
		}
		public boolean matches(String extension) {
			String ext = element.getAttribute("extensions");
			if (ext==null)
				return false;
			StringTokenizer stok = new StringTokenizer(ext, ",");
			for (; stok.hasMoreTokens();) {
				String token = stok.nextToken().trim();
				if (token.equalsIgnoreCase(extension))
					return true;
			}
			return false;
		}
		public boolean hasExtensions() {
			return element.getAttribute("extensions")!=null;
		}
		public IHelpResource getCategory() {
			return this;
		}
		public LuceneSearchParticipant getParticipant() {
			if (participant==null) {
				try {
					Object obj = element.createExecutableExtension("participant");
					if (obj instanceof LuceneSearchParticipant) {
						participant = (LuceneSearchParticipant)obj;
						participant.init(getId());
					}
				}
				catch (CoreException e) {
					HelpPlugin
					.logError(
						"Exception occurred creating Lucene search participant.", e); //$NON-NLS-1$ //$NON-NLS-2$				
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
			return element.getAttribute("name");
		}
		public URL getIconURL() {
			String relativePath = element.getAttribute("icon");
			if (relativePath ==null)
				return null;
			String bundleId = element.getNamespace();
			Bundle bundle = Platform.getBundle(bundleId);
			if (bundle==null)
				return null;
			return Platform.find(bundle, new Path(relativePath));
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
	
	public static String trimQuery(String href) {
		// trim the query
		int qloc = href.indexOf('?');
		if (qloc!= -1)
			return href.substring(0, qloc);
		return href;
	}
	
	public boolean isIndexable(String url) {
		url = trimQuery(url);
		ArrayList list = getParticipantDescriptors(getPluginId(url));
		if (list==null)
			return false;
		int dotLoc = url.lastIndexOf('.');
		String ext = url.substring(dotLoc+1);
		for (int i=0; i<list.size(); i++) {
			ParticipantDescriptor desc = (ParticipantDescriptor)list.get(i);
			if (desc.matches(ext))
				return true;
		}
		return false;
	}
	
	public static String getPluginId(String href) {
		href = trimQuery(href);
		// Assume the url is pluginID/path_to_topic.html
		int i = href.indexOf('/');
		String pluginId = i == -1 ? "" : href.substring(0, i); //$NON-NLS-1$
		pluginId = URLCoder.decode(pluginId);
		if ("PRODUCT_PLUGIN".equals(pluginId)){
			IProduct product = Platform.getProduct();
			if(product !=null) {
				pluginId = product.getDefiningBundle().getSymbolicName();
			}
		}
		return pluginId;
	}
	
	public LuceneSearchParticipant getGlobalParticipant(String participantId) {
		ParticipantDescriptor desc = getGlobalParticipantDescriptor(participantId);
		return desc!=null?desc.getParticipant():null;
	}
	
	public IHelpResource getParticipantCategory(String participantId) {
		ParticipantDescriptor desc = getGlobalParticipantDescriptor(participantId);
		return desc!=null?desc.getCategory():null;
	}
	
	public URL getParticipantIconURL(String participantId) {
		ParticipantDescriptor desc = getGlobalParticipantDescriptor(participantId);
		return desc!=null?desc.getIconURL():null;
	}
	
	private ParticipantDescriptor getGlobalParticipantDescriptor(String participantId) {
		if (globalSearchParticipants==null) {
			createGlobalSearchParticipants();
		}
		for (int i=0; i<globalSearchParticipants.size(); i++) {
			ParticipantDescriptor desc = (ParticipantDescriptor)globalSearchParticipants.get(i);
			if (desc.getId().equals(participantId)) {
				return desc;
			}
		}
		return null;
	}	
	
	/**
	 * Returns a TOC file participant for the provided plug-in and file name.
	 * @param pluginId
	 * @param fileName
	 * @return
	 */
	
	public LuceneSearchParticipant getParticipant(String pluginId, String fileName) {
		ArrayList list = getParticipantDescriptors(pluginId);
		if (list==null)
			return null;
		int dotLoc = fileName.lastIndexOf('.');
		String ext = fileName.substring(dotLoc+1);
		for (int i=0; i<list.size(); i++) {
			ParticipantDescriptor desc = (ParticipantDescriptor)list.get(i);
			if (desc.matches(ext))
				return desc.getParticipant();
		}
		return null;
	}
	
	/**
	 * Returns a set of plug-in Ids that have search participants
	 * or bindings.
	 * @return a set of plug-in Ids
	 */

	public Set getPluginsWithSearchParticipants() {
		HashSet set = new HashSet();
		IConfigurationElement [] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(SEARCH_PARTICIPANT_XP_FULLNAME);

		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement element = elements[i];
			if (element.getName().equals("binding") ||
					element.getName().equals("searchParticipant"))
				set.add(element.getNamespace());
		}
		// must ask global search participants directly
		LuceneSearchParticipant [] gps = getGlobalParticipants();
		for (int i=0; i<gps.length; i++) {
			Set ids = gps[i].getContributingPlugins();
			set.addAll(ids);
		}
		return set;
	}
	
	private ArrayList createSearchParticipants(String pluginId) {
		IConfigurationElement [] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(SEARCH_PARTICIPANT_XP_FULLNAME);
		if (elements.length==0)
			return null;
		ArrayList list=null;

		ArrayList binding = null;
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement element = elements[i];
			if (!element.getNamespace().equals(pluginId)) {
				continue;
			}
			if (BINDING_XP_NAME.equals(element.getName())) {
				// binding - locate the referenced participant
				String refId = element.getAttribute("participantId");
				for (int j=0; j<elements.length; j++) {
					IConfigurationElement rel = elements[j];
					if (!rel.getName().equals("searchParticipant"))
						continue;
					// don't allow binding the global participants
					if (rel.getAttribute("extensions")==null)
						continue;
					String id = rel.getAttribute("id");
					if (id!=null && id.equals(refId)) {
						// match
						if (binding==null)
							binding = new ArrayList();
						binding.add(rel);
						break;
					}
				}
			}
			else if (SEARCH_PARTICIPANT_XP_NAME.equals(element.getName())) {
				// ignore global participant
				if (element.getAttribute("extensions")==null)
					continue;
				if (list==null)
					list = new ArrayList();
				list.add(new ParticipantDescriptor(element));
			}
		}
		if (binding!=null)
			list = addBoundDescriptors(list, binding);
		return list;
	}
	
	/**
	 * Locates the 
	 * @param list
	 * @param binding
	 * @return
	 */
	
	private ArrayList addBoundDescriptors(ArrayList list, ArrayList binding) {
		for (int i=0; i<binding.size(); i++) {
			IConfigurationElement refEl = (IConfigurationElement)binding.get(i);
			Collection collection = searchParticipants.values();
			boolean found=false;
			for (Iterator iter = collection.iterator(); iter.hasNext();) {
				if (found) break;
				Object entry = iter.next();
				if (entry==PARTICIPANTS_NOT_FOUND)
					continue;
				ArrayList participants = (ArrayList)entry;
				for (int j=0; j<participants.size(); j++) {
					ParticipantDescriptor desc = (ParticipantDescriptor)participants.get(j);
					if (desc.contains(refEl)) {
						// found the matching rescriptor - add it to the list
						if (list==null)
							list = new ArrayList();
						list.add(desc);
						found=true;
						break;
					}
				}
			}
			if (!found) {
				if (list==null)
					list = new ArrayList();
				list.add(new ParticipantDescriptor(refEl));
			}
		}
		return list;
	}
	
	/**
	 * Returns an array of search participants with the global scope (no extensions).
	 * 
	 * @return an array of the global search participants.
	 */

	public LuceneSearchParticipant [] getGlobalParticipants() {
		if (globalSearchParticipants==null) {
			createGlobalSearchParticipants();
		}
		ArrayList result = new ArrayList();
		for (int i=0; i<globalSearchParticipants.size(); i++) {
			ParticipantDescriptor desc = (ParticipantDescriptor)globalSearchParticipants.get(i);
			LuceneSearchParticipant p = desc.getParticipant();
			if (p!=null)
				result.add(p);
		}
		return (LuceneSearchParticipant[])result.toArray(new LuceneSearchParticipant[result.size()]);
	}

	private void createGlobalSearchParticipants() {
		globalSearchParticipants = new ArrayList();		
		IConfigurationElement [] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(SEARCH_PARTICIPANT_XP_FULLNAME);
		for (int i=0; i<elements.length; i++) {
			IConfigurationElement element = elements[i];
			if (!element.getName().equals(SEARCH_PARTICIPANT_XP_NAME))
				continue;
			if (element.getAttribute("extensions")!=null)
				continue;
			ParticipantDescriptor desc = new ParticipantDescriptor(element);
			globalSearchParticipants.add(desc);
		}
	}
	
	private ArrayList getParticipantDescriptors(String pluginId) {
		Object result = searchParticipants.get(pluginId);
		if (result==null) {
			result = createSearchParticipants(pluginId);
			if (result==null)
				result = PARTICIPANTS_NOT_FOUND;
			searchParticipants.put(pluginId, result);
		}
		if (result==PARTICIPANTS_NOT_FOUND)
			return null;
		return (ArrayList)result;
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
	 * Performs the federated search.
	 */
	
	public void search(String expression, FederatedSearchEntry [] entries) {
		for (int i=0; i<entries.length; i++) {
			FederatedSearchJob job = new FederatedSearchJob(expression, entries[i]);
			job.schedule();
		}
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
