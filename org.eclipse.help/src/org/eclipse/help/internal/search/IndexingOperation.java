/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.search;

import java.net.*;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.util.*;

/**
 * Indexing Operation represents a long operation,
 * which performs indexing of the group (Collection) of documents.
 * It is used Internally by SlowIndex and returned by its getIndexUpdateOperation() method.
 */
class IndexingOperation {
	private Collection addedDocs = null;
	private int numAdded;
	private Collection removedDocs = null;
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
	 * @param ix ISearchIndex already opened
	 * @param removedDocs collection of removed documents, including changed ones
	 * @param addedDocs collection of new documents, including changed ones
	 */
	public IndexingOperation(
		SearchIndex ix,
		Collection removedDocs,
		Collection addedDocs) {
		this.index = ix;
		this.removedDocs = removedDocs;
		this.addedDocs = addedDocs;
		numRemoved = this.removedDocs.size();
		numAdded = this.addedDocs.size();
		workTotal =
			(numRemoved + numAdded) * WORK_PREPARE
				+ numAdded * WORK_INDEXDOC
				+ (numRemoved + numAdded) * WORK_SAVEINDEX;

		if (numRemoved > 0) {
			workTotal += (numRemoved + numAdded) * WORK_PREPARE
				+ numRemoved * WORK_DELETEDOC
				+ (numRemoved + numAdded) * WORK_SAVEINDEX;
		}
	}

	private void checkCancelled(IProgressMonitor pm)
		throws OperationCanceledException {
		if (pm.isCanceled())
			throw new OperationCanceledException();
	}
	/**
	 * Executes indexing, given the progress monitor.
	 * @param monitor progres monitor to be used during this long operation
	 *  for reporting progress
	 * @throws OperationCanceledException if indexing was cancelled
	 * @throws Exception if error occured
	 */
	protected void execute(IProgressMonitor pm)
		throws OperationCanceledException, IndexingException {

		// if collection is empty, we may return right away
		// need to check if we have to do anything to the progress monitor
		if (numRemoved + numAdded <= 0) {
			pm.done();
			return;
		}

		LazyProgressMonitor monitor = new LazyProgressMonitor(pm);
		monitor.beginTask("", workTotal);
		removeDocuments(monitor);
		addDocuments(monitor);
		monitor.done();
	}

	private void addDocuments(IProgressMonitor pm) throws IndexingException {
		// Do not check here if (addedDocs.size() > 0), always perform add batch
		// to ensure that index is created and saved even if no new documents exist

		// now add all the new documents
		if (!index.beginAddBatch()) {
			throw new IndexingException();
		}
		try {
			checkCancelled(pm);
			pm.worked((numRemoved + numAdded) * WORK_PREPARE);
			pm.subTask(Resources.getString("UpdatingIndex"));
			for (Iterator it = addedDocs.iterator(); it.hasNext();) {
				URL doc = (URL) it.next();
				index.addDocument(getName(doc), doc);
				checkCancelled(pm);
				pm.worked(WORK_INDEXDOC);
			}
		} catch (OperationCanceledException oce) {
			// Need to perform rollback on the index
			pm.subTask(Resources.getString("Undoing_document_adds"));
			pm.worked(workTotal);
			//			if (!index.abortUpdate())
			//				throw new Exception();
			throw oce;
		}
		pm.subTask(Resources.getString("Writing_index"));
		if (!index.endAddBatch())
			throw new IndexingException();
	}

	private void removeDocuments(IProgressMonitor pm)
		throws IndexingException {

		pm.subTask(Resources.getString("Preparing_for_indexing"));
		checkCancelled(pm);

		if (numRemoved > 0) {
			if (!index.beginDeleteBatch())
				throw new IndexingException();
			try {
				checkCancelled(pm);
				pm.worked((numRemoved + numAdded) * WORK_PREPARE);
				pm.subTask(Resources.getString("UpdatingIndex"));
				for (Iterator it = removedDocs.iterator(); it.hasNext();) {
					URL doc = (URL) it.next();
					index.removeDocument(getName(doc));
					checkCancelled(pm);
					pm.worked(WORK_DELETEDOC);
				}
			} catch (OperationCanceledException oce) {
				// Need to perform rollback on the index
				pm.subTask(Resources.getString("Undoing_document_deletions"));
				pm.worked(workTotal);
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
	 * Returns the document identifier. Currently we use the 
	 * document file name as identifier.
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
	}
}