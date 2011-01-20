/*******************************************************************************
 * Copyright (c) 2010, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.help.internal.search;

import java.io.Reader;
import java.io.StringReader;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.eclipse.help.search.ISearchDocument;

/**
 * Class which adapts a Lucene Document to ISearchDocument. 
 */

public class LuceneSearchDocument implements ISearchDocument {
		
	private Document doc;

	public LuceneSearchDocument(Document document) {
		this.doc = document;
	}

	public void setTitle(String title) {
		doc.add(new Field("title", title, Field.Store.NO, Field.Index.ANALYZED)); //$NON-NLS-1$
		doc.add(new Field("exact_title", title, Field.Store.NO, Field.Index.ANALYZED)); //$NON-NLS-1$
		doc.add(new Field("raw_title", title, Field.Store.YES, Field.Index.NO)); //$NON-NLS-1$
	}

	public void setSummary(String summary) {
	  	doc.add(new Field("summary", summary, Field.Store.YES, Field.Index.NO)); //$NON-NLS-1$				        
	}

	public void addContents(String contents) {
		doc.add(new Field("contents", new StringReader(contents))); //$NON-NLS-1$
		doc.add(new Field("exact_contents", new StringReader(contents))); //$NON-NLS-1$		
	}

	public void setHasFilters(boolean hasFilters) {
		doc.add(new Field("filters", Boolean.toString(hasFilters), Field.Store.YES, Field.Index.NO)); //$NON-NLS-1$ 	
	}
	
	public Document getDocument() {
		return doc;
	}

	public void addContents(Reader contents, Reader exactContents) {
		doc.add(new Field("contents", contents)); //$NON-NLS-1$
		doc.add(new Field("exact_contents", exactContents)); //$NON-NLS-1$
	}

}
