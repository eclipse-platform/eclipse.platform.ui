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

/**
 * Adapts a LuceneSearchParticipant to SearchParticipant. This allows
 * the deprecated extension point org.eclipse.help.base.luceneSearchParticipants
 * to continue to function even though the rest of the help system has
 * switched to use the class SearchParticipant instead of LuceneSearchParticipant
 */

import java.net.URL;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.help.search.IHelpSearchIndex;
import org.eclipse.help.search.ISearchDocument;
import org.eclipse.help.search.LuceneSearchParticipant;
import org.eclipse.help.search.SearchParticipant;

public class LuceneSearchParticipantAdapter extends SearchParticipant {
		
	private LuceneSearchParticipant searchParticipant;

	public LuceneSearchParticipantAdapter(LuceneSearchParticipant participant) {
		this.searchParticipant = participant;
	}

	public IStatus addDocument(IHelpSearchIndex index, String pluginId, String name, URL url, String id,
			ISearchDocument doc) {
		// In the help system the only class that implements ISearchDocument is LuceneSearchDocument
		// and the only class that implements IHelpSearchIndex is SearchIndex
		LuceneSearchDocument luceneDoc = (LuceneSearchDocument)doc;
		SearchIndex searchIndex = (SearchIndex) index;
		return searchParticipant.addDocument(searchIndex, pluginId, name, url, id, luceneDoc.getDocument());
	}
	
	public Set<String> getAllDocuments(String locale) {
		return searchParticipant.getAllDocuments(locale);
	}
	
	public void clear() {
		searchParticipant.clear();
	}
	
	public boolean equals(Object obj) {
		return searchParticipant.equals(obj);
	}
	
	public Set<String> getContributingPlugins() {
		return searchParticipant.getContributingPlugins();
	}
	
	public int hashCode() {
		return searchParticipant.hashCode();
	}
	
	public boolean open(String id) {
		return searchParticipant.open(id);
	}

}
