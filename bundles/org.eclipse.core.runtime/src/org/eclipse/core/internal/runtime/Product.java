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

import java.util.Dictionary;
import java.util.Hashtable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProduct;

public class Product implements IProduct {
	private static final String ATTR_DESCRIPTION = "description"; //$NON-NLS-1$
	private static final String ATTR_ID = "id"; //$NON-NLS-1$
	private static final String ATTR_NAME = "name"; //$NON-NLS-1$
	private static final String ATTR_APPLICATION = "application"; //$NON-NLS-1$
	String application = null;
	String name = null;
	String id = null;
	String description = null;
	Dictionary properties;
	
	public Product(IConfigurationElement element) {
		if (element == null)
			return;
		application = element.getAttribute(ATTR_APPLICATION);
		name = element.getAttribute(ATTR_NAME);
		id = element.getAttribute(ATTR_ID);
		description = element.getAttribute(ATTR_DESCRIPTION);
		loadProperties(element);
	}
	
	private void loadProperties(IConfigurationElement element) {
		String[] attributes = element.getAttributeNames();
		properties = new Hashtable(attributes.length);
		for (int i = 0; i < attributes.length; i++) {
			if (attributes[i].equalsIgnoreCase(ATTR_DESCRIPTION) ||
				attributes[i].equalsIgnoreCase(ATTR_ID) ||
				attributes[i].equalsIgnoreCase(ATTR_NAME) ||
				attributes[i].equalsIgnoreCase(ATTR_APPLICATION))
				continue;
			properties.put(attributes[i], element.getAttribute(attributes[i]));
		}
	}
	
	public String getApplication() {
		return application;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getId() {
		return id;
	}
	public String getProperty(String key) {
		return (String)properties.get(key);
	}
}
