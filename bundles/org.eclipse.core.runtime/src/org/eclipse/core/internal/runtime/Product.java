/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.runtime;

import java.util.HashMap;
import org.eclipse.core.runtime.*;
import org.osgi.framework.Bundle;

public class Product implements IProduct {
	private static final String ATTR_DESCRIPTION = "description"; //$NON-NLS-1$
	private static final String ATTR_NAME = "name"; //$NON-NLS-1$
	private static final String ATTR_APPLICATION = "application"; //$NON-NLS-1$
	private static final String ATTR_VALUE = "value"; //$NON-NLS-1$

	String application = null;
	String name = null;
	String id = null;
	String description = null;
	HashMap properties;
	Bundle definingBundle = null;

	public Product(String id, IConfigurationElement element) {
		this.id = id;
		if (element == null)
			return;
		application = element.getAttribute(ATTR_APPLICATION);
		name = element.getAttribute(ATTR_NAME);
		description = element.getAttribute(ATTR_DESCRIPTION);
		loadProperties(element);
	}

	private void loadProperties(IConfigurationElement element) {
		IConfigurationElement[] children = element.getChildren();
		properties = new HashMap(children.length);
		for (int i = 0; i < children.length; i++) {
			IConfigurationElement child = children[i];
			String key = child.getAttribute(ATTR_NAME);
			String value = child.getAttribute(ATTR_VALUE);
			if (key != null && value != null)
				properties.put(key, value);
		}
		definingBundle = Platform.getBundle(element.getNamespace());
	}

	public Bundle getDefiningBundle() {
		return definingBundle;
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
		return (String) properties.get(key);
	}
}
