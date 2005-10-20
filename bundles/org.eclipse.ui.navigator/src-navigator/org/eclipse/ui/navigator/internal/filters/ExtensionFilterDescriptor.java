/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Jan 12, 2004
 * 
 * To change the template for this generated file go to Window - Preferences - Java - Code
 * Generation - Code and Comments
 */
package org.eclipse.ui.navigator.internal.filters;

import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.navigator.INavigatorExtensionFilter;
import org.eclipse.ui.navigator.internal.NavigatorMessages;
import org.eclipse.ui.navigator.internal.NavigatorPlugin;

/**
 * @author mdelder
 * 
 * To change the template for this generated type comment go to Window - Preferences - Java - Code
 * Generation - Code and Comments
 */
public class ExtensionFilterDescriptor {

	public static final String ID = "id"; //$NON-NLS-1$

	public static final String NAVIGATOR_EXTENSION_ID = "navigatorExtensionId"; //$NON-NLS-1$

	public static final String NAME = "name"; //$NON-NLS-1$

	public static final String DESCRIPTION = "description"; //$NON-NLS-1$

	public static final String CLASS_NAME = "className"; //$NON-NLS-1$

	public static final String VIEW_ID = "viewId"; //$NON-NLS-1$

	public static final String ENABLED_BY_DEFAULT = "enabledByDefault"; //$NON-NLS-1$

	public static final String PROPERTY = "property"; //$NON-NLS-1$

	public static final String PROPERTY_NAME = "name"; //$NON-NLS-1$

	public static final String PROPERTY_VALUE = "value"; //$NON-NLS-1$

	private IConfigurationElement element = null;

	private String id = null;

	private String navigatorExtensionId = null;

	private String name = null;

	private String description = null;

	private String viewId = null;

	private boolean enabledByDefault = false;

	private Properties properties = null;

	private INavigatorExtensionFilter instance = null;

	private ViewerFilter viewerFilter;

	private String tostring;

	public ExtensionFilterDescriptor(IConfigurationElement element) {
		this.element = element;
		init();
	}

	public ExtensionFilterDescriptor(String filterId, String navigatorExtensionId, String name, String description, String viewerId, boolean enabledByDefault, ViewerFilter viewerFilter) {
		Assert.isNotNull(filterId, NavigatorMessages.getString("ExtensionFilterDescriptor.10")); //$NON-NLS-1$
		Assert.isNotNull(navigatorExtensionId, NavigatorMessages.getString("ExtensionFilterDescriptor.11")); //$NON-NLS-1$
		this.id = filterId;
		this.navigatorExtensionId = navigatorExtensionId;
		this.name = name;
		this.description = description;
		this.viewId = viewerId;
		this.enabledByDefault = enabledByDefault;
		this.viewerFilter = viewerFilter;

	}

	private void init() {
		if (this.element != null) {
			this.id = this.element.getAttribute(ID);
			this.navigatorExtensionId = this.element.getAttribute(NAVIGATOR_EXTENSION_ID);
			this.name = this.element.getAttribute(NAME);
			this.description = this.element.getAttribute(DESCRIPTION);
			this.viewId = this.element.getAttribute(VIEW_ID);
			this.enabledByDefault = Boolean.valueOf(this.element.getAttribute(ENABLED_BY_DEFAULT)).booleanValue();
		}
	}

	public Properties getProperties() {
		if (this.properties == null) {

			this.properties = new Properties();
			if (this.element != null) {
				IConfigurationElement[] children = this.element.getChildren(PROPERTY);
				String localName = null;
				String value = null;
				for (int i = 0; i < children.length; i++) {
					localName = children[i].getAttribute(PROPERTY_NAME);
					value = children[i].getAttribute(PROPERTY_VALUE);
					this.properties.put(localName, value);
				}
			}
		}
		return this.properties;
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return Returns the enabledByDefault.
	 */
	public boolean isEnabledByDefault() {
		return enabledByDefault;
	}

	/**
	 * @return Returns the id.
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return Returns the viewId.
	 */
	public String getViewId() {
		return viewId;
	}

	/**
	 * @return Returns the navigatorExtensionId.
	 */
	public String getNavigatorExtensionId() {
		return navigatorExtensionId;
	}

	public INavigatorExtensionFilter getInstance() {
		if (instance == null) {
			if (this.element != null) {
				try {
					instance = (INavigatorExtensionFilter) this.element.createExecutableExtension(CLASS_NAME);
				} catch (CoreException e) {
					NavigatorPlugin.log(e.toString());
				}
			} else if (viewerFilter != null)
				instance = new NavigatorViewerFilter(viewerFilter);

		}
		return instance;
	}

	public String toString() {
		if (tostring == null)
			tostring = getClass().getName() + "[id=\"" + getId() + "\", name=\"" + getName() + "\", enabledByDefault=\"" + isEnabledByDefault() + "\"]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		return tostring;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof ExtensionFilterDescriptor)
			return id.equals(((ExtensionFilterDescriptor) obj).id);
		return false;

	}

}