package org.eclipse.update.internal.ui.properties;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.views.properties.*;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import java.util.*;
import org.eclipse.update.internal.ui.model.*;
import java.net.*;
import org.eclipse.update.internal.ui.UpdateUIPlugin;

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
					UpdateUIPlugin.getResourceString(KEY_URL_LABEL));
		else
			desc =
				new PropertyDescriptor(
					SiteBookmark.P_URL,
					UpdateUIPlugin.getResourceString(KEY_URL_LABEL));
		desc.setDescription(UpdateUIPlugin.getResourceString(KEY_URL_DESC));
		descriptors[0] = desc;

		if (bookmark.getType() == SiteBookmark.USER)
			desc =
				new TextPropertyDescriptor(
					SiteBookmark.P_NAME,
					UpdateUIPlugin.getResourceString(KEY_NAME_LABEL));
		else
			desc =
				new PropertyDescriptor(
					SiteBookmark.P_NAME,
					UpdateUIPlugin.getResourceString(KEY_NAME_LABEL));
		desc.setDescription(UpdateUIPlugin.getResourceString(KEY_NAME_DESC));
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