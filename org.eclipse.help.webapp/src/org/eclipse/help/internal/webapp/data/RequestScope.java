/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.help.internal.webapp.data;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.help.HelpSystem;
import org.eclipse.help.base.AbstractHelpScope;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.eclipse.help.internal.base.scope.EnablementScope;
import org.eclipse.help.internal.base.scope.FilterScope;
import org.eclipse.help.internal.base.scope.IntersectionScope;
import org.eclipse.help.internal.base.scope.ScopeRegistry;
import org.eclipse.help.internal.base.scope.UniversalScope;
import org.eclipse.help.internal.base.scope.WorkingSetScope;
import org.eclipse.help.internal.webapp.servlet.WebappWorkingSetManager;

public class RequestScope {
	
	private static final String SCOPE_PARAMETER_NAME = "scope"; //$NON-NLS-1$
	private static final String SCOPE_COOKIE_NAME = "filter"; //$NON-NLS-1$

	public static AbstractHelpScope getScopeFromRequest(HttpServletRequest req, HttpServletResponse resp ) {
		if (!HelpSystem.isShared()) {
			// Filtering by scope is currently only in information center mode
			if (HelpBasePlugin.getActivitySupport().isFilteringEnabled()) {
				AbstractHelpScope[] scopes = { new FilterScope(), new EnablementScope() };
				return new IntersectionScope(scopes);
			}
			return new FilterScope();
		}

		String scopesFromParameter= getScopeFromCookies(req);
		List scopes = new ArrayList();
		if (scopesFromParameter != null) {
			StringTokenizer tokenizer = new StringTokenizer(scopesFromParameter, "/"); //$NON-NLS-1$
			while (tokenizer.hasMoreTokens()) {
				String nextScope = tokenizer.nextToken().trim();
				if (!nextScope.equals("")) { //$NON-NLS-1$
					AbstractHelpScope scope = ScopeRegistry.getInstance().getScope(nextScope);
					if (scope != null) {
						scopes.add(scope);
					} else {
						// Try for a working set
						try {
							WorkingSetScope workingSetScope = new WorkingSetScope(
									nextScope, new WebappWorkingSetManager(req,
											resp, UrlUtil.getLocale(req, resp)));
							scopes.add(workingSetScope);
						} catch (Exception e) {
						}
					}

				}
			}
		}
		switch (scopes.size()) {
		case 0:
			return new UniversalScope();
		case 1:
			return (AbstractHelpScope) scopes.get(0);
		default:
			return new IntersectionScope(
					(AbstractHelpScope[]) scopes.toArray(new AbstractHelpScope[scopes.size()]));
		}	
	}
	
	public static void setScopeCookie(HttpServletRequest request, HttpServletResponse response) {
		// See if there is a scope parameter, if so save as cookie
		String[] scope = request.getParameterValues(SCOPE_PARAMETER_NAME); 
		String scopeString = null;
		if (scope != null) {
			// save scope (in session cookie) for later use in a user session
			// If there are multiple values separate them with a '/'
			if (scope != null && scope.length > 0 && response != null) {
				scopeString = scope[0];			
				for (int s = 1; s < scope.length; s++) {
					scopeString += '/';
					scopeString += scope[s];
				}
				Cookie scopeCookie = new Cookie(SCOPE_COOKIE_NAME, scopeString); 
				response.addCookie(scopeCookie);
			}
		} else {
			if (response != null) {
				// No scope parameter. Set the cookie to show all
				scopeString = "all"; //$NON-NLS-1$
				Cookie scopeCookie = new Cookie(SCOPE_COOKIE_NAME, scopeString); 
				response.addCookie(scopeCookie);
			}
		}
	}
	
	public static String getScopeFromCookies(HttpServletRequest request) {
		// check if scope was passed earlier in this session
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (int c = 0; c < cookies.length; c++) {
				if (SCOPE_COOKIE_NAME.equals(cookies[c].getName())) { 
					return cookies[c].getValue();
				}
			}
		}
		return null;
	}

}
