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
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.eclipse.help.internal.webapp.data.UrlUtil;

/**
 * This class inserts a CSSs for narrow and disabled CSSs when called from the
 * dynamic help view.
 */
public class InjectionFilter implements IFilter {
	private static final String narrowBook1 = "\n<link rel=\"stylesheet\" href=\""; //$NON-NLS-1$

	private static final String narrowBook2 = "narrow_book.css\" charset=\"ISO-8859-1\" type=\"text/css\">"; //$NON-NLS-1$
	
	private static final String osNarrowBook2 = "_narrow_book.css\" charset=\"ISO-8859-1\" type=\"text/css\">"; //$NON-NLS-1$
	private static final String disabledBook1 = "\n<link rel=\"stylesheet\" href=\""; //$NON-NLS-1$

	private static final String disabledBook2 = "disabled_book.css\" charset=\"ISO-8859-1\" type=\"text/css\">"; //$NON-NLS-1$

	private static final String disabledBook3 = "\n<script language=\"JavaScript\" src=\""; //$NON-NLS-1$

	private static final String disabledBook4 = "livehelp.js\"> </script>"; //$NON-NLS-1$
	
	/*
	 * @see IFilter#filter(HttpServletRequest, OutputStream)
	 */
	public OutputStream filter(HttpServletRequest req, OutputStream out) {
		// This filter only works inside the workbench
		if (BaseHelpSystem.getMode() != BaseHelpSystem.MODE_WORKBENCH)
			return out;

		boolean addNarrow = false;
		boolean addDisabled = false;
		boolean needsLiveHelp = false;		

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
		if (!addNarrow && !addDisabled)
			return out;
		
		needsLiveHelp = HelpBasePlugin.getActivitySupport().getDocumentMessageUsesLiveHelp(addNarrow);

		IPath path = new Path(pathInfo);
		int upLevels = path.segmentCount() - 1;
		StringBuffer script = new StringBuffer();
		StringBuffer disabledContent = new StringBuffer();
		//StringBuffer feedbackContent = new StringBuffer();
		if (addNarrow) {
			script.append(narrowBook1);
			appendRelativePath(script, upLevels);
			script.append(narrowBook2);
			script.append(narrowBook1);
			appendRelativePath(script, upLevels);
			script.append(Platform.getOS());
			script.append(osNarrowBook2);
		}
		if (addDisabled) {
			script.append(disabledBook1);
			appendRelativePath(script, upLevels);
			script.append(disabledBook2);
			if (needsLiveHelp) {
				script.append(disabledBook3);
				appendRelativePath(script, upLevels, "org.eclipse.help"); //$NON-NLS-1$
				script.append(disabledBook4);
			}
			appendDisabled(disabledContent, upLevels, addNarrow);
		}
		try {
			return new FilterHTMLHeadAndBodyOutputStream(
					out,
					script.toString().getBytes("ASCII"), addDisabled ? disabledContent.toString() : null); //$NON-NLS-1$
		} catch (UnsupportedEncodingException uee) {
			return out;
		}
	}

	private void appendRelativePath(StringBuffer buff, int nsteps,
			String pluginId) {
		for (int i = 0; i < nsteps; i++) {
			buff.append("../"); //$NON-NLS-1$
		}
		buff.append(pluginId + "/"); //$NON-NLS-1$
	}

	private void appendRelativePath(StringBuffer buff, int nsteps) {
		appendRelativePath(buff, nsteps, "PRODUCT_PLUGIN"); //$NON-NLS-1$
	}

	private void appendDisabled(StringBuffer buff, int nsteps, boolean narrow) {
		String message = HelpBasePlugin.getActivitySupport().getDocumentMessage(narrow);
		if (message==null)
			return;
		buff.append("<div id=\"help-disabledTopic\">"); //$NON-NLS-1$
		buff.append("<img src=\""); //$NON-NLS-1$
		appendRelativePath(buff, nsteps, "org.eclipse.help.webapp"); //$NON-NLS-1$
		buff.append("advanced/images/e_show_all.gif\" border=\"0\" align=\"bottom\">&nbsp;"); //$NON-NLS-1$		
		buff.append(message);
		buff.append("<br><hr></div>"); //$NON-NLS-1$
	}
}