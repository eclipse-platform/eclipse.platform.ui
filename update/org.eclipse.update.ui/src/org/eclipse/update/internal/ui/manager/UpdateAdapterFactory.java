package org.eclipse.update.internal.ui.manager;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.ui.views.properties.*;
import org.eclipse.update.ui.internal.model.*;

public class UpdateAdapterFactory implements IAdapterFactory {
	SiteBookmarkPropertySource siteBookmarkPropertySource;

public Object getAdapter(Object adaptableObject, Class adapterType) {
	if (adapterType.equals(IPropertySource.class)) 
		return getProperties(adaptableObject);
	return null;	
}
public Class[] getAdapterList() {
	return new Class[] { IPropertySource.class };
}

private Object getProperties(Object object) {
	if (object instanceof SiteBookmark) {
		if (siteBookmarkPropertySource==null)
		   siteBookmarkPropertySource = new SiteBookmarkPropertySource();
		siteBookmarkPropertySource.setBookmark((SiteBookmark)object);
		return siteBookmarkPropertySource;
	}
	return null;
}

}

