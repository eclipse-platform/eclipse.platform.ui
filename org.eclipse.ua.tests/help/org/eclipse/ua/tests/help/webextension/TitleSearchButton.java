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

package org.eclipse.ua.tests.help.webextension;

import java.util.Locale;

import org.eclipse.core.runtime.Platform;
import org.eclipse.help.webapp.AbstractButton;
import org.eclipse.ua.tests.plugin.UserAssistanceTestPlugin;

public class TitleSearchButton extends AbstractButton {

	public String getAction() {
		return "toggleTitleView";
	}

	public String getId() {
		return "tsearch";
	}

	public String getImageURL() {
		return "/titlesearch/icons/sample3.gif";
	}

	public String getTooltip(Locale locale) {
		if ("es".equals(locale.getLanguage())) {
			return "Muestre busque en titulos";
		}
		return "Show Search Topic Title";
	}
	
	public String getJavaScriptURL() {
		return "/titlesearch/script/titlesearch.js";
	}
	
	public boolean isAddedToToolbar(String toolbarName) {
		boolean addButton = Platform.getPreferencesService().getBoolean
	    (UserAssistanceTestPlugin.getPluginId(), "extraButton", false, null);
		return (addButton & toolbarName.equals(AbstractButton.CONTENT_TOOLBAR));
	}

}
