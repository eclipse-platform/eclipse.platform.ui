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

import java.util.Locale;

import org.eclipse.help.internal.webapp.WebappResources;
import org.eclipse.help.webapp.AbstractView ;

public class View extends AbstractView {
    public static char NO_SHORTCUT = (char)0;
	private String name;
	private String url;
	private String imageURL;
	private char shortcut;
	private boolean isDeferred;

	public View(String name, String url, String imageURL, char shortcut, boolean isDeferred) {
		this.name = name;
		this.url = url;
		this.imageURL = imageURL;
		this.shortcut = shortcut;
		this.isDeferred = isDeferred;
	}

	public String getName() {
		return name;
	}

	public String getURL() {
		return url;
	}

	/**
	 * Returns the image shown on the view tab
	 * 
	 * @return String
	 */
	public String getImageURL() {
		int i = imageURL.lastIndexOf('/');
		return imageURL.substring(0, i) + "/e_" + imageURL.substring(i + 1); //$NON-NLS-1$
	
	}
	/**
	 * Returns the image when selected
	 * 
	 * @return char or 0 if no shortcut
	 */
	public char getKey() {
		return shortcut;
	}
	
	/**
	 * Returns whether or not this view should do a deferred load; i.e. it will
	 * take some time to load and should show a progress message while loading.
	 */
	public boolean isDeferred() {
		return isDeferred;
	}

	public String getAdvancedUrl() {
		return null;
	}

	public String getImageUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getTitle(Locale locale) {
		return WebappResources.getString(name, locale);
	}
}
