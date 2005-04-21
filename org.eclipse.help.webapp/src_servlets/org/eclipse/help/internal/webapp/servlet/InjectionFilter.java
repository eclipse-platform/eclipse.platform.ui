/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.help.internal.webapp.servlet;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.eclipse.help.internal.webapp.data.UrlUtil;

/**
 * This class inserts a CSSs for narrow and disabled CSSs when called from the
 * dynamic help view.
 */
public class InjectionFilter implements IFilter {
	private static final String narrowBook1 = "\n<link rel=\"stylesheet\" href=\""; //$NON-NLS-1$

	private static final String narrowBook2 = "narrow_book.css\" charset=\"ISO-8859-1\" type=\"text/css\">"; //$NON-NLS-1$

	private static final String disabledBook1 = "\n<link rel=\"stylesheet\" href=\""; //$NON-NLS-1$

	private static final String disabledBook2 = "disabled_book.css\" charset=\"ISO-8859-1\" type=\"text/css\">"; //$NON-NLS-1$	

	/*
	 * @see IFilter#filter(HttpServletRequest, OutputStream)
	 */
	public OutputStream filter(HttpServletRequest req, OutputStream out) {
		boolean addNarrow = false;
		boolean addDisabled = false;
		boolean addFeedback = true;

		String uri = req.getRequestURI();
		if (uri == null || !uri.endsWith("html") && !uri.endsWith("htm")) { //$NON-NLS-1$ //$NON-NLS-2$
			return out;
		}
		if (UrlUtil.isBot(req)) {
			return out;
		}
		String pathInfo = req.getPathInfo();
		if (pathInfo == null) {
			return out;
		}
		boolean enabled = HelpBasePlugin.getActivitySupport().isRoleEnabled(
				pathInfo);
		if ("/ntopic".equals(req.getServletPath())) //$NON-NLS-1$
			addNarrow = true;
		if (!enabled) {
			addDisabled = true;
		}
		if (!addNarrow && !addDisabled && !addFeedback)
			return out;

		IPath path = new Path(pathInfo);
		int segmentCount = path.segmentCount();
		StringBuffer script = new StringBuffer();
		StringBuffer disabledContent = new StringBuffer();
		StringBuffer feedbackContent = new StringBuffer();
		if (addNarrow) {
			script.append(narrowBook1);
			appendRelativePath(script, segmentCount - 1);
			script.append(narrowBook2);
		}
		if (addDisabled) {
			script.append(disabledBook1);
			appendRelativePath(script, segmentCount - 1);
			script.append(disabledBook2);
			appendDisabled(disabledContent);
		}
		if (addFeedback) {
			appendFeedback(feedbackContent);
		}
		try {
			return new FilterHTMLHeadAndBodyOutputStream(out, script.toString()
					.getBytes("ASCII"), addDisabled ? disabledContent
					.toString().getBytes("ASCII") : null,
					addFeedback ? feedbackContent.toString().getBytes("ASCII")
							: null);
		} catch (UnsupportedEncodingException uee) {
			return out;
		}
	}

	private void appendRelativePath(StringBuffer buff, int nsteps) {
		for (int i = 0; i < nsteps; i++) {
			buff.append("../");
		}
		buff.append("PRODUCT_PLUGIN/");
	}

	private void appendDisabled(StringBuffer buff) {
		buff.append("<p><b>This topic belongs to a role that is disabled. <a href=\"enable.html\">Enable the role.</a></b></p>");
	}
	private void appendFeedback(StringBuffer buff) {
		buff.append("<p>Did you like this topic? <a href=\"feedback.html\">Tell us.</a></p>");
	}
}