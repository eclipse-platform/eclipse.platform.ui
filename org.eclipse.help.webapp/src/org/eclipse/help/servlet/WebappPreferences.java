package org.eclipse.help.servlet;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import javax.servlet.*;

import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.*;

/**
 * Uses a resource bundle to load images and strings from
 * a property file in a documentation plugin
 */
public class WebappPreferences {

	private String banner = null;
	private String banner_height = "45";
	private String help_home = null;
	private String bookmarksView = null;
	private String bookmarks = null;
	private String linksView = null;
	private String imagesDirectory = "images";
	private String toolbarBackground = "ButtonFace";
	private String toolbarFont = "icon";
	private String viewBackground = "ButtonFace";
	private String viewFont = "icon";

	private ServletContext context;

	/**
	 * Resources constructort.
	 */
	protected WebappPreferences(ServletContext context) {
		this.context = context;
		loadPreferences();
	}

	public String getBanner() {
		return banner;
	}

	public String getBannerHeight() {
		return banner_height;
	}

	public String getHelpHome() {
		return help_home;
	}

	public boolean isBookmarksView() {
		return "true".equals(bookmarksView);
	}

	public boolean isLinksView() {
		return "true".equals(linksView);
	}

	public String getImagesDirectory() {
		return imagesDirectory;
	}

	public String getToolbarBackground() {
		return toolbarBackground;
	}

	public String getToolbarFont() {
		return toolbarFont;
	}

	public String getViewBackground() {
		return viewBackground;
	}

	public String getViewFont() {
		return viewFont;
	}

	/**
	 * Loads preferences 
	 */
	private void loadPreferences() {
		Preferences prefs = HelpPlugin.getDefault().getPluginPreferences();
		String bookmarks = prefs.getString(HelpSystem.BOOKMARKS);

		banner = prefs.getString("banner");
		banner_height = prefs.getString("banner_height");
		help_home = prefs.getString("help_home");
		bookmarksView = prefs.getString("bookmarksView");
		bookmarks = prefs.getString("bookmarks");
		linksView = prefs.getString("linksView");
		imagesDirectory = prefs.getString("imagesDirectory");
		toolbarBackground = prefs.getString("toolbarBackground");
		toolbarFont = prefs.getString("toolbarFont");
		viewBackground = prefs.getString("viewBackground");
		viewFont = prefs.getString("viewFont");
		
		if (banner != null) {
			if (banner.trim().length() == 0)
				banner = null;
			else
				banner = UrlUtil.getHelpURL(banner);
		}

		if (imagesDirectory != null && imagesDirectory.startsWith("/"))
			imagesDirectory = UrlUtil.getHelpURL(imagesDirectory);
	}
}