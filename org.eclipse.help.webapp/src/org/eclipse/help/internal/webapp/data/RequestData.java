/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.webapp.data;

import javax.servlet.*;
import javax.servlet.http.*;

import org.eclipse.help.internal.base.*;

/**
 * Helper class for contents.jsp initialization
 */
public class RequestData {
	public final static int MODE_WORKBENCH = BaseHelpSystem.MODE_WORKBENCH;
	public final static int MODE_INFOCENTER = BaseHelpSystem.MODE_INFOCENTER;
	public final static int MODE_STANDALONE = BaseHelpSystem.MODE_STANDALONE;

	protected ServletContext context;
	protected HttpServletRequest request;
	protected HttpServletResponse response;
	protected String locale;
	protected WebappPreferences preferences;
	protected boolean advancedUI;
	/**
	 * Constructs the data for a request.
	 * 
	 * @param context
	 * @param request
	 */
	public RequestData(ServletContext context, HttpServletRequest request,
			HttpServletResponse response) {
		this.context = context;
		this.request = request;
		this.response = response;
		preferences = new WebappPreferences();

		locale = UrlUtil.getLocale(request, response);
		String agent = request.getHeader("User-Agent"); //$NON-NLS-1$
		advancedUI = UrlUtil.isAdvanced(agent);
	}

	/**
	 * Returns the preferences object
	 */
	public WebappPreferences getPrefs() {
		return preferences;
	}

	public boolean isBot() {
		return UrlUtil.isBot(request);
	}

	public boolean isGecko() {
		return UrlUtil.isGecko(request);
	}

	public boolean isIE() {
		return UrlUtil.isIE(request);
	}

	public String getIEVersion() {
		return UrlUtil.getIEVersion(request);
	}

	public boolean isKonqueror() {
		return UrlUtil.isKonqueror(request);
	}

	public boolean isMozilla() {
		return UrlUtil.isMozilla(request);
	}

	public boolean isMacMozilla() {
		return UrlUtil.isMozilla(request) && 
		(request.getHeader("User-Agent").indexOf("Macintosh") > 0 ); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public String getMozillaVersion() {
		return UrlUtil.getMozillaVersion(request);
	}

	public boolean isSafari() {
		return UrlUtil.isSafari(request);
	}

	public String getSafariVersion() {
		return UrlUtil.getSafariVersion(request);
	}

	public boolean isOpera() {
		return UrlUtil.isOpera(request);
	}

	public String getLocale() {
		return locale;
	}

	public int getMode() {
		return BaseHelpSystem.getMode();
	}
	public boolean isAdvancedUI() {
		return advancedUI;
	}

}
