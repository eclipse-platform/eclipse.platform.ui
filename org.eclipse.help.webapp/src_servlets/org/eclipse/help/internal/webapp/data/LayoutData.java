package org.eclipse.help.internal.webapp.data;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.help.internal.HelpSystem;
import org.eclipse.help.internal.webapp.servlet.*;

/*
 * (c) Copyright IBM Corp. 2002.
 * All Rights Reserved.
 */

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
		if (banner != null) {
			if (banner.trim().length() == 0)
				banner = null;
			else
				banner = UrlUtil.getHelpURL(banner);
		}
		return banner;
	}

	public String getBannerHeight() {
		if (getBannerURL() == null)
			return "0";
		else
			return preferences.getBannerHeight();
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
		if (HelpSystem.getMode() == HelpSystem.MODE_INFOCENTER) {
			views =
				new View[] {
					new View(
						"toc",
						"",
						preferences.getImagesDirectory() + "/contents_view.gif"),
					new View(
						"search",
						"",
						preferences.getImagesDirectory()
							+ "/search_results_view.gif"),
					};
		} else {
			// workbench or standalone
			views =
				new View[] {
					new View(
						"toc",
						"",
						preferences.getImagesDirectory() + "/contents_view.gif"),
					new View(
						"search",
						"",
						preferences.getImagesDirectory()
							+ "/search_results_view.gif"),
					new View(
						"links",
						"",
						preferences.getImagesDirectory() + "/links_view.gif"),
					new View(
						"bookmarks",
						"",
						preferences.getImagesDirectory() + "/bookmarks_view.gif")};
		}
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
