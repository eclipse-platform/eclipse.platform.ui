package org.eclipse.update.internal.ui.manager;

import org.eclipse.ui.views.properties.*;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import java.util.*;
import org.eclipse.update.ui.model.*;
import java.net.*;

public class SiteBookmarkPropertySource implements IPropertySource {
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
			desc = new TextPropertyDescriptor(SiteBookmark.P_URL, "Site URL");
			desc.setDescription("a URL of the update site");
			descriptors.add(desc);
			desc = new TextPropertyDescriptor(SiteBookmark.P_NAME, "Site name");
			desc.setDescription("a display name of the update site");
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

