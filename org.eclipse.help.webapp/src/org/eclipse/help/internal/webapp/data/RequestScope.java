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

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.help.HelpSystem;
import org.eclipse.help.base.AbstractHelpScope;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.eclipse.help.internal.base.HelpBaseResources;
import org.eclipse.help.internal.base.IHelpBaseConstants;
import org.eclipse.help.internal.base.scope.FilterScope;
import org.eclipse.help.internal.base.scope.IntersectionScope;
import org.eclipse.help.internal.base.scope.ScopeRegistry;
import org.eclipse.help.internal.base.scope.UniversalScope;
import org.eclipse.help.internal.base.scope.WorkingSetScope;
import org.eclipse.help.internal.webapp.servlet.WebappWorkingSetManager;
import org.osgi.service.prefs.BackingStoreException;

public class RequestScope {
	
	private static final String SCOPE_PARAMETER_NAME = "scope"; //$NON-NLS-1$
	private static final String SCOPE_COOKIE_NAME = "filter"; //$NON-NLS-1$

	/**
	 * Gets a scope object based upon the preferences and request
	 * @param isSearchFilter is true if this filter will be used to filter search results.
	 * Search results are already filtered by search scope and if this parameter is true search 
	 * scopes will not be considered
	 * @return
	 */
	public static AbstractHelpScope getScope(HttpServletRequest req, HttpServletResponse resp, boolean isSearchFilter ) {
		AbstractHelpScope[] scopeArray;
		scopeArray = getActiveScopes(req, resp, isSearchFilter);	
		switch (scopeArray.length) {
		case 0:
			return new UniversalScope();
		case 1:
			return scopeArray[0];
		default:
			return new IntersectionScope(
					scopeArray);
		}	
	}

	public static AbstractHelpScope[] getActiveScopes(HttpServletRequest req,
			HttpServletResponse resp, boolean isSearchFilter) {
		AbstractHelpScope[] scopeArray;
		String scopeString;
		List scopes = new ArrayList();
		if (HelpSystem.isShared()) {
			scopes.add(new FilterScope()); // Workbench is always filtered
		}
		scopeString = getScopeString(req);
		if (scopeString != null) {
			StringTokenizer tokenizer = new StringTokenizer(scopeString, "/"); //$NON-NLS-1$
			while (tokenizer.hasMoreTokens()) {
			String nextScope = tokenizer.nextToken().trim();	
				AbstractHelpScope scope = ScopeRegistry.getInstance().getScope(nextScope);
				if (scope != null) {
					scopes.add(scope);
				}
			}
		}
		// Add filter by search scope
		if (!isSearchFilter) {
			// Try for a working set
			try {
				WebappWorkingSetManager manager = new WebappWorkingSetManager(req,
						resp, UrlUtil.getLocale(req, resp));
				String wset = manager.getCurrentWorkingSet();
				WorkingSetScope workingSetScope = new WorkingSetScope(
						wset, manager, HelpBaseResources.SearchScopeFilterName);
				scopes.add(workingSetScope);
			} catch (Exception e) {
			}
		}
		scopeArray = (AbstractHelpScope[]) scopes.toArray(new AbstractHelpScope[scopes.size()]);
		return scopeArray;
	}

	private static String getScopeString(HttpServletRequest req) {
		String scopeString;
		if (HelpSystem.isShared()) {
			scopeString = getScopeFromCookies(req);
		} else {
			scopeString = getScopeFromPreferences();
			if (scopeString == null) {
				scopeString = ScopeRegistry.ENABLEMENT_SCOPE_ID;
			}
		}
		return scopeString;
	}
	
	public static String getFilterButtonState() {
		boolean  scope = Platform.getPreferencesService().getBoolean
            (HelpBasePlugin.PLUGIN_ID, IHelpBaseConstants.P_KEY_FILTER_DIALOG, false, null); 
		if (scope && ScopeRegistry.getInstance().getScopes().length > 0) {
			return "off"; //$NON-NLS-1$
		}
		return "hidden"; //$NON-NLS-1$
	}
	
	
	public static void setScopeFromRequest(HttpServletRequest request, HttpServletResponse response) {
		// See if there is a scope parameter, if so save as cookie or preference
		String[] scope = request.getParameterValues(SCOPE_PARAMETER_NAME); 
		String scopeString = null;
		// save scope (in session cookie) for later use in a user session
		// If there are multiple values separate them with a '/'
		if (scope != null) {
			scopeString = scope[0];			
			for (int s = 1; s < scope.length; s++) {
				scopeString += '/';
				scopeString += scope[s];
			}
			saveScope(scopeString, response);
		} 
	}
	
	public static void saveScope(String scope, HttpServletResponse response) {
		if (HelpSystem.isShared()) {
			if (response != null) {			
				Cookie scopeCookie = new Cookie(SCOPE_COOKIE_NAME, scope); 
				response.addCookie(scopeCookie);
			}
		} else {
			InstanceScope instanceScope = new InstanceScope();
			IEclipsePreferences pref = instanceScope.getNode(HelpBasePlugin.PLUGIN_ID);
			pref.put(IHelpBaseConstants.P_KEY_HELP_SCOPE, scope);
			try {
				pref.flush();
			} catch (BackingStoreException e) {
			}
		}
	}
	
	private static String getScopeFromCookies(HttpServletRequest request) {
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
	
	private static String getScopeFromPreferences() {
		String scope = Platform.getPreferencesService().getString
	         (HelpBasePlugin.PLUGIN_ID, IHelpBaseConstants.P_KEY_HELP_SCOPE, null, null); 
		return scope;
	}
	
	public static boolean filterBySearchScope(HttpServletRequest request) {
        return true;
	}

}
