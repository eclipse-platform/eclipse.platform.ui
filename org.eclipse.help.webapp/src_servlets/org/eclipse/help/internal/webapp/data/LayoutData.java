/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
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

public class LayoutData extends RequestData {

	private String query = "";
	private View[] views;

	public LayoutData(ServletContext context, HttpServletRequest request) {
		super(context, request);

		// initialize the query string
		String qs = request.getQueryString();
		if (qs != null && qs.length() > 0)
			query = "?" + qs;
	}

	public String getQuery() {
		return query;
	}

	public String getBannerURL() {
		String banner = preferences.getBanner();
		if (banner == null || banner.trim().length() == 0) {
			banner = "about:blank";
		} else if (banner.startsWith("http:/")) {
		} else if (banner.startsWith("file:/") || banner.startsWith("jar:")) {
			banner = "topic/" + banner;
		} else {
			banner = "topic" + banner;
		}
		return banner;
	}

	public String getBannerHeight() {
		String height = preferences.getBannerHeight();
		if (height == null || height.length() == 0) {
			height = "0";
		}
		return height;
	}

	public String getContentURL() {
		TocData tocData = new TocData(context, request);
		String topic = tocData.getSelectedTopic();
		String help_home = preferences.getHelpHome();

		if (topic != null)
			help_home = topic;
		else
			help_home = UrlUtil.getHelpURL(help_home);

		return help_home;
	}

	/**
	 * Return array of length 0 if no views
	 */
	public View[] getViews() {
		if (views != null)
			return views;

		View tocview =
			new View(
				"toc",
				"",
				preferences.getImagesDirectory() + "/contents_view.gif");
		View searchview =
			new View(
				"search",
				"",
				preferences.getImagesDirectory() + "/search_results_view.gif");

		View linksview = null;
		View bookmarksview = null;

		if (preferences.isLinksView())
			linksview =
				new View(
					"links",
					"",
					preferences.getImagesDirectory() + "/links_view.gif");
		if (preferences.isBookmarksView())
			bookmarksview =
				new View(
					"bookmarks",
					"",
					preferences.getImagesDirectory() + "/bookmarks_view.gif");

		if (linksview != null && bookmarksview != null)
			views =
				new View[] { tocview, searchview, linksview, bookmarksview };
		else if (linksview != null)
			views = new View[] { tocview, searchview, linksview };
		else if (bookmarksview != null)
			views = new View[] { tocview, searchview, bookmarksview };
		else
			views = new View[] { tocview, searchview };

		return views;
	}

	public String getVisibleView() {
		String view = request.getParameter("tab");
		if (view != null && view.length() > 0)
			return view;
		else
			return "toc";
	}

	public View getCurrentView() {
		String name = request.getParameter("view");
		views = getViews();
		for (int i = 0; i < views.length; i++)
			if (views[i].getName().equals(name))
				return views[i];
		return null;
	}
}
