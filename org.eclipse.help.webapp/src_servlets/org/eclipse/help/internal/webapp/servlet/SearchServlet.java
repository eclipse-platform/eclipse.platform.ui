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
package org.eclipse.help.internal.webapp.servlet;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.*;
import org.eclipse.help.internal.search.*;
import org.eclipse.help.internal.webapp.*;
import org.eclipse.help.internal.webapp.data.*;

/**
 * Returns search results.
 * Each hits contains a prameter "resultsof" that is the url encoded query
 * string.
 */
public class SearchServlet extends HttpServlet {
	private String locale;

	/**
	 * Called by the server (via the <code>service</code> method) to
	 * allow a servlet to handle a GET request. 
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {

		locale = UrlUtil.getLocale(req);
		req.setCharacterEncoding("UTF-8");

		resp.setContentType("application/xml; charset=UTF-8");
		resp.setHeader("Cache-Control", "max-age=0");

		SearchHit[] hits = loadSearchResults(req);

		ResultsWriter resultsWriter = new ResultsWriter(resp.getWriter());
		resultsWriter.generate(hits, resp);
		resultsWriter.close();
	}
	/**
	 *
	 * Called by the server (via the <code>service</code> method)
	 * to allow a servlet to handle a POST request.
	 *
	 * Handle the search requests,
	 *
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {

		doGet(req, resp);
	}

	/**
				* Call the search engine, and get results or the percentage of 
				* indexed documents.
				*/
	private SearchHit[] loadSearchResults(HttpServletRequest request) {
		SearchHit[] hits = null;
		try {
			NullProgressMonitor pm = new NullProgressMonitor();

			SearchResults results = createHitCollector(request);
			HelpSystem.getSearchManager().search(
				createSearchQuery(request),
				results,
				pm);
			hits = results.getSearchHits();
		} catch (Exception e) {
			HelpWebappPlugin.logError("", e);
		} finally {
			if (hits == null)
				hits = new SearchHit[0];
			return hits;
		}
	}

	private ISearchQuery createSearchQuery(HttpServletRequest request) {
		String searchWord = request.getParameter("searchWord");
		String fieldSearchStr = request.getParameter("fieldSearch");
		boolean fieldSearch =
			fieldSearchStr != null
				? new Boolean(fieldSearchStr).booleanValue()
				: false;
		return new SearchQuery(
			searchWord,
			fieldSearch,
			new ArrayList(),
			locale);
	}

	private SearchResults createHitCollector(HttpServletRequest request) {
		String[] scopes = request.getParameterValues("scope");
		Collection scopeCol = null;
		if (scopes != null) {
			if (scopes.length
				!= HelpSystem.getTocManager().getTocs(locale).length) {
				// scope only if not all books selected
				scopeCol = new ArrayList(scopes.length);
				for (int i = 0; i < scopes.length; i++) {
					scopeCol.add(scopes[i]);
				}
			}
		}

		int maxHits = 500;
		String maxHitsStr = request.getParameter("maxHits");
		if (maxHitsStr != null) {
			try {
				int clientmaxHits = Integer.parseInt(maxHitsStr);
				if (0 < clientmaxHits && clientmaxHits < 500) {
					maxHits = clientmaxHits;
				}
			} catch (NumberFormatException nfe) {
			}
		}
		return new SearchResults(scopeCol, maxHits, locale);
	}

	/**
	 * This generates the XML file for the help navigation.
	 */
	private static class ResultsWriter extends XMLGenerator {
		/**
		 * @param writer java.io.Writer
		 */
		public ResultsWriter(Writer writer) {
			super(writer);
		}

		/** XML representation of search results.
		* &lt;pre&gt;
		* 	&lt;			hits&gt; 		&lt;topic label=".." score="..."
		* toc=".." toclabel=".."/&gt;  .....
		*/
		public void generate(SearchHit[] hits, HttpServletResponse resp) {

			println("<hits>");
			pad++;
			for (int i = 0; i < hits.length; i++) {
				printPad();
				print(
					"<topic label=\""
						+ xmlEscape(hits[i].getLabel())
						+ "\""
						+ " href=\""
						+ hits[i].getHref()
						+ "\""
						+ " score=\""
						+ Float.toString(hits[i].getScore())
						+ "\"");

				if (hits[i].getToc() != null) {
					print(
						" toc=\""
							+ hits[i].getToc().getHref()
							+ "\""
							+ " toclabel=\""
							+ hits[i].getToc().getLabel()
							+ "\"");
				}
				print(" />");
			}
			pad--;
			println("</hits>");
		}
	}
}
