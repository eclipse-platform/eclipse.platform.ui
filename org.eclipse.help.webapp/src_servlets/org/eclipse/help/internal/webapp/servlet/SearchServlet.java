/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.webapp.servlet;

import java.io.*;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.search.*;
import org.eclipse.help.internal.util.*;
/**
 * Servlet to interface client with remote Eclipse
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
		if (hits == null) {
			Logger.logError(Resources.getString("index_is_busy"), null);
			throw new ServletException();
		}

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
		try {
			NullProgressMonitor pm = new NullProgressMonitor();

			SearchResults results = createHitCollector(request);
			HelpSystem.getSearchManager().search(
				createSearchQuery(request),
				results,
				pm);
			return results.getSearchHits();
		} catch (Exception e) {
			return null;
		}
	}

	private ISearchQuery createSearchQuery(HttpServletRequest request) {
		String searchWord = "";
		if (UrlUtil.isIE(request)
			&& request.getParameter("encoding") != null) {
			// parameter is escaped using JavaScript
			searchWord =
				UrlUtil.unescape(
					UrlUtil.getRawRequestParameter(request, "searchWord"));
		} else {
			searchWord = request.getParameter("searchWord");
		}

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
		 * @param toc Toc
		 * @param writer java.io.Writer
		 */
		public ResultsWriter(Writer writer) {
			super(writer);
		}

		/** XML representation of search results.
		* &lt;pre&gt;
		* 	&lt;toc&gt;
		* 		&lt;topic label=".." score="..." toc=".." toclabel=".."/&gt;
		*  .....
		*/
		public void generate(SearchHit[] hits, HttpServletResponse resp) {

			println("<toc>");
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
			println("</toc>");
		}
	}
}