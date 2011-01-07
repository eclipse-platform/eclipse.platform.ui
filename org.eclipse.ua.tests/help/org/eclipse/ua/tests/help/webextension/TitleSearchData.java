/*******************************************************************************
 *  Copyright (c) 2009, 2011 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.help.webextension;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.help.IToc;
import org.eclipse.help.ITopic;
import org.eclipse.help.internal.webapp.data.TocData;

public class TitleSearchData extends TocData {

public TitleSearchData(ServletContext context, HttpServletRequest request,
			HttpServletResponse response) {
		super(context, request, response);
}

private List<SearchResult> results;
private String searchTerm;

public class SearchResult {
	public String title;
	public String href;
}

public SearchResult[] getSearchResults() {
     results = new ArrayList<SearchResult>();
     searchTerm = request.getParameter("searchWord");
     IToc[] tocs = getTocs();
     for (int i = 0; i < tocs.length; i++) {
    	 ITopic[] topics = tocs[i].getTopics();
    	 for (int t = 0; t < topics.length; t++) {
    		 searchTopic(topics[t]);
    	 }
     }
     return results.toArray(new SearchResult[results.size()]);
}

private void searchTopic(ITopic topic) {
	if (topic.getLabel().toLowerCase().indexOf(searchTerm.toLowerCase()) != -1
		&& topic.getHref() != null) {
		SearchResult result = new SearchResult();
		result.title = topic.getLabel();
		result.href = "../../../topic" + topic.getHref();		
		results.add(result);
	}
	ITopic[] topics = topic.getSubtopics();
	for (int t = 0; t < topics.length; t++) {
		searchTopic(topics[t]);
	}
}

}
