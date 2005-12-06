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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.help.IToc;
import org.eclipse.help.ITopic;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.eclipse.help.internal.base.HelpBaseResources;
import org.eclipse.help.internal.base.util.HelpProperties;
import org.eclipse.help.internal.protocols.HelpURLConnection;
import org.eclipse.help.internal.toc.Toc;
import org.eclipse.help.search.LuceneSearchParticipant;

/**
 * Indexing Operation represents a long operation, which performs indexing of
 * the group (Collection) of documents. It is used Internally by SlowIndex and
 * returned by its getIndexUpdateOperation() method.
 */
class IndexingOperation {
	private int numAdded;

	private int numRemoved;

	private SearchIndex index = null;

	/**
	 * Construct indexing operation.
	 * 
	 * @param ix
	 *            ISearchIndex already opened
	 */
	public IndexingOperation(SearchIndex ix) {
		this.index = ix;
	}

	private void checkCancelled(IProgressMonitor pm)
			throws OperationCanceledException {
		if (pm.isCanceled())
			throw new OperationCanceledException();
	}

	/**
	 * Executes indexing, given the progress monitor.
	 * 
	 * @param pm
	 *            progres monitor to be used during this long operation for
	 *            reporting progress
	 * @throws OperationCanceledException
	 *             if indexing was cancelled
	 */
	protected void execute(IProgressMonitor pm)
			throws OperationCanceledException, IndexingException {
		checkCancelled(pm);
		Collection staleDocs = getRemovedDocuments(index);
		numRemoved = staleDocs.size();
		Collection newDocs = getAddedDocuments(index);
		numAdded = newDocs.size();
		if (HelpBasePlugin.DEBUG_SEARCH) {
			System.out
					.println("IndexingOperation.execute: " + numRemoved + " documents in deleted plug-ins, " + numAdded + " documents in added plug-ins."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}

		// if collection is empty, we may return right away
		// need to check if we have to do anything to the progress monitor
		if (numRemoved + numAdded <= 0) {
			pm.done();
			return;
		}
		pm.beginTask(HelpBaseResources.UpdatingIndex, numRemoved + 10
				* numAdded);

		// 1. remove all documents for plugins changed (including change in a
		// fragment)
		removeStaleDocuments(new SubProgressMonitor(pm, numRemoved), staleDocs);
		checkCancelled(pm);
		// 2. merge prebult plugin indexes and addjust
		addNewDocuments(new SubProgressMonitor(pm, 10 * numAdded), newDocs,
				staleDocs.size() == 0);

		pm.done();
	}

	private Map calculateNewToRemove(Collection newDocs, Map prebuiltDocs) {
		// Calculate document that were in prebuilt indexes, but are not in
		// TOCs. (prebuiltDocs - newDocs)
		/*
		 * Map. Keys are /pluginid/href of docs to delete. Values are null to
		 * delete completely, or String[] of indexIds with duplicates of the
		 * document
		 */
		Map docsToDelete = prebuiltDocs;
		ArrayList prebuiltHrefs = new ArrayList(prebuiltDocs.keySet());
		for (int i = 0; i < prebuiltHrefs.size(); i++) {
			String href = (String) prebuiltHrefs.get(i);
			URL u = SearchIndex.getIndexableURL(index.getLocale(), href);
			if (u == null) {
				// should never be here
				docsToDelete.put(href, null);
			}
			if (newDocs.contains(u)) {
				// delete duplicates only
				if (docsToDelete.get(href) != null) {
					// duplicates exist, leave map entry as is
				} else {
					// no duplicates, do not delete
					docsToDelete.remove(href);
				}
			} else {
				// document should not be indexed at all (TOC not built)
				// delete completely, not just duplicates
				docsToDelete.put(href, null);
			}
		}
		return docsToDelete;
	}

	/**
	 * Returns documents that must be deleted
	 * 
	 * @param pm
	 * @param newDocs
	 * @return
	 * @throws IndexingException
	 */
	private Map addNewDocuments(IProgressMonitor pm, Collection newDocs,
			boolean opened) throws IndexingException {
		Map prebuiltDocs = mergeIndexes(pm, opened);
		if (HelpBasePlugin.DEBUG_SEARCH) {
			System.out
					.println("IndexOperation.addNewDocuments: " + prebuiltDocs.size() + " different documents merged."); //$NON-NLS-1$ //$NON-NLS-2$
		}
		checkCancelled(pm);
		Collection docsToIndex = calculateDocsToAdd(newDocs, prebuiltDocs);
		checkCancelled(pm);
		Map docsToDelete = calculateNewToRemove(newDocs, prebuiltDocs);
		// IProgressMonitor addMonitor = new SubProgressMonitor(pm,
		// docsToDelete.size()*10 + docsToIndex.size()*100;
		if (HelpBasePlugin.DEBUG_SEARCH) {
			System.out
					.println("IndexOperation.addNewDocuments: " + docsToIndex.size() + " documents not yet indexed."); //$NON-NLS-1$ //$NON-NLS-2$
			System.out
					.println("IndexOperation.addNewDocuments: " + docsToDelete.size() + " documents have more than one copy indexed."); //$NON-NLS-1$ //$NON-NLS-2$
		}
		pm.beginTask("", 10 * docsToIndex.size() + docsToDelete.size()); //$NON-NLS-1$
		checkCancelled(pm);
		addDocuments(new SubProgressMonitor(pm, 10 * docsToIndex.size()),
				docsToIndex, docsToDelete.size() == 0);
		checkCancelled(pm);
		removeNewDocuments(new SubProgressMonitor(pm, docsToDelete.size()),
				docsToDelete);
		pm.done();
		return docsToDelete;
	}

	private Collection calculateDocsToAdd(Collection newDocs, Map prebuiltDocs) {
		// Calculate documents that were not in prebuilt indexes, and still need
		// to be added
		// (newDocs minus prebuiltDocs)
		Collection docsToIndex = null;
		if (prebuiltDocs.size() > 0) {
			docsToIndex = new HashSet(newDocs);
			for (Iterator it = prebuiltDocs.keySet().iterator(); it.hasNext();) {
				String href = (String) it.next();
				URL u = SearchIndex.getIndexableURL(index.getLocale(), href);
				if (u != null) {
					docsToIndex.remove(u);
				}
			}
		} else {
			docsToIndex = newDocs;
		}
		return docsToIndex;
	}

	/**
	 * @param docsToDelete
	 *            Keys are /pluginid/href of all merged Docs. Values are null to
	 *            delete href, or String[] of indexIds to delete duplicates with
	 *            given index IDs
	 */
	private void removeNewDocuments(IProgressMonitor pm, Map docsToDelete)
			throws IndexingException {
		pm = new LazyProgressMonitor(pm);
		pm.beginTask("", docsToDelete.size()); //$NON-NLS-1$
		checkCancelled(pm);
		Set keysToDelete = docsToDelete.keySet();
		if (keysToDelete.size() > 0) {
			if (!index.beginRemoveDuplicatesBatch()) {
				throw new IndexingException();
			}
			MultiStatus multiStatus = null;
			for (Iterator it = keysToDelete.iterator(); it.hasNext();) {
				String href = (String) it.next();
				String[] indexIds = (String[]) docsToDelete.get(href);
				if (indexIds == null) {
					// delete all copies
					index.removeDocument(href);
					continue;
				}
				IStatus status = index.removeDuplicates(href, indexIds);
				if (status.getCode() != IStatus.OK) {
					if (multiStatus == null) {
						multiStatus = new MultiStatus(
								HelpBasePlugin.PLUGIN_ID,
								IStatus.WARNING,
								"Some help documents could not removed from index.", //$NON-NLS-1$
								null);
					}
					multiStatus.add(status);
				}
				checkCancelled(pm);
				pm.worked(1);
				if (multiStatus != null) {
					HelpBasePlugin.logStatus(multiStatus);
				}
			}
			if (!index.endRemoveDuplicatesBatch()) {
				throw new IndexingException();
			}
		}
		pm.done();
	}

	private void addDocuments(IProgressMonitor pm, Collection addedDocs,
			boolean lastOperation) throws IndexingException {
		pm = new LazyProgressMonitor(pm);
		// beginAddBatch()) called when processing prebuilt indexes
		pm.beginTask("", addedDocs.size()); //$NON-NLS-1$
		checkCancelled(pm);
		pm.subTask(HelpBaseResources.UpdatingIndex);
		MultiStatus multiStatus = null;
		for (Iterator it = addedDocs.iterator(); it.hasNext();) {
			URL doc = (URL) it.next();
			IStatus status = index.addDocument(getName(doc), doc);
			if (status.getCode() != IStatus.OK) {
				if (multiStatus == null) {
					multiStatus = new MultiStatus(
							HelpBasePlugin.PLUGIN_ID,
							IStatus.ERROR,
							"Help documentation could not be indexed completely.", //$NON-NLS-1$
							null);
				}
				multiStatus.add(status);
			}
			checkCancelled(pm);
			pm.worked(1);
		}
		if (multiStatus != null) {
			HelpBasePlugin.logStatus(multiStatus);
		}
		pm.subTask(HelpBaseResources.Writing_index);
		if (!index.endAddBatch(addedDocs.size() > 0, lastOperation))
			throw new IndexingException();
		pm.done();
	}

	private void removeStaleDocuments(IProgressMonitor pm,
			Collection removedDocs) throws IndexingException {
		pm = new LazyProgressMonitor(pm);
		pm.beginTask("", removedDocs.size()); //$NON-NLS-1$
		pm.subTask(HelpBaseResources.Preparing_for_indexing);
		checkCancelled(pm);

		if (numRemoved > 0) {
			if (HelpBasePlugin.DEBUG_SEARCH) {
				System.out.println("SearchIndex.removeStaleDocuments"); //$NON-NLS-1$
			}

			if (!index.beginDeleteBatch())
				throw new IndexingException();
			checkCancelled(pm);
			pm.subTask(HelpBaseResources.UpdatingIndex);
			MultiStatus multiStatus = null;
			for (Iterator it = removedDocs.iterator(); it.hasNext();) {
				URL doc = (URL) it.next();
				IStatus status = index.removeDocument(getName(doc));
				if (status.getCode() != IStatus.OK) {
					if (multiStatus == null) {
						multiStatus = new MultiStatus(
								HelpBasePlugin.PLUGIN_ID,
								IStatus.WARNING,
								"Uninstalled or updated help documents could not be removed from index.", //$NON-NLS-1$
								null);
					}
					multiStatus.add(status);
				}
				checkCancelled(pm);
				pm.worked(1);
			}
			if (multiStatus != null) {
				HelpBasePlugin.logStatus(multiStatus);
			}
			if (!index.endDeleteBatch()) {
				throw new IndexingException();
			}
		}
		pm.done();
	}

	/**
	 * Returns the document identifier. Currently we use the document file name
	 * as identifier.
	 */
	private String getName(URL doc) {
		String name = doc.getFile();
		// remove query string if any
		int i = name.indexOf('?');
		if (i != -1)
			name = name.substring(0, i);
		return name;
	}

	public class IndexingException extends Exception {
		private static final long serialVersionUID = 1L;
	}

	/**
	 * Returns IDs of plugins which need docs added to index.
	 */
	private Collection getAddedPlugins(SearchIndex index) {
		// Get the list of added plugins
		Collection addedPlugins = index.getDocPlugins().getAdded();

		if (addedPlugins == null || addedPlugins.isEmpty())
			return new ArrayList(0);
		return addedPlugins;
	}

	/**
	 * Returns the documents to be added to index. The collection consists of
	 * the associated PluginURL objects.
	 */
	private Collection getAddedDocuments(SearchIndex index) {
		// Get the list of added plugins
		Collection addedPlugins = getAddedPlugins(index);
		// get the list of all navigation urls.
		Set urls = getAllDocuments(index.getLocale());
		Set addedDocs = new HashSet(urls.size());
		for (Iterator docs = urls.iterator(); docs.hasNext();) {
			String doc = (String) docs.next();
			// Assume the url is /pluginID/path_to_topic.html
			int i = doc.indexOf('/', 1);
			String plugin = i == -1 ? "" : doc.substring(1, i); //$NON-NLS-1$
			if (!addedPlugins.contains(plugin)) {
				continue;
			}

			URL url = SearchIndex.getIndexableURL(index.getLocale(), doc);
			if (url != null) {
				addedDocs.add(url);
			}
		}
		//Add documents from global search participants
		LuceneSearchParticipant[] participants = BaseHelpSystem.getSearchManager().getGlobalParticipants();
		for (int j=0; j<participants.length; j++) {
			Set set = participants[j].getAllDocuments(index.getLocale());
			for (Iterator docs = set.iterator(); docs.hasNext();) {
				String doc = (String) docs.next();
				String id = null;
				int qloc = doc.indexOf('?');
				if (qloc!= -1) {
					String query = doc.substring(qloc+1);
					doc = doc.substring(0, qloc);
					HashMap arguments = new HashMap();
					HelpURLConnection.parseQuery(query, arguments);
					id = (String)arguments.get("id");
				}
				// Assume the url is /pluginID/path_to_topic.html
				int i = doc.indexOf('/', 1);
				String plugin = i == -1 ? "" : doc.substring(1, i); //$NON-NLS-1$
				if (!addedPlugins.contains(plugin)) {
					continue;
				}

				URL url = SearchIndex.getIndexableURL(index.getLocale(), doc, id, participants[j].getId());
				if (url != null) {
					addedDocs.add(url);
				}
			}			
		}
		return addedDocs;
	}

	/**
	 * Returns the documents to be removed from index. The collection consists
	 * of the associated PluginURL objects.
	 */
	private Collection getRemovedDocuments(SearchIndex index) {
		// Get the list of removed plugins
		Collection removedPlugins = index.getDocPlugins().getRemoved();
		if (removedPlugins == null || removedPlugins.isEmpty())
			return new ArrayList(0);
		// get the list of indexed docs. This is a hashtable (url, plugin)
		HelpProperties indexedDocs = index.getIndexedDocs();
		Set removedDocs = new HashSet(indexedDocs.size());
		for (Iterator docs = indexedDocs.keySet().iterator(); docs.hasNext();) {
			String doc = (String) docs.next();
			// Assume the url is /pluginID/path_to_topic.html
			int i = doc.indexOf('/', 1);
			String plugin = i == -1 ? "" : doc.substring(1, i); //$NON-NLS-1$
			if (!removedPlugins.contains(plugin)) {
				continue;
			}

			URL url = SearchIndex.getIndexableURL(index.getLocale(), doc);
			if (url != null) {
				removedDocs.add(url);
			}
		}
		return removedDocs;
	}

	/**
	 * Adds the topic and its subtopics to the list of documents
	 */
	private void add(ITopic topic, Set hrefs) {
		String href = topic.getHref();
		if (href != null
				&& !href.equals("") && !href.startsWith("http://") && !href.startsWith("https://")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			hrefs.add(href);
		ITopic[] subtopics = topic.getSubtopics();
		for (int i = 0; i < subtopics.length; i++)
			add(subtopics[i], hrefs);
	}

	/**
	 * Returns the collection of href's for all the help topics.
	 */
	private Set getAllDocuments(String locale) {
		// Add documents from TOCs
		HashSet hrefs = new HashSet();
		IToc[] tocs = index.getTocManager().getTocs(locale);
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
			ITopic tocDescriptionTopic = tocs[i].getTopic(null);
			if (tocDescriptionTopic != null)
				add(tocDescriptionTopic, hrefs);
		}
		return hrefs;
	}

	/**
	 * Obtains PluginIndexes pointing to prebuilt indexes
	 * 
	 * @param pluginIds
	 * @param locale
	 * @return
	 */
	private PrebuiltIndexes getIndexesToAdd(Collection pluginIds) {
		PrebuiltIndexes indexes = new PrebuiltIndexes(index);
		for (Iterator it = pluginIds.iterator(); it.hasNext();) {
			String pluginId = (String) it.next();
			String indexPath = HelpPlugin.getTocManager()
					.getIndexPath(pluginId);
			if (indexPath != null) {
				indexes.add(pluginId, indexPath);
			}
		}
		return indexes;
	}

	/**
	 * 
	 * @param monitor
	 * @param addedDocs
	 * @param indices
	 * @return Map. Keys are /pluginid/href of all merged Docs. Values are null
	 *         for added document, or String[] of indexIds with duplicates of
	 *         the document
	 */
	private Map mergeIndexes(IProgressMonitor monitor, boolean opened)
			throws IndexingException {
		Collection addedPluginIds = getAddedPlugins(index);
		PrebuiltIndexes indexes = getIndexesToAdd(addedPluginIds);
		PluginIndex[] pluginIndexes = indexes.getIndexes();
		if (HelpBasePlugin.DEBUG_SEARCH) {
			System.out
					.println("IndexingOperation.mergeIndexes: " + pluginIndexes.length + " plugins with prebuilt index."); //$NON-NLS-1$ //$NON-NLS-2$
		}
		Map mergedDocs = null;
		// Always perform add batch to ensure that index is created and saved
		// even if no new documents
		if (!index.beginAddBatch(opened)) {
			throw new IndexingException();
		}
		if (pluginIndexes.length > 0) {
			mergedDocs = index.merge(pluginIndexes, monitor);
		}

		if (mergedDocs == null) {
			return Collections.EMPTY_MAP;
		}
		return mergedDocs;
	}

}