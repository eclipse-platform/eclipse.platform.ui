/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.search;

import java.io.*;
import java.util.Iterator;

import org.apache.lucene.HTMLParser.HTMLParser;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.util.*;

/**
 * Text search index.  Documents added to this index
 * can than be searched against a search query.
 */
public class SearchIndex {
	private IndexReader ir;
	private IndexWriter iw;
	private File indexDir;
	private String locale;
	private AnalyzerDescriptor analyzerDescriptor;
	private PluginVersionInfo docPlugins;
	private HelpProperties indexedDocs;
	private static final String INDEXED_CONTRIBUTION_INFO_FILE =
		"indexed_contributions";
	public static final String INDEXED_DOCS_FILE = "indexed_docs";
	private static final String ANALYZER_VERSION_FILENAME = "indexed_analyzer";
	private File analyzerVersionFile;
	private File inconsistencyFile;
	
	/**
	 * Constructor.
	 * @param locale the locale this index uses
	 * @param analyzerDesc the analyzer used to index
	 */
	public SearchIndex(String locale, AnalyzerDescriptor analyzerDesc) {
		super();
		this.locale = locale;
		analyzerDescriptor = analyzerDesc;
		String helpStatePath = HelpPlugin.getDefault().getStateLocation().toOSString();
		String searchStatePath =
			helpStatePath + File.separator + "nl" + File.separator + locale;
		indexDir = new File(searchStatePath);
		inconsistencyFile =
			new File(indexDir.getParentFile(), locale + ".inconsistent");
		analyzerVersionFile =
			new File(searchStatePath + File.separator + ANALYZER_VERSION_FILENAME);
		indexedDocs =
			new HelpProperties(
				"nl" + File.separator + locale + File.separator + INDEXED_DOCS_FILE,
				HelpPlugin.getDefault());
	}
	/**
	 * Indexes one document from a stream.
	 * Index has to be open and close outside of this method
	 * @param name the document identifier (could be a URL)
	 * @param text the text of the document
	 * @return true if success
	 */
	public boolean addDocument(String name, InputStream stream) {
		try {
			Document doc = new Document();
			doc.add(Field.Keyword("name", name));
			HTMLParser parser = new HTMLParser(stream);
			doc.add(Field.Text("contents", parser.getReader()));
			String title = "";
			try {
				title = parser.getTitle();
			} catch (InterruptedException ie) {
			}
			doc.add(Field.UnStored("title", title));
			doc.add(Field.UnIndexed("raw_title", title));
			// doc.add(Field.UnIndexed("summary", parser.getSummary()));
			iw.addDocument(doc);
			indexedDocs.put(name, "0");
			return true;
		} catch (IOException e) {
			Logger.logError(Resources.getString("ES16", indexDir.getAbsolutePath()), e);
			return false;
		}
	}
	/**
	 * Starts additions.
	 * To be called before adding documents.
	 */
	public boolean beginAddBatch() {
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
			indexedDocs.restore();
			setInconsistent(true);
			iw = new IndexWriter(indexDir, analyzerDescriptor.getAnalyzer(), create);
			iw.mergeFactor = 20;
			iw.maxFieldLength = 1000000;
			return true;
		} catch (IOException e) {
			Logger.logError(Resources.getString("ES17"), e);
			return false;
		}
	}
	/**
	 * Starts deletions.
	 * To be called before deleting documents.
	 */
	public boolean beginDeleteBatch() {
		try {
			if (ir != null) {
				ir.close();
			}
			indexedDocs.restore();
			setInconsistent(true);
			ir = IndexReader.open(indexDir);
			return true;
		} catch (IOException e) {
			Logger.logError(Resources.getString("ES18"), e);
			return false;
		}
	}
	/**
	 * Deletes a single document from the index.
	 * @param name - document name
	 * @return true if success
	 */
	public boolean removeDocument(String name) {
		Term term = new Term("name", name);
		try {
			ir.delete(term);
			indexedDocs.remove(name);
		} catch (IOException e) {
			Logger.logError(
				Resources.getString("ES22", name, indexDir.getAbsolutePath()),
				e);
			return false;
		}
		return true;
	}
	/**
	 * Finish additions.
	 * To be called after adding documents.
	 */
	public boolean endAddBatch() {
		try {
			if (iw == null)
				return false;
			iw.optimize();
			iw.close();
			// save the update info:
			//  - all the docs
			//  - plugins (and their version) that were indexed
			indexedDocs.save();
			getDocPlugins().save();
			saveAnalyzerId();
			setInconsistent(false);
			return true;
		} catch (IOException e) {
			Logger.logError(Resources.getString("ES19"), e);
			return false;
		}
	}
	/**
	 * Finish deletions.
	 * To be called after deleting documents.
	 */
	public boolean endDeleteBatch() {
		try {
			if (ir == null)
				return false;
			ir.close();
			// save the update info:
			//  - all the docs
			//  - plugins (and their version) that were indexed
			indexedDocs.save();
			getDocPlugins().save();
			saveAnalyzerId();
			setInconsistent(false);
			return true;
		} catch (IOException e) {
			Logger.logError(Resources.getString("ES20"), e);
			return false;
		}
	}
	/**
	 * Checks if index exists and is usable.
	 * @return true if index exists
	 */
	public boolean exists() {
		return indexDir.exists() && !isInconsistent();
		// assume index exists if directory does
	}
	/** 
	 * Performs a query search on this index 
	 * @param fieldNames - Collection of field names of type String (e.g. "h1");
	 *  the search will be performed on the given fields
	 * @param fieldSearch - boolean indicating if field only search
	 *  should be performed; if set to false, default field "contents"
	 *  and all other fields will be searched
	 * @param searchResult SearchResult that will contain all the hits
	 * @return - an array of document ids. 
	 * Later, we can extend this to return more data (rank, # of occs, etc.)
	 */
	public void search(ISearchQuery searchQuery, ISearchHitCollector collector) {
		try {
			QueryBuilder queryBuilder =
				new QueryBuilder(searchQuery.getSearchWord(), analyzerDescriptor.getAnalyzer());
			Query luceneQuery =
				queryBuilder.getLuceneQuery(
					searchQuery.getFieldNames(),
					searchQuery.isFieldSearch());
			String analyzedWords = queryBuilder.getAnalyzedWords();
			if (luceneQuery != null) {
				Searcher searcher = new IndexSearcher(indexDir.getAbsolutePath());
				Hits hits = searcher.search(luceneQuery);
				collector.addHits(hits, analyzedWords);
				searcher.close();
			}
		} catch (Exception e) {
			Logger.logError(Resources.getString("ES21", searchQuery.getSearchWord()), e);
		}
	}
	public String getLocale() {
		return locale;
	}
	/**
	 * Returns the list of all the plugins in this session
	 * that have declared a help contribution.
	 */
	public PluginVersionInfo getDocPlugins() {
		if (docPlugins == null) {
			Iterator docPluginsIterator =
				HelpSystem.getTocManager().getContributingPlugins().iterator();
			docPlugins =
				new PluginVersionInfo(
					"nl"
						+ File.separator
						+ locale
						+ File.separator
						+ INDEXED_CONTRIBUTION_INFO_FILE,
					docPluginsIterator,
					HelpPlugin.getDefault(),
					!exists());
		}
		return docPlugins;
	}
	/**
	 * We use HelpProperties, but a list would suffice.
	 * We only need the key values.
	 * @return HelpProperties, keys are URLs of indexed documents
	 */
	public HelpProperties getIndexedDocs() {
		HelpProperties indexedDocs =
			new HelpProperties(
				"nl" + File.separator + locale + File.separator + INDEXED_DOCS_FILE,
				HelpPlugin.getDefault());
		if (exists())
			indexedDocs.restore();
		return indexedDocs;
	}
	/**
	 * Gets analyzer identifier from a file.
	 */
	private String readAnalyzerId() {
		if (!analyzerVersionFile.exists())
			return "";
		try {
			DataInputStream dis =
				new DataInputStream(new FileInputStream(analyzerVersionFile));
			String id = dis.readUTF();
			dis.close();
			return id;
		} catch (IOException ioe) {
		}
		return "";
	}
	/**
	 * Saves analyzer identifier to a file.
	 */
	private void saveAnalyzerId() {
		try {
			DataOutputStream dos =
				new DataOutputStream(new FileOutputStream(analyzerVersionFile));
			dos.writeUTF(analyzerDescriptor.getId());
			dos.flush();
			dos.close();
		} catch (IOException ioe) {
		}
	}
	/**
	 * @return Returns true if index has been left in inconsistent state
	 * If analyzer has changed, index is treated as inconsistent as well.
	 */
	private boolean isInconsistent() {
		if (inconsistencyFile.exists()) {
			return true;
		}
		return !analyzerDescriptor.getId().equals(readAnalyzerId());
	}
	/**
	 * Writes or deletes inconsistency flag file
	 */
	private void setInconsistent(boolean inconsistent) {
		if (inconsistent) {
			try {
				// parent directory already created by beginAddBatch on new index
				FileOutputStream fos = new FileOutputStream(inconsistencyFile);
				fos.close();
			} catch (IOException ioe) {
			}
		} else
			inconsistencyFile.delete();
	}
}