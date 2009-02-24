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

package org.eclipse.help.internal.webapp.servlet;

import java.io.*;

import javax.servlet.http.*;

import org.eclipse.help.internal.webapp.data.*;
import org.eclipse.help.webapp.IFilter;

/**
 * This class inserts a script for showing the page inside the table of contents
 */
public class ShowInTocFilter implements IFilter {
	private static final String scriptPart1 = "\n<script type=\"text/javascript\" src=\""; //$NON-NLS-1$
	private static final String scriptPart3 = "advanced/synchWithToc.js\"></script>"; //$NON-NLS-1$

	/*
	 * @see IFilter#filter(HttpServletRequest, OutputStream)
	 */
	public OutputStream filter(HttpServletRequest req, OutputStream out) {
		String uri = req.getRequestURI();
		if (uri == null) return out;
		if (!uri.endsWith("html") && !uri.endsWith("htm") && !UrlUtil.isNavPath(uri)) { //$NON-NLS-1$ //$NON-NLS-2$
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
		String relativePath = FilterUtils.getRelativePathPrefix(req);
		script.append(relativePath);
	
		script.append(scriptPart3);
		try {
			return new FilterHTMLHeadOutputStream(out, script.toString()
					.getBytes("ASCII")); //$NON-NLS-1$
		} catch (UnsupportedEncodingException uee) {
			return out;
		}
	}
	
	
}
