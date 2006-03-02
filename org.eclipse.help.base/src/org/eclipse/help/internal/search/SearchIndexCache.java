/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.search;

import java.io.File;
import java.net.URL;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.eclipse.help.internal.toc.TocManager;

/**
 * A search index used for caching filtered indexed documents. The master index
 * documents are indexed unfiltered so there are potential false hits. This index
 * is used to reindex those potential false hits (filtered this time) and search
 * again. The index is updated as needed.
 */
public class SearchIndexCache extends SearchIndex {

	private String filters;
	
	/**
	 * Constructs a new cache index with the given info. The cache index sits
	 * beside the master index in the file system.
	 * 
	 * @param locale the locale for this index
	 * @param analyzerDesc the analyzer to use
	 * @param tocManager the toc manager to use
	 */
	public SearchIndexCache(String locale, AnalyzerDescriptor analyzerDesc, TocManager tocManager) {
		super(new File(HelpBasePlugin.getConfigurationDirectory(), "indexCache/" + locale), //$NON-NLS-1$
				locale, analyzerDesc, tocManager, null);
	}

	/**
	 * A variant of addDocument() that also takes the current state of filters
	 * at the time of indexing.
	 * 
	 * e.g. "os=win32,plugin=org.eclipse.help,plugin=org.eclipse.help.base"
	 *  
	 * @param name the document name (href in our case)
	 * @param url the URL to get the content to index
	 * @param filters the currently active filters
	 * @return the status of the operation
	 */
	public IStatus addDocument(String name, URL url, String filters) {
		this.filters = filters;
		return super.addDocument(name, url);
	}
	
	protected void addExtraFields(Document doc) {
		super.addExtraFields(doc);
		if (filters != null) {
			doc.add(Field.UnIndexed("filters", filters)); //$NON-NLS-1$
		}
	}
}