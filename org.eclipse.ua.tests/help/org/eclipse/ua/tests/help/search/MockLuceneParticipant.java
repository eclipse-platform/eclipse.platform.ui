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

package org.eclipse.ua.tests.help.search;

import java.io.StringReader;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.help.search.ISearchIndex;
import org.eclipse.help.search.LuceneSearchParticipant;

public class MockLuceneParticipant extends LuceneSearchParticipant {

	private static final String DOC_1 = "/org.eclipse.help.base/lucene1.xml";
	private static final String DOC_2 = "/org.eclipse.help.base/lucene2.xml";
	
	public IStatus addDocument(ISearchIndex index, String pluginId,
			String name, URL url, String id, Document doc) {
		boolean isDoc1 = url.getPath().equals(DOC_1) ;
		String title = isDoc1 ? "Title1" : "Title2";
		String summary = isDoc1? "Summary1" : "Summary2";
		String contents = isDoc1? "sehdtegd jduehdye" : "nhduehrf ldoekfij";
		addTitle(title, doc);
        doc.add(new Field("summary", summary, Field.Store.YES, Field.Index.NO)); //$NON-NLS-1$				
		doc.add(new Field("contents", new StringReader(contents))); //$NON-NLS-1$
		doc.add(new Field("exact_contents", new StringReader(contents))); //$NON-NLS-1$
		return Status.OK_STATUS;
	}
	
	public Set<String> getAllDocuments(String locale) {
		HashSet<String> set = new HashSet<String>();
		set.add(DOC_1);
		set.add(DOC_2);
		return set;
	}
	
	public String getId() {
		return super.getId();
	}

}
