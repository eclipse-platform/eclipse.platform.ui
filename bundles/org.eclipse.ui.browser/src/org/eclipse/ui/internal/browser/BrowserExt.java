/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.browser;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.ui.browser.BrowserFactory;
import org.eclipse.ui.browser.IWebBrowser;
/**
 * @since 1.0
 */
public class BrowserExt implements IBrowserExt {
	private static final String ATTR_FACTORY_CLASS = "factoryclass"; //$NON-NLS-1$

	private IConfigurationElement element;

	private BrowserFactory delegate;

	/**
	 * BrowserExt constructor comment.
	 */
	public BrowserExt(IConfigurationElement element) {
		super();
		this.element = element;
	}

	/**
	 * Returns the id of this browser.
	 * 
	 * @return java.lang.String
	 */
	public String getId() {
		return element.getAttribute("id"); //$NON-NLS-1$
	}

	public String getName() {
		String label = element.getAttribute("name"); //$NON-NLS-1$
		if (label == null)
			return "n/a"; //$NON-NLS-1$
		return label;
	}

	public String getParameters() {
		return element.getAttribute("parameters"); //$NON-NLS-1$
	}

	public String getExecutable() {
		return element.getAttribute("executable"); //$NON-NLS-1$
	}

	public String getOS() {
		String os = element.getAttribute("os"); //$NON-NLS-1$
		if (os == null)
			os = ""; //$NON-NLS-1$
		return os;
	}

	public String[] getDefaultLocations() {
		List<String> list = new ArrayList<String>();
		IConfigurationElement[] children = element.getChildren("location"); //$NON-NLS-1$
		if (children != null) {
			int size = children.length;
			for (int i = 0; i < size; i++) {
				list.add(children[i].getValue());
			}
		}

		String[] s = new String[list.size()];
		list.toArray(s);
		return s;
	}

	protected BrowserFactory getDelegate() {
		if (delegate == null) {
			if (element.getAttribute(ATTR_FACTORY_CLASS) == null
					|| element.getAttribute(ATTR_FACTORY_CLASS).length() == 0)
				return null;

			try {
				delegate = (BrowserFactory) element
						.createExecutableExtension(ATTR_FACTORY_CLASS);
			} catch (Exception e) {
				Trace
						.trace(
								Trace.SEVERE,
								"Could not create delegate" + toString() + ": " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		return delegate;
	}

	/**
	 * Checks whether the factory can work on the user system.
	 * 
	 * @return false if the factory cannot work on this system; for example the
	 *         required native browser required by browser adapters that it
	 *         creates is not installed
	 */
	public boolean isAvailable() {
		if (delegate == null && (element.getAttribute(ATTR_FACTORY_CLASS) == null
				|| element.getAttribute(ATTR_FACTORY_CLASS).length() == 0))
			return true;
		
		try {
			return getDelegate().isAvailable();
		} catch (Exception e) {
			Trace
					.trace(
							Trace.SEVERE,
							"Error calling delegate " + toString() + ": " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}
	}

	/**
	 * Obtains a new instance of a web browser.
	 * 
	 * @return instance of IBrowser
	 */
	public IWebBrowser createBrowser(String id, String location,
			String parameters) {
		try {
			return getDelegate().createBrowser(id, location, parameters);
		} catch (Exception e) {
			Trace
					.trace(
							Trace.SEVERE,
							"Error calling delegate " + toString() + ": " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
	}

	/**
	 * Return a string representation of this object.
	 * 
	 * @return java.lang.String
	 */
	public String toString() {
		String s = "BrowserExt: " + getId() + ", " + getName() + ", " + getOS()  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
				+ ", " + getExecutable() + ", " + getParameters() + ", ";  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		String[] locations = getDefaultLocations();
		if (locations != null) {
			int size = locations.length;
			for (int i = 0; i < size; i++) {
				s += locations[i] + ";"; //$NON-NLS-1$
			}
		}
		return s;
	}
}
