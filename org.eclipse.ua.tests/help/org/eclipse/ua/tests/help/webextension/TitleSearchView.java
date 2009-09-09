/*******************************************************************************
 *  Copyright (c) 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ua.tests.help.webextension;

import java.util.Locale;

import org.eclipse.help.webapp.AbstractView;

public class TitleSearchView extends AbstractView {

	public String getImageURL() {
		return "/titlesearch/icons/sample3.gif";
	}

	public char getKey() {
		return 0;
	}

	public String getName() {
		return "titlesearch";
	}

	public String getTitle(Locale locale) {
		if ("es".equals(locale.getLanguage())) {
			return "Busque en titulos";
		}
		return "Search Topic Title";
	}

	public String getURL() {
		return "/titlesearch/jsp/advanced/";
	}
	
	public String getBasicURL() {
		return "/titlesearch/jsp/basic/";
	}
	
	public boolean isVisible() {
		return true;
	}
	
	public boolean isVisibleBasic() {
		return true;
	}

}
