/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.search;
import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.util.*;
import java.util.zip.*;

import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.base.*;
import org.eclipse.help.internal.base.util.*;
import org.eclipse.help.internal.toc.*;
import org.eclipse.help.internal.util.*;
import org.osgi.framework.*;
/**
 * Text search index. Documents added to this index can than be searched against
 * a search query.
 */
public class SearchIndex {
	private IndexReader ir;
	private IndexWriter iw;
	private File indexDir;
	private String locale;
	private TocManager tocManager;
	private AnalyzerDescriptor analyzerDescriptor;
	private PluginVersionInfo docPlugins;
	// table of all document names, used during indexing batches
	private HelpProperties indexedDocs;
	private static final String INDEXED_CONTRIBUTION_INFO_FILE = "indexed_contributions"; //$NON-NLS-1$
	public static final String INDEXED_DOCS_FILE = "indexed_docs"; //$NON-NLS-1$
	private static final String DEPENDENCIES_VERSION_FILENAME = "indexed_dependencies"; //$NON-NLS-1$
	private static final String LUCENE_PLUGIN_ID = "org.apache.lucene"; //$NON-NLS-1$
	private File inconsistencyFile;
	private HTMLDocParser parser;
	private IndexSearcher searcher;
	private Object searcherCreateLock = new Object();
	private HelpProperties dependencies;
	private boolean closed = false;
	// Collection of searches occuring now
	private Collection searches = new ArrayList();
	private FileLock lock;
	/**
	 * Constructor.
	 * 
	 * @param locale
	 *            the locale this index uses
	 * @param analyzerDesc
	 *            the analyzer used to index
	 */
	public SearchIndex(String locale, AnalyzerDescriptor analyzerDesc,
			TocManager tocManager) {
		this.locale = locale;
		this.analyzerDescriptor = analyzerDesc;
		this.tocManager = tocManager;
		indexDir = new File(HelpBasePlugin.getConfigurationDirectory(),
				"index/" + locale); //$NON-NLS-1$
		inconsistencyFile = new File(indexDir.getParentFile(), locale
				+ ".inconsistent"); //$NON-NLS-1$
		parser = new HTMLDocParser();
		if (!exists()) {
			try {
				if (tryLock()) {
					// don't block or unzip when another instance is indexing
					try {
						unzipProductIndex();
					} finally {
						releaseLock();
					}
				}
			} catch (OverlappingFileLockException ofle) {
				// another thread in this process is unzipping
				// should never be here - one index instance per locale exists in vm
			}
		}
	}
	/**
	 * Indexes one document from a stream. Index has to be open and close
	 * outside of this method
	 * 
	 * @param name
	 *            the document identifier (could be a URL)
	 * @param url
	 *            the URL of the document
	 * @return true if success
	 */
	public boolean addDocument(String name, URL url) {
		if (HelpBasePlugin.DEBUG_SEARCH) {
			System.out.println("SearchIndex.addDocument(" + name + ", " + url //$NON-NLS-1$ //$NON-NLS-2$
					+ ")"); //$NON-NLS-1$
		}
		try {
			Document doc = new Document();
			doc.add(Field.Keyword("name", name)); //$NON-NLS-1$
			try {
				try {
					parser.openDocument(url);
				} catch (IOException ioe) {
					HelpBasePlugin.logError(HelpBaseResources.getString("ES25", //$NON-NLS-1$
							name), null);
					return false;
				}
				ParsedDocument parsed = new ParsedDocument(parser
						.getContentReader());
				doc.add(Field.Text("contents", parsed.newContentReader())); //$NON-NLS-1$
				doc.add(Field.Text("exact_contents", parsed //$NON-NLS-1$
						.newContentReader()));
				String title = parser.getTitle();
				doc.add(Field.UnStored("title", title)); //$NON-NLS-1$
				doc.add(Field.UnStored("exact_title", title)); //$NON-NLS-1$
				doc.add(Field.UnIndexed("raw_title", title)); //$NON-NLS-1$
				// doc.add(Field.UnIndexed("summary", parser.getSummary()));
				iw.addDocument(doc);
			} finally {
				parser.closeDocument();
			}
			indexedDocs.put(name, "0"); //$NON-NLS-1$
			return true;
		} catch (IOException e) {
			HelpBasePlugin.logError(HelpBaseResources.getString("ES16", name, //$NON-NLS-1$
					indexDir.getAbsolutePath()), e);
			return false;
		}
	}
	/**
	 * Starts additions. To be called before adding documents.
	 */
	public synchronized boolean beginAddBatch() {
		try {
			if (iw != null) {
				iw.close();
			}
			boolean create = false;
			if (!exists()) {
				create = true;
				indexDir.mkdirs();
				if (!indexDir.exists())
					return false; // unable to setup index directory
			}
			indexedDocs = new HelpProperties(INDEXED_DOCS_FILE, indexDir);
			indexedDocs.restore();
			setInconsistent(true);
			iw = new IndexWriter(indexDir, analyzerDescriptor.getAnalyzer(),
					create);
			iw.mergeFactor = 20;
			iw.maxFieldLength = 1000000;
			return true;
		} catch (IOException e) {
			HelpBasePlugin.logError(HelpBaseResources.getString("ES17"), e); //$NON-NLS-1$
			return false;
		}
	}
	/**
	 * Starts deletions. To be called before deleting documents.
	 */
	public synchronized boolean beginDeleteBatch() {
		try {
			if (ir != null) {
				ir.close();
			}
			indexedDocs = new HelpProperties(INDEXED_DOCS_FILE, indexDir);
			indexedDocs.restore();
			setInconsistent(true);
			ir = IndexReader.open(indexDir);
			return true;
		} catch (IOException e) {
			HelpBasePlugin.logError(HelpBaseResources.getString("ES18"), e); //$NON-NLS-1$
			return false;
		}
	}
	/**
	 * Deletes a single document from the index.
	 * 
	 * @param name -
	 *            document name
	 * @return true if success
	 */
	public boolean removeDocument(String name) {
		if (HelpBasePlugin.DEBUG_SEARCH) {
			System.out.println("SearchIndex.removeDocument(" + name + ")"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		Term term = new Term("name", name); //$NON-NLS-1$
		try {
			ir.delete(term);
			indexedDocs.remove(name);
		} catch (IOException e) {
			HelpBasePlugin.logError(HelpBaseResources.getString("ES22", name, //$NON-NLS-1$
					indexDir.getAbsolutePath()), e);
			return false;
		}
		return true;
	}
	/**
	 * Finish additions. To be called after adding documents.
	 */
	public synchronized boolean endAddBatch() {
		try {
			if (iw == null)
				return false;
			iw.optimize();
			iw.close();
			// save the update info:
			//  - all the docs
			//  - plugins (and their version) that were indexed
			indexedDocs.save();
			indexedDocs = null;
			getDocPlugins().save();
			saveDependencies();
			setInconsistent(false);
			return true;
		} catch (IOException e) {
			HelpBasePlugin.logError(HelpBaseResources.getString("ES19"), e); //$NON-NLS-1$
			return false;
		}
	}
	/**
	 * Finish deletions. To be called after deleting documents.
	 */
	public synchronized boolean endDeleteBatch() {
		try {
			if (ir == null)
				return false;
			ir.close();
			// save the update info:
			//  - all the docs
			//  - plugins (and their version) that were indexed
			indexedDocs.save();
			indexedDocs = null;
			getDocPlugins().save();
			saveDependencies();
			setInconsistent(false);
			return true;
		} catch (IOException e) {
			HelpBasePlugin.logError(HelpBaseResources.getString("ES20"), e); //$NON-NLS-1$
			return false;
		}
	}
	/**
	 * Checks if index exists and is usable.
	 * 
	 * @return true if index exists
	 */
	public boolean exists() {
		return indexDir.exists() && !isInconsistent();
		// assume index exists if directory does
	}
	/**
	 * Performs a query search on this index
	 */
	public void search(ISearchQuery searchQuery, ISearchHitCollector collector)
			throws QueryTooComplexException {
		try {
			if (closed)
				return;
			registerSearch(Thread.currentThread());
			if (closed)
				return;
			QueryBuilder queryBuilder = new QueryBuilder(searchQuery
					.getSearchWord(), analyzerDescriptor);
			Query luceneQuery = queryBuilder.getLuceneQuery(searchQuery
					.getFieldNames(), searchQuery.isFieldSearch());
			String highlightTerms = queryBuilder.gethighlightTerms();
			if (luceneQuery != null) {
				if (searcher == null) {
					openSearcher();
				}
				Hits hits = searcher.search(luceneQuery);
				collector.addHits(hits, highlightTerms);
			}
		} catch (QueryTooComplexException qe) {
			throw qe;
		} catch (Exception e) {
			HelpBasePlugin.logError(HelpBaseResources.getString("ES21", //$NON-NLS-1$
					searchQuery.getSearchWord()), e);
		} finally {
			unregisterSearch(Thread.currentThread());
		}
	}
	public String getLocale() {
		return locale;
	}
	/**
	 * Returns the list of all the plugins in this session that have declared a
	 * help contribution.
	 */
	public PluginVersionInfo getDocPlugins() {
		if (docPlugins == null) {
			Collection docPluginsIds = tocManager.getContributingPlugins();
			docPlugins = new PluginVersionInfo(INDEXED_CONTRIBUTION_INFO_FILE,
					docPluginsIds, indexDir, !exists());
		}
		return docPlugins;
	}
	/**
	 * We use HelpProperties, but a list would suffice. We only need the key
	 * values.
	 * 
	 * @return HelpProperties, keys are URLs of indexed documents
	 */
	public HelpProperties getIndexedDocs() {
		HelpProperties indexedDocs = new HelpProperties(INDEXED_DOCS_FILE,
				indexDir);
		if (exists())
			indexedDocs.restore();
		return indexedDocs;
	}
	/**
	 * Gets properties with versions of Lucene plugin and Analyzer used for
	 * indexing
	 */
	private HelpProperties getDependencies() {
		if (dependencies == null) {
			dependencies = new HelpProperties(DEPENDENCIES_VERSION_FILENAME,
					indexDir);
			dependencies.restore();
		}
		return dependencies;
	}
	/**
	 * Gets analyzer identifier from a file.
	 */
	private String readAnalyzerId() {
		String analyzerVersion = getDependencies().getProperty("analyzer"); //$NON-NLS-1$
		if (analyzerVersion == null) {
			return ""; //$NON-NLS-1$
		}
		return analyzerVersion;
	}
	/**
	 * Gets Lucene plugin version from a file.
	 */
	private boolean isLuceneCompatible() {
		String usedLuceneVersion = getDependencies().getProperty("lucene"); //$NON-NLS-1$
		String currentLuceneVersion = ""; //$NON-NLS-1$
		Bundle lucenePluginDescriptor = Platform.getBundle(LUCENE_PLUGIN_ID);
		if (lucenePluginDescriptor != null) {
			currentLuceneVersion += (String) lucenePluginDescriptor
					.getHeaders().get(Constants.BUNDLE_VERSION);
		}
		// Later might add code to return true for other known cases
		// of compatibility between post 1.2.1 versions.
		return currentLuceneVersion.equals(usedLuceneVersion);
	}
	/**
	 * Saves Lucene version and analyzer identifier to a file.
	 */
	private void saveDependencies() {
		getDependencies().put("analyzer", analyzerDescriptor.getId()); //$NON-NLS-1$
		Bundle luceneBundle = Platform.getBundle(LUCENE_PLUGIN_ID);
		if (luceneBundle != null) {
			String luceneBundleVersion = "" //$NON-NLS-1$
					+ luceneBundle.getHeaders().get(Constants.BUNDLE_VERSION);
			getDependencies().put("lucene", luceneBundleVersion); //$NON-NLS-1$
		} else {
			getDependencies().put("lucene", ""); //$NON-NLS-1$ //$NON-NLS-2$
		}
		getDependencies().save();
	}
	/**
	 * @return Returns true if index has been left in inconsistent state If
	 *         analyzer has changed to incompatible one, index is treated as
	 *         inconsistent as well.
	 */
	public boolean isInconsistent() {
		if (inconsistencyFile.exists()) {
			return true;
		}
		return !isLuceneCompatible()
				|| !analyzerDescriptor.isCompatible(readAnalyzerId());
	}
	/**
	 * Writes or deletes inconsistency flag file
	 */
	public void setInconsistent(boolean inconsistent) {
		if (inconsistent) {
			try {
				// parent directory already created by beginAddBatch on new
				// index
				FileOutputStream fos = new FileOutputStream(inconsistencyFile);
				fos.close();
			} catch (IOException ioe) {
			}
		} else
			inconsistencyFile.delete();
	}
	public void openSearcher() throws IOException {
		synchronized (searcherCreateLock) {
			if (searcher == null) {
				searcher = new IndexSearcher(indexDir.getAbsolutePath());
			}
		}
	}
	/**
	 * Closes IndexReader used by Searcher. Should be called on platform
	 * shutdown, or when TOCs have changed when no more reading from this index
	 * is to be performed.
	 */
	public void close() {
		closed = true;
		// wait for all sarches to finish
		synchronized (searches) {
			while (searches.size() > 0) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException ie) {
				}
			}
			//
			if (searcher != null) {
				try {
					searcher.close();
				} catch (IOException ioe) {
				}
			}
		}
	}
	/**
	 * Finds and unzips prebuild index specified in preferences
	 */
	private void unzipProductIndex() {
		String indexPluginId = HelpBasePlugin.getDefault()
				.getPluginPreferences().getString("productIndex"); //$NON-NLS-1$
		if (indexPluginId == null || indexPluginId.length() <= 0) {
			return;
		}
		InputStream zipIn = ResourceLocator.openFromPlugin(indexPluginId,
				"doc_index.zip", getLocale()); //$NON-NLS-1$
		if (zipIn == null) {
			return;
		}
		byte[] buf = new byte[8192];
		File destDir = indexDir;
		ZipInputStream zis = new ZipInputStream(zipIn);
		FileOutputStream fos = null;
		try {
			ZipEntry zEntry;
			while ((zEntry = zis.getNextEntry()) != null) {
				// if it is empty directory, create it
				if (zEntry.isDirectory()) {
					new File(destDir, zEntry.getName()).mkdirs();
					continue;
				}
				// if it is a file, extract it
				String filePath = zEntry.getName();
				int lastSeparator = filePath.lastIndexOf("/"); //$NON-NLS-1$
				String fileDir = ""; //$NON-NLS-1$
				if (lastSeparator >= 0) {
					fileDir = filePath.substring(0, lastSeparator);
				}
				//create directory for a file
				new File(destDir, fileDir).mkdirs();
				//write file
				File outFile = new File(destDir, filePath);
				fos = new FileOutputStream(outFile);
				int n = 0;
				while ((n = zis.read(buf)) >= 0) {
					fos.write(buf, 0, n);
				}
				fos.close();
			}
			if (HelpBasePlugin.DEBUG_SEARCH) {
				System.out.println("SearchIndex: Prebuilt index restored to " //$NON-NLS-1$
						+ destDir + "."); //$NON-NLS-1$
			}
		} catch (IOException ioe) {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException ioe2) {
				}
			}
		} finally {
			try {
				zipIn.close();
				if (zis != null)
					zis.close();
			} catch (IOException ioe) {
			}
		}
	}
	/**
	 * Returns true when the index must be updated.
	 */
	public synchronized boolean needsUpdating() {
		if (!exists()) {
			return true;
		}
		return getDocPlugins().detectChange();
	}
	/**
	 * @return Returns the tocManager.
	 */
	public TocManager getTocManager() {
		return tocManager;
	}
	private void registerSearch(Thread t) {
		synchronized (searches) {
			searches.add(t);
		}
	}
	private void unregisterSearch(Thread t) {
		synchronized (searches) {
			searches.remove(t);
		}
	}
	/**
	 * @return Returns the closed.
	 */
	public boolean isClosed() {
		return closed;
	}
	/**
	 * @return true if lock obtained for this Eclipse instance
	 * @throws OverlappingFileLockException
	 *             if lock already obtained
	 */
	public synchronized boolean tryLock() throws OverlappingFileLockException {
		if (lock != null) {
			throw new OverlappingFileLockException();
		}
		File lockFile = new File(indexDir.getParentFile(), locale + ".lock"); //$NON-NLS-1$
		lockFile.getParentFile().mkdirs();
		try {
			RandomAccessFile raf = new RandomAccessFile(lockFile, "rw"); //$NON-NLS-1$
			FileLock l = raf.getChannel().tryLock();
			if (l != null) {
				lock = l;
				return true;
			}
		} catch (IOException ioe) {
			lock = null;
		}
		return false;
	}
	public synchronized void releaseLock() {
		if (lock != null) {
			try {
				lock.channel().close();
			} catch (IOException ioe) {
			}
			lock = null;
		}
	}
}