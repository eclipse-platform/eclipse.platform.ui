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
package org.eclipse.update.internal.ui.properties;
import java.net.*;

import org.eclipse.ui.views.properties.*;
import org.eclipse.update.internal.ui.UpdateUI;
import org.eclipse.update.internal.ui.model.SiteBookmark;

public class SiteBookmarkPropertySource implements IPropertySource {
	private static final String KEY_URL_LABEL =
		"SiteBookmarkPropertySource.url.label";
	private static final String KEY_URL_DESC =
		"SiteBookmarkPropertySource.url.desc";
	private static final String KEY_NAME_LABEL =
		"SiteBookmarkPropertySource.name.label";
	private static final String KEY_NAME_DESC =
		"SiteBookmarkPropertySource.name.desc";

	private SiteBookmark bookmark;

	public SiteBookmarkPropertySource(SiteBookmark bookmark) {
		this.bookmark = bookmark;
	}
	/**
	 * @see IPropertySource#getEditableValue()
	 */
	public Object getEditableValue() {
		return null;
	}

	/**
	 * @see IPropertySource#getPropertyDescriptors()
	 */
	public IPropertyDescriptor[] getPropertyDescriptors() {
		IPropertyDescriptor[] descriptors = new IPropertyDescriptor[2];
		PropertyDescriptor desc;
		if (bookmark.getType() != SiteBookmark.LOCAL)
			desc =
				new TextPropertyDescriptor(
					SiteBookmark.P_URL,
					UpdateUI.getString(KEY_URL_LABEL));
		else
			desc =
				new PropertyDescriptor(
					SiteBookmark.P_URL,
					UpdateUI.getString(KEY_URL_LABEL));
		desc.setDescription(UpdateUI.getString(KEY_URL_DESC));
		descriptors[0] = desc;

		if (bookmark.getType() == SiteBookmark.USER)
			desc =
				new TextPropertyDescriptor(
					SiteBookmark.P_NAME,
					UpdateUI.getString(KEY_NAME_LABEL));
		else
			desc =
				new PropertyDescriptor(
					SiteBookmark.P_NAME,
					UpdateUI.getString(KEY_NAME_LABEL));
		desc.setDescription(UpdateUI.getString(KEY_NAME_DESC));
		descriptors[1] = desc;
		return descriptors;
	}

	/**
	 * @see IPropertySource#getPropertyValue(Object)
	 */
	public Object getPropertyValue(Object property) {
		if (property.equals(SiteBookmark.P_NAME))
			return bookmark.getName();
		if (property.equals(SiteBookmark.P_URL))
			return bookmark.getURL().toString();
		return "";
	}

	/**
	 * @see IPropertySource#isPropertySet(Object)
	 */
	public boolean isPropertySet(Object arg0) {
		return false;
	}

	/**
	 * @see IPropertySource#resetPropertyValue(Object)
	 */
	public void resetPropertyValue(Object arg0) {
	}

	/**
	 * @see IPropertySource#setPropertyValue(Object, Object)
	 */
	public void setPropertyValue(Object property, Object value) {
		if (property.equals(SiteBookmark.P_NAME))
			bookmark.setName(value.toString());
		if (property.equals(SiteBookmark.P_URL)) {
			try {
				URL url = new URL(value.toString());
				bookmark.setURL(url);
			} catch (MalformedURLException e) {
			}
		}
	}
}
