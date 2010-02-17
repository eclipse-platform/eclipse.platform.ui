/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.help.internal.webapp.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.core.runtime.Path;
import org.eclipse.help.ITopic;
import org.eclipse.help.base.AbstractHelpScope;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.base.scope.ScopeUtils;
import org.eclipse.help.internal.toc.Toc;
import org.eclipse.help.internal.webapp.data.RequestScope;
import org.eclipse.help.internal.webapp.data.UrlUtil;

public class ChildLinkInserter {
	
	private static final String UTF_8 = "UTF-8"; //$NON-NLS-1$
	private HttpServletRequest req;
	private OutputStream out;
	private static final String NO_CHILDREN = "no_child_topics"; //$NON-NLS-1$
	private static final String HAS_CHILDREN = "has_child_topics"; //$NON-NLS-1$
	private AbstractHelpScope scope;

	public ChildLinkInserter(HttpServletRequest req, OutputStream out) {
		this.req = req;
		this.out = out;
		scope = RequestScope.getScope(req, null, false);
	}
	
	public void addContents(String encoding) throws IOException {	
	    ITopic topic = getTopic();
	    if (topic == null || topic.getHref() == null) return;
	    ITopic[] subtopics = topic.getSubtopics();
	    if (subtopics.length == 0) {
	    	return;
	    }
	    StringBuffer links = new StringBuffer("\n<ul class=\"childlinks\">\n"); //$NON-NLS-1$
		for (int i=0;i<subtopics.length;++i) {
			if (ScopeUtils.showInTree(subtopics[i], scope)) {
				links.append("\n<li><a href=\""); //$NON-NLS-1$
				String href = subtopics[i].getHref();
				if (href == null) {
					href = "nav.html";   // TODO, handle nav topics as children //$NON-NLS-1$
				}
				else {
					href = XMLGenerator.xmlEscape(href);
				}
				links.append(getBackpath(req.getPathInfo()));
				links.append(href);
				links.append("\">" + subtopics[i].getLabel() + "</a></li>\n");  //$NON-NLS-1$//$NON-NLS-2$
			}
		}
		links.append("\n</ul>\n"); //$NON-NLS-1$
		String linkString = links.toString();
		try {
			if (encoding != null) {
			    out.write(linkString.getBytes(encoding));
			} else {
			    out.write(linkString.getBytes("UTF8")); //$NON-NLS-1$
			}
		} catch (UnsupportedEncodingException e) {
			out.write(linkString.getBytes());
		}
	}
	
	private ITopic getTopic() {
		String locale = UrlUtil.getLocale(req, null);
		String pathInfo = req.getPathInfo();
		String servletPath = req.getServletPath();
		if ("/nav".equals(servletPath)) return null; //$NON-NLS-1$
		String topicPath= servletPath + pathInfo;
		int[] path = UrlUtil.getTopicPath(topicPath, locale );
		if (path == null || path.length == 1) return null;
		Toc[] tocs = HelpPlugin.getTocManager().getTocs(locale.toString());
		ITopic topic = tocs[path[0]].getTopics()[path[1]];
		for (int i = 2; i < path.length; i++) {
			topic = topic.getSubtopics()[path[i]];
		}
		return topic;
	}
	
	private String getBackpath(String path) {
		int num = new Path(path).segmentCount() - 1;
		StringBuffer buf = new StringBuffer();
		for (int i=0; i < num; ++i) {
			if (i > 0) {
				buf.append('/');
			}
			buf.append(".."); //$NON-NLS-1$
		}
		return buf.toString();
	}

	public void addStyle() throws UnsupportedEncodingException, IOException {
		ITopic topic = getTopic();
		if (topic != null && topic.getHref() != null) {
			ITopic[] subtopics = topic.getSubtopics();
			for (int i = 0; i < subtopics.length; ++i) {
				if (ScopeUtils.showInTree(subtopics[i], scope)) {
					out.write(HAS_CHILDREN.getBytes(UTF_8));
					return;
				}
			}
		}
		out.write(NO_CHILDREN.getBytes(UTF_8));
	}

}
