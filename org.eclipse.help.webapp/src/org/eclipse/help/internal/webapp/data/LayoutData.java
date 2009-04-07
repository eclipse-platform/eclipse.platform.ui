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
package org.eclipse.help.internal.webapp.data;

import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.core.runtime.Platform;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.webapp.HelpWebappPlugin;
import org.eclipse.osgi.service.localization.BundleLocalization;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class LayoutData extends RequestData {

	private String query = ""; //$NON-NLS-1$
	private View[] views;

	public LayoutData(ServletContext context, HttpServletRequest request,
			HttpServletResponse response) {
		super(context, request, response);

		// initialize the query string
		String qs = request.getQueryString();
		if (qs != null && qs.length() > 0)
			query = "?" + qs; //$NON-NLS-1$
	}

	public String getQuery() {
		return query;
	}

	public String getBannerURL() {
		String banner = preferences.getBanner();
		if (banner == null || banner.trim().length() == 0) {
			banner = "about:blank"; //$NON-NLS-1$
		} else if (banner.startsWith("http:/") || banner.startsWith("https:/")) { //$NON-NLS-1$ //$NON-NLS-2$
		} else if (banner.startsWith("file:/") || banner.startsWith("jar:file:/")) { //$NON-NLS-1$ //$NON-NLS-2$
			banner = "topic/" + banner; //$NON-NLS-1$
		} else {
			banner = "topic" + banner; //$NON-NLS-1$
		}
		return banner;
	}

	public String getBannerHeight() {
		String height = preferences.getBannerHeight();
		if (height == null || height.length() == 0) {
			height = "0"; //$NON-NLS-1$
		}
		return height;
	}

	public String getContentURL() {
		String navHref = request.getParameter("nav"); //$NON-NLS-1$
		if (navHref != null) {
			return "../nav" + navHref; //$NON-NLS-1$
		}
		String topicHref = request.getParameter("topic"); //$NON-NLS-1$
		if (topicHref == null || topicHref.length() == 0) {
			return UrlUtil.getHelpURL(preferences.getHelpHome());
		}
		else {
			TocData tocData = new TocData(context, request, response);
			String topic = tocData.getSelectedTopic();
			if (topic == null || !UrlUtil.isValidTopicURL(topic)) {
				return UrlUtil.getHelpURL(preferences.getHelpHome());
			}
			return  topic;
		}
	}

	/**
	 * Return array of length 0 if no views
	 */
	public View[] getViews() {
		if (views != null)
			return views;

		View tocview = new View("toc", //$NON-NLS-1$
				"", //$NON-NLS-1$
				preferences.getImagesDirectory() + "/contents_view.gif", 'C', !HelpPlugin.getTocManager().isTocLoaded(getLocale())); //$NON-NLS-1$
		View indexview = null;
		View searchview = new View("search", //$NON-NLS-1$
				"", //$NON-NLS-1$
				preferences.getImagesDirectory() + "/search_results_view.gif", 'R', false); //$NON-NLS-1$
		View bookmarksview = null;

		if (preferences.isIndexView())
			indexview = new View("index", //$NON-NLS-1$
					"", //$NON-NLS-1$
					preferences.getImagesDirectory() + "/index_view.gif", 'I', false); //$NON-NLS-1$
		if (preferences.isBookmarksView())
			bookmarksview = new View("bookmarks", //$NON-NLS-1$
					"", //$NON-NLS-1$
					preferences.getImagesDirectory() + "/bookmarks_view.gif", (char)0, false); //$NON-NLS-1$

		ArrayList viewList = new ArrayList();
		viewList.add(tocview);
		if (indexview != null) {
			viewList.add(indexview);
		}
		viewList.add(searchview);
		if (bookmarksview !=null) {
			viewList.add(bookmarksview);
		}
		
		views = (View[]) viewList.toArray(new View[viewList.size()]);
		return views;
	}

	public String getVisibleView() {
		String requestedView = request.getParameter("tab"); //$NON-NLS-1$
		View[] allViews = getViews();
		for (int i = 0; i < allViews.length; i++) {
			if (allViews[i].getName().equals(requestedView)) {
				return requestedView;
			}
		}
		return "toc"; //$NON-NLS-1$
	}

	public View getCurrentView() {
		String name = request.getParameter("view"); //$NON-NLS-1$
		views = getViews();
		for (int i = 0; i < views.length; i++)
			if (views[i].getName().equals(name))
				return views[i];
		return null;
	}
	public String getWindowTitle() {
		String titlePref = preferences.getTitleResource();
		int slash = titlePref.indexOf('/');
		if (slash > 0) {
			String resourceContainer = titlePref.substring(0, slash);
			String resource = titlePref.substring(slash + 1);	
			try {
				Bundle bundle = Platform.getBundle(resourceContainer);
				BundleContext bundleContext = HelpWebappPlugin.getContext();
				ServiceReference ref = bundleContext.getServiceReference(BundleLocalization.class.getName()); 
				BundleLocalization localization = (BundleLocalization) bundleContext.getService(ref); 
                return localization.getLocalization(bundle, locale).getString(resource);
			} catch (Exception e) {
				// Fall through
			}
		}
		
		if (preferences.isWindowTitlePrefix()) {
			return ServletResources.getString("browserTitle", //$NON-NLS-1$
					BaseHelpSystem.getProductName(), request);
		}
		return BaseHelpSystem.getProductName();
	}
}
