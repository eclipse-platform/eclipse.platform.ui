/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.search;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.eclipse.core.runtime.*;
import org.eclipse.help.IHelpResource;
import org.eclipse.help.search.*;

/**
 * A general-purpose web query engine that allows users to provide the query
 * string themselves.
 */
public class WebSearch implements ISearchEngine {
	public static final char C_START = '{';

	public static final char C_STOP = '}';

	public static class Scope implements ISearchScope {
		private String urlTemplate;

		public Scope(String urlTemplate) {
			this.urlTemplate = urlTemplate;
		}

		public String getURLTemplate() {
			return urlTemplate;
		}
	}

	public static class SearchResult implements ISearchEngineResult {
		private String query;

		private String urlTemplate;

		public SearchResult(String query, String urlTemplate) {
			this.query = query;
			this.urlTemplate = urlTemplate;
		}

		public String getDescription() {
			return "Click on this link to see the results";
		}

		public String getHref() {
			String href = null;
			String equery;
			try {
				equery = URLEncoder.encode(query, "UTF-8");

			} catch (UnsupportedEncodingException e) {
				equery = query;
			}
			href = composeURL(equery, urlTemplate);
			return href;
		}

		public String getLabel() {
			return "Web Search";
		}

		public float getScore() {
			return 1;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.help.internal.search.federated.ISearchEngineResult#getCategory()
		 */
		public IHelpResource getCategory() {
			return null;
		}

		public boolean getForceExternalWindow() {
			return true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.internal.search.federated.ISearchEngine#run(java.lang.String,
	 *      org.eclipse.help.internal.search.ISearchScope,
	 *      org.eclipse.help.internal.search.federated.ISearchEngineResultCollector,
	 *      org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(String query, ISearchScope scope,
			ISearchEngineResultCollector collector, IProgressMonitor monitor)
			throws CoreException {

		collector
				.add(new SearchResult(query, ((Scope) scope).getURLTemplate()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.internal.search.federated.ISearchEngine#cancel()
	 */
	public void cancel() {
		// TODO Auto-generated method stub
	}

	private static String composeURL(String query, String urlTemplate) {
		StringBuffer result = new StringBuffer();
		boolean inSubstitution = false;
		int varStart = -1;
		for (int i = 0; i < urlTemplate.length(); i++) {
			char c = urlTemplate.charAt(i);
			if (c == C_START && !inSubstitution) {
				if (i < urlTemplate.length() - 1) {
					// look ahead
					char c2 = urlTemplate.charAt(i + 1);
					if (c2 == C_START) {
						result.append(c);
						i++;
						continue;
					}
				}
				inSubstitution = true;
				varStart = i;
				continue;
			} else if (c == C_STOP && inSubstitution) {
				if (i < urlTemplate.length() - 1) {
					// look ahead
					char c2 = urlTemplate.charAt(i + 1);
					if (c2 == C_STOP) {
						result.append(c);
						i++;
						continue;
					}
				}
				if (varStart != -1) {
					String key = urlTemplate.substring(varStart + 1, i);
					String value = getVariable(key, query);
					result.append(value);
				}
				inSubstitution = false;
			} else if (!inSubstitution) {
				result.append(c);
			}
		}
		return result.toString();
	}

	private static String getVariable(String key, String query) {
		if (key.equals("expression"))
			return query;
		return key;
	}
}