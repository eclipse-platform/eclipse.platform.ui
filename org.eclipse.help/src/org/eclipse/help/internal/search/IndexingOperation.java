package org.eclipse.help.internal.search;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.*;
import java.net.URL;
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
	private Collection removedDocs = null;
	private SearchIndex index = null;
	// Constants for calculating progress
	private final static int WORK_PREPARE = 50;
	private final static int WORK_INDEXDOC = 10;
	private final static int WORK_SAVEINDEX = 200;
	private final static int BUF_SIZE = 256 * 1024;
	private static byte[] buf = new byte[BUF_SIZE];
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
	}
	/**
	 * Adds document  to the index.
	 * @param doc PluginURL
	 */
	private void add(URL doc) {
		try {
			InputStream contentStream = doc.openStream();
			if (contentStream == null) {
				Logger.logError(Resources.getString("IS001", doc.getPath()), null);
				return;
			}
			// index document 
			if (!index.addDocument(getName(doc), contentStream))
				Logger.logInfo(Resources.getString("IS002", doc.getPath()));
			if (contentStream != null)
				contentStream.close();
		} catch (IOException ioe10) {
		}
	}
	/**
	 * Removes document from index.
	 * @param doc PluginURL
	 */
	private void remove(URL doc) {
		if (!index.removeDocument(getName(doc)))
			Logger.logInfo(Resources.getString("IS003", doc.getFile()));
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
		throws OperationCanceledException, Exception {
		// if collection is empty, we may return right away
		// need to check if we have to do anything to the progress monitor
		int numDocs = removedDocs.size() + addedDocs.size();
		if (numDocs <= 0)
			return;
		int workTotal = WORK_PREPARE + numDocs * WORK_INDEXDOC + WORK_SAVEINDEX;
		pm.beginTask(""/*Resources.getString("Index_needs_updated")*/, workTotal);
		pm.subTask(Resources.getString("Preparing_for_indexing"));
		checkCancelled(pm);
		// first delete all the removed documents
		if (removedDocs.size() > 0) {
			if (!index.beginDeleteBatch())
				throw new Exception();
			try {
				checkCancelled(pm);
				pm.worked(WORK_PREPARE);
				for (Iterator it = removedDocs.iterator(); it.hasNext();) {
					URL doc = (URL) it.next();
					pm.subTask(Resources.getString("Removing") + doc.getFile());
					remove(doc);
					checkCancelled(pm);
					pm.worked(WORK_INDEXDOC);
				}
			} catch (OperationCanceledException oce) {
				// Need to perform rollback on the index
				pm.subTask(Resources.getString("Undoing_document_deletions"));
				pm.worked(workTotal);
				//			if (!index.abortUpdate())
				//				throw new Exception();
				throw oce;
			}
			if (!index.endDeleteBatch())
				throw new Exception();
		}
		if (addedDocs.size() > 0) {
			// now add all the new documents
			if (!index.beginAddBatch())
				throw new Exception();
			try {
				checkCancelled(pm);
				pm.worked(WORK_PREPARE);
				for (Iterator it = addedDocs.iterator(); it.hasNext();) {
					URL doc = (URL) it.next();
					pm.subTask(Resources.getString("Indexing") + doc.getFile());
					add(doc);
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
				throw new Exception();
		}
		pm.done();
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
}