/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.servlet.data;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.core.boot.BootLoader;
import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.servlet.*;

/**
 * Helper class for contents.jsp initialization
 */
public class RequestData {
	public final static int MODE_WORKBENCH = HelpSystem.MODE_WORKBENCH;
	public final static int MODE_INFOCENTER = HelpSystem.MODE_INFOCENTER;
	public final static int MODE_STANDALONE = HelpSystem.MODE_STANDALONE;
	
	protected ServletContext context;
	protected HttpServletRequest request;
	protected String locale;
	protected WebappPreferences preferences;
	/**
	 * Constructs the data for a request.
	 * @param context
	 * @param request
	 */
	public RequestData(ServletContext context, HttpServletRequest request) {
		this.context = context;
		this.request = request;
		preferences=new WebappPreferences();

		if ((HelpSystem.getMode() == HelpSystem.MODE_INFOCENTER)
			&& request != null)
			locale = request.getLocale().toString();
		else
			locale = BootLoader.getNL();
	}

	/**
	 * Returns the preferences object
	 */
	public WebappPreferences getPrefs() {
		return preferences;
	}

	public boolean isIE() {
		return UrlUtil.isIE(request);
	}

	public boolean isMozilla() {
		return UrlUtil.isMozilla(request);
	}
	
	public String getLocale() {
		return locale;
	}
	
	public int getMode(){
		return HelpSystem.getMode();
	}
}