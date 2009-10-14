/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.help.remote;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;

import org.eclipse.help.internal.toc.Toc;
import org.eclipse.help.internal.toc.TocContribution;
import org.eclipse.help.internal.webapp.data.UrlUtil;
import org.eclipse.help.internal.webapp.servlet.TocServlet;
import org.eclipse.ua.tests.help.other.UserToc;
import org.eclipse.ua.tests.help.other.UserTopic;

public class MockTocServlet extends TocServlet {

	private static final long serialVersionUID = 2934062693291854845L;

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String locale = UrlUtil.getLocale(req, resp);
		req.setCharacterEncoding("UTF-8"); //$NON-NLS-1$
		resp.setContentType("application/xml; charset=UTF-8"); //$NON-NLS-1$
		UserToc toc = new UserToc("Mock Toc " + locale, null, true);
		UserTopic topic = new UserTopic("Topic_" + locale, "http://www.eclipse.org", true);
		toc.addTopic(topic);
		TocContribution contribution = new TocContribution();
		contribution.setCategoryId(null);
		contribution.setContributorId("org.eclipse.ua.tests");
		contribution.setExtraDocuments(new String[0]);
		contribution.setId("mockToc");
		contribution.setLocale(locale);
	    contribution.setPrimary(true);
	    contribution.setSubToc(false);
	    contribution.setToc(new Toc(toc));
	    String response;
		try {
			response = serialize(new TocContribution[] { contribution }, locale);
		    resp.getWriter().write(response);
		} catch (TransformerException e) {
			resp.sendError(400);
		}
	}

}
