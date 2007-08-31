/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.webapp.data;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Helper class for reading cookie values
 */
public class CookiesData extends RequestData {

	public CookiesData(ServletContext context, HttpServletRequest request,
			HttpServletResponse response) {
		super(context, request, response);
		
	}

	public boolean isSynchToc() {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
        		for (int i=0;i<cookies.length;++i) {
        			if ("synchToc".equals(cookies[i].getName())) { //$NON-NLS-1$
        				return String.valueOf(true).equals(cookies[i].getValue());
        			}
        		}
		}
		// default off
		return false;
	}

}
