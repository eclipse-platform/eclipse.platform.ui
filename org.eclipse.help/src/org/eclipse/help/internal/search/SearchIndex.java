package org.eclipse.help.internal.search;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;
import java.util.*;
import org.apache.lucene.HTMLParser.HTMLParser;
import org.apache.lucene.analysis.*;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.util.*;
/**
 * Text search index.  Documents added to this index
 * can than be searched against a search query.
 */
public class SearchIndex implements ISearchIndex {
	private IndexWriter iw = null;
	private File indexDir;
	private String locale;
	private PluginVersionInfo docPlugins;
	private HelpProperties indexedDocs;
	private final static String name = "index";
	private static final String INDEXED_CONTRIBUTION_INFO_FILE =
		"indexed_contributions";
	public static final String INDEXED_DOCS_FILE = "indexed_docs";
	private static final String dummy = "0";
	public SearchIndex(String locale) {
		super();
		this.locale = locale;
		String helpStatePath = HelpPlugin.getDefault().getStateLocation().toOSString();
		String searchStatePath =
			helpStatePath + File.separator + "nl" + File.separator + locale;
		indexDir = new File(searchStatePath);
		open();
	}
	public boolean abortUpdate() {
		return false; // not supported
	}
	/**
	 * Indexes one document in a string buffer
	 * All the addDocument() calls must be enclosed by
	 * beginUpdate() and endUpdate().
	 * @param name the document identifier (could be a URL)
	 * @param text the text of the document
	 * @return true if success
	 */
	public boolean addDocument(String name, byte[] buf, int size) {
		// Not supported.
		return false;
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
			doc.add(Field.UnIndexed("name", name));
			HTMLParser parser = new HTMLParser(stream);
			doc.add(Field.Text("contents", parser.getReader()));
			doc.add(Field.UnIndexed("summary", parser.getSummary()));
			doc.add(Field.Text("title", parser.getTitle()));
			iw.addDocument(doc);
			getIndexedDocs().put(name, dummy);
			return true;
		} catch (IOException e) {
			System.out.println(e);
			return false;
		} catch (InterruptedException e) {
			System.out.println(e);
			return false;
		}
	}
	public boolean removeDocument(String name) {
		return false; // not supported
	}
	public boolean beginUpdate() {
		try {
			if (iw != null) {
				iw.close();
			}
			boolean create = false;
			if (!exists()) {
				create = true;
				if (!indexDir.mkdirs())
					return false; // unable to setup index directory
			}
			iw = new IndexWriter(indexDir, new StopAnalyzer(), create);
			iw.mergeFactor = 20; //FIXME: verify we need this
			iw.maxFieldLength = 1000000; //FIXME: verify we need this
			return true;
		} catch (IOException e) {
			System.out.println(e);
			return false;
		}
	}
	public boolean close() {
		return true; // not supported
	}
	public boolean create() {
		return true; // not supported
	}
	public boolean delete() {
		return true; // not supported
	}
	public boolean deleteDocument(String name) {
		return false; // not supported
	}
	public boolean endUpdate() {
		try {
			if (iw == null)
				return false;
			iw.optimize();
			iw.close();
			// save the update info:
			//  - all the docs
			//  - plugins (and their version) that were indexed
			getIndexedDocs().save();
			getDocPlugins().save();
			return true;
		} catch (IOException e) {
			System.out.println(e);
			return false;
		}
	}
	public boolean exists() {
		return indexDir.exists(); // assume index exists if directory does
	}
	public int[][] getHighlightInfo(byte[] docBuffer, int docNumber) {
		return null; // not supported
	}
	public boolean open() {
		return true; // not supported
	}
	/** 
	 * Performs a query search on this index 
	 * @param fieldNames - Collection of field names of type String (e.g. "h1")
	 *  the search will be performed on the given fields only
	 *  if empty, then entire document will be searched
	 * @param fieldSearch - boolean indicating if field only search
	 *  should be performed
	 * @return - an array of document ids. 
	 * Later, we can extend this to return more data (rank, # of occs, etc.)
	 */
	public String[] search(
		String queryString,
		Collection fieldNames,
		boolean fieldSearch,
		int maxhits) {
		ArrayList list = new ArrayList();
		String[] results;
		try {
			Searcher searcher = new IndexSearcher(indexDir.getAbsolutePath());
			Analyzer analyzer = new StopAnalyzer();
			Query query = QueryParser.parse(queryString, "contents", analyzer);
			Hits hits = searcher.search(query);
			for (int i = 0; i < hits.length(); i++) {
				list.add((String) hits.doc(i).get("name"));
			}
			searcher.close();
			results = new String[list.size()];
			list.toArray(results);
			return results;
		} catch (Exception e) {
			System.out.println(e);
			return null;
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
					HelpPlugin.getDefault());
		}
		return docPlugins;
	}
	/**
	 * We use HelpProperties, but a list would suffice.
	 * We only need the key values.
	 */
	public HelpProperties getIndexedDocs() {
		if (indexedDocs == null) {
			indexedDocs =
				new HelpProperties(
					"nl" + File.separator + locale + File.separator + INDEXED_DOCS_FILE,
					HelpPlugin.getDefault());
			indexedDocs.restore();
		}
		return indexedDocs;
	}
}