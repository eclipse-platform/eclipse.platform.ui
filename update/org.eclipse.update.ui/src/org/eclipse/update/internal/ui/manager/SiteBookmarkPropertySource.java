package org.eclipse.update.internal.ui.manager;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.views.properties.*;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import java.util.*;
import org.eclipse.update.ui.internal.model.*;
import java.net.*;
import org.eclipse.update.internal.ui.UpdateUIPlugin;

public class SiteBookmarkPropertySource implements IPropertySource {
private static final String KEY_URL_LABEL = "SiteBookmarkPropertySource.url.label";
private static final String KEY_URL_DESC = "SiteBookmarkPropertySource.url.desc";
private static final String KEY_NAME_LABEL = "SiteBookmarkPropertySource.name.label";
private static final String KEY_NAME_DESC = "SiteBookmarkPropertySource.name.desc";
 
	private SiteBookmark bookmark;
	private Vector descriptors;
	/**
	 * @see IPropertySource#getEditableValue()
	 */
	public Object getEditableValue() {
		return null;
	}
	
	public void setBookmark(SiteBookmark bookmark) {
		this.bookmark = bookmark;
	}

	/**
	 * @see IPropertySource#getPropertyDescriptors()
	 */
	public IPropertyDescriptor[] getPropertyDescriptors() {
		if (descriptors==null) {
			descriptors = new Vector();
			PropertyDescriptor desc;
			desc = new TextPropertyDescriptor(SiteBookmark.P_URL, UpdateUIPlugin.getResourceString(KEY_URL_LABEL));
			desc.setDescription(UpdateUIPlugin.getResourceString(KEY_URL_DESC));
			descriptors.add(desc);
			desc = new TextPropertyDescriptor(SiteBookmark.P_NAME, UpdateUIPlugin.getResourceString(KEY_NAME_LABEL));
			desc.setDescription(UpdateUIPlugin.getResourceString(KEY_NAME_DESC));
			descriptors.add(desc);
		}
		return (IPropertyDescriptor[])
			descriptors.toArray(new IPropertyDescriptor[descriptors.size()]);
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
			}
			catch (MalformedURLException e) {
			}
		}
	}
}

