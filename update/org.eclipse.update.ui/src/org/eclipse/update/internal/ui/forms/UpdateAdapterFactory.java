package org.eclipse.update.internal.ui.forms;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.ui.views.properties.*;
import org.eclipse.update.internal.ui.model.*;
import org.eclipse.update.internal.ui.properties.*;

public class UpdateAdapterFactory implements IAdapterFactory {

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
	   return new SiteBookmarkPropertySource((SiteBookmark)object);
	}
	return null;
}

}

