package org.eclipse.help.internal.webapp.servlet;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.Preferences;
import org.eclipse.help.internal.HelpPlugin;

/**
 * Preferences for availiable to webapp
 */
public class WebappPreferences {
	Preferences prefs;
	/**
	 * Constructor.
	 */
	public WebappPreferences() {
		prefs = HelpPlugin.getDefault().getPluginPreferences();
	}
	/**
	 * @return String - URL of banner page or null
	 */
	public String getBanner() {
		String banner = prefs.getString("banner");
		if (banner != null) {
			if (banner.trim().length() == 0)
				banner = null;
			else
				banner = UrlUtil.getHelpURL(banner);
		}

		return banner;
	}

	public String getBannerHeight() {
		return prefs.getString("banner_height");
	}

	public String getHelpHome() {
		return prefs.getString("help_home");
	}

	public boolean isBookmarksView() {
		return "true".equals(prefs.getString("bookmarksView"));
	}

	public boolean isLinksView() {
		return "true".equals(prefs.getString("linksView"));
	}

	public String getImagesDirectory() {
		String imagesDirectory = prefs.getString("imagesDirectory");
		if (imagesDirectory != null && imagesDirectory.startsWith("/"))
			imagesDirectory = UrlUtil.getHelpURL(imagesDirectory);
		return imagesDirectory;

	}

	public String getToolbarBackground() {
		return prefs.getString("advanced.toolbarBackground");
	}

	public String getBasicToolbarBackground() {
		return prefs.getString("basic.toolbarBackground");
	}

	public String getToolbarFont() {
		return prefs.getString("advanced.toolbarFont");
	}

	public String getBasicToolbarFont() {
		return prefs.getString("basic.toolbarFont");
	}

	public String getViewBackground() {
		return prefs.getString("advanced.viewBackground");
	}

	public String getBasicViewBackground() {
		return prefs.getString("basic.viewBackground");
	}

	public String getViewFont() {
		return prefs.getString("advanced.viewFont");
	}

	public String getBasicViewFont() {
		return prefs.getString("basic.viewFont");
	}
}