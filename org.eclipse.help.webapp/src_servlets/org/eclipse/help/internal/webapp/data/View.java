/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.webapp.data;

public class View {
    public static char NO_SHORTCUT = (char)0;
	private String name;
	private String url;
	private String imageURL;
	private char shortcut;

	public View(String name, String url, String imageURL, char shortcut) {
		this.name = name;
		this.url = url;
		this.imageURL = imageURL;
		this.shortcut = shortcut;
	}

	public String getName() {
		return name;
	}

	public String getURL() {
		return url;
	}

	/**
	 * Returns the enabled gray image
	 * 
	 * @return String
	 */
	public String getImage() {
		int i = imageURL.lastIndexOf('/');
		return imageURL.substring(0, i) + "/e_" + imageURL.substring(i + 1); //$NON-NLS-1$
	}

	/**
	 * Returns the image when selected
	 * 
	 * @return String
	 */
	public String getOnImage() {
		return getImage();
	}
	/**
	 * Returns the image when selected
	 * 
	 * @return char or 0 if no shortcut
	 */
	public char getKey() {
		return shortcut;
	}
}
