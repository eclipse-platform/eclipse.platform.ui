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
package org.eclipse.help.internal.webapp.data;

import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.base.*;

/**
 * Preferences for availiable to webapp
 */
public class WebappPreferences {
	Preferences prefs;
	/**
	 * Constructor.
	 */
	public WebappPreferences() {
		prefs = HelpBasePlugin.getDefault().getPluginPreferences();
	}
	/**
	 * @return String - URL of banner page or null
	 */
	public String getBanner() {
		return prefs.getString("banner"); //$NON-NLS-1$
	}

	public String getBannerHeight() {
		return prefs.getString("banner_height"); //$NON-NLS-1$
	}

	public String getHelpHome() {
		return prefs.getString("help_home"); //$NON-NLS-1$
	}

	public boolean isIndexView() {
		return "true".equals(prefs.getString("indexView")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public boolean isBookmarksView() {
		return BaseHelpSystem.getMode() != BaseHelpSystem.MODE_INFOCENTER
				&& "true".equals(prefs.getString("bookmarksView")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public boolean isBookmarksAction() {
		return "true".equals(prefs.getString("bookmarksView")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public boolean isLinksView() {
		return BaseHelpSystem.getMode() != BaseHelpSystem.MODE_INFOCENTER
				&& "true".equals(prefs.getString("linksView")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public String getImagesDirectory() {
		String imagesDirectory = prefs.getString("imagesDirectory"); //$NON-NLS-1$
		if (imagesDirectory != null && imagesDirectory.startsWith("/")) //$NON-NLS-1$
			imagesDirectory = UrlUtil.getHelpURL(imagesDirectory);
		return imagesDirectory;

	}

	public String getToolbarBackground() {
		return prefs.getString("advanced.toolbarBackground"); //$NON-NLS-1$
	}

	public String getBasicToolbarBackground() {
		return prefs.getString("basic.toolbarBackground"); //$NON-NLS-1$
	}

	public String getToolbarFont() {
		return prefs.getString("advanced.toolbarFont"); //$NON-NLS-1$
	}

	public String getViewBackground() {
		return prefs.getString("advanced.viewBackground"); //$NON-NLS-1$
	}

	public String getBasicViewBackground() {
		return prefs.getString("basic.viewBackground"); //$NON-NLS-1$
	}

	public String getViewFont() {
		return prefs.getString("advanced.viewFont"); //$NON-NLS-1$
	}

	public int getBookAtOnceLimit() {
		return prefs.getInt("loadBookAtOnceLimit"); //$NON-NLS-1$
	}

	public int getLoadDepth() {
		int value = prefs.getInt("dynamicLoadDepthsHint"); //$NON-NLS-1$
		if (value < 1) {
			return 1;
		}
		return value;
	}
	public boolean isWindowTitlePrefix() {
		return "true".equalsIgnoreCase(prefs.getString("windowTitlePrefix")); //$NON-NLS-1$ //$NON-NLS-2$
	}
	public boolean isDontConfirmShowAll() {
		return prefs.getBoolean("dontConfirmShowAll"); //$NON-NLS-1$
	}
	public void setDontConfirmShowAll(boolean dontconfirm) {
		prefs.setValue("dontConfirmShowAll", dontconfirm); //$NON-NLS-1$
	}
	public boolean isActiveHelp() {
		return "true".equalsIgnoreCase(prefs.getString("activeHelp")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public boolean isIndexInstruction() {
		return "true".equalsIgnoreCase(prefs.getString("indexInstruction")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public boolean isIndexButton() {
		return "true".equalsIgnoreCase(prefs.getString("indexButton")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public boolean isIndexPlusMinus() {
		return "true".equalsIgnoreCase(prefs.getString("indexPlusMinus")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public boolean isIndexExpandAll() {
		return "true".equalsIgnoreCase(prefs.getString("indexExpandAll")); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
