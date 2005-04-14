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

import java.net.*;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.help.*;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.base.*;
import org.eclipse.help.internal.base.util.*;
import org.eclipse.help.internal.protocols.*;
import org.eclipse.help.internal.toc.*;

/**
 * Indexing Operation represents a long operation, which performs indexing of
 * the group (Collection) of documents. It is used Internally by SlowIndex and
 * returned by its getIndexUpdateOperation() method.
 */
class IndexingOperation {
	private int numAdded;
	private int numRemoved;
	private SearchIndex index = null;
	// Constants for alocating progress among subtasks.
	// The goal is to have a ratio among them
	// that results in work accumulating at a constant rate.
	private final static int WORK_PREPARE = 1; // * all documents
	private final static int WORK_DELETEDOC = 5; // * removed documents
	private final static int WORK_INDEXDOC = 50; // * added documents
	private final static int WORK_SAVEINDEX = 2; // * all documents
	private int workTotal;
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
		Collection removedDocs = getRemovedDocuments(index);
		numRemoved = removedDocs.size();
		Collection addedDocs = getAddedDocuments(index);
		numAdded = addedDocs.size();

		workTotal = (numRemoved + numAdded) * WORK_PREPARE + numAdded
				* WORK_INDEXDOC + (numRemoved + numAdded) * WORK_SAVEINDEX;

		if (numRemoved > 0) {
			workTotal += (numRemoved + numAdded) * WORK_PREPARE + numRemoved
					* WORK_DELETEDOC + (numRemoved + numAdded) * WORK_SAVEINDEX;
		}
		// if collection is empty, we may return right away
		// need to check if we have to do anything to the progress monitor
		if (numRemoved + numAdded <= 0) {
			pm.done();
			return;
		}

		LazyProgressMonitor monitor = new LazyProgressMonitor(pm);
		monitor.beginTask("", workTotal); //$NON-NLS-1$
		removeDocuments(monitor, removedDocs);
		addDocuments(monitor, addedDocs);
		monitor.done();
	}

	private void addDocuments(IProgressMonitor pm, Collection addedDocs)
			throws IndexingException {
		// Do not check here if (addedDocs.size() > 0), always perform add batch
		// to ensure that index is created and saved even if no new documents
		// exist

		// now add all the new documents
		if (!index.beginAddBatch()) {
			throw new IndexingException();
		}
		try {
			checkCancelled(pm);
			pm.worked((numRemoved + numAdded) * WORK_PREPARE);
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
								"Help documentation could not be indexed completely.",
								null);
					}
					multiStatus.add(status);
				}
				checkCancelled(pm);
				pm.worked(WORK_INDEXDOC);
			}
			if(multiStatus!=null){
				HelpPlugin.logError(multiStatus);
				
			}
		} catch (OperationCanceledException oce) {
			// Need to perform rollback on the index
			pm.subTask(HelpBaseResources.Undoing_document_adds); 
			//			if (!index.abortUpdate())
			//				throw new Exception();
			throw oce;
		}
		pm.subTask(HelpBaseResources.Writing_index); 
		if (!index.endAddBatch())
			throw new IndexingException();
	}

	private void removeDocuments(IProgressMonitor pm, Collection removedDocs)
			throws IndexingException {

		pm.subTask(HelpBaseResources.Preparing_for_indexing); 
		checkCancelled(pm);

		if (numRemoved > 0) {
			if (!index.beginDeleteBatch())
				throw new IndexingException();
			try {
				checkCancelled(pm);
				pm.worked((numRemoved + numAdded) * WORK_PREPARE);
				pm.subTask(HelpBaseResources.UpdatingIndex); 
				for (Iterator it = removedDocs.iterator(); it.hasNext();) {
					URL doc = (URL) it.next();
					index.removeDocument(getName(doc));
					checkCancelled(pm);
					pm.worked(WORK_DELETEDOC);
				}
			} catch (OperationCanceledException oce) {
				// Need to perform rollback on the index
				pm.subTask(HelpBaseResources.Undoing_document_deletions); //$NON-NLS-1$
				//			if (!index.abortUpdate())
				//				throw new Exception();
				throw oce;
			}
			if (!index.endDeleteBatch()) {
				throw new IndexingException();
			}
			pm.worked((numRemoved + numAdded) * WORK_SAVEINDEX);
		}
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
	 * Returns the documents to be added to index. The collection consists of
	 * the associated PluginURL objects.
	 */
	private Collection getAddedDocuments(SearchIndex index) {
		// Get the list of added plugins
		Collection addedPlugins = index.getDocPlugins().getAdded();
		if (addedPlugins == null || addedPlugins.isEmpty())
			return new ArrayList(0);
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
}
