/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.eclipse.help.internal.webapp.data.CssUtil;
import org.eclipse.help.internal.webapp.data.UrlUtil;

/**
 * This class inserts a CSSs for narrow and disabled CSSs when called from the
 * dynamic help view.
 */
public class InjectionFilter implements IFilter {
	private final static String cssLink1 = "\n<link rel=\"stylesheet\" href=\""; //$NON-NLS-1$
	private static final String cssLink2 = "\" type=\"text/css\"></link>"; //$NON-NLS-1$

	private static final String disabledBook3 = "\n<script type=\"text/javascript\" src=\""; //$NON-NLS-1$

	private static final String disabledBook4 = "livehelp.js\"> </script>"; //$NON-NLS-1$	

	private final String TOPIC_CSS = "topic_css"; //$NON-NLS-1$
	private final String NAV_CSS   = "nav_css"; //$NON-NLS-1$
	private final String NARROW_CSS = "narrow_css"; //$NON-NLS-1$
	private final String DISABLED_CSS = "disabled_css"; //$NON-NLS-1$
	
	/*
	 * @see IFilter#filter(HttpServletRequest, OutputStream)
	 */
	public OutputStream filter(HttpServletRequest req, OutputStream out) {
		boolean isInfocenter = BaseHelpSystem.getMode() != BaseHelpSystem.MODE_WORKBENCH;

		boolean addNarrow = false;
		boolean addDisabled = false;
		boolean needsLiveHelp = false;	 

		String uri = req.getRequestURI();
		boolean isNav = "/nav".equals(req.getServletPath()); //$NON-NLS-1$
		if (uri == null || !uri.endsWith("html") && !uri.endsWith("htm") && !isNav) { //$NON-NLS-1$ //$NON-NLS-2$
			return out;
		}
		if (UrlUtil.isBot(req)) {
			return out;
		}
		String pathInfo = req.getPathInfo();
		if (pathInfo == null) {
			return out;
		}

		List cssIncludes = new ArrayList();
		if (isNav) {
			addCssFiles(NAV_CSS, cssIncludes);
		} else {
			addCssFiles(TOPIC_CSS, cssIncludes);
		}
		
		boolean enabled = isInfocenter || isNav || HelpBasePlugin.getActivitySupport().isRoleEnabled(
				pathInfo);
		if ("/ntopic".equals(req.getServletPath())) { //$NON-NLS-1$
			addNarrow = true;
			addCssFiles(NARROW_CSS, cssIncludes);
		}
		if (!enabled) {
			addDisabled = true;
			addCssFiles(DISABLED_CSS, cssIncludes);
		}
		
		needsLiveHelp = !enabled && HelpBasePlugin.getActivitySupport().getDocumentMessageUsesLiveHelp(addNarrow);
		
		if (cssIncludes.size() == 0 && !addDisabled)
			return out;

		IPath path = new Path(pathInfo);
		int upLevels = path.segmentCount() - 1;
		String relativePath = FilterUtils.getRelativePathPrefix(req);
		StringBuffer script = new StringBuffer();
		StringBuffer disabledContent = new StringBuffer();
		for (Iterator iter = cssIncludes.iterator(); iter.hasNext();) {
			String cssPath = (String)iter.next();
			script.append(cssLink1);
			script.append("../content/PLUGINS_ROOT"); //$NON-NLS-1$
			script.append(cssPath);
			script.append(cssLink2);
		}
		if (addDisabled) {
			if (needsLiveHelp) {
				script.append(disabledBook3);
				script.append("../content/PLUGINS_ROOT/org.eclipse.help/"); //$NON-NLS-1$
				script.append(disabledBook4);
			}
			appendDisabled(disabledContent, upLevels, addNarrow, relativePath);
		}
		try {
			return new FilterHTMLHeadAndBodyOutputStream(
					out,
					script.toString().getBytes("ASCII"), addDisabled ? disabledContent.toString() : null); //$NON-NLS-1$
		} catch (UnsupportedEncodingException uee) {
			return out;
		}
	}

	private void addCssFiles(final String preference, List list) {
		String topicCssPath = Platform.getPreferencesService().getString(HelpBasePlugin.PLUGIN_ID, preference, "", null);  //$NON-NLS-1$
		String[] cssFiles = CssUtil.getCssFilenames(topicCssPath);
		for (int i = 0; i < cssFiles.length; i++) {
			list.add(cssFiles[i]);
		}
	}

	private void appendDisabled(StringBuffer buff, int nsteps, boolean narrow, String relativePath) {
		String message = HelpBasePlugin.getActivitySupport().getDocumentMessage(narrow);
		if (message==null)
			return;
		buff.append("<div id=\"help-disabledTopic\">"); //$NON-NLS-1$
		buff.append("<img src=\""); //$NON-NLS-1$
		buff.append("../content/PLUGINS_ROOT/org.eclipse.help.webapp/"); //$NON-NLS-1$
		buff.append("advanced/images/e_show_all.gif\" border=\"0\" align=\"bottom\">&nbsp;"); //$NON-NLS-1$		
		buff.append(message);
		buff.append("<br><hr></div>"); //$NON-NLS-1$
	}
}
