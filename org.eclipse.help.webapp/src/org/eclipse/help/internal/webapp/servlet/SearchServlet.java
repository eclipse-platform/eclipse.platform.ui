/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.webapp.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.search.ISearchHitCollector;
import org.eclipse.help.internal.search.ISearchQuery;
import org.eclipse.help.internal.search.QueryTooComplexException;
import org.eclipse.help.internal.search.SearchHit;
import org.eclipse.help.internal.search.SearchQuery;
import org.eclipse.help.internal.util.URLCoder;
import org.eclipse.help.internal.webapp.data.UrlUtil;

/*
 * Returns the search hits for the query provided in the phrase parameter.
 * 
 * This is called on infocenters by client workbenches configured for remote
 * help in order to retrieve search hits from the remote help server.
 */
public class SearchServlet extends HttpServlet {
	
	private final class HitCollector implements ISearchHitCollector {
		public Collection results = new ArrayList();

		public void addHits(List hits, String wordsSearched) {
			if (results != null) {
				results.addAll(hits);
			}
		}

		public void addQTCException(QueryTooComplexException exception)
				throws QueryTooComplexException {
			searchException = exception;			
		}
	}

	private static final long serialVersionUID = 1L;
	private static final String PARAMETER_PHRASE = "phrase"; //$NON-NLS-1$
	private QueryTooComplexException searchException;
	
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		BaseHelpSystem.checkMode();
		HitCollector collector = new HitCollector();
		String locale = UrlUtil.getLocale(req, resp);
		req.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
		resp.setContentType("application/xml; charset=UTF-8"); //$NON-NLS-1$
		String phrase = req.getParameter(PARAMETER_PHRASE); 
		if (phrase != null) {
		    phrase = URLCoder.decode(phrase);
			ISearchQuery query = new SearchQuery(phrase, false, Collections.EMPTY_LIST, locale);
			collector.results.clear();
			BaseHelpSystem.getSearchManager().search(query, collector, new NullProgressMonitor());
			if (searchException == null) {
				String response = serialize(collector.results);
				resp.getWriter().write(response);
				return;
			}
		}

		resp.sendError(400); // bad request; missing parameter

	}
	
	public static String serialize(Collection results) {
		StringBuffer buf = new StringBuffer();
		buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //$NON-NLS-1$
		buf.append("<searchHits>\n"); //$NON-NLS-1$
		Iterator iter = results.iterator();
		while (iter.hasNext()) {
			SearchHit hit = (SearchHit)iter.next();
			serialize(hit, buf, "   "); //$NON-NLS-1$
		}
		buf.append("</searchHits>\n"); //$NON-NLS-1$
		return buf.toString();
	}
	
	private static void serialize(SearchHit hit, StringBuffer buf, String indent) {
		buf.append(indent + "<hit"); //$NON-NLS-1$
		if (hit.getHref() != null) {
			buf.append('\n' + indent	+ "      href=\"" + XMLGenerator.xmlEscape(hit.getHref()) + '"'); //$NON-NLS-1$
		}
		if (hit.getLabel() != null) {
			buf.append('\n' + indent	+ "      label=\"" + XMLGenerator.xmlEscape(hit.getLabel()) + '"'); //$NON-NLS-1$
		}
		if (hit.isPotentialHit()) {
			buf.append('\n' + indent	+ "      isPotentialHit=\"true\""); //$NON-NLS-1$
		}
		buf.append('\n' + indent + "      score=\"" + hit.getScore() + '"'); //$NON-NLS-1$
		buf.append(">\n"); //$NON-NLS-1$
		
		String summary = hit.getSummary();
		if (summary != null) {
			serialize(summary, buf, indent + "   "); //$NON-NLS-1$
		}
		buf.append(indent + "</hit>\n"); //$NON-NLS-1$
	}

	private static void serialize(String summary, StringBuffer buf, String indent) {
		buf.append(indent + "<summary>"); //$NON-NLS-1$
		buf.append(XMLGenerator.xmlEscape(summary));
		buf.append("</summary>\n"); //$NON-NLS-1$
	}
}
