/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.servlet.data;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

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
	/**
	 * Constructs the data for a request.
	 * @param context
	 * @param request
	 */
	public RequestData(ServletContext context, HttpServletRequest request) {
		this.context = context;
		this.request = request;

		if ((HelpSystem.getMode() == HelpSystem.MODE_INFOCENTER)
			&& request != null)
			locale = request.getLocale().toString();
		else
			locale = Locale.getDefault().toString();
	}

	/**
	 * Returns the preferences object
	 */
	public WebappPreferences getPrefs() {
		return (WebappPreferences) context.getAttribute("WebappPreferences");
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