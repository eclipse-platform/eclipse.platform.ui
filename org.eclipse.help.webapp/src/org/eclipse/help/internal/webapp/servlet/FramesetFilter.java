/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.help.internal.webapp.servlet;

import java.io.*;
import java.net.URLEncoder;

import javax.servlet.http.*;

import org.eclipse.help.internal.webapp.data.*;
import org.eclipse.help.webapp.IFilter;

/**
 * This class inserts a script for showing the page inside the appropriate
 * frameset when bookmarked.
 */
public class FramesetFilter implements IFilter {
	private static final String scriptPart1 = "<script type=\"text/javascript\">\nif( self == top ){" //$NON-NLS-1$
		    + "\n  var  anchorParam = location.hash.length > 0 ? '"  //$NON-NLS-1$
		    + UrlUtil.JavaScriptEncode("&") + "anchor=' + location.hash.substr(1) : '';" //$NON-NLS-1$ //$NON-NLS-2$
			+ "\n  window.location.replace( \""; //$NON-NLS-1$
	private static final String scriptPart3 = "\" + anchorParam);\n}\n</script>"; //$NON-NLS-1$

	/*
	 * @see IFilter#filter(HttpServletRequest, OutputStream)
	 */
	public OutputStream filter(HttpServletRequest req, OutputStream out) {
		String uri = req.getRequestURI();
		String url = req.getPathInfo();
		String query = req.getQueryString();
		if (uri == null) {
			return out;
		}
		boolean isNavPath = UrlUtil.isNavPath(uri);
		if (!uri.endsWith("html") && !uri.endsWith("htm") && !isNavPath) { //$NON-NLS-1$ //$NON-NLS-2$
			return out;
		}

		if ("/nftopic".equals(req.getServletPath()) ||  //$NON-NLS-1$
			"/ntopic".equals(req.getServletPath()) ||  //$NON-NLS-1$
			"/rtopic".equals(req.getServletPath()) ||  //$NON-NLS-1$
			UrlUtil.isBot(req)) { 
			return out;
		}

		String noframes = req.getParameter("noframes"); //$NON-NLS-1$
		if ("true".equals(noframes)) { //$NON-NLS-1$
			return out;
		}

		String path = req.getPathInfo();
		if (path == null) {
			return out;
		}
		StringBuffer script = new StringBuffer(scriptPart1);
		for (int i; 0 <= (i = path.indexOf('/')); path = path.substring(i + 1)) {
			script.append("../"); //$NON-NLS-1$
		}
		if (isNavPath) {
			script.append("index.jsp?nav="); //$NON-NLS-1$
		} else {
			script.append("index.jsp?topic="); //$NON-NLS-1$
		}

		try{
			// Bug 317055 -  [webapp] URLEncode url requests from local users
			url = URLEncoder.encode(url, "UTF-8"); //$NON-NLS-1$
			if ( query != null ) {
				url = url + UrlUtil.JavaScriptEncode("&")  + query;  //$NON-NLS-1$ 
			}
			script.append(url);
		} catch (UnsupportedEncodingException uee){
			return out;
		}

		script.append(scriptPart3);
		try {
			return new FilterHTMLHeadOutputStream(out, script.toString()
					.getBytes("ASCII")); //$NON-NLS-1$
		} catch (UnsupportedEncodingException uee) {
			return out;
		}
	}
}
