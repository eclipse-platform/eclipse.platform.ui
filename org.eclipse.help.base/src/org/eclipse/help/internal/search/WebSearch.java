/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.search;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.eclipse.core.runtime.*;
import org.eclipse.help.IHelpResource;
import org.eclipse.help.internal.base.HelpBaseResources;
import org.eclipse.help.search.ISearchEngine;
import org.eclipse.help.search.ISearchEngineResult;
import org.eclipse.help.search.ISearchEngineResultCollector;
import org.eclipse.help.search.ISearchScope;

/**
 * This implementation of <code>ISearchEngine</code> interface performs search
 * by running a query on the remote web site using the provided query URL.
 * Instances of this engine type are required to supply the URL template with
 * the query string replaced with the substitution string
 * <code>{expression}</code>.
 * <p>
 * This class is made public in order to be instantiated and parametrized
 * directly in the extensions. Clients are required to supply the URL template
 * string as a parameter <code>url</code>.
 * 
 * <p>
 * This class is not expected to be subclassed or otherwise accessed
 * programmatically.
 * 
 * @since 3.1
 */
public final class WebSearch implements ISearchEngine {
	private static final char C_START = '{';

	private static final char C_STOP = '}';

	public static class Scope implements ISearchScope {
		private String urlTemplate;

		public Scope(String urlTemplate) {
			this.urlTemplate = urlTemplate;
		}

		public String getURLTemplate() {
			return urlTemplate;
		}
	}

	private static class SearchResult implements ISearchEngineResult {
		private String query;

		private String urlTemplate;

		public SearchResult(String query, String urlTemplate) {
			this.query = query;
			this.urlTemplate = urlTemplate;
		}

		public String getDescription() {
			return HelpBaseResources.WebSearch_click;
		}

		public String getHref() {
			String href = null;
			String equery;
			try {
				equery = URLEncoder.encode(query, "UTF-8"); //$NON-NLS-1$

			} catch (UnsupportedEncodingException e) {
				equery = query;
			}
			href = composeURL(equery, urlTemplate);
			return href;
		}

		public String getLabel() {
			return HelpBaseResources.WebSearch_label;
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

		public String toAbsoluteHref(String href, boolean frames) {
			return href;
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
				.accept(new SearchResult(query, ((Scope) scope).getURLTemplate()));
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
		if (key.equals("expression")) //$NON-NLS-1$
			return query;
		return key;
	}
}
