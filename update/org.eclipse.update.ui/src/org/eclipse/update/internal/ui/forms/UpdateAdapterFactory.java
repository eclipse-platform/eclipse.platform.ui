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
package org.eclipse.update.internal.ui.forms;
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

