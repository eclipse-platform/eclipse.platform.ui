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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.help.base.AbstractHelpScope;
import org.eclipse.help.internal.base.scope.ScopeHandle;
import org.eclipse.help.internal.base.scope.ScopeRegistry;

/**
 * This class manages help scopes
 */
public class ScopeData extends RequestData {
	public final static short STATE_UNCHECKED = 0;
	public final static short STATE_GRAYED = 1;
	public final static short STATE_CHECKED = 2;
	private ScopeHandle[] allScopes;
	private Set activeScopes;
	private Locale locale;
	

	public ScopeData(ServletContext context, HttpServletRequest request,
			HttpServletResponse response) {
		super(context, request, response);
		ScopeHandle[] predefinedScopes = ScopeRegistry.getInstance().getScopes();
		List scopeList = new ArrayList();
		for (int i = 0; i < predefinedScopes.length; i++) {
			scopeList.add(predefinedScopes[i]);
		}
		locale = UrlUtil.getLocaleObj(request, response);
		activeScopes = new HashSet();
		AbstractHelpScope[] allActiveScopes = RequestScope.getActiveScopes(request, response, false);
		for (int i = 0; i < allActiveScopes.length; i++) {
			activeScopes.add(allActiveScopes[i].getName(locale));
		}
		/*
		 * The code below adds working sets to the dialog. Commented out as the current UI does not
		 * show these.
		WebappWorkingSetManager manager = new WebappWorkingSetManager(request, response, locale.toString());
	    WorkingSet[] wsets = manager.getWorkingSets();
	    for (int i = 0; i < wsets.length; i++) {
	    	AbstractHelpScope scope = new WorkingSetScope(wsets[i].getName(), manager);
	    	ScopeHandle handle = new ScopeHandle(wsets[i].getName(), scope);
	    	scopeList.add(handle);
	    }
	    */
	    allScopes = (ScopeHandle[]) scopeList.toArray(new ScopeHandle[scopeList.size()]);
	}
	
	public int getScopeCount() {
		return allScopes.length;
	}

	public String getScopeLabel(int i) {
		return allScopes[i].getScope().getName(locale);
	}
	
	public String getScopeId(int i) {
		return allScopes[i].getId();
	}
	
	public boolean isScopeEnabled(int scopeIndex) {
		return activeScopes.contains(allScopes[scopeIndex].getScope().getName(locale));
	}
	
}
