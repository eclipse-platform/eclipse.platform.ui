/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.webapp.data;
import javax.servlet.*;
import javax.servlet.http.*;

import org.eclipse.help.*;
import org.eclipse.help.internal.*;

/**
 * Helper class for linksView.jsp initialization
 */
public class LinksData extends RequestData {

	// Request parameters
	private String topicHref;
	private String selectedTopicId = ""; //$NON-NLS-1$

	// list of related links
	private IHelpResource[] links;

	/**
	 * Constructs data for the links page.
	 * 
	 * @param context
	 * @param request
	 */
	public LinksData(ServletContext context, HttpServletRequest request,
			HttpServletResponse response) {
		super(context, request, response);
		this.topicHref = request.getParameter("topic"); //$NON-NLS-1$
		if (topicHref != null && topicHref.length() == 0)
			topicHref = null;

		if (isLinksRequest())
			loadLinks();
	}

	/**
	 * Returns true when there is a search request
	 * 
	 * @return boolean
	 */
	public boolean isLinksRequest() {
		return (request.getParameter("contextId") != null); //$NON-NLS-1$
	}

	/**
	 * Return the number of links
	 * 
	 * @return int
	 */
	public int getLinksCount() {
		return links.length;
	}

	public String getSelectedTopicId() {
		return selectedTopicId;
	}

	public String getTopicHref(int i) {
		return UrlUtil.getHelpURL(links[i].getHref());
	}

	public String getTopicLabel(int i) {
		return UrlUtil.htmlEncode(links[i].getLabel());
	}

	public String getTopicTocLabel(int i) {
		IToc toc = findTocForTopic(links[i].getHref());
		if (toc != null)
			return UrlUtil.htmlEncode(toc.getLabel());
		else
			return ""; //$NON-NLS-1$
	}

	/**
	 * Finds a topic in a toc or within a scope if specified
	 */
	private IToc findTocForTopic(String href) {
		IToc[] tocs = HelpPlugin.getTocManager().getTocs(getLocale());
		for (int i = 0; i < tocs.length; i++) {
			ITopic topic = tocs[i].getTopic(href);
			if (topic != null)
				return tocs[i];
		}
		return null;
	}

	private void loadLinks() {

		String contextId = request.getParameter("contextId"); //$NON-NLS-1$
		IContext context = HelpSystem.getContext(contextId);
		if (context == null) {
			links = new IHelpResource[0];
			return;
		}
		links = context.getRelatedTopics();
		if (links == null) {
			links = new IHelpResource[0];
			return;
		}

		for (int i = 0; i < links.length; i++) {
			// the following assume topic numbering as in linksView.jsp
			if (links[i].getHref().equals(topicHref)) {
				selectedTopicId = "a" + i; //$NON-NLS-1$
				break;
			}
		}
	}

}
