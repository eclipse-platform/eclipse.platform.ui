/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.runtime;

import java.net.MalformedURLException;
import java.net.URL;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.IConfigurationElement;

public class Product implements IProduct {
	String application = null;
	String name = null;
	URL image = null;
	String id = null;
	String perspective = null;
	String description = null;
	
	public Product(IConfigurationElement element) {
		if (element == null)
			return;
		application = element.getAttribute("application");
		name = element.getAttribute("name");
		String location = element.getAttribute("image");
		if (location != null)
			try {
			image = new URL(location);
		} catch (MalformedURLException e) {
			// TODO log this error somewhere
		}
		id = element.getAttribute("id");
		perspective = element.getAttribute("perspective");
		description = element.getAttribute("description");
	}
	
	public String getApplication() {
		return application;
	}

	public String getName() {
		return name;
	}

	public URL getImage() {
		return image;
	}

	public String getDescription() {
		return description;
	}

	public String getId() {
		return id;
	}

	public String getDefaultPerspective() {
		return perspective;
	}

}
